package com.ocr.receiptless.model;

import java.util.HashMap;

public class Category {

    private int id;
    private int userId;
    private String categoryName;
    private boolean isDeleted;


    public Category() {
        this.id = -1;
        this.userId = -1;
        this.categoryName = "";
        this.isDeleted = false;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}