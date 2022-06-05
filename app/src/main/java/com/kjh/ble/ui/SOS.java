package com.kjh.ble.ui;

public class SOS {
    String phone_number;
    String name;

    public SOS(String phone_number, String name){
        this.phone_number = phone_number;
        this.name = name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getName() {
        return name;
    }
}
