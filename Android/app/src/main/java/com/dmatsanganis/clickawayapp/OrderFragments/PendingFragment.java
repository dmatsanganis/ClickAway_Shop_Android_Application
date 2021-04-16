package com.dmatsanganis.clickawayapp.OrderFragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.Order;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.Login;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.OrderListRecyclerViewAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class PendingFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private TextView message;
    private RecyclerView recyclerView;
    private FloatingActionButton micButton;
    private int REC_RESULT = 0;
    private MySnackBar mySnackBar;
    private ArrayList<Order> orders;

    public PendingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders_list, container, false);

        orders = new ArrayList<>();
        ArrayList<Store> stores = new ArrayList<>();
        ArrayList<Store> stores_of_orders = new ArrayList<>();

        message = view.findViewById(R.id.orderListMessage);
        message.setText(getString(R.string.loading));
        micButton = getActivity().findViewById(R.id.micButton);

        //Set the recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewOrders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        OrderListRecyclerViewAdapter adapter = new OrderListRecyclerViewAdapter(getContext(), orders);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        myRef.child("Stores")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orders.clear();
                        stores.clear();
                        stores_of_orders.clear();
                        for (DataSnapshot store_snapshot : snapshot.getChildren()){
                            Store store = store_snapshot.getValue(Store.class);
                            stores.add(store);
                        }
                        for (int i=1; i<=8; i++){
                            int index = i;
                            myRef.child("Stores").child("ST"+String.valueOf(index)).child("orders")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot order_snapshot : snapshot.getChildren()){
                                                Order order = order_snapshot.getValue(Order.class);
                                                try {
                                                    Date date = dateFormat.parse(order.getDate() + " " + order.getTime());
                                                    Date ending_order_time = new Date(date.getTime() + (15 * 60000));
                                                    if (order.getUser().getEmail().equals(currentUser.getEmail()) && !order.getCompleted() && ending_order_time.after(new Date())){
                                                        orders.add(order);
                                                        stores_of_orders.add(stores.get(index-1));
                                                    }
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (orders.isEmpty()){
                                                message.setText(getString(R.string.pendingEmpty));
                                                message.setVisibility(View.VISIBLE);
                                            } else {
                                                message.setVisibility(View.GONE);
                                            }
                                            adapter.setOrders(orders, stores_of_orders, 0);
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }

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

        //Connect the list to the recycler view
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
            if(matches.get(0).toLowerCase().contains("edit") || matches.get(0).toLowerCase().contains("change") || matches.get(0).toLowerCase().contains("reschedule")
                    || matches.get(0).toLowerCase().contains("αλλαγή") || matches.get(0).toLowerCase().contains("επαναπρογραμματισμός") || matches.get(0).toLowerCase().contains("επεξεργασία")){
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= orders.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.editOrder).performClick();
                }
            } else if(matches.get(0).toLowerCase().contains("start") ||matches.get(0).toLowerCase().contains("έναρξη")){
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= orders.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.startOrder).performClick();
                }
            } else if(matches.get(0).toLowerCase().contains("completed") || matches.get(0).toLowerCase().contains("ολοκληρωμένες")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager1);
                viewPager2.setCurrentItem(2);
            } else if(matches.get(0).toLowerCase().contains("expired") || matches.get(0).toLowerCase().contains("ληγμένες")){
                ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager1);
                viewPager2.setCurrentItem(0);
            } else if (matches.get(0).toLowerCase().contains("καλάθι") || matches.get(0).toLowerCase().contains("cart")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.cartFragment);
            } else if (matches.get(0).toLowerCase().contains("προϊόντα") || matches.get(0).toLowerCase().contains("products")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.productsFragment);
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