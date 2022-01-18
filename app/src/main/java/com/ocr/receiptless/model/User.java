package com.ocr.receiptless.model;

import java.util.HashMap;

public class User {

    private String id;
    private String name;
    private String surName;
    private String phone;
    private String email;
    private String password;
    private HashMap<String, Receipt> receipts;

    public User() {
        this.id = "-1";
        this.name = "";
        this.surName = "";
        this.phone = "";
        this.email = "";
        this.password = "";
        this.receipts = new HashMap<>();
    }

    public User(String id, String name, String surName, String phone, String email, String password) {
        this.id = id;
        this.name = name;
        this.surName = surName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.receipts = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setReceipts(HashMap<String, Receipt> receipts) {
        this.receipts = receipts;
    }

    public String getName() {
        return name;
    }

    public String getSurName() {
        return surName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public HashMap<String, Receipt> getReceipts() {
        return receipts;
    }
}