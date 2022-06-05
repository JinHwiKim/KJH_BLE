// Bind.aidl
package com.kjh.ble.service;

// Declare any non-default types here with import statements

interface Bind {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    boolean addmac(String _devicename, int kind_of_tracker);
    void addmac_connect();
    void add_cancel();
    void deletemac(String macaddress);

}