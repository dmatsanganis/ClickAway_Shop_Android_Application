package com.dmatsanganis.clickawayapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;

import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.MenuFragments.ActiveOrdersFragment;
import com.dmatsanganis.clickawayapp.MenuFragments.CartFragment;
import com.dmatsanganis.clickawayapp.MenuFragments.ProductsFragment;
import com.dmatsanganis.clickawayapp.MenuFragments.SettingsFragment;
import com.dmatsanganis.clickawayapp.MenuFragments.StoresFragment;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private String selectedLanguage, selectedTheme;
    private int selectedBottom;
    private BottomNavigationView bottomNavigationView;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set language based on user preferences
        selectedBottom = (int) getIntent().getIntExtra("selectedBottom", R.id.productsFragment);
        selectedLanguage = (String) getIntent().getStringExtra("selectedLanguage");
        selectedTheme = (String) getIntent().getStringExtra("selectedTheme");
        if (selectedLanguage.equals("Greek")) {
            setLocale("el");
        } else {
            setLocale("en");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(selectedBottom);

        if (selectedBottom == R.id.productsFragment){
            getFragment(new ProductsFragment());
        } else if (selectedBottom == R.id.settingsFragment) {
            Bundle data = new Bundle();
            data.putString("selectedTheme", selectedTheme);
            data.putString("selectedLanguage", selectedLanguage);
            Fragment fragment = new SettingsFragment();
            fragment.setArguments(data);
            getFragment(fragment);
        }

        //Set the bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.productsFragment) {
                getFragment(new ProductsFragment());
            } else if (item.getItemId() == R.id.cartFragment) {
                getFragment(new CartFragment());
            } else if (item.getItemId() == R.id.activeOrdersFragment) {
                getFragment(new ActiveOrdersFragment());
            } else if (item.getItemId() == R.id.storesFragment) {
                getFragment(new StoresFragment());
            } else if (item.getItemId() == R.id.settingsFragment) {
                //Pass current preferences of the user to the fragment
                Bundle data = new Bundle();
                data.putString("selectedTheme", selectedTheme);
                data.putString("selectedLanguage", selectedLanguage);
                Fragment fragment = new SettingsFragment();
                fragment.setArguments(data);
                getFragment(fragment);
            }
            return true;
        });

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Update the badge of the cart on the bottom navigation every time there is a change in the database
        myRef = mFirebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Cart");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ShoppingCart cart = snapshot.getValue(ShoppingCart.class);
                BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.cartFragment);
                badge.setVisible(true);
                if (cart != null) {
                    int badge_number = 0;
                    for (ItemToPurchase item : cart.getItems()){
                        badge_number = badge_number + item.getQuantity();
                    }
                    badge.setNumber(badge_number);
                } else {
                    badge.setVisible(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //Method that change the fragment
    public void getFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainContainer, fragment);
        fragmentTransaction.commit();
    }

    //Method that sets the language of the application
    public void setLocale(String lang) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(lang);
        res.updateConfiguration(conf, dm);
    }

    //On back button pressed show the dialog that asks the user to exit or sign out
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View alert_dialog = LayoutInflater.from(this).inflate(R.layout.quit_dialog,null);
        builder.setView(alert_dialog);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button signOut = alert_dialog.findViewById(R.id.quitRight);
        Button exit = alert_dialog.findViewById(R.id.quitCenter);

        //If the user presses the exit button the application closes
        exit.setOnClickListener(view -> {
            dialog.dismiss();
            finishAffinity();
        });
        //If the user presses the sign out button the user signs out and the Login Activity is now open
        signOut.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
            mAuth.signOut();
            startActivity(new Intent(this, Login.class));
        });
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