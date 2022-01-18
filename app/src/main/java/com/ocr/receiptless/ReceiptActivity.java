package com.ocr.receiptless;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.ocr.receiptless.base.BaseRecyclerAdapter;
import com.ocr.receiptless.base.BaseRecyclerViewHolder;
import com.ocr.receiptless.model.Receipt;
import com.ocr.receiptless.model.User;
import com.ocr.receiptless.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ReceiptActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivPicture;
    EditText etType, etMerchant, etTotal, etTotalTax;
    TextView etDate, tvRecognizer;
    LinearLayout llText;
    Button btnSave, btnDelete;
    String imagePath;
    int categoryId, receiptId;
    boolean fromCamera;

    private TextRecognizer textRecognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        imagePath = getIntent().getStringExtra(Util.IMAGE_URI_PATH);
        categoryId = getIntent().getIntExtra("category_id",1);
        fromCamera = getIntent().getBooleanExtra(Util.FROM_CAMERA,false);

        ivPicture = findViewById(R.id.iv_picture);
        etType = findViewById(R.id.et_type);
        etMerchant = findViewById(R.id.et_merchant);
        etDate = findViewById(R.id.et_date);
        etTotal = findViewById(R.id.et_total);
        etTotalTax = findViewById(R.id.et_total_tax);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        llText = findViewById(R.id.ll_text_container);
        tvRecognizer = findViewById(R.id.tv_recognizer);

        btnDelete.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        if(fromCamera) {
            Uri imageUri = Uri.fromFile(new File(imagePath));
            ivPicture.setImageURI(imageUri);

            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            try {
                InputImage image = InputImage.fromFilePath(ReceiptActivity.this, imageUri);
                Task<Text> result = textRecognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        Log.e(Util.LOG, "success " + visionText.getTextBlocks());
                        String resultText = "";
                        boolean isTotalFound = false;
                        boolean isFirstBlock = true;
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            if(isFirstBlock){
                                etMerchant.setText(block.getText());
                                isFirstBlock = false;
                            }
                            String blockText = block.getText();
                            if(isTotalFound){
                                if(blockText.matches(".*\\d.*")){
                                    String total = blockText.replaceAll("\\D+","");
                                    etTotal.setText(total);
                                    isTotalFound = false;
                                }
                            }
                            resultText += blockText;
                            resultText = resultText + "\n";
                            if(blockText.toLowerCase().contains("total")){
                                isTotalFound = true;
                            }
                            //Point[] blockCornerPoints = block.getCornerPoints();
                            //Rect blockFrame = block.getBoundingBox();
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                //resultText+=lineText;
                                //Point[] lineCornerPoints = line.getCornerPoints();
                                //Rect lineFrame = line.getBoundingBox();
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    //resultText+=elementText;
                                    //Point[] elementCornerPoints = element.getCornerPoints();
                                    //Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }

                        Log.e(Util.LOG, resultText);
                        tvRecognizer.setText(resultText);
                    }
                }).addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                                Log.e(Util.LOG, "text recognizer failed");
                                tvRecognizer.setText("text recognizer failed");
                            }
                        });


            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Util.LOG, e.getMessage());
            }

            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            etDate.setText(String.valueOf(day + "/" + (month + 1) + "/" + year));
            etType.setText("Receipt");
        }else{
            //not from camera
            ivPicture.setVisibility(View.GONE);
            llText.setVisibility(View.GONE);
            receiptId = getIntent().getIntExtra("receipt_id",-1);
            if(receiptId != -1){
                getReceipt();
            }else{
                Toast.makeText(this, "failed to get receipt", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_save:
                if(fromCamera) {
                    addReceipt();
                }else {
                    updateReceipt();
                }
                break;
            case R.id. btn_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ReceiptActivity.this);
                builder.setTitle("Delete Receipt");
                TextView text = new TextView(ReceiptActivity.this);
                text.setText(String.valueOf(
                        "Are you sure you want to delete this receipt?"));
                text.setTextSize(20);
                text.setPadding(50, 50, 50, 50);
                builder.setView(text);
                builder.setPositiveButton("DELETE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReceipt();
                            }
                        });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
        }
    }

    private void updateReceipt(){
        if(!checkEditText()){
            return;
        }

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=updateReceipt&receiptId="+receiptId+"&type="+etType.getText().toString()
                +"&merchant="+etMerchant.getText().toString()+"&date="+etDate.getText().toString()+"&total="+etTotal.getText().toString()
                +"&totalTax="+etTotalTax.getText().toString();
        Log.e(Util.LOG, url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        if(response!=null){
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Toast.makeText(ReceiptActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                                if(jsonObject.getString("status").equals("success")) {
                                    finish();
                                }else{
                                    Toast.makeText(ReceiptActivity.this, "failed to update", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(ReceiptActivity.this, "Error",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(ReceiptActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getReceipt(){
        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readReceiptById&receiptId="+receiptId;
        Log.e(Util.LOG,"is posting ... "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        if(response!=null) {
                            try {
                                Log.e(Util.LOG, "response = " + response);
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonData = jsonObject.getJSONArray("data");
                                Log.e(Util.LOG, "data = " + jsonData.length());


                                if (jsonData.length() > 0) {

                                    JSONObject jsonReceipt = jsonData.getJSONObject(0);

                                    Receipt receipt = new Receipt();
                                    String id = jsonReceipt.getString("id");
                                    receipt.setId(Integer.parseInt(id));
                                    String categoryId = jsonReceipt.getString("category_id");
                                    receipt.setCategoryId(Integer.parseInt(categoryId));
                                    receipt.setMerchant(jsonReceipt.getString("merchant"));
                                    receipt.setType(jsonReceipt.getString("type"));
                                    receipt.setDate(jsonReceipt.getString("date"));
                                    receipt.setTotal(jsonReceipt.getString("total"));
                                    receipt.setTotalTax(jsonReceipt.getString("total_tax"));

                                    etType.setText(receipt.getType());
                                    etMerchant.setText(receipt.getMerchant());
                                    etDate.setText(receipt.getDate());
                                    etTotal.setText(receipt.getTotal());
                                    etTotalTax.setText(receipt.getTotalTax());

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(ReceiptActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                        finish();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void deleteReceipt(){
        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=deleteReceipt&userId="+Util.getUser(ReceiptActivity.this).getId()+
                "&receiptId="+receiptId;
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("status").equals("success")){
                                Toast.makeText(ReceiptActivity.this, "Receipt deleted successfully",Toast.LENGTH_LONG).show();
                                finish();
                            }else{
                                Toast.makeText(ReceiptActivity.this, "Failed to delete receipt",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(ReceiptActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void addReceipt() {

        if(!checkEditText()){
            return;
        }

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=addReceipt&userId="+Util.getUser(ReceiptActivity.this).getId()+"&type="+etType.getText().toString()
                +"&merchant="+etMerchant.getText().toString()+"&date="+etDate.getText().toString()+"&total="+etTotal.getText().toString()
                +"&totalTax="+etTotalTax.getText().toString()+"&categoryId="+categoryId;
        Log.e(Util.LOG, url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        if(response!=null){
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Toast.makeText(ReceiptActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                                if(jsonObject.getString("status").equals("success")) {
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(ReceiptActivity.this, "Error",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(ReceiptActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private boolean checkEditText(){
        if (TextUtils.isEmpty(etType.getText().toString().trim())) {
            Toast.makeText(this, "Please enter type", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(etMerchant.getText().toString().trim())) {
            Toast.makeText(this, "Please enter merchant", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(etDate.getText().toString().trim())) {
            Toast.makeText(this, "Please enter date", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(etTotal.getText().toString().trim())) {
            Toast.makeText(this, "Please enter total", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(etTotalTax.getText().toString().trim())) {
            Toast.makeText(this, "Please enter total tax", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void chooseDate(View view) {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        DatePickerDialog picker = new DatePickerDialog(ReceiptActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        etDate.setText(String.valueOf(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                    }
                }, year, month, day);
        picker.show();
    }
}
