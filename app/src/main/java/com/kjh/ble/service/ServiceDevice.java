package com.kjh.ble.service;

import android.bluetooth.BluetoothGatt;

public class ServiceDevice {
    private String mac;
    private int alarm_from_phone;
    private int alarm_from_tracker;
    private String sms_text;
    BluetoothGatt bluetoothGatt;

    public ServiceDevice(String mac, int alarm_from_phone, int alarm_from_tracker, String sms_text, BluetoothGatt bluetoothGatt){
        this.mac = mac;
        this.alarm_from_phone = alarm_from_phone;
        this.alarm_from_tracker = alarm_from_tracker;
        this.sms_text = sms_text;
        this.bluetoothGatt = bluetoothGatt;
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

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
