package com.dmatsanganis.clickawayapp.MenuFragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Login;
import com.dmatsanganis.clickawayapp.MainActivity;
import com.dmatsanganis.clickawayapp.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchTheme;
    private RadioGroup radioGroupLang;
    private String selectedTheme, selectedLanguage;
    private FloatingActionButton micButton;
    private int REC_RESULT=0;
    private MySnackBar mySnackBar;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //Get user's language and theme preference and set the switch and radio button
        selectedTheme = getArguments().getString("selectedTheme");
        selectedLanguage = getArguments().getString("selectedLanguage");

        switchTheme = view.findViewById(R.id.switchTheme);
        if(selectedTheme.equals("Dark")){
            switchTheme.setChecked(true);
        } else {
            switchTheme.setChecked(false);
        }

        radioGroupLang = view.findViewById(R.id.radioGroupLang);
        if (selectedLanguage.equals("Greek")){
            radioGroupLang.check(R.id.radioButtonGreek);
        } else {
            radioGroupLang.check(R.id.radioButtonEnglish);
        }
        
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        currentUser = mAuth.getCurrentUser();
        micButton = getActivity().findViewById(R.id.micButton);


        //When the switch changes and it's checked, dark theme is on, so change the theme and update the user's preference on firebase
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                selectedTheme = "Dark";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                selectedTheme = "Light";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            myRef.child("Users").child(currentUser.getUid()).child("app_theme")
                    .setValue(selectedTheme);
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("selectedBottom", R.id.settingsFragment);
            intent.putExtra("selectedTheme", selectedTheme);
            intent.putExtra("selectedLanguage", selectedLanguage);
            getActivity().finish();
            startActivity(intent);
        });

        //When the radio button changes and the first one is checked, greek language is on, so change the language and update the user's preference on firebase
        radioGroupLang.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.radioButtonGreek){
                selectedLanguage = "Greek";
            } else if (i == R.id.radioButtonEnglish){
                selectedLanguage = "English";
            }
            myRef.child("Users").child(currentUser.getUid()).child("language")
                    .setValue(selectedLanguage);
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("selectedBottom", R.id.settingsFragment);
            intent.putExtra("selectedTheme", selectedTheme);
            intent.putExtra("selectedLanguage", selectedLanguage);
            getActivity().finish();
            startActivity(intent);
        });

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            recognise();
            micButton.setOnClickListener(null);
        });

        return view;
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
            if (matches.get(0).toLowerCase().contains("greek") || matches.get(0).toLowerCase().contains("ελληνικά")) {
                radioGroupLang.check(R.id.radioButtonGreek);
            } else if(matches.get(0).toLowerCase().contains("english") || matches.get(0).toLowerCase().contains("αγγλικά")){
                radioGroupLang.check(R.id.radioButtonEnglish);
            } if (matches.get(0).toLowerCase().equals("enable dark theme") || matches.get(0).toLowerCase().equals("ενεργοποίηση σκοτεινού θέματος")) {
                switchTheme.setChecked(true);
            } else if(matches.get(0).toLowerCase().equals("disable dark theme") || matches.get(0).toLowerCase().equals("απενεργοποίηση σκοτεινού θέματος")){
                switchTheme.setChecked(false);
                Log.d("DARK", "OFF");
            } else if (matches.get(0).toLowerCase().contains("καλάθι") || matches.get(0).toLowerCase().contains("cart")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.cartFragment);
            } else if (matches.get(0).toLowerCase().contains("παραγγελίες") || matches.get(0).toLowerCase().contains("orders")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.activeOrdersFragment);
            } else if (matches.get(0).toLowerCase().contains("προϊόντα") || matches.get(0).toLowerCase().contains("products")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.productsFragment);
            } else if (matches.get(0).toLowerCase().contains("καταστήματα") || matches.get(0).toLowerCase().contains("stores")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.storesFragment);
            } else if (matches.get(0).toLowerCase().equals("exit") || matches.get(0).toLowerCase().equals("έξοδος")){
                getActivity().finishAffinity();
            } else if (matches.get(0).toLowerCase().equals("sign out") || matches.get(0).toLowerCase().equals("αποσύνδεση")){
                getActivity().finish();
                mAuth.signOut();
                startActivity(new Intent(getContext(), Login.class));
            } else {
                mySnackBar = new MySnackBar();
                mySnackBar.show(getString(R.string.understand), getActivity().getApplicationContext(), micButton, true);
            }
        } else {
            mySnackBar = new MySnackBar();
            mySnackBar.show(getString(R.string.understand), getActivity().getApplicationContext(), micButton, true);
        }
    }

    //When this fragment is visible run this recognise method
    @Override
    public void onResume() {
        micButton.setOnClickListener((view1) -> {
            recognise();
            micButton.setOnClickListener(null);
        });
        super.onResume();
    }

}