package com.dmatsanganis.clickawayapp.ProductFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.Laptop;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Login;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.LaptopRecyclerViewAdapter;
import com.dmatsanganis.clickawayapp.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class LaptopsFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ArrayList<Laptop> laptops;
    private RecyclerView recyclerView;
    private FloatingActionButton micButton;
    private int REC_RESULT = 0;
    private MySnackBar mySnackBar;
    private FirebaseAuth mAuth;


    public LaptopsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products_list, container, false);
        //List that will contain all laptops
        laptops = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        //Set the recycler view
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LaptopRecyclerViewAdapter adapter = new LaptopRecyclerViewAdapter(laptops);
        recyclerView.setAdapter(adapter);

        micButton = getActivity().findViewById(R.id.micButton);

        //Get all laptops from firebase and notify the adapter of the recycler view
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        myRef.child("Products").child("Laptops")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        laptops.clear();
                        for (DataSnapshot laptop_snapshot : snapshot.getChildren()){
                            Laptop laptop = laptop_snapshot.getValue(Laptop.class);
                            laptops.add(laptop);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
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
            if(matches.get(0).toLowerCase().contains("select") || (matches.get(0).toLowerCase().contains("open") || matches.get(0).toLowerCase().contains("επιλογή") || matches.get(0).toLowerCase().contains("άνοιγμα"))) {
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= laptops.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.cardView).performClick();
                }
            } else if(matches.get(0).toLowerCase().equals("smartphones") || matches.get(0).toLowerCase().equals("smartphone")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager);
                viewPager2.setCurrentItem(0);
            } else if(matches.get(0).toLowerCase().equals("tablets") || matches.get(0).toLowerCase().equals("tablet")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager);
                viewPager2.setCurrentItem(1);
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