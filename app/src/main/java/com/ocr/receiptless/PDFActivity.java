package com.ocr.receiptless;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ocr.receiptless.base.BaseRecyclerAdapter;
import com.ocr.receiptless.base.BaseRecyclerViewHolder;
import com.ocr.receiptless.model.Receipt;
import com.ocr.receiptless.model.User;
import com.ocr.receiptless.util.Util;
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.tejpratapsingh.pdfcreator.views.PDFBody;
import com.tejpratapsingh.pdfcreator.views.PDFFooterView;
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView;
import com.tejpratapsingh.pdfcreator.views.PDFTableView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFPageBreakView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class PDFActivity extends PDFCreatorActivity {

    private final int TYPE_DAILY = 101;
    private final int TYPE_MONTHLY = 102;

    ArrayList<Receipt> receipts = new ArrayList<>();
    double total;
    int categoryId;
    int type;
    String date;
    String categoryName;
    File pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        categoryId = getIntent().getIntExtra("category",1);
        type = getIntent().getIntExtra("type",102);
        date = getIntent().getStringExtra("date");
        categoryName = getIntent().getStringExtra("category_name");

        readTotal(categoryId,date,type);
    }

    public void readTotal(int currentCategoryId, String subdate, int currentType){
        String url_rest_api;

        url_rest_api = Util.API_URL_PREFIX + "/rest_api.php?action=readTotal&userId="+Util.getUser(this).getId()
                    +"&categoryId="+currentCategoryId+"&date="+subdate+"&type="+currentType;


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
                                            total = Double.valueOf(jsonTotal.getString("total"));

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

                                            }
                                        }
                                    }
                                    String pdfName = "MXD"+Util.getUser(PDFActivity.this).getId()+categoryId+date;
                                    createPDF(pdfName, new PDFUtil.PDFUtilListener() {
                                        @Override
                                        public void pdfGenerationSuccess(File savedPDFFile) {
                                            Toast.makeText(PDFActivity.this, "PDF Created ", Toast.LENGTH_SHORT).show();
                                            pdfFile = savedPDFFile;
                                        }

                                        @Override
                                        public void pdfGenerationFailure(Exception exception) {
                                            Toast.makeText(PDFActivity.this, "PDF NOT Created", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
                        Toast.makeText(PDFActivity.this, "Error: " + error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected PDFHeaderView getHeaderView(int pageIndex) {
        PDFHeaderView headerView = new PDFHeaderView(getApplicationContext());

        PDFHorizontalView horizontalView = new PDFHorizontalView(getApplicationContext());

        PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString word = new SpannableString("MANEXID");
        word.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pdfTextView.setText(word);
        pdfTextView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        pdfTextView.getView().setGravity(Gravity.CENTER_VERTICAL);
        pdfTextView.getView().setTypeface(pdfTextView.getView().getTypeface(), Typeface.BOLD);

        horizontalView.addView(pdfTextView);

        PDFImageView imageView = new PDFImageView(getApplicationContext());
        LinearLayout.LayoutParams imageLayoutParam = new LinearLayout.LayoutParams(
                60,
                60, 0);
        imageView.setImageScale(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(R.drawable.manexidlogo);
        imageLayoutParam.setMargins(0, 0, 10, 0);
        imageView.setLayout(imageLayoutParam);

        horizontalView.addView(imageView);

        headerView.addView(horizontalView);

        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        headerView.addView(lineSeparatorView1);

        return headerView;
    }

    @Override
    protected PDFBody getBodyViews() {
        PDFBody pdfBody = new PDFBody();

        PDFTextView pdfCompanyNameView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.H3);
        pdfCompanyNameView.setText(Util.getUser(PDFActivity.this).getName() +" "+ Util.getUser(PDFActivity.this).getSurName());
        pdfBody.addView(pdfCompanyNameView);
        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView1);
        PDFTextView pdfAddressView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

        if(type == TYPE_MONTHLY){
            pdfAddressView.setText("Monthly - "+date);
        }else{
            pdfAddressView.setText("Daily - "+date);
        }
        pdfBody.addView(pdfAddressView);

        PDFLineSeparatorView lineSeparatorView2 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView2.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8, 0));
        pdfBody.addView(lineSeparatorView2);

        PDFLineSeparatorView lineSeparatorView3 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView3);

        int[] widthPercent = {20, 20, 20, 20, 20}; // Sum should be equal to 100%
        //String[] textInTable = {"1", "2", "3", "4"};
        PDFTextView pdfTableTitleView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        pdfTableTitleView.setText(categoryName);
        pdfBody.addView(pdfTableTitleView);

        int startNum = 1;
        PDFTableView.PDFTableRowView tableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());
        for(int i = 0; i < 5; i++) {

            PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            if(i == 0) {
                pdfTextView.setText("No.");
            }else if(i == 1){
                pdfTextView.setText("Merchant");
            }else if(i == 2){
                pdfTextView.setText("Date");
            }else if(i == 3){
                pdfTextView.setText("Total");
            }else if(i == 5){
                pdfTextView.setText("Total Tax");
            }
            tableHeader.addToRow(pdfTextView);
        }

        PDFTableView.PDFTableRowView tableRowView1 = new PDFTableView.PDFTableRowView(getApplicationContext());
        for (int i = 0; i < 5; i++) {
            PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText("");
            tableRowView1.addToRow(pdfTextView);
        }

        PDFTableView tableView = new PDFTableView(getApplicationContext(), tableHeader, tableRowView1);

        for (int i = 0; i < receipts.size(); i++) {
            // Create 10 rows
            PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (int j = 0; j < 5; j++) {
                PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                switch (j){
                    case 0:
                        pdfTextView.setText(startNum+"");
                        startNum = startNum + 1;
                        break;
                    case 1:
                        pdfTextView.setText(receipts.get(i).getMerchant());
                        break;
                    case 2:
                        pdfTextView.setText(receipts.get(i).getDate());
                        break;
                    case 3:
                        pdfTextView.setText(receipts.get(i).getTotal());
                        break;
                    case 4:
                        pdfTextView.setText(receipts.get(i).getTotalTax());
                        break;
                }
                tableRowView.addToRow(pdfTextView);
            }
            tableView.addRow(tableRowView);
        }
        tableView.setColumnWidth(widthPercent);
        pdfBody.addView(tableView);

        PDFLineSeparatorView lineSeparatorView4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView4.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8, 0));
        pdfBody.addView(lineSeparatorView4);

        PDFLineSeparatorView lineSeparatorView5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView5);

        PDFLineSeparatorView lineSeparatorView6 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView4.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8, 0));
        pdfBody.addView(lineSeparatorView6);

        PDFTextView pdfIconLicenseView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.H2);
        String formattedTotal = Util.formatCurrency(total);
        String totalText = "RP " + formattedTotal + ",-";

        pdfIconLicenseView.setText(totalText);
        pdfIconLicenseView.getView().setGravity(Gravity.END);
        pdfBody.addView(pdfIconLicenseView);

        return pdfBody;
    }

    @Override
    protected PDFFooterView getFooterView(int pageIndex) {
        PDFFooterView footerView = new PDFFooterView(getApplicationContext());

        PDFTextView pdfTextViewPage = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        pdfTextViewPage.setText(String.format(Locale.getDefault(), "Page: %d", pageIndex + 1));
        pdfTextViewPage.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0));
        pdfTextViewPage.getView().setGravity(Gravity.CENTER_HORIZONTAL);

        footerView.addView(pdfTextViewPage);

        return footerView;
    }

    @Nullable
    @Override
    protected PDFImageView getWatermarkView(int forPage) {
        PDFImageView pdfImageView = new PDFImageView(getApplicationContext());
        FrameLayout.LayoutParams childLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200, Gravity.CENTER);
        pdfImageView.setLayout(childLayoutParams);

        pdfImageView.setImageResource(R.drawable.manexidlogo);
        pdfImageView.setImageScale(ImageView.ScaleType.FIT_CENTER);
        pdfImageView.getView().setAlpha(0.3F);

        return pdfImageView;
    }

    @Override
    protected void onNextClicked(final File savedPDFFile) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = savedPDFFile.getName().substring(savedPDFFile.getName().lastIndexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(PDFActivity.this, "com.ocr.receiptless.fileProvider", savedPDFFile);
                intent.setDataAndType(contentUri, type);
            } else {
                intent.setDataAndType(Uri.fromFile(savedPDFFile), type);
            }
            startActivityForResult(intent, 101);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(PDFActivity.this, "No activity found to open this attachment.", Toast.LENGTH_LONG).show();
        }
        finish();
    }

}
