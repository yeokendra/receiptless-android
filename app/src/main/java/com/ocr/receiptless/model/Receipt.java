package com.ocr.receiptless.model;

public class Receipt {

    private int categoryId;
    private int id;
    private String type;
    private String merchant;
    private String date;
    private String total;
    private String totalTax;

    public Receipt() {
        this.id = -1;
        this.categoryId = -1;
        this.type = "";
        this.merchant = "";
        this.date = "";
        this.total = "";
        this.totalTax = "";
    }

    public Receipt(int id, int category, String type, String merchant, String date, String total, String totalTax) {
        this.id = id;
        this.categoryId = category;
        this.type = type;
        this.merchant = merchant;
        this.date = date;
        this.total = total;
        this.totalTax = totalTax;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(String totalTax) {
        this.totalTax = totalTax;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
