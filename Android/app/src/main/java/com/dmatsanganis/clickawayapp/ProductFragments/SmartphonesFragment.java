package com.dmatsanganis.clickawayapp.ProductFragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Login;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.SmartphoneRecyclerViewAdapter;
import com.dmatsanganis.clickawayapp.Classes.Smartphone;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class SmartphonesFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private int REC_RESULT = 0;
    private FloatingActionButton micButton;
    private ArrayList<Smartphone> smartphones;
    private RecyclerView recyclerView;
    private MySnackBar mySnackBar;
    private FirebaseAuth mAuth;


    public SmartphonesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products_list, container, false);
        //List that will contain all smartphones
        smartphones = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        //Set the recycler view
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SmartphoneRecyclerViewAdapter adapter = new SmartphoneRecyclerViewAdapter(smartphones);
        recyclerView.setAdapter(adapter);
        micButton = getActivity().findViewById(R.id.micButton);

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            recognise();
            micButton.setOnClickListener(null);
        });

        //Get all smartphones from firebase and notify the adapter of the recycler view
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        myRef.child("Products").child("Smartphones")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        smartphones.clear();
                        for (DataSnapshot smartphone_snapshot : snapshot.getChildren()){
                            Smartphone smartphone = smartphone_snapshot.getValue(Smartphone.class);
                            smartphones.add(smartphone);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
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
            if(matches.get(0).toLowerCase().contains("select") || (matches.get(0).toLowerCase().contains("open") || matches.get(0).toLowerCase().contains("επιλογή") || matches.get(0).toLowerCase().contains("άνοιγμα"))) {
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= smartphones.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.cardView).performClick();
                }
            } else if(matches.get(0).toLowerCase().equals("tablets") || matches.get(0).toLowerCase().equals("tablet")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager);
                viewPager2.setCurrentItem(1);
            } else if(matches.get(0).toLowerCase().equals("laptops") || matches.get(0).toLowerCase().equals("laptop")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager);
                viewPager2.setCurrentItem(2);
            } else if (matches.get(0).toLowerCase().contains("καλάθι") || matches.get(0).toLowerCase().contains("cart")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.cartFragment);
            } else if (matches.get(0).toLowerCase().contains("παραγγελίες") || matches.get(0).toLowerCase().contains("orders")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.activeOrdersFragment);
            } else if (matches.get(0).toLowerCase().contains("καταστήματα") || matches.get(0).toLowerCase().contains("stores")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.storesFragment);
            } else if (matches.get(0).toLowerCase().contains("ρυθμίσεις") || matches.get(0).toLowerCase().contains("settings")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.settingsFragment);
            }else if (matches.get(0).toLowerCase().equals("exit") || matches.get(0).toLowerCase().equals("έξοδος")){
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