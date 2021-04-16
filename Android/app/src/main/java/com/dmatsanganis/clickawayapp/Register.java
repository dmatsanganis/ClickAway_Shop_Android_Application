package com.dmatsanganis.clickawayapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.User;

import java.util.Locale;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser currentUser;
    private EditText username, email, password, phone;
    private Button register;
    private ConstraintLayout parent;
    private MySnackBar mySnackBar;
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        currentUser = mAuth.getCurrentUser();

        username = findViewById(R.id.myEmail);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        phone = findViewById(R.id.editTextPhone);
        register = findViewById(R.id.buttonRegister);
        parent = findViewById(R.id.parent);
        checkBox = findViewById(R.id.checkBoxRegister);
        mySnackBar = new MySnackBar();

        //If user presses the register button execute the sign up method
        register.setOnClickListener((view) -> {
            parent.requestFocus();
            singUpWithEmailAndPassword();
        });
        username.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        email.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        phone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
    }

    //Method for the sign up
    public void singUpWithEmailAndPassword(){
        //If at least one input is empty then show the warning snackbar
        if (username.getText().toString().isEmpty() || email.getText().toString().isEmpty() ||
                password.getText().toString().isEmpty() || phone.getText().toString().isEmpty()){
            mySnackBar.show(getString(R.string.empty), getApplicationContext(), parent, false);
        } else {
            //Save the checkbox value to the shared preferences
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("keepLogged", checkBox.isChecked());
            editor.apply();

            //Create the user with the firebase method
            mAuth.createUserWithEmailAndPassword(
                    email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            //As default set the preferences that user has on his phone when he signs up
                            String language = null;
                            if(Locale.getDefault().getLanguage().equals("el")){
                                language = "Greek";
                            } else {
                                language = "English";
                            }
                            String app_theme = null;
                            int currentNightMode = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                            switch (currentNightMode) {
                                case Configuration.UI_MODE_NIGHT_NO:
                                    app_theme = "Light";
                                    break;
                                case Configuration.UI_MODE_NIGHT_YES:
                                    app_theme = "Dark";
                                    break;
                            }

                            //If the registration is completed successfully create a user object
                            User user = new User(
                                    username.getText().toString(),
                                    email.getText().toString(),
                                    phone.getText().toString(),
                                    language,
                                    app_theme,
                                    null
                                    );

                            currentUser = mAuth.getCurrentUser();
                            //Add the user to the realtime database
                            myRef.child("Users").child(currentUser.getUid())
                                    .setValue(user).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            //If the registration is completed successfully open the Main Activity
                                            currentUser = mAuth.getCurrentUser();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("selectedLanguage", user.getLanguage());
                                            intent.putExtra("selectedTheme", user.getApp_theme());
                                            startActivity(intent);
                                        }
                                    });
                        } else {
                            //Else show the warning snackbar
                            switch (task.getException().getMessage()) {
                                case "The email address is already in use by another account.":
                                    mySnackBar.show(getString(R.string.emailInUse), getApplicationContext(), parent, false);
                                    break;
                                case "The given password is invalid. [ Password should be at least 6 characters ]":
                                    mySnackBar.show(getString(R.string.wrongPasswordForm), getApplicationContext(), parent, false);
                                    break;
                            }
                        }
                    });
        }
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onStop(){
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @Override
    protected void onStart(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
}