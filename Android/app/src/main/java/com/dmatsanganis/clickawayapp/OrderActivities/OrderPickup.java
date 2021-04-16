package com.dmatsanganis.clickawayapp.OrderActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.Order;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderPickup extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SensorEventListener {
    private LocationManager locationManager;
    private android.app.AlertDialog.Builder builder;
    private AlertDialog dialog, dialog1, dialog2;
    private SupportMapFragment mapFragment;
    private Location myLocation;
    private Location store_location;
    private Store store;
    private Order order;
    private boolean gettingReady;
    private boolean ready, suggestion1, suggestion2, storeClosed, wait;
    private TextView orderStatus, storeName, storeAddress, time, humidity, temperature, sensorsMessage;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private SensorManager sensorManager;
    private Sensor humiditySensor, temperatureSensor;
    private double temp = 0;
    private double hum = 0;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_pickup);

        orderStatus = findViewById(R.id.textOrderStatus);
        storeName = findViewById(R.id.textStoreName);
        storeAddress = findViewById(R.id.textStoreAddress);
        time = findViewById(R.id.textTime);
        humidity = findViewById(R.id.humidity);
        temperature = findViewById(R.id.temperature);
        sensorsMessage = findViewById(R.id.sensorsMessage);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        store = (Store) getIntent().getSerializableExtra("Store");
        order = (Order) getIntent().getSerializableExtra("Order");
        gettingReady = false;
        suggestion1 = false;
        suggestion2 = false;
        storeClosed = false;
        wait = false;

        if (getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            storeName.setText(store.getTitle_gr());
            storeAddress.setText(store.getAddress_gr());
        } else {
            storeName.setText(store.getTitle_en());
            storeAddress.setText(store.getAddress_en());
        }
        orderStatus.setText(R.string.waiting);
        time.setText(getString(R.string.time) + ": " + order.getTime());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        myLocation = new Location("");
        store_location = new Location("");
        store_location.setLatitude(store.getLatitude());
        store_location.setLongitude(store.getLongitude());
        openGPS();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)!=null){
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            humidity.setText(R.string.humidityOff);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!=null){
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            temperature.setText(R.string.temperatureOff);
        }

    }

    public void openGPS(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                builder = new android.app.AlertDialog.Builder(this);
                View delete_layout = LayoutInflater.from(this).inflate(R.layout.wait_gps_dialog,null);
                builder.setView(delete_layout);
                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                showGPSDisabledAlert();
            }
        }
    }

    public void alert(String message, boolean isSuggestion){
        builder = new android.app.AlertDialog.Builder(this);
        View alert_dialog = LayoutInflater.from(this).inflate(R.layout.alert_dialog,null);
        builder.setView(alert_dialog);
        dialog1 = builder.create();
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.show();

        TextView alertMessage = alert_dialog.findViewById(R.id.alertMessage);
        Button buttonRight = alert_dialog.findViewById(R.id.alertRight);
        Button buttonLeft = alert_dialog.findViewById(R.id.alertLeft);

        alertMessage.setText(message);

        if(isSuggestion){
            buttonRight.setOnClickListener(view -> {
                buttonRight.setText(R.string.change);
                buttonRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_restore, 0);
                Intent intent = new Intent(getApplicationContext(), OrderDateTime.class);
                intent.putExtra("User", order.getUser());
                intent.putExtra("Store", store);
                intent.putExtra("PastOrder", order);
                startActivity(intent);
            });

            buttonLeft.setOnClickListener(view -> {
                dialog1.dismiss();
            });
        } else {
            buttonLeft.setVisibility(View.GONE);
            buttonRight.setText(R.string.okay);
            buttonRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0);
            buttonRight.setOnClickListener(view -> {
                dialog1.dismiss();
            });
        }
    }
    
    public void rating(){
        builder = new android.app.AlertDialog.Builder(this);
        View final_alert = LayoutInflater.from(this).inflate(R.layout.final_alert_dialog,null);
        builder.setView(final_alert);
        dialog2 = builder.create();
        dialog2.setCancelable(false);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.show();

        Button button = final_alert.findViewById(R.id.buttonFinalAlert);
        TextView message = final_alert.findViewById(R.id.messageFinalAlert);
        ImageView imageView = final_alert.findViewById(R.id.imageFinalAlert);
        RatingBar ratingBar = final_alert.findViewById(R.id.ratingBar);

        imageView.setVisibility(View.GONE);
        ratingBar.setVisibility(View.VISIBLE);
        message.setText(R.string.rate);
        button.setOnClickListener(view -> {
            dialog2.dismiss();
            finish();
        });
    }

    public void finalAlert(){
        builder = new android.app.AlertDialog.Builder(this);
        View final_alert = LayoutInflater.from(this).inflate(R.layout.final_alert_dialog,null);
        builder.setView(final_alert);
        dialog1 = builder.create();
        dialog1.setCancelable(false);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.show();

        Button button = final_alert.findViewById(R.id.buttonFinalAlert);
        TextView message = final_alert.findViewById(R.id.messageFinalAlert);
        ImageView imageView = final_alert.findViewById(R.id.imageFinalAlert);
        RatingBar ratingBar = final_alert.findViewById(R.id.ratingBar);

        imageView.setVisibility(View.VISIBLE);
        ratingBar.setVisibility(View.GONE);
        message.setText(R.string.qrCode);
        button.setOnClickListener(view -> {
            dialog1.dismiss();
            updateOrder();
            rating();
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        dialog.dismiss();
        myLocation.set(location);
        mapFragment.getMapAsync(googleMap -> googleMap.setOnMapLoadedCallback(() -> {
            googleMap.clear();
            loadMap(googleMap);
        }));
        double distance = location.distanceTo(store_location);

        DateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date starting_order_time = timeFormat.parse(order.getDate() + " " + order.getTime());
            Date ending_order_time = new Date(starting_order_time.getTime() + (15 * 60000));

            Date starting_time = timeFormat.parse(order.getDate() +  " 09:00");
            Date closing_time = timeFormat.parse(order.getDate() + " 21:00");
            Date now = new Date();

            if (closing_time.after(now) && starting_time.before(now)) {
                if (ending_order_time.before(now)){
                    orderStatus.setText(R.string.orderExpired);
                    alert(getString(R.string.orderExpired), false);
                    locationManager.removeUpdates(this);
                } else if (distance > 500 && now.after(starting_order_time)){
                    if (!suggestion1){
                        suggestion1 = true;
                        alert(getString(R.string.suggestionFar), true);
                    }
                } else if (starting_order_time.after(now) && distance <= 500 && distance>20) {
                    if (!gettingReady) {
                        orderStatus.setText(R.string.orderGetting);
                        gettingReady = true;
                        alert(getString(R.string.orderGetting), false);
                    }
                } else if (now.after(starting_order_time) && distance > 20){
                    if (!ready){
                        ready = true;
                        orderStatus.setText(R.string.ready);
                        alert(getString(R.string.ready), false);
                        RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                } else if (now.after(starting_order_time) && distance <= 20) {
                    orderStatus.setText(getString(R.string.ready));
                    RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    locationManager.removeUpdates(this);
                    finalAlert();
                } else if (now.before(starting_order_time) && distance <= 20){
                    long difference_In_Minutes = ((starting_order_time.getTime()-now.getTime()) / (1000 * 60)) % 60;
                    if (difference_In_Minutes > 30){
                        if(!suggestion2){
                            suggestion2 = true;
                            orderStatus.setText(R.string.suggestionReschedule);
                            alert(getString(R.string.suggestionReschedule), true);
                        }
                    } else {
                        orderStatus.setText(R.string.orderLine);
                        if (!wait){
                            alert(getString(R.string.orderLine), false);
                            wait = true;
                        }
                    }
                }
            } else if (!closing_time.after(now) && starting_time.before(now)){
                orderStatus.setText(R.string.storeClosedAfter);
                alert(getString(R.string.storeClosedAfter), false);
                locationManager.removeUpdates(this);
            } else if (closing_time.after(now) && !starting_time.before(now)){
                orderStatus.setText(R.string.storeOpening);
                if (!storeClosed){
                    alert(getString(R.string.storeOpening), false);
                    storeClosed = true;
                }
            }
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }
    }

    public void updateOrder(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        ArrayList<Order> orders = new ArrayList<>();
        ArrayList<Order> new_orders = new ArrayList<>();

        myRef.child("Stores").child(store.getId()).child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orders.clear();
                        new_orders.clear();
                        for (DataSnapshot order_snapshot : snapshot.getChildren()) {
                            Order order_in_db = order_snapshot.getValue(Order.class);
                            if (order_in_db != null) {
                                orders.add(order_in_db);
                            }
                        }
                        new_orders.addAll(orders);
                        int index;
                        for (Order old_order : orders) {
                            if (old_order.getUser().getEmail().equals(order.getUser().getEmail()) && old_order.getDate().equals(order.getDate()) && old_order.getTime().equals(order.getTime())) {
                                index = orders.indexOf(old_order);
                                new_orders.remove(index);
                                Order new_order = new Order(order.getUser(), order.getDate(), order.getTime(), true);
                                new_orders.add(index, new_order);
                            }
                        }
                        myRef.child("Stores").child(store.getId()).child("orders")
                                .setValue(new_orders).addOnCompleteListener(task -> {
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadMap(GoogleMap googleMap){
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        builder.include(myPosition);
        googleMap.addMarker(new MarkerOptions()
                .position(myPosition)
                .title(getString(R.string.yourLocation))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        LatLng storePosition = new LatLng(store.getLatitude(), store.getLongitude());
        builder.include(storePosition);
        String storeTitle, storeAddress;
        if (getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
            storeTitle = store.getTitle_gr();
            storeAddress = store.getAddress_gr();
        } else {
            storeTitle = store.getTitle_en();
            storeAddress = store.getAddress_en();
        }
        googleMap.addMarker(new MarkerOptions()
                .position(storePosition)
                .title(storeTitle)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_store))
                .snippet(storeAddress));
        LatLngBounds bounds = builder.build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        loadMap(googleMap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean gpsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        //If gps permission was granted then run the code without the need of clicking the button again
        if (gpsAccepted) {
            openGPS();
        } else{
            finish();
        }
    }

    private void showGPSDisabledAlert(){
        builder = new android.app.AlertDialog.Builder(this);
        View delete_layout = LayoutInflater.from(this).inflate(R.layout.gps_disabled_dialog,null);
        builder.setView(delete_layout);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button buttonYes = delete_layout.findViewById(R.id.gpsDisabledYes);
        Button buttonNo = delete_layout.findViewById(R.id.gpsDisabledNo);

        buttonYes.setOnClickListener(view -> {
            Intent callGPSSettingIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(callGPSSettingIntent);
            dialog.dismiss();
        });

        buttonNo.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });
    }

    @Override
    public void onProviderDisabled (String provider){
        showGPSDisabledAlert();
    }

    @Override
    public void onBackPressed() {
        // code here to show dialog
        super.onBackPressed();
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        openGPS();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        DrawableCompat.setTint(vectorDrawable, getResources().getColor(R.color.red));
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        if (sensorEvent.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            temperature.setText(sensorEvent.values[0] + " °C");
            temp = sensorEvent.values[0];
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            humidity.setText(sensorEvent.values[0] + " %");
            hum = sensorEvent.values[0];
        }

        StringBuilder message = new StringBuilder();

        if (temp <= 3){
            message.append(getString(R.string.ice));
        } else if (temp > 3 && temp <= 10){
            message.append(getString(R.string.veryCold));
        } else if (temp > 10 && temp <= 15){
            message.append(getString(R.string.cold));
        } else if (temp > 15 && temp <= 20){
            message.append(getString(R.string.cool));
        } else if (temp > 20 && temp <= 30){
            message.append(getString(R.string.hot));
        } else {
            message.append(getString(R.string.veryHot));
        }

        if (hum < 60){
            message.append(getString(R.string.lowHumidity));
        } else if (hum >= 60 && hum < 80){
            message.append(getString(R.string.highHumidity));
        } else {
            message.append(getString(R.string.rain));
        }

        sensorsMessage.setText(message);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
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