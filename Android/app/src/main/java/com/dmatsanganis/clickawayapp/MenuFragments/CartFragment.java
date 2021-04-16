package com.dmatsanganis.clickawayapp.MenuFragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.Classes.User;
import com.dmatsanganis.clickawayapp.Login;
import com.dmatsanganis.clickawayapp.OrderActivities.OrderLocation;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.CartRecyclerViewAdapter;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class CartFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ArrayList<ItemToPurchase> items;
    private FloatingActionButton deleteAll, micButton;
    private TextView emptyCart;
    private Button orderStart;
    private ShoppingCart shoppingCart;
    private int REC_RESULT=0;
    private User user;
    private RecyclerView recyclerView;
    private MySnackBar mySnackBar;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        //List that will contain all the products inside user's cart
        items = new ArrayList<>();

        deleteAll = view.findViewById(R.id.deleteAllCart);
        emptyCart = view.findViewById(R.id.emptyCart);
        orderStart = view.findViewById(R.id.buttonOrderStart);

        micButton = getActivity().findViewById(R.id.micButton);

        //Set the recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewCart);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        CartRecyclerViewAdapter adapter = new CartRecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        emptyCart.setVisibility(View.VISIBLE);
        emptyCart.setText(getString(R.string.loading));
        deleteAll.setVisibility(View.INVISIBLE);
        orderStart.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode(getResources().getConfiguration().getLocales().get(0).toString());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        currentUser = mAuth.getCurrentUser();

        //Get user's cart from firebase and notify the adapter of the recycler view
        myRef = myRef.child("Users").child(currentUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                user = snapshot.getValue(User.class);
                shoppingCart = user.getCart();
                if (shoppingCart != null) {
                    items.addAll(shoppingCart.getItems());
                }
                adapter.notifyDataSetChanged();
                //If there are no products inside the cart show the message
                if(items.isEmpty()){
                    emptyCart.setText(R.string.emptyCart);
                    emptyCart.setVisibility(View.VISIBLE);
                    deleteAll.setVisibility(View.INVISIBLE);
                    orderStart.setVisibility(View.INVISIBLE);
                }
                else{
                    emptyCart.setVisibility(View.INVISIBLE);
                    deleteAll.setVisibility(View.VISIBLE);
                    orderStart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //If user presses the delete all button starts the delete all method
        deleteAll.setOnClickListener((view1) -> {
            deleteAllFromCart(view);
            adapter.notifyDataSetChanged();
        });

        //If user presses the order start button the next activity opens passing the user object
        orderStart.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), OrderLocation.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            recognise();
            micButton.setOnClickListener(null);
        });
        return view;
    }

    //Method that clears the cart of the user
    public void deleteAllFromCart(View view){
        //Open the dialog for the deletion of all products of the cart
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View delete_all_layout = LayoutInflater.from(view.getContext()).inflate(R.layout.delete_all_dialog,null);
        builder.setView(delete_all_layout);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button buttonYes = delete_all_layout.findViewById(R.id.deleteAllYes);
        Button buttonNo = delete_all_layout.findViewById(R.id.deleteAllNo);

        //If yes button is pressed delete the cart of the user
        buttonYes.setOnClickListener(view1 -> {
            myRef.child("Cart").removeValue();
            items.clear();
            dialog.dismiss();
        });

        buttonNo.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
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
            if (matches.get(0).toLowerCase().equals("delete all") || matches.get(0).toLowerCase().equals("διαγραφή όλων")){
                deleteAllFromCart(getView());
            } else if (matches.get(0).toLowerCase().contains("delete") || matches.get(0).toLowerCase().contains("διαγραφή")) {
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= items.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.cartDelete).performClick();
                }
            } else if(matches.get(0).toLowerCase().contains("edit") || matches.get(0).toLowerCase().contains("επεξεργασία")){
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= items.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.cartEdit).performClick();
                }
            } else if  (matches.get(0).toLowerCase().equals("continue") || matches.get(0).toLowerCase().equals("next") || matches.get(0).toLowerCase().equals("buy")
                            || matches.get(0).toLowerCase().equals("επόμενο") || matches.get(0).toLowerCase().equals("αγορά") || matches.get(0).toLowerCase().equals("πληρωμή")){
                Intent intent = new Intent(getContext(), OrderLocation.class);
                intent.putExtra("User", user);
                startActivity(intent);
            } else if (matches.get(0).toLowerCase().contains("προϊόντα") || matches.get(0).toLowerCase().contains("products")) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.productsFragment);
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