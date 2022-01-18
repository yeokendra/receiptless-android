package com.ocr.receiptless;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.ocr.receiptless.base.BaseRecyclerAdapter;
import com.ocr.receiptless.base.BaseRecyclerViewHolder;
import com.ocr.receiptless.model.Category;
import com.ocr.receiptless.model.Receipt;
import com.ocr.receiptless.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ReceiptListActivity extends AppCompatActivity {

    RecyclerView rvReceipt;
    ArrayList<Receipt> receipts;
    int categoryId;

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        rvReceipt = findViewById(R.id.rv_receipt_list);
        categoryId = getIntent().getIntExtra("category_id",1);
        String category = "";
        category =  getIntent().getStringExtra("category_name");
        try {
            getSupportActionBar().setTitle(category);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getReceiptList(categoryId);
    }

    public void getReceiptList(int categoryId){

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readReceipt&userId="+Util.getUser(this).getId()
                +"&categoryId="+categoryId;
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
                                    receipts = new ArrayList<>();
                                    for(int i = 0; i < jsonData.length(); i++) {
                                        JSONObject jsonReceipt = jsonData.getJSONObject(i);

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

                                    LinearLayoutManager layoutListManager = new LinearLayoutManager(ReceiptListActivity.this, LinearLayoutManager.VERTICAL, false);
                                    layoutListManager.setItemPrefetchEnabled(false);
                                    BaseRecyclerAdapter adapterCategory = new BaseRecyclerAdapter<Receipt>(ReceiptListActivity.this, receipts, layoutListManager) {

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
                                                    Intent intent = new Intent(ReceiptListActivity.this, ReceiptActivity.class);
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
                        Toast.makeText(ReceiptListActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

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


}
