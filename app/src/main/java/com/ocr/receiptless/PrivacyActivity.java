package com.ocr.receiptless;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ocr.receiptless.util.Util;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PrivacyActivity extends AppCompatActivity {

    EditText etPw, etConfirmPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        etPw = findViewById(R.id.et_pw);
        etConfirmPw = findViewById(R.id.et_confirm_pw);

        ((Button)findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(CategoryActivity.RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword(){
        String pw = etPw.getText().toString();
        String conPw = etConfirmPw.getText().toString();
        if(TextUtils.isEmpty(pw)) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_LONG).show();
            return;
        }else if(TextUtils.isEmpty(conPw)) {
            Toast.makeText(this, "Please confirm new password", Toast.LENGTH_LONG).show();
            return;
        }else if(!pw.equals(conPw)){
            Toast.makeText(this, "password does not match", Toast.LENGTH_LONG).show();
            return;
        }

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=updatePassword&userId="+Util.getUser(this).getId()+"&password="+etPw.getText().toString();
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(PrivacyActivity.this, jsonObject.getString("status"),Toast.LENGTH_LONG).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(PrivacyActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }



}
