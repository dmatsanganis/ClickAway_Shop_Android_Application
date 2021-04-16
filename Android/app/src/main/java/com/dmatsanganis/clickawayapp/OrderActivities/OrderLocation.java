package com.dmatsanganis.clickawayapp.OrderActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.ItemInStorage;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.Classes.User;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.RecyclerViewAdapters.OrderLocationRecyclerViewAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OrderLocation extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private double longitude = 0;
    private double latitude = 0;
    private boolean canContinue;
    private ArrayList<Store> stores;
    private ArrayList<Store> results_stores;
    private User user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private Button searchByName, buttonGps, buttonNext;
    private EditText editText;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private Location myLocation;
    private OrderLocationRecyclerViewAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private boolean searchActive = false;
    private ConstraintLayout parent;
    private MySnackBar mySnackBar;
    private RecyclerView recyclerView;
    private int REC_RESULT=0;
    private FloatingActionButton micButton;
    private TextView textView;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_location);

        searchByName = findViewById(R.id.searchByName);
        buttonGps = findViewById(R.id.buttonGps);
        buttonNext = findViewById(R.id.buttonNext);
        editText = findViewById(R.id.searchLocationName);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        myLocation = new Location("");

        canContinue = false;
        buttonNext.setVisibility(View.INVISIBLE);

        mySnackBar = new MySnackBar();

        results_stores = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewOrderStores);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new OrderLocationRecyclerViewAdapter(getApplicationContext(), results_stores);
        recyclerView.setAdapter(adapter);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        parent = findViewById(R.id.parent);
        micButton = findViewById(R.id.micButtonLocation);
        textView = findViewById(R.id.textAddLocation);

        user = (User) getIntent().getSerializableExtra("User");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        stores = new ArrayList<>();

        //Get all the stores from the firebase
        myRef = myRef.child("Stores");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stores.clear();
                results_stores.clear();
                for (DataSnapshot store_snapshot : snapshot.getChildren()){
                    Store store = store_snapshot.getValue(Store.class);
                    stores.add(store);
                }
                results_stores.addAll(getResultsStores(stores, myLocation));
                adapter.setStores(results_stores, canContinue);
                adapter.notifyDataSetChanged();
                if(canContinue){
                    buttonNext.setVisibility(View.VISIBLE);
                } else {
                    buttonNext.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //When user presses the search button, execute the method to find the available stores for the location the user adds
        searchByName.setOnClickListener(view -> {
            hideKeyboard(view);
            searchActive = true;
            if (editText.getText().toString().equals("")){
                mySnackBar.show(getString(R.string.addLocation), getApplicationContext(), parent, false);
            } else {
                textView.setVisibility(View.INVISIBLE);
                results_stores.clear();
                results_stores.addAll(searchAddressByName(stores, editText.getText().toString()));
                adapter.setStores(results_stores, canContinue);
                adapter.notifyDataSetChanged();
                if(canContinue){
                    buttonNext.setVisibility(View.VISIBLE);
                } else {
                    buttonNext.setVisibility(View.INVISIBLE);
                }
            }
        });

        //When user presses the find my location button, execute the method to find the available stores for the current location of the user
        buttonGps.setOnClickListener(view -> {
            textView.setVisibility(View.INVISIBLE);
            hideKeyboard(view);
            searchActive = true;
            results_stores.clear();
            searchByGPS();
        });

        //When user presses the next button opens the next activity passing the user and the store object
        buttonNext.setOnClickListener(view -> {
            hideKeyboard(view);
            if (adapter.getSelected()!=null){
                Intent intent = new Intent(this, OrderDateTime.class);
                intent.putExtra("User", user);
                for (Store store : stores){
                    if (store.getId().equals(adapter.getSelected().getId())){
                        intent.putExtra("Store", store);
                        break;
                    }
                }
                startActivity(intent);
            } else {
                mySnackBar.show(getString(R.string.selectStore), getApplicationContext(), parent, false);
            }
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        recyclerView.setOnClickListener(view -> parent.requestFocus());
        parent.setOnClickListener(view -> parent.requestFocus());

        //If user presses the mic button starts the voice recognition
        micButton.setOnClickListener((view1) -> {
            parent.requestFocus();
            recognise();
        });
    }

    //Method for voice recognition
    public void recognise() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
        startActivityForResult(intent, REC_RESULT);
    }

    //Method that finds the available stores for the location of the user by using gps
    public void searchByGPS(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                builder = new android.app.AlertDialog.Builder(OrderLocation.this);
                View delete_layout = LayoutInflater.from(OrderLocation.this).inflate(R.layout.wait_gps_dialog,null);
                builder.setView(delete_layout);
                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }else{
                showGPSDisabledAlert();
            }
        }
    }

    //Method that shows dialog when gps is off, and opens the location settings
    private void showGPSDisabledAlert(){
        builder = new android.app.AlertDialog.Builder(OrderLocation.this);
        View delete_layout = LayoutInflater.from(OrderLocation.this).inflate(R.layout.gps_disabled_dialog,null);
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

        buttonNo.setOnClickListener(view -> dialog.dismiss());
    }

    //Method that finds the available stores for the address the user's enters
    public ArrayList<Store> searchAddressByName(ArrayList<Store> stores, String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                latitude = addresses.get(0).getLatitude();
                longitude = addresses.get(0).getLongitude();
                myLocation.setLatitude(latitude);
                myLocation.setLongitude(longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResultsStores(stores, myLocation);
    }

    //Method that returns the result stores, first searches for stores that have all products from the cart available, then search for the stores that have a part of the products of the cart
    public ArrayList<Store> getResultsStores(ArrayList<Store> stores, Location myLocation){
        ArrayList<Store> available_stores = getDistances(stores, myLocation);
        ArrayList<Store> result_stores = new ArrayList<>();
        if (available_stores.isEmpty() && searchActive){
            mySnackBar.show(getString(R.string.notInLocation), getApplicationContext(), parent, false);
            canContinue = false;
        } else if (searchActive){
                result_stores.addAll(checkIfAllInStores(user.getCart(), available_stores));
                if (result_stores.isEmpty()){
                    canContinue = false;
                    mySnackBar.show(getString(R.string.notAllProducts), getApplicationContext(), parent, false);
                    for (Store storeWithCartItems : checkQuantities(user.getCart(), available_stores)){
                        result_stores.add(storeWithCartItems);
                        boolean noItemsInStorage = true;
                        for (ItemInStorage itemInStorage : storeWithCartItems.getStorage()){
                            if (itemInStorage.getQuantity() > 0) {
                                noItemsInStorage = false;
                                break;
                            }
                        }
                        if(noItemsInStorage){
                            result_stores.remove(storeWithCartItems);
                        }
                    }
                } else {
                    ArrayList<Store> tmp = new ArrayList<>(checkQuantities(user.getCart(), result_stores));
                    result_stores.clear();
                    result_stores.addAll(tmp);
                    canContinue = true;
                }
        }
        //Put the current location, and the available stores for this location on the map
        mapFragment.getMapAsync(googleMap -> {
            googleMap.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            builder.include(myPosition);
            googleMap.addMarker(new MarkerOptions()
                    .position(myPosition)
                    .title(getString(R.string.yourLocation))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            for (Store store : result_stores){
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
            }
            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        });
        return result_stores;
    }

    //Method that creates a list with the stores (and their distances from current location) that are inside a 50.000 meters radius and sort them by ascending distance
    public ArrayList<Store> getDistances(ArrayList<Store> stores, Location location){
        class StoreWithDistance {
            private final Store store;
            private final double distance;
            public StoreWithDistance(Store store, double distance){
                this.store = store;
                this.distance = distance;
            }
            public double getDistance() {
                return distance;
            }
            public Store getStore() {
                return store;   
            }
        }

        ArrayList<StoreWithDistance> storesWithDistances = new ArrayList<>();
        for (Store store : stores){
            Location store_location = new Location("");
            store_location.setLongitude(store.getLongitude());
            store_location.setLatitude(store.getLatitude());

            double distance = location.distanceTo(store_location);
            if (distance <= 50000) {
                StoreWithDistance storeWithDistance = new StoreWithDistance(store, distance);
                storesWithDistances.add(storeWithDistance);
            }
        }
        storesWithDistances.sort(Comparator.comparing(StoreWithDistance::getDistance));
        ArrayList<Store> available_stores = new ArrayList<>();
        for(StoreWithDistance store : storesWithDistances){
            available_stores.add(store.getStore());
        }
        return available_stores;
    }

    //Method that returns a list with all the stores that have availability for all the products in the cart
    public ArrayList<Store> checkIfAllInStores(ShoppingCart cart, ArrayList<Store> stores){
        ArrayList<Store> storesWithAllCart = new ArrayList<>(stores);
        for (ItemToPurchase item : cart.getItems()){
            for(Store store : stores){
                for (ItemInStorage itemInStorage : store.getStorage()){
                    if (( (item.getSmartphone() == itemInStorage.getSmartphone()) || (item.getLaptop() == itemInStorage.getLaptop()) || (item.getTablet() == itemInStorage.getTablet()) )
                            && (item.getQuantity() > itemInStorage.getQuantity())){
                        storesWithAllCart.remove(store);
                    }
                }
            }
        }
        return storesWithAllCart;
    }

    //Method that returns a list with the stores that have some of the products available
    public ArrayList<Store> checkQuantities(ShoppingCart cart, ArrayList<Store> stores){
        ArrayList<Store> storesWithCartItems = new ArrayList<>();
        for(Store store : stores){
            ArrayList<ItemInStorage> itemsAvailablePerStore = new ArrayList<>();
            for (ItemToPurchase item : cart.getItems()){
                for (ItemInStorage itemInStorage : store.getStorage()){
                    if(item.getSmartphone() != null && itemInStorage.getSmartphone() != null){
                        if(item.getSmartphone().getId().equals(itemInStorage.getSmartphone().getId())){
                            ItemInStorage availableItem = new ItemInStorage(itemInStorage.getSmartphone(), itemInStorage.getQuantity());
                            itemsAvailablePerStore.add(availableItem);
                        }
                    } else if (item.getLaptop() != null && itemInStorage.getLaptop() != null){
                        if(item.getLaptop().getId().equals(itemInStorage.getLaptop().getId())){
                            ItemInStorage availableItem = new ItemInStorage(itemInStorage.getLaptop(), itemInStorage.getQuantity());
                            itemsAvailablePerStore.add(availableItem);
                        }
                    } else if (item.getTablet() != null && itemInStorage.getTablet() != null){
                        if(item.getTablet().getId().equals(itemInStorage.getTablet().getId())){
                            ItemInStorage availableItem = new ItemInStorage(itemInStorage.getTablet(), itemInStorage.getQuantity());
                            itemsAvailablePerStore.add(availableItem);
                        }
                    }
                }
            }
            Store storeOnlyCartItems = new Store(store.getId(), store.getTitle_en(), store.getTitle_gr(), store.getLongitude(), store.getLatitude(),
                    store.getPhone(), store.getAddress_gr(), store.getAddress_en(), itemsAvailablePerStore, store.getOrders());
            storesWithCartItems.add(storeOnlyCartItems);
        }
        return storesWithCartItems;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    //Method that creates a bitmap icon for the map based on a drawable
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        DrawableCompat.setTint(vectorDrawable, getResources().getColor(R.color.red));
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //When we get one gps signal find the available stores for this location and update
    @Override
    public void onLocationChanged(@NonNull Location location) {
        myLocation = location;
        locationManager.removeUpdates(this);
        dialog.dismiss();
        results_stores.clear();
        results_stores.addAll(getResultsStores(stores, myLocation));
        adapter.setStores(results_stores, canContinue);
        adapter.notifyDataSetChanged();
        if(canContinue){
            buttonNext.setVisibility(View.VISIBLE);
        } else {
            buttonNext.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean gpsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        //If gps permission was granted then run the code without the need of clicking the button again
        if (gpsAccepted) {
            searchByGPS();
        }
        //Else show snackbar with the permission denial
        else{
            mySnackBar = new MySnackBar();
            mySnackBar.show(getString(R.string.permissionDenied), getApplicationContext(), parent, false);
        }
    }

    //Show the results of the voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.get(0).toLowerCase().contains("select") || matches.get(0).toLowerCase().contains("επιλογή")) {
                String numberOnly = matches.get(0).replaceAll("[^0-9]", "");
                if (!numberOnly.equals("") && Integer.parseInt(numberOnly) <= results_stores.size() && Integer.parseInt(numberOnly) > 0){
                    recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(numberOnly)-1).itemView.findViewById(R.id.check).performClick();
                }
            } else if ( matches.get(0).toLowerCase().contains("back") || matches.get(0).toLowerCase().contains("πίσω")) {
                onBackPressed();
            } else if (matches.get(0).toLowerCase().contains("find me") || matches.get(0).toLowerCase().contains("my location")
                    || matches.get(0).toLowerCase().contains("τοποθεσία μου") || matches.get(0).toLowerCase().contains("βρες με")){
                buttonGps.performClick();
            } else if (matches.get(0).toLowerCase().contains("search") || matches.get(0).toLowerCase().contains("αναζήτηση")){
                searchByName.performClick();
            } else if (matches.get(0).toLowerCase().contains("next") || matches.get(0).toLowerCase().contains("επόμενο")){
                buttonNext.performClick();
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