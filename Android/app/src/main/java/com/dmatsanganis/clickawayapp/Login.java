package com.dmatsanganis.clickawayapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dmatsanganis.clickawayapp.Classes.InternetUtilities.NetworkChangeListener;
import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.User;

import java.util.Locale;

public class Login extends AppCompatActivity {
    private TextInputEditText email, password;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private Button login;
    private TextView register, forgotPassword;
    private ConstraintLayout parent;
    private SharedPreferences sharedPreferences;
    private CheckBox checkBox;
    private MySnackBar mySnackBar;
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode(getResources().getConfiguration().getLocales().get(0).toString());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        email = findViewById(R.id.myEmail);
        password = findViewById(R.id.editTextEmail);
        login = findViewById(R.id.buttonLogin);
        parent = findViewById(R.id.parent);
        checkBox = findViewById(R.id.checkBox);
        register = findViewById(R.id.register);
        forgotPassword = findViewById(R.id.forgotPassword);
        mySnackBar = new MySnackBar();


        checkBox.setOnClickListener((view) -> parent.requestFocus());

        //If user presses the login button execute the sign in method
        login.setOnClickListener((view) -> {
            parent.requestFocus();
            signInWithEmailAndPassword();
        });

        //If user presses the register Textview open the Register Activity
        register.setOnClickListener((view) -> {
            parent.requestFocus();
            startActivity(new Intent(this, Register.class));
        });

        //If user presses the forgotPassword Textview the dialog appears
        forgotPassword.setOnClickListener((view) -> {
            parent.requestFocus();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View forgot_password_layout = LayoutInflater.from(this).inflate(R.layout.forgot_password_dialog,null);
            builder.setView(forgot_password_layout);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            EditText myEmail = forgot_password_layout.findViewById(R.id.myEmail);
            Button buttonSend = forgot_password_layout.findViewById(R.id.buttonSend);

            //If he presses the send button
            buttonSend.setOnClickListener(view1 -> {
                //If email is not empty
                if (!myEmail.getText().toString().isEmpty()){
                    //Use the firebase method for password reset
                    mAuth.sendPasswordResetEmail(myEmail.getText().toString()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            //If it was successful close the dialog and show the snackbar
                            dialog.dismiss();
                            mySnackBar.show(getString(R.string.emailSent), getApplicationContext(), parent, false);
                        } else {
                            //Else show the warning snackbar
                            switch (task.getException().getMessage()) {
                                case "The email address is badly formatted.":
                                    mySnackBar.show(getString(R.string.emailBad), getApplicationContext(), myEmail, false);
                                    break;
                                case "There is no user record corresponding to this identifier. The user may have been deleted.":
                                    mySnackBar.show(getString(R.string.wrongEmail), getApplicationContext(), myEmail, false);
                                    break;
                            }
                        }
                    });
                } else {
                    //Else show the warning snackbar
                    mySnackBar.show(getString(R.string.emailEmpty), getApplicationContext(), myEmail,false);
                }
            });

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
    }

    //Method for the sign in
    public void signInWithEmailAndPassword(){
        //If at least one input is empty then show the warning snackbar
        if (password.getText().toString().isEmpty() || email.getText().toString().isEmpty()){
            mySnackBar.show(getString(R.string.empty), getApplicationContext(), parent, false);
        } else {
            //Save the checkbox value to the shared preferences
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("keepLogged", checkBox.isChecked());
            editor.apply();

            //Sign in the user with the firebase method
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            myRef.child("Users").child(currentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            //Get the user's preferences and apply them and open the Main Activity
                                            User user = snapshot.getValue(User.class);
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("selectedLanguage", user.getLanguage());
                                            intent.putExtra("selectedTheme", user.getApp_theme());
                                            mySnackBar.show(getString(R.string.connected), getApplicationContext(), parent, false);
                                            finish();
                                            startActivity(intent);
                                            if(user.getLanguage().equals("Greek")){
                                                setLocale("el");
                                            } else {
                                                setLocale("en");
                                            }
                                            if (user.getApp_theme().equals("Dark")){
                                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                            } else {
                                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            //Else show the warning snackbar
                            switch (task.getException().getMessage()) {
                                case "The email address is badly formatted.":
                                    mySnackBar.show(getString(R.string.emailBad), getApplicationContext(), parent, false);
                                    break;
                                case "The password is invalid or the user does not have a password.":
                                    mySnackBar.show(getString(R.string.wrongPassword), getApplicationContext(), parent, false);
                                    break;
                                case "There is no user record corresponding to this identifier. The user may have been deleted.":
                                    mySnackBar.show(getString(R.string.wrongEmail), getApplicationContext(), parent, false);
                                    break;
                            }
                        }
                    });
        }
    }

    //Method that set the application's language
    public void setLocale(String lang) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(lang);
        res.updateConfiguration(conf, dm);
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //On back button pressed close the application
    @Override
    public void onBackPressed(){
        finishAffinity();
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
