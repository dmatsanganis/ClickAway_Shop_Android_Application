package com.dmatsanganis.clickawayapp.OrderActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.ItemInStorage;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.Order;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.MainActivity;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.OrderCartRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderPurchase extends AppCompatActivity {
    private Store store;
    private Order order;
    private Button completeOrder;
    private TextView storeName, storeAddress, date, time, totalCost;
    private RecyclerView recyclerView;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText cardName, cardNumber, cardDate, cardPin;
    private ConstraintLayout parent;
    private FloatingActionButton micButton;
    private int REC_RESULT=0;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_purchase);

        order = (Order) getIntent().getSerializableExtra("Order");
        store = (Store) getIntent().getSerializableExtra("Store");

        storeName = findViewById(R.id.finalStoreName);
        storeAddress = findViewById(R.id.finalStoreAddress);
        date = findViewById(R.id.finalDate);
        time = findViewById(R.id.finalTime);
        totalCost = findViewById(R.id.finalTotalCost);
        completeOrder = findViewById(R.id.buttonCompleteOrder);
        micButton = findViewById(R.id.micButtonPurchase);
        mySnackBar = new MySnackBar();

        cardName = findViewById(R.id.cardName);
        cardDate = findViewById(R.id.cardDate);
        cardNumber = findViewById(R.id.cardNumber);
        cardPin = findViewById(R.id.cardPin);
        parent = findViewById(R.id.parent);

        recyclerView = findViewById(R.id.recyclerViewFinalProducts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OrderCartRecyclerViewAdapter adapter = new OrderCartRecyclerViewAdapter(order.getUser().getCart().getItems());
        recyclerView.setAdapter(adapter);

        if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            storeName.setText(store.getTitle_gr());
            storeAddress.setText(store.getAddress_gr());
        } else {
            storeName.setText(store.getTitle_en());
            storeAddress.setText(store.getAddress_en());
        }
        date.setText(getString(R.string.date) + ": " + order.getDate());
        time.setText(getString(R.string.time) + ": " + order.getTime());
        DecimalFormat formatter = new DecimalFormat("0.00");
        totalCost.setText(getString(R.string.total_cost) + ": " + formatter.format(order.getUser().getCart().getTotal_cost()) + "€");

        //When user presses the complete button check if card details fields are filled correctly
        completeOrder.setOnClickListener(view -> {
            parent.requestFocus();
            if (cardNumber.getText().toString().equals("") || cardName.getText().toString().equals("")
                    || cardDate.getText().toString().equals("") || cardPin.getText().toString().equals("")){
                mySnackBar.show(getString(R.string.empty), getApplicationContext(), parent, false);
            } else {
                if(cardNumber.getText().length() < 16){
                    mySnackBar.show(getString(R.string.wrongCC), getApplicationContext(), parent, false);
                } else if (cardPin.getText().length() < 3){
                    mySnackBar.show(getString(R.string.wrongCCV), getApplicationContext(), parent, false);
                } else if (!cardDate.getText().toString().matches("^[0-9]{2}/[0-9]{2}$")){
                    mySnackBar.show(getString(R.string.wrongDate), getApplicationContext(), parent, false);
                } else {
                    completeOrder();
                }
            }
        });

        cardPin.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        cardName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        cardDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        cardNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            recognise();
        });
    }

    //Method for voice recognition
    public void recognise() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
        startActivityForResult(intent, REC_RESULT);
    }

    //Method that completes the order, save it in firebase, send notification and return to main activity
    public void completeOrder(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        currentUser = mAuth.getCurrentUser();

        ArrayList<Order> new_orders = new ArrayList<>();

        myRef.child("Stores").child(store.getId()).child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    new_orders.clear();
                    for (DataSnapshot order_snapshot : snapshot.getChildren()){
                        Order order_in_db = order_snapshot.getValue(Order.class);
                        if (order_in_db!=null){
                            new_orders.add(order_in_db);
                        }
                    }
                    new_orders.add(order);
                    myRef.child("Stores").child(store.getId()).child("orders")
                            .setValue(new_orders).addOnCompleteListener(task -> myRef.child("Users").child(currentUser.getUid()).child("Cart")
                                    .removeValue().addOnCompleteListener(task1 -> {
                                        for (ItemToPurchase itemToPurchase : order.getUser().getCart().getItems()){
                                            if (itemToPurchase.getSmartphone() != null){
                                                for(ItemInStorage itemInStorage : store.getStorage()){
                                                    if(itemInStorage.getSmartphone() != null && itemToPurchase.getSmartphone().getId().equals(itemInStorage.getSmartphone().getId())){
                                                        int index = store.getStorage().indexOf(itemInStorage);
                                                        ItemInStorage new_item = new ItemInStorage(itemInStorage.getSmartphone(), itemInStorage.getQuantity()-itemToPurchase.getQuantity());
                                                        myRef.child("Stores").child(store.getId()).child("storage").child(String.valueOf(index)).setValue(new_item);
                                                    }
                                                }
                                            } else if (itemToPurchase.getTablet() != null){
                                                for(ItemInStorage itemInStorage : store.getStorage()){
                                                    if(itemInStorage.getTablet() != null && itemToPurchase.getTablet().getId().equals(itemInStorage.getTablet().getId())){
                                                        int index = store.getStorage().indexOf(itemInStorage);
                                                        ItemInStorage new_item = new ItemInStorage(itemInStorage.getTablet(), itemInStorage.getQuantity()-itemToPurchase.getQuantity());
                                                        myRef.child("Stores").child(store.getId()).child("storage").child(String.valueOf(index)).setValue(new_item);
                                                    }
                                                }
                                            } else if (itemToPurchase.getLaptop() != null){
                                                for(ItemInStorage itemInStorage : store.getStorage()){
                                                    if(itemInStorage.getLaptop() != null && itemToPurchase.getLaptop().getId().equals(itemInStorage.getLaptop().getId())){
                                                        int index = store.getStorage().indexOf(itemInStorage);
                                                        ItemInStorage new_item = new ItemInStorage(itemInStorage.getLaptop(), itemInStorage.getQuantity()-itemToPurchase.getQuantity());
                                                        myRef.child("Stores").child(store.getId()).child("storage").child(String.valueOf(index)).setValue(new_item);
                                                    }
                                                }
                                            }
                                        }


                                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationChannel channel = new NotificationChannel("0", "Notifications", NotificationManager.IMPORTANCE_HIGH);
                                        notificationManager.createNotificationChannel(channel);
                                        String storeTitle = null;
                                        if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                                            storeTitle = store.getTitle_gr();
                                        } else {
                                            storeTitle = store.getTitle_en();
                                        }
                                        String notification = getString(R.string.notificationReset) + order.getDate() + " - " + order.getTime() + " - " + storeTitle;
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "0")
                                                .setSmallIcon(getApplicationInfo().icon)
                                                .setContentTitle(getString(R.string.notification))
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification));

                                        notificationManager.notify(0, builder.build());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra("selectedLanguage", order.getUser().getLanguage());
                                        intent.putExtra("selectedTheme", order.getUser().getApp_theme());
                                        finish();
                                        startActivity(intent);
                                    }));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Show the results of the voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if ( matches.get(0).toLowerCase().contains("back") || matches.get(0).toLowerCase().contains("πίσω")) {
                onBackPressed();
            } else if (matches.get(0).toLowerCase().contains("done") || matches.get(0).toLowerCase().contains("ολοκλήρωση")){
                completeOrder.performClick();
            } else {
                mySnackBar = new MySnackBar();
                mySnackBar.show(getString(R.string.understand), getApplicationContext(), micButton, true);
            }
        } else {
            mySnackBar = new MySnackBar();
            mySnackBar.show(getString(R.string.understand), getApplicationContext(), micButton, true);
        }
    }

    @Override
    protected void onStart(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop(){
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}