package com.ocr.receiptless;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.ocr.receiptless.model.User;
import com.ocr.receiptless.util.Util;

import org.json.JSONObject;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    TextView name;
    TextView surname;
    TextView phone;
    TextView email;
    TextView password;
    TextView rPassword;

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.name_tf);
        surname = findViewById(R.id.surname_tf);
        phone = findViewById(R.id.phone_tf);
        email = findViewById(R.id.email_tf);
        password = findViewById(R.id.password_tf);
        rPassword = findViewById(R.id.rPassword_tf);

    }

    public void registerAccount(View view) {

        final User user = new User("-1",name.getText().toString(), surname.getText().toString(),
                phone.getText().toString(), email.getText().toString(),
                password.getText().toString());

        if(TextUtils.isEmpty(user.getName().trim())) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(user.getSurName().trim())) {
            Toast.makeText(this, "Please enter sur name", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(user.getPhone().trim())) {
            Toast.makeText(this, "Please enter phone", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(user.getEmail().trim())) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(user.getPassword().trim())) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(rPassword.getText().toString().trim())) {
            Toast.makeText(this, "Please re-enter password", Toast.LENGTH_LONG).show();
            return;
        }
        else if(!user.getPassword().equals(rPassword.getText().toString())) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=register&name="+name.getText().toString()+"&surname="+surname.getText().toString()
                +"&phone="+phone.getText().toString()+"&email="+email.getText().toString()+"&password="+password.getText().toString();
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        Toast.makeText(CreateAccountActivity.this, response,Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(CreateAccountActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
