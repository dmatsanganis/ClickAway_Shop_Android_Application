package com.dmatsanganis.clickawayapp.ProductDetails;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.dmatsanganis.clickawayapp.Classes.Smartphone;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SmartphoneDetails extends AppCompatActivity {
    private TextView name, os, battery, ram, rom, screen_size, desc, cores, front_camera, rear_camera, price;
    private ImageView image;
    private Button add;
    private EditText quantity;
    private FloatingActionButton addOne, removeOne, micButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private ShoppingCart shoppingCart;
    private ConstraintLayout parent;
    private int REC_RESULT=0;
    private Smartphone smartphone;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartphone_details);

        smartphone = (Smartphone) getIntent().getSerializableExtra("Smartphone");

        name = findViewById(R.id.smartphoneName);
        image = findViewById(R.id.smartphoneImage);
        desc = findViewById(R.id.smartphoneDesc);
        os = findViewById(R.id.smartphoneOs);
        battery = findViewById(R.id.smartphoneBattery);
        rom = findViewById(R.id.smartphoneRom);
        ram = findViewById(R.id.smartphoneRam);
        screen_size = findViewById(R.id.smartphoneScreen_size);
        cores = findViewById(R.id.smartphoneCores);
        price = findViewById(R.id.smartphonePrice);
        front_camera = findViewById(R.id.smartphoneFront_camera);
        rear_camera = findViewById(R.id.smartphoneRear_camera);
        add = findViewById(R.id.addSmartphone);
        quantity = findViewById(R.id.smartphoneQuantity);
        addOne = findViewById(R.id.addOneSmartphone);
        removeOne = findViewById(R.id.removeOneSmartphone);
        parent = findViewById(R.id.parent);
        micButton = findViewById(R.id.micButtonSmartphone);

        name.setText(smartphone.getName());
        Glide.with(getApplicationContext()).load(smartphone.getImage_url()).into(image);
        if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            desc.setText(smartphone.getDesc_gr());
        } else {
            desc.setText(smartphone.getDesc_en());
        }
        os.setText(getString(R.string.os) + smartphone.getOs());
        battery.setText(getString(R.string.battery) + smartphone.getBattery());
        rom.setText(getString(R.string.rom) + smartphone.getRom());
        ram.setText(getString(R.string.ram) + smartphone.getRam());
        screen_size.setText(getString(R.string.screen) + String.valueOf(smartphone.getScreen_size()) +  "″");
        cores.setText(getString(R.string.cores) + smartphone.getCores());
        front_camera.setText(getString(R.string.front_camera) + smartphone.getFront_camera());
        rear_camera.setText(getString(R.string.rear_camera) + smartphone.getRear_camera());
        DecimalFormat formatter = new DecimalFormat("0.00");
        price.setText(getString(R.string.price) + ": " + formatter.format(smartphone.getPrice()) + " €");

        quantity.setText("1");

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
        add.setOnClickListener((view) -> {
            hideKeyboard(view);
            //If quantity is 0 or empty show warning snackbar
            if (quantity.getText().toString().equals("0")|| quantity.getText().toString().equals("")){
                mySnackBar = new MySnackBar();
                mySnackBar.show(getString(R.string.atLeastOne), getApplicationContext(), parent, false);
                quantity.setText("1");
            } else {
                //Else execute the add laptop to the cart method
                addSmartphoneToCart(shoppingCart, smartphone, Integer.parseInt(quantity.getText().toString()));
            }
        });

        //If user presses the plus button, the quantity increases by one
        addOne.setOnClickListener(view1 -> quantity.setText(String.valueOf(Integer.parseInt(quantity.getText().toString()) + 1)));
        //If user presses the minus button, the quantity decreases by one and stop from going below 1
        removeOne.setOnClickListener(view1 -> {
            if (Integer.parseInt(quantity.getText().toString()) > 1){
                quantity.setText(String.valueOf(Integer.parseInt(quantity.getText().toString()) - 1));
            }
        });

        parent.setOnClickListener(view1 -> parent.requestFocus());

        quantity.setOnFocusChangeListener((v, hasFocus) -> {
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

    //Method that check if smartphone is already in cart by checking their ids
    public ItemToPurchase checkIfSmartphoneInCart(Smartphone smartphone, ShoppingCart shoppingCart){
        for (ItemToPurchase item : shoppingCart.getItems()){
            if (item.getSmartphone() != null) {
                if (item.getSmartphone().getId().equals(smartphone.getId())) {
                    return item;
                }
            }
        }
        return null;
    }

    //Method that adds the smartphone to the cart
    public void addSmartphoneToCart(ShoppingCart shoppingCart, Smartphone smartphone, int quantity){
        //Get the items in the cart and check if the smartphone is already in. If it is, add the new quantity to the previous
        ArrayList<ItemToPurchase> items = new ArrayList<>();
        int index = 0;
        if(shoppingCart != null) {
            items.clear();
            items.addAll(shoppingCart.getItems());
            ItemToPurchase old_item = checkIfSmartphoneInCart(smartphone, shoppingCart);
            if(old_item != null) {
                quantity = quantity + old_item.getQuantity();
                index = items.indexOf(old_item);
                items.remove(old_item);
            }
        }
        ItemToPurchase new_item = new ItemToPurchase(smartphone, quantity);
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
                    addSmartphoneToCart(shoppingCart, smartphone, Integer.parseInt(numberOnly));
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