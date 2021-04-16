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
import com.dmatsanganis.clickawayapp.Classes.Laptop;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class LaptopDetails extends AppCompatActivity {
    private TextView name, os, gpu, ram, rom, screen_size, desc, cores, cpu, price;
    private ImageView image;
    private Button add_laptop;
    private EditText quantity_laptop;
    private FloatingActionButton addOne, removeOne, micButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ShoppingCart shoppingCart;
    private ConstraintLayout parent;
    private MySnackBar mySnackBar;
    private int REC_RESULT=0;
    private Laptop laptop;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laptop_details);

        laptop = (Laptop) getIntent().getSerializableExtra("Laptop");

        name = findViewById(R.id.laptopName);
        image = findViewById(R.id.laptopImage);
        desc = findViewById(R.id.laptopDesc);
        os = findViewById(R.id.laptopOs);
        gpu = findViewById(R.id.laptopGpu);
        rom = findViewById(R.id.laptopRom);
        ram = findViewById(R.id.laptopRam);
        screen_size = findViewById(R.id.laptopScreen_size);
        cores = findViewById(R.id.laptopCores);
        cpu = findViewById(R.id.laptopCpu);
        price = findViewById(R.id.laptopPrice);
        add_laptop = findViewById(R.id.addLaptop);
        quantity_laptop = findViewById(R.id.laptopQuantity);
        addOne = findViewById(R.id.addOneLaptop);
        removeOne = findViewById(R.id.removeOneLaptop);
        parent = findViewById(R.id.parent);
        micButton = findViewById(R.id.micButtonLaptop);


        quantity_laptop.setText("1");

        name.setText(laptop.getName());
        Glide.with(getApplicationContext()).load(laptop.getImage_url()).into(image);
        if(getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            desc.setText(laptop.getDesc_gr());
        } else {
            desc.setText(laptop.getDesc_en());
        }
        os.setText(getString(R.string.os) + laptop.getOs());
        gpu.setText(getString(R.string.gpu) + laptop.getGpu());
        rom.setText(getString(R.string.rom) + laptop.getRom());
        ram.setText(getString(R.string.ram) + laptop.getRam());
        screen_size.setText(getString(R.string.screen) + String.valueOf(laptop.getScreen_size()));
        cores.setText(getString(R.string.cores) + laptop.getCores());
        cpu.setText(getString(R.string.cpu) + laptop.getCpu());
        DecimalFormat formatter = new DecimalFormat("0.00");
        price.setText(getString(R.string.price) + ": " + formatter.format(laptop.getPrice()) + " €");

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
        add_laptop.setOnClickListener((view) -> {
            hideKeyboard(view);
            //If quantity is 0 or empty show warning snackbar
            if (quantity_laptop.getText().toString().equals("0")|| quantity_laptop.getText().toString().equals("")){
                mySnackBar = new MySnackBar();
                mySnackBar.show(getString(R.string.atLeastOne), getApplicationContext(), parent, false);
                quantity_laptop.setText("1");
            } else {
                //Else execute the add laptop to the cart method
                addLaptopToCart(shoppingCart, laptop, Integer.parseInt(quantity_laptop.getText().toString()));
            }
        });

        //If user presses the plus button, the quantity increases by one
        addOne.setOnClickListener(view1 -> quantity_laptop.setText(String.valueOf(Integer.parseInt(quantity_laptop.getText().toString()) + 1)));
        //If user presses the minus button, the quantity decreases by one and stop from going below 1
        removeOne.setOnClickListener(view1 -> {
            if (Integer.parseInt(quantity_laptop.getText().toString()) > 1){
                quantity_laptop.setText(String.valueOf(Integer.parseInt(quantity_laptop.getText().toString()) - 1));
            }
        });

        parent.setOnClickListener(view1 -> parent.requestFocus());

        quantity_laptop.setOnFocusChangeListener((v, hasFocus) -> {
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

    //Method that check if laptop is already in cart by checking their ids
    public ItemToPurchase checkIfLaptopInCart(Laptop laptop, ShoppingCart shoppingCart){
        for (ItemToPurchase item : shoppingCart.getItems()){
            if (item.getLaptop() != null) {
                if (item.getLaptop().getId().equals(laptop.getId())) {
                    return item;
                }
            }
        }
        return null;
    }

    //Method that adds the laptop to the cart
    public void addLaptopToCart(ShoppingCart shoppingCart, Laptop laptop, int quantity){
        //Get the items in the cart and check if the laptop is already in. If it is, add the new quantity to the previous
        ArrayList<ItemToPurchase> items = new ArrayList<>();
        int index = 0;
        if(shoppingCart != null) {
            items.clear();
            items.addAll(shoppingCart.getItems());
            ItemToPurchase old_item = checkIfLaptopInCart(laptop, shoppingCart);
            if(old_item != null) {
                quantity = quantity + old_item.getQuantity();
                index = items.indexOf(old_item);
                items.remove(old_item);
            }
        }
        ItemToPurchase new_item = new ItemToPurchase(laptop, quantity);
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
                    addLaptopToCart(shoppingCart, laptop, Integer.parseInt(numberOnly));
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