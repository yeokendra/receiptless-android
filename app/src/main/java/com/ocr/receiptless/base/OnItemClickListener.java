package com.ocr.receiptless.base;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);
}
