package com.dmatsanganis.clickawayapp.ProductDetails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.Classes.Tablet;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TabletDetails extends AppCompatActivity {
    private TextView name, os, battery, ram, rom, screen_size, desc, cores, front_camera, rear_camera, price;
    private ImageView image;
    private Button add_tablet;
    private EditText quantity_tablet;
    private FloatingActionButton addOne, removeOne, micButton;
    private ConstraintLayout parent;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ShoppingCart shoppingCart;
    private Tablet tablet;
    private int REC_RESULT=0;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet_details);

        tablet = (Tablet) getIntent().getSerializableExtra("Tablet");

        name = findViewById(R.id.tabletName);
        image = findViewById(R.id.tabletImage);
        desc = findViewById(R.id.tabletDesc);
        os = findViewById(R.id.tabletOs);
        battery = findViewById(R.id.tabletBattery);
        rom = findViewById(R.id.tabletRom);
        ram = findViewById(R.id.tabletRam);
        screen_size = findViewById(R.id.tabletScreen_size);
        cores = findViewById(R.id.tabletCores);
        price = findViewById(R.id.tabletPrice);
        front_camera = findViewById(R.id.tabletFront_camera);
        rear_camera = findViewById(R.id.tabletRear_camera);
        add_tablet = findViewById(R.id.addTablet);
        quantity_tablet = findViewById(R.id.tabletQuantity);
        addOne = findViewById(R.id.addOneTablet);
        removeOne = findViewById(R.id.removeOneTablet);
        parent = findViewById(R.id.parent);
        micButton = findViewById(R.id.micButtonTablet);

        name.setText(tablet.getName());
        Glide.with(getApplicationContext()).load(tablet.getImage_url()).into(image);
        if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            desc.setText(tablet.getDesc_gr());
        } else {
            desc.setText(tablet.getDesc_en());
        }
        os.setText(getString(R.string.os) + tablet.getOs());
        battery.setText(getString(R.string.battery) + tablet.getBattery());
        rom.setText(getString(R.string.rom) + tablet.getRom());
        ram.setText(getString(R.string.ram) + tablet.getRam());
        screen_size.setText(getString(R.string.screen) + String.valueOf(tablet.getScreen_size()) +  "″");
        cores.setText(getString(R.string.cores) + tablet.getCores());
        front_camera.setText(getString(R.string.front_camera) + tablet.getFront_camera());
        rear_camera.setText(getString(R.string.rear_camera) + tablet.getRear_camera());
        DecimalFormat formatter = new DecimalFormat("0.00");
        price.setText(getString(R.string.price) + ": " + formatter.format(tablet.getPrice()) + " €");
        quantity_tablet.setText("1");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();
        myRef = mFirebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Cart");

        //Get shoppingCart of user
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingCart = snapshot.getValue(ShoppingCart.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        //If user presses the add to cart button
        add_tablet.setOnClickListener((view) -> {
            hideKeyboard(view);
            //If quantity is 0 or empty show warning snackbar
            if (quantity_tablet.getText().toString().equals("0")|| quantity_tablet.getText().toString().equals("")){
                mySnackBar = new MySnackBar();
                mySnackBar.show(getString(R.string.atLeastOne), getApplicationContext(), parent, false);
                quantity_tablet.setText("1");
            } else {
                //Else execute the add laptop to the cart method
                addTabletToCart(shoppingCart, tablet, Integer.parseInt(quantity_tablet.getText().toString()));
            }
        });

        //If user presses the plus button, the quantity increases by one
        addOne.setOnClickListener(view1 -> quantity_tablet.setText(String.valueOf(Integer.parseInt(quantity_tablet.getText().toString()) + 1)));
        //If user presses the minus button, the quantity decreases by one and stop from going below 1
        removeOne.setOnClickListener(view1 -> {
            if (Integer.parseInt(quantity_tablet.getText().toString()) > 1){
                quantity_tablet.setText(String.valueOf(Integer.parseInt(quantity_tablet.getText().toString()) - 1));
            }
        });

        parent.setOnClickListener(view1 -> parent.requestFocus());

        quantity_tablet.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            parent.requestFocus();
            recognise();
        });
    }

    //Method that check if tablet is already in cart by checking their ids
    public ItemToPurchase checkIfTabletInCart(Tablet tablet, ShoppingCart shoppingCart){
        for (ItemToPurchase item : shoppingCart.getItems()){
            if (item.getTablet() != null){
                if (item.getTablet().getId().equals(tablet.getId())){
                    return item;
                }
            }
        }
        return null;
    }

    //Method that adds the tablet to the cart
    public void addTabletToCart(ShoppingCart shoppingCart, Tablet tablet, int quantity){
        //Get the items in the cart and check if the tablet is already in. If it is, add the new quantity to the previous
        ArrayList<ItemToPurchase> items = new ArrayList<>();
        int index = 0;
        if(shoppingCart != null) {
            items.clear();
            items.addAll(shoppingCart.getItems());
            ItemToPurchase old_item = checkIfTabletInCart(tablet, shoppingCart);
            if(old_item != null) {
                quantity = quantity + old_item.getQuantity();
                index = items.indexOf(old_item);
                items.remove(old_item);
            }
        }
        ItemToPurchase new_item = new ItemToPurchase(tablet, quantity);
        items.add(index, new_item);
        ShoppingCart new_shoppingCart = new ShoppingCart(items);

        //Send the new cart to the firebase
        myRef.setValue(new_shoppingCart)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mySnackBar = new MySnackBar();
                        mySnackBar.show(getString(R.string.added), getApplicationContext(), parent, false);
                    } else {
                        //Else show the warning toast
                        Toast.makeText(getApplicationContext(),
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Method for voice recognition
    public void recognise() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
        startActivityForResult(intent, REC_RESULT);
    }

    //Show the results of the voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.get(0).toLowerCase().contains("add") || matches.get(0).toLowerCase().contains("πρόσθεσε") || matches.get(0).toLowerCase().contains("προσθήκη")) {
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (Integer.parseInt(numberOnly)>0){
                    addTabletToCart(shoppingCart, tablet, Integer.parseInt(numberOnly));
                }
            } else if ( matches.get(0).toLowerCase().contains("back") || matches.get(0).toLowerCase().contains("πίσω")) {
                onBackPressed();
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