package com.kjh.ble.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyLocation {
    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    String provide=null;
    String mac = null;
    Location network_location;
    Location gps_location;

    private static MyLocation single_instance = null;
    int gps_permission;

    boolean exit_flag = false;

    String sdPath;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private boolean log_fileIO_flag = false;


    public static MyLocation getInstance()
    {
        if (single_instance == null)
            single_instance = new MyLocation();

        return single_instance;
    }

    public void setMac(String _mac){
        if(_mac != null)
            mac = _mac;
    }

    public boolean getLocation(Context context, LocationResult result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult = result;

        gps_location = null;
        network_location = null;

        if (lm == null) {
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.d("gps","disable");
        }
        //network_enabled = false;

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("network","disable");
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled){
            return false;
        }


        try {
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setBearingRequired(false);
            criteria.setAltitudeRequired(false);
            lm.requestLocationUpdates(5000,0, criteria, locationListenerGps, Looper.getMainLooper());
            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);

        } catch (SecurityException e) {
            Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
        }

        /*
        if (gps_enabled) {
            try {
                Criteria criteria = new Criteria();
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                lm.requestSingleUpdate(criteria, locationListenerGps, Looper.getMainLooper());
                //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);

            } catch (SecurityException e) {
                Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
            }


            provide="gps";
        }


        if (network_enabled) {
            try {
                Criteria criteria = new Criteria();
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                lm.requestSingleUpdate(criteria, locationListenerNetwork, Looper.getMainLooper());
                //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

            } catch (SecurityException e) {
                Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
            }




            provide="network";
        }

         */



        /*
        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 3000);
        */
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            if(location==null)
                return;

            gps_location = location;



            //locationResult.gotLocation(location,mac);

            /*
            if(!mac.equals("emergency")){
                try {

                    lm.removeUpdates(this);
                    lm.removeUpdates(locationListenerNetwork);

                } catch (SecurityException e) {
                    Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
                }

            }

             */
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {

            if(location==null)
                return;

            network_location = location;



            //locationResult.gotLocation(location,mac);

            /*
            if(!mac.equals("emergency")){
                try {

                    lm.removeUpdates(this);
                    lm.removeUpdates(locationListenerGps);

                } catch (SecurityException e) {
                    Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
                }
            }

             */
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try {

                lm.removeUpdates(locationListenerGps);
                lm.removeUpdates(locationListenerNetwork);

            } catch (SecurityException e) {
                Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
            }

            Location net_loc = null, gps_loc = null;

            try {
                if (gps_enabled) {
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (network_enabled) {
                    net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    //gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

            } catch (SecurityException e) {
                Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
            }


            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime()) {
                    locationResult.gotLocation(gps_loc, mac);
                } else {
                    locationResult.gotLocation(net_loc, mac);
                    //locationResult.gotLocation(gps_loc, mac);
                }
                return;
            }

            if (gps_loc != null) {
                locationResult.gotLocation(gps_loc,mac);
                return;
            }
            if (net_loc != null) {
                locationResult.gotLocation(net_loc,mac);
                //locationResult.gotLocation(gps_loc,mac);
                return;
            }
            locationResult.gotLocation(null,mac);
        }
    }

    public static abstract class LocationResult {
        public abstract boolean gotLocation(Location location,String mac);
    }

    public Location getGps_location() {
        return gps_location;
    }

    public Location getNetwork_location() {
        return network_location;
    }

    public void exit_locaion(){
        exit_flag = true;
        try {

            lm.removeUpdates(locationListenerNetwork);
            lm.removeUpdates(locationListenerGps);

        } catch (SecurityException e) {
            Log.d("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED:"+e);
        }
    }


}