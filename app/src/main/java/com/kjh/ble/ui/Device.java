package com.kjh.ble.ui;

public class Device {
    private String mac;
    private int alarm_from_phone;
    private int alarm_from_tracker;
    private String sms_text;

    public Device(String mac, int alarm_from_phone, int alarm_from_tracker, String sms_text){
        this.mac = mac;
        this.alarm_from_phone = alarm_from_phone;
        this.alarm_from_tracker = alarm_from_tracker;
        this.sms_text = sms_text;
    }

    public String getMac() {
        return mac;
    }

    public int getAlarm_from_phone() {
        return alarm_from_phone;
    }

    public int getAlarm_from_tracker() {
        return alarm_from_tracker;
    }

    public String getSms_text() {
        return sms_text;
    }
}
