package com.ocr.receiptless;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ocr.receiptless.base.BaseRecyclerAdapter;
import com.ocr.receiptless.base.BaseRecyclerViewHolder;
import com.ocr.receiptless.model.Category;
import com.ocr.receiptless.model.Receipt;
import com.ocr.receiptless.model.User;
import com.ocr.receiptless.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.max;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private final int TYPE_DAILY = 101;
    private final int TYPE_MONTHLY = 102;

    FloatingActionButton btnCapture;
    TextView tvCategory, tvDateType, tvDate, tvUser, tvTotal, tvSeeDetail;
    ImageButton btnMenu, btnAbout;
    CardView cvMenu;
    View vShadow;
    FrameLayout btnMenuHome, btnMenuReceipt, btnMenuProfile, btnMenuAbout, btnMenuChangePw, btnMenuLogout;
    RecyclerView rvReceipt;
    RelativeLayout rlSeeDetail;
    ImageView btnDownload;

    private ArrayList<Category> categories;
    private ArrayList<Receipt> receipts;

    int currentType = TYPE_DAILY;
    int currentCategoryId = -1;
    User user;
    boolean isFirsttime = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // hiding action bar
        getSupportActionBar().hide();

        // ask for permissions
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},PackageManager.GET_PERMISSIONS);
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.INTERNET},PackageManager.GET_PERMISSIONS);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PackageManager.GET_PERMISSIONS);

        btnCapture = findViewById(R.id.btn_capture);
        tvCategory = findViewById(R.id.tv_category_selector);
        tvDateType = findViewById(R.id.tv_daily_selector);
        tvDate = findViewById(R.id.tv_date_selector);
        tvUser = findViewById(R.id.tv_user);
        tvTotal = findViewById(R.id.tv_total_big);
        btnMenu = findViewById(R.id.btn_menu);
        btnAbout = findViewById(R.id.btn_about);
        cvMenu = findViewById(R.id.cv_menu);
        vShadow = findViewById(R.id.v_shadow);
        btnMenuHome = findViewById(R.id.btn_fl_home);
        btnMenuAbout = findViewById(R.id.btn_fl_about);
        btnMenuReceipt = findViewById(R.id.btn_fl_receipt);
        btnMenuProfile = findViewById(R.id.btn_fl_profile);
        btnMenuChangePw = findViewById(R.id.btn_fl_privacy);
        btnMenuLogout = findViewById(R.id.btn_fl_logout);
        rvReceipt = findViewById(R.id.rv_receipt_list);
        rlSeeDetail = findViewById(R.id.rl_see_detail);
        tvSeeDetail = findViewById(R.id.tv_see_detail);
        btnDownload = findViewById(R.id.btn_download);

        tvCategory.setOnClickListener(this);
        tvDateType.setOnClickListener(this);
        tvDate.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
        btnMenuHome.setOnClickListener(this);
        btnMenuProfile.setOnClickListener(this);
        btnMenuReceipt.setOnClickListener(this);
        btnMenuAbout.setOnClickListener(this);
        btnMenuChangePw.setOnClickListener(this);
        btnMenuLogout.setOnClickListener(this);
        vShadow.setOnClickListener(this);
        rlSeeDetail.setOnClickListener(this);
        btnDownload.setOnClickListener(this);

        currentType = TYPE_DAILY;
        tvDateType.setText(getString(R.string.daily));

        if(Util.getUser(this) == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }else{
            user = Util.getUser(this);
            tvUser.setText(user.getName()+ " "+user.getSurName());
        }

        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String date = day + "/" + (month + 1) + "/" + year;
        tvDate.setText(date);
        showReceiptList(false);


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(view);
                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMenu(false);
        User user = Util.getUser(this);
        if(user!=null){
            getCategory();
        }else{
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void chooseDate() {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        DatePickerDialog picker = new DatePickerDialog(HomeActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tvDate.setText(String.valueOf(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                        readTotal();
                    }
                }, year, month, day);
        picker.show();
    }

    public void readTotal(){
        String url_rest_api;
        String date = tvDate.getText().toString();
        if(currentType == TYPE_MONTHLY){
            String subdate = date.substring(date.indexOf('/'));
            url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readTotal&userId="+Util.getUser(this).getId()
                    +"&categoryId="+currentCategoryId+"&date="+subdate+"&type="+currentType;
        }else{
            url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readTotal&userId="+Util.getUser(this).getId()
                    +"&categoryId="+currentCategoryId+"&date="+date+"&type="+currentType;
        }

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
                                if (jsonData.length() > 0) {
                                    for(int i = 0; i < jsonData.length(); i++) {
                                        if(i == 0) {
                                            JSONObject jsonTotal = jsonData.getJSONObject(i);
                                            Double total = Double.valueOf(jsonTotal.getString("total"));
                                            String formattedTotal = Util.formatCurrency(total);
                                            String totalText = "RP " + formattedTotal + ",-";
                                            tvTotal.setText(totalText);
                                            if(total == 0){
                                                showSeeDetailButton(false);
                                            }else{
                                                showSeeDetailButton(true);
                                            }
                                        }else{
                                            JSONObject jsonReceiptData = jsonData.getJSONObject(i);
                                            JSONArray jsonReceiptList = jsonReceiptData.getJSONArray("receipt");
                                            if (jsonReceiptList.length() > 0) {
                                                receipts = new ArrayList<>();
                                                for(int j = 0; j < jsonData.length(); j++) {
                                                    JSONObject jsonReceipt = jsonReceiptList.getJSONObject(j);

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
                                                    receipts.add(receipt);


                                                }

                                                LinearLayoutManager layoutListManager = new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL, false);
                                                layoutListManager.setItemPrefetchEnabled(false);
                                                BaseRecyclerAdapter adapterCategory = new BaseRecyclerAdapter<Receipt>(HomeActivity.this, receipts, layoutListManager) {

                                                    @Override
                                                    public int getItemLayoutId(int viewType) {
                                                        return R.layout.list_receipt;
                                                    }

                                                    @Override
                                                    public void bindData(final BaseRecyclerViewHolder holder, final int position, final Receipt item) {
                                                        holder.getTextView(R.id.tv_type).setText(item.getType());
                                                        holder.getTextView(R.id.tv_merchant).setText(item.getMerchant());
                                                        holder.getTextView(R.id.tv_date).setText(item.getDate());
                                                        double totalTax = Double.parseDouble(item.getTotalTax());
                                                        holder.getTextView(R.id.tv_total_tax).setText(Util.formatCurrency(totalTax));
                                                        holder.setOnClickListener(R.id.rl_container, new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent intent = new Intent(HomeActivity.this, ReceiptActivity.class);
                                                                intent.putExtra(Util.FROM_CAMERA,false);
                                                                intent.putExtra("receipt_id", item.getId());
                                                                startActivity(intent);
                                                            }
                                                        });
                                                    }
                                                };
                                                adapterCategory.setHasStableIds(true);
                                                rvReceipt.setLayoutManager(layoutListManager);
                                                rvReceipt.setAdapter(adapterCategory);
                                                showReceiptList(false);

                                            }
                                        }
                                    }
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
                        Toast.makeText(HomeActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void chooseType(){
        if(currentType == TYPE_DAILY){
            currentType = TYPE_MONTHLY;
            tvDateType.setText(getString(R.string.monthly));
        }else{
            currentType = TYPE_DAILY;
            tvDateType.setText(getString(R.string.daily));
        }
        readTotal();
    }

    public void getCategory(){

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readCategory&userId="+Util.getUser(this).getId();
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
                                    categories = new ArrayList<>();
                                    for(int i = 0; i < jsonData.length(); i++) {
                                        JSONObject jsonCategory = jsonData.getJSONObject(i);

                                        if(jsonCategory.getString("is_deleted").equals("0")) {

                                            Category category = new Category();
                                            String id = jsonCategory.getString("id");
                                            category.setId(Integer.parseInt(id));
                                            category.setCategoryName(jsonCategory.getString("category_name"));
                                            category.setUserId(Integer.parseInt(jsonCategory.getString("user_id")));
                                            category.setDeleted(false);
                                            categories.add(category);
                                            Log.e(Util.LOG, "category = " + categories.get(categories.size()-1).getCategoryName());
                                        }
                                    }
                                    if(isFirsttime && categories.size() > 0){
                                        currentCategoryId = categories.get(0).getId();
                                        tvCategory.setText(categories.get(0).getCategoryName());
                                        isFirsttime = false;
                                        readTotal();
                                    }
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
                        Toast.makeText(HomeActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public void selectCategory(){
        if(categories != null && categories.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            AlertDialog alertDialog = builder.create();
            View v = getLayoutInflater().inflate(R.layout.dialog_rv, null);
            RecyclerView recyclerView = v.findViewById(R.id.rv_dialog);
            LinearLayoutManager layoutListManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            layoutListManager.setItemPrefetchEnabled(false);
            BaseRecyclerAdapter adapterCategory = new BaseRecyclerAdapter<Category>(this, categories, layoutListManager) {

                @Override
                public int getItemLayoutId(int viewType) {
                    return R.layout.list_item;
                }

                @Override
                public void bindData(final BaseRecyclerViewHolder holder, final int position, final Category item) {
                    holder.getTextView(R.id.item_details).setText(item.getCategoryName());
                    holder.setOnClickListener(R.id.rl_container, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            currentCategoryId = item.getId();
                            tvCategory.setText(item.getCategoryName());
                            alertDialog.dismiss();
                            readTotal();
                        }
                    });
                }
            };
            adapterCategory.setHasStableIds(true);
            recyclerView.setLayoutManager(layoutListManager);

            recyclerView.setAdapter(adapterCategory);
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setView(v);
            alertDialog.show();
        }else{
            getCategory();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_category_selector:
                selectCategory();
                break;
            case R.id.tv_daily_selector:
                chooseType();
                break;
            case R.id.tv_date_selector:
                chooseDate();
                break;
            case R.id.btn_menu:

                showMenu(true);
                break;
            case R.id.btn_about:
            case R.id.btn_fl_about:
                Intent intent5 = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent5);
                break;
            case R.id.btn_fl_home:
            case R.id.v_shadow:
                showMenu(false);
                break;
            case R.id.btn_fl_receipt:
                Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
                intent.putExtra(Util.FROM_CAMERA,false);
                startActivity(intent);
                break;
            case R.id.btn_fl_profile:
                Intent intent1 = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_fl_privacy:
                Intent intent2 = new Intent(HomeActivity.this, PrivacyActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_fl_logout:
                Util.saveUser(HomeActivity.this,null);
                Intent intent3 = new Intent(getApplicationContext(), LoginActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent3);
                finish();
                break;
            case R.id.rl_see_detail:
                if(rvReceipt.getVisibility() == View.VISIBLE) {
                    showReceiptList(false);
                }else{
                    showReceiptList(true);
                }
                break;
            case R.id.btn_download:
                Intent intent4 = new Intent(HomeActivity.this, PDFActivity.class);
                String date = tvDate.getText().toString();
                if(currentType == TYPE_MONTHLY) {
                    date = date.substring(date.indexOf('/'));
                }
                intent4.putExtra("date",date);
                intent4.putExtra("category",currentCategoryId);
                intent4.putExtra("type",currentType);
                intent4.putExtra("category_name",tvCategory.getText().toString());
                startActivity(intent4);

                break;
        }
    }

    private void showMenu(boolean isShow){
        if(isShow){
            cvMenu.setVisibility(View.VISIBLE);
            vShadow.setVisibility(View.VISIBLE);
        }else {
            cvMenu.setVisibility(View.GONE);
            vShadow.setVisibility(View.GONE);
        }
    }

    private void showReceiptList(boolean isShow){
        if(isShow){
            rvReceipt.setVisibility(View.VISIBLE);
            tvSeeDetail.setText(getString(R.string.see_less));
        }else{
            rvReceipt.setVisibility(View.GONE);
            tvSeeDetail.setText(getString(R.string.see_detail));
        }
    }

    private void showSeeDetailButton(boolean isShow){
        if(isShow){
            rvReceipt.setVisibility(View.VISIBLE);
            rlSeeDetail.setVisibility(View.VISIBLE);
            btnDownload.setVisibility(View.VISIBLE);
        }else{
            rvReceipt.setVisibility(View.GONE);
            rlSeeDetail.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);
        }
    }
}
