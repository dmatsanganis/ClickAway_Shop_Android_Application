package com.dmatsanganis.clickawayapp.OrderActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.dmatsanganis.clickawayapp.Classes.User;
import com.dmatsanganis.clickawayapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OrderDateTime extends AppCompatActivity {
    private User user;
    private Store store;
    private Order pastOrder;
    private CalendarView calendarView;
    private Calendar calendar;
    private String dayName;
    private Spinner dropdown;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ArrayList<Order> orders;
    private ArrayList<String> available_times;
    private DateFormat dateFormat, timeFormat;
    private Button button;
    private TextView message;
    private FloatingActionButton micButton;
    private int REC_RESULT=0;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_date_time);

        user = (User) getIntent().getSerializableExtra("User");
        store = (Store) getIntent().getSerializableExtra("Store");
        pastOrder = (Order) getIntent().getSerializableExtra("PastOrder");

        message = findViewById(R.id.timeMessage);
        button = findViewById(R.id.buttonNext1);
        mySnackBar = new MySnackBar();

        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        timeFormat = new SimpleDateFormat("HH:mm");

        available_times = new ArrayList<>();
        orders = new ArrayList<>();

        micButton = findViewById(R.id.micButtonTime);
        dropdown = findViewById(R.id.dropDown);

        calendarView = findViewById(R.id.datePicker);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        calendar =  Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getDate());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, available_times);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(dataAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        myRef = myRef.child("Stores").child(store.getId()).child("orders");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                for (DataSnapshot order_snapshot : snapshot.getChildren()){
                    Order order = order_snapshot.getValue(Order.class);
                    if (order!=null && !order.getCompleted()){
                        orders.add(order);
                    }
                }
                setTimes();
                dataAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        calendarView.setOnDateChangeListener((calendarView, i, i1, i2) -> {
            calendar.set(i, i1, i2);
            getAvailableTimes(calendar);
            dataAdapter.notifyDataSetChanged();
        });

        //When user presses the next button, opens the next activity and pass the store and order
        button.setOnClickListener(view -> {
            Order order = new Order(user, dateFormat.format(calendar.getTime()), dropdown.getSelectedItem().toString(), false);
            Intent intent;
            //If it's a new purchase pass only store and order
            if(pastOrder==null){
                intent = new Intent(this, OrderPurchase.class);
                intent.putExtra("Store", store);
                intent.putExtra("Order", order);
            } else {
                //else if it is for reschedule pass and the past order
                intent = new Intent(this, OrderReschedule.class);
                intent.putExtra("Store", store);
                intent.putExtra("Order", order);
                intent.putExtra("PastOrder", pastOrder);
            }
            startActivity(intent);
        });

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            recognise();
        });

        getAvailableTimes(calendar);
    }

    //Method for voice recognition
    public void recognise() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
        startActivityForResult(intent, REC_RESULT);
    }

    //Method that checks if it is Sunday
    public void getAvailableTimes(Calendar calendar){
        dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, getResources().getConfiguration().locale);
        if (dayName.equals("Κυριακή") || dayName.equals("Sunday")){
            message.setText(getString(R.string.sunday));
            message.setVisibility(View.VISIBLE);
            dropdown.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        } else {
            setTimes();
        }
    }

    //Method that gets all the available times for the selected date
    public void setTimes(){
        available_times.clear();
        for(int i=9; i<21; i=i+1){
            for(int j=0; j<=45; j=j+15){
                StringBuilder stringBuilder = new StringBuilder();
                if(i==9){
                    stringBuilder.append("0").append(i).append(":");
                } else {
                    stringBuilder.append(i).append(":");
                }
                if (j==0){
                    stringBuilder.append("00");
                } else {
                    stringBuilder.append(j);
                }
                available_times.add(stringBuilder.toString());
            }
        }

        if (dateFormat.format(calendar.getTime()).equals(dateFormat.format(new Date()))){
           ArrayList<String> tmp = new ArrayList<>();
           for(String i : available_times){
               if (i.compareTo(timeFormat.format(new Date())) > 0){
                    tmp.add(i);
               }
           }
           available_times.clear();
           available_times.addAll(tmp);
        }
        removeTaken();
    }

    //method that removes the taken times
    public void removeTaken(){
        for (Order order : orders){
            if (order.getDate().equals(dateFormat.format(calendar.getTime()))){
                available_times.remove(order.getTime());
            }
        }
        if (available_times.isEmpty()){
            message.setText(getString(R.string.noTime));
            message.setVisibility(View.VISIBLE);
            dropdown.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        } else {
            message.setText(getString(R.string.noTime));
            message.setVisibility(View.INVISIBLE);
            dropdown.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }

    //Show the results of the voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if ( matches.get(0).toLowerCase().contains("back") || matches.get(0).toLowerCase().contains("πίσω")) {
                onBackPressed();
            } else if (matches.get(0).toLowerCase().contains("next") || matches.get(0).toLowerCase().contains("επόμενο")){
                button.performClick();
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