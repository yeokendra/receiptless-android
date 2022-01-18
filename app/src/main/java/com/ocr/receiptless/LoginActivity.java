package com.ocr.receiptless;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.ocr.receiptless.model.User;
import com.ocr.receiptless.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    TextView email,password;
//
//    FirebaseAuth firebaseAuth;
//    private GoogleSignInClient mGoogleSignInClient;
//    DatabaseReference databaseReference;
//    FirebaseDatabase firebaseDatabase;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ask for permissions
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PackageManager.GET_PERMISSIONS);
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.INTERNET}, PackageManager.GET_PERMISSIONS);

        // hiding action bar
        getSupportActionBar().hide();

        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
//
//        FirebaseApp.initializeApp(this);
//        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseReference = firebaseDatabase.getReference("users");
//        firebaseDatabase.getReference("app_title").setValue(getTitle());
//
//        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
//                GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void logIn(View view) {

        if (TextUtils.isEmpty(email.getText().toString().trim())) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(password.getText().toString().trim())) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=login&email="+email.getText().toString()+"&password="+password.getText().toString();
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        if(response!=null){
                            try {
                                Log.e(Util.LOG,"data = "+response);
                                JSONObject jsonObject = new JSONObject(response);


                                if(jsonObject.length() > 0) {
                                    JSONObject jsonUser = jsonObject.getJSONArray("data").getJSONObject(0);

                                    User user = new User();
                                    user.setId(jsonUser.getString("id"));
                                    user.setEmail(jsonUser.getString("email"));
                                    user.setName(jsonUser.getString("name"));
                                    user.setSurName(jsonUser.getString("surname"));
                                    user.setPhone(jsonUser.getString("phone"));

                                    Util.saveUser(LoginActivity.this, user);

                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                }else{
                                    Toast.makeText(LoginActivity.this, "Failed to login",Toast.LENGTH_LONG).show();
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "Failed to login",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void signUp(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
        startActivityForResult(intent, 0);
    }

//
//        final AppCompatActivity thisActivity = this;
//
//        final ProgressDialog progress = ProgressDialog.show(LoginActivity.this, "Logging...",
//                "Please Wait...", true);
//        (firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()))
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(LoginActivity.this, "Login successfully.", Toast.LENGTH_LONG).show();
//
//                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                            intent.putExtra("email", email.getText().toString());
//                            startActivity(intent);
//
//                            thisActivity.finish();
//                        }
//                        else
//                            Toast.makeText(LoginActivity.this, "Login unsuccessfully.\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//
//                        progress.dismiss();
//                    }
//                });
//    }
//
//    public void signUp(View view) {
//        //Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
//        //startActivityForResult(intent, 0);
//    }
//
//    public void forgotPass(View view) {
//        //Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
//        //startActivityForResult(intent,0);
//    }
//
//    public void gmailLogIn(View view) {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, 9001);
//    }
//
//    public void fbLogIn(View view) {
//
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == 9001) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//
//                final AppCompatActivity thisActivity = this;
//
//                final ProgressDialog progress = ProgressDialog.show(LoginActivity.this, "Logging...",
//                        "Please Wait...", true);
//
//                final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//                firebaseAuth.signInWithCredential(credential)
//                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(LoginActivity.this, "Login successfully.", Toast.LENGTH_LONG).show();
//
//                                    //Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                    //intent.putExtra("email", firebaseAuth.getCurrentUser().getEmail());
//                                    //startActivity(intent);
//
//                                    //thisActivity.finish();
//                                }
//                                else {
//                                    Toast.makeText(LoginActivity.this, "Login unsuccessfully.\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//
//                                    if(false) {
//                                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//
//                                        String firstName = firebaseUser.getDisplayName().substring(0, firebaseUser.getDisplayName().indexOf(" "));
//                                        String surName = firebaseUser.getDisplayName().substring(1 + firebaseUser.getDisplayName().indexOf(" "));
//
//                                        final User user = new User(firstName, surName,
//                                                firebaseUser.getPhoneNumber(), firebaseUser.getEmail(),
//                                                firebaseUser.getUid(), "USD", "US");
//                                        (firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()))
//                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<AuthResult> task) {
//                                                        progress.dismiss();
//                                                        if (task.isSuccessful()) {
//                                                            Toast.makeText(LoginActivity.this, "Account created successfully.\nLogin again using Gmail.", Toast.LENGTH_LONG).show();
//                                                            databaseReference.child(user.getEmail().replace(".", "")).setValue(user);
//                                                            thisActivity.finish();
//                                                        } else
//                                                            Toast.makeText(LoginActivity.this, "Account was not created.\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                                    }
//                                                });
//                                    }
//                                }
//
//                                progress.dismiss();
//                            }
//                        });
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Toast.makeText(LoginActivity.this, "Login unsuccessfully.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}

