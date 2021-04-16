package com.dmatsanganis.clickawayapp.OrderActivities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.Order;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.MainActivity;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.OrderCartRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderReschedule extends AppCompatActivity {
    private Store store;
    private Order order, pastOrder;
    private Button completeOrder;
    private TextView storeName, storeAddress, date, time, totalCost;
    private RecyclerView recyclerView;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FloatingActionButton micButton;
    private int REC_RESULT=0;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_reschedule);

        order = (Order) getIntent().getSerializableExtra("Order");
        pastOrder = (Order) getIntent().getSerializableExtra("PastOrder");
        store = (Store) getIntent().getSerializableExtra("Store");

        storeName = findViewById(R.id.finalStoreName);
        storeAddress = findViewById(R.id.finalStoreAddress);
        date = findViewById(R.id.finalDate);
        time = findViewById(R.id.finalTime);
        totalCost = findViewById(R.id.finalTotalCost);
        completeOrder = findViewById(R.id.buttonCompleteOrder);
        micButton = findViewById(R.id.micButtonReschedule);

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

        //When user presses the complete button, executes the method for the completion of the rescheduling
        completeOrder.setOnClickListener(view -> {
            completeOrder();
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

    //Method that finds the order, update date and time of the order, send notification and return to main activity
    public void completeOrder(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        ArrayList<Order> orders = new ArrayList<>();
        ArrayList<Order> new_orders = new ArrayList<>();

        myRef.child("Stores").child(store.getId()).child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orders.clear();
                        new_orders.clear();
                        for (DataSnapshot order_snapshot : snapshot.getChildren()){
                            Order order_in_db = order_snapshot.getValue(Order.class);
                            if (order_in_db!=null){
                                orders.add(order_in_db);
                            }
                            Log.d("ORDER",order_in_db.getDate()+"/"+order_in_db.getTime());
                        }
                        new_orders.addAll(orders);
                        int index;
                        for (Order old_order : orders){
                            if(old_order.getUser().getEmail().equals(pastOrder.getUser().getEmail()) && old_order.getDate().equals(pastOrder.getDate()) && old_order.getTime().equals(pastOrder.getTime())){
                                index = orders.indexOf(old_order);
                                new_orders.remove(index);
                                new_orders.add(index, order);
                            }
                        }
                        myRef.child("Stores").child(store.getId()).child("orders")
                                .setValue(new_orders).addOnCompleteListener(task -> {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    if (getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                                        intent.putExtra("selectedLanguage", "Greek");
                                    } else {
                                        intent.putExtra("selectedLanguage", "English");
                                    }
                                    String app_theme = null;
                                    int currentNightMode = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                                    switch (currentNightMode) {
                                        case Configuration.UI_MODE_NIGHT_NO:
                                            app_theme = "Light";
                                            break;
                                        case Configuration.UI_MODE_NIGHT_YES:
                                            app_theme = "Dark";
                                            break;
                                    }
                                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    NotificationChannel channel = new NotificationChannel("1", "Notifications", NotificationManager.IMPORTANCE_HIGH);
                                    notificationManager.createNotificationChannel(channel);
                                    String storeTitle = null;
                                    if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                                        storeTitle = store.getTitle_gr();
                                    } else {
                                        storeTitle = store.getTitle_en();
                                    }
                                    String notification = getString(R.string.notificationReset) + order.getDate() + " - " + order.getTime() + " - " + storeTitle;
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "1")
                                            .setSmallIcon(getApplicationInfo().icon)
                                            .setContentTitle(getString(R.string.reschComplete))
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notification));

                                    notificationManager.notify(0, builder.build());
                                    intent.putExtra("selectedTheme", app_theme);
                                    finish();
                                    startActivity(intent);
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    //Show the results of the voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if ( matches.get(0).toLowerCase().contains("back") || matches.get(0).toLowerCase().contains("πίσω")) {
                onBackPressed();
            } else if (matches.get(0).toLowerCase().contains("done") || matches.get(0).toLowerCase().contains("ολοκλήρωη")){
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