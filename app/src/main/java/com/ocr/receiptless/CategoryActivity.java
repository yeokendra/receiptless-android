package com.ocr.receiptless;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.ocr.receiptless.model.Category;
import com.ocr.receiptless.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class CategoryActivity extends AppCompatActivity {

    private ListItemAdapter listItemAdapter;
    private String imagePath;
    private boolean fromCamera;
    private ArrayList<Category> categories = new ArrayList<>();
    private SwipeMenuListView listView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listView);
        imagePath = getIntent().getStringExtra(Util.IMAGE_URI_PATH);
        fromCamera = getIntent().getBooleanExtra(Util.FROM_CAMERA,false);

        // set creator
        listView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem goodItem = new SwipeMenuItem(getApplicationContext());
                goodItem.setBackground(new ColorDrawable(Color.rgb(255, 165,
                        0)));
                goodItem.setWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        90, getResources().getDisplayMetrics()));
                goodItem.setIcon(R.drawable.edit);
                menu.addMenuItem(goodItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(255,0,
                        0)));
                deleteItem.setWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        90, getResources().getDisplayMetrics()));
                deleteItem.setIcon(R.drawable.delete);
                menu.addMenuItem(deleteItem);
            }
        });

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    // edit
                    case 0:
                        editCategory(categories.get(position).getId());
                        break;
                    // delete
                    case 1:
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                CategoryActivity.this);
                        builder.setTitle("Delete Category");
                        TextView text = new TextView(CategoryActivity.this);
                        text.setText(String.valueOf(
                                "Are you sure you want to delete this category?"));
                        text.setTextSize(20);
                        text.setPadding(50, 50, 50, 50);
                        builder.setView(text);
                        builder.setPositiveButton("DELETE",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteCategory(position);
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

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //selectSubCategory(arrayList.get(position));
                if(fromCamera) {
                    Intent intent = new Intent(CategoryActivity.this, ReceiptActivity.class);
                    intent.putExtra(Util.IMAGE_URI_PATH, imagePath);
                    intent.putExtra(Util.FROM_CAMERA,fromCamera);
                    intent.putExtra("category_id", categories.get(position).getId());
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(CategoryActivity.this, ReceiptListActivity.class);
                    intent.putExtra("category_name", categories.get(position).getCategoryName());
                    intent.putExtra("category_id", categories.get(position).getId());
                    startActivity(intent);
                }
            }
        });

        getCategory(Util.getUser(this).getId());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void getCategory(String userId){

        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readCategory&userId="+userId;
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
                                            if(!(fromCamera && jsonCategory.getString("id").equals("1"))) {
                                                Category category = new Category();
                                                String id = jsonCategory.getString("id");
                                                category.setId(Integer.parseInt(id));
                                                category.setCategoryName(jsonCategory.getString("category_name"));
                                                category.setUserId(Integer.parseInt(jsonCategory.getString("user_id")));
                                                category.setDeleted(false);
                                                categories.add(category);
                                            }
                                        }
                                    }

                                    if(listItemAdapter == null) {
                                        listItemAdapter = new ListItemAdapter(categories);
                                    }else{
                                        listItemAdapter.updateCategories(categories);
                                    }
                                    listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
                                    listView.setAdapter(listItemAdapter);
                                    Log.e(Util.LOG,"size = "+categories.size());
                                    Log.e(Util.LOG,"list = "+listItemAdapter.getCount());

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
                        Toast.makeText(CategoryActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void deleteCategory(int position){
        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=deleteCategory&userId="+Util.getUser(CategoryActivity.this).getId()+
                "&categoryId="+categories.get(position).getId();
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(CategoryActivity.this, jsonObject.getString("status"),Toast.LENGTH_LONG).show();
                            getCategory(Util.getUser(CategoryActivity.this).getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void addCategory(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        EditText editText = (EditText)v.findViewById(R.id.et_text);
                        if(editText != null && editText.getText() != null && editText.getText().toString().length() > 0){
                            //add category and refresh
                            addCategory(editText.getText().toString());
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };
        builder.setMessage(getString(R.string.input_category_name)).setPositiveButton(getString(R.string.save), dialogClickListener)
                .setNegativeButton(getString(R.string.close), dialogClickListener);
        builder.setView(v);
        builder.show();
    }

    public void editCategory(int categoryId){
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        EditText editText = (EditText)v.findViewById(R.id.et_text);
                        if(editText != null && editText.getText() != null && editText.getText().toString().length() > 0){
                            //add category and refresh
                            updateCategory(categoryId, editText.getText().toString());
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };
        builder.setMessage(getString(R.string.input_category_name)).setPositiveButton(getString(R.string.save), dialogClickListener)
                .setNegativeButton(getString(R.string.close), dialogClickListener);
        builder.setView(v);
        builder.show();
    }

    private void addCategory(String category){
        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=addCategory&userId="+Util.getUser(CategoryActivity.this).getId()+
                "&category="+category;
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(CategoryActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                            getCategory(Util.getUser(CategoryActivity.this).getId());
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void updateCategory(int categoryId, String category){
        String url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=updateCategory&userId="+Util.getUser(CategoryActivity.this).getId()+
                "&category="+category+"&categoryId="+categoryId;
        Log.e(Util.LOG,"url = "+url_rest_api);
        StringRequest stringRequest = new StringRequest(url_rest_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("status").equals("success")) {
                                getCategory(Util.getUser(CategoryActivity.this).getId());
                            }else{
                                Toast.makeText(CategoryActivity.this, getString(R.string.failed_to_update_category), Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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

    @Override
    public void onBackPressed() {
//        Intent returnIntent = new Intent();
//        setResult(CategoryActivity.RESULT_OK, returnIntent);
//        finish();
        super.onBackPressed();
    }

    class ListItemAdapter extends BaseAdapter {
        TextView textView;
        ArrayList<Category> categoryList = new ArrayList<>();

        ListItemAdapter(ArrayList<Category> categories){
            categoryList = categories;
        }

        ListItemAdapter(){
            categoryList = new ArrayList<>();
        }

        public void updateCategories(ArrayList<Category> categories){
            categoryList = new ArrayList<>();
            categoryList.addAll(categories);
        }

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public Object getItem(int i) {
            return categoryList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.list_item,null);
                textView = convertView.findViewById(R.id.item_details);
                convertView.setTag(textView);
            }
            else
                textView = (TextView) convertView.getTag();
            textView.setText(categoryList.get(position).getCategoryName());
            return convertView;
        }
    }
}