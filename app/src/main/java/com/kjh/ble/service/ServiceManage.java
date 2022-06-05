package com.kjh.ble.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServiceManage {
    Context _context;
    Bind mBind;

    public static final String mBroadcastBind = "mBroadcastBind";

    public ServiceManage(Context _context){

        this._context = _context;

        if (isServiceRunningCheck(_context)) {
            if (mBind == null) {
                _context.bindService(new Intent(_context, BLEService.class), srvConn, Context.BIND_AUTO_CREATE);
            }
        } else {
            Intent serviceintent = new Intent(_context, BLEService.class);

            serviceintent.putExtra("packagename", _context.getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                _context.startForegroundService(serviceintent);
            }else {
                _context.startService(serviceintent);
            }
            _context.bindService(serviceintent, srvConn, Context.BIND_AUTO_CREATE);
        }

    }


    ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBind = Bind.Stub.asInterface(service);

            //Intent broadcastIntent = new Intent();
            //broadcastIntent.setAction(mBroadcastBind);
            //LocalBroadcastManager.getInstance(_context).sendBroadcast(broadcastIntent);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(mBroadcastBind);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(broadcastIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBind = null;
        }
    };

    public boolean isServiceRunningCheck(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.kjh.ble.service.BLEService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void unbind(Context context){
        try {
            context.unbindService(srvConn);
        }catch(Exception e){
            //Log.e("unbind1"," error:"+e);
        }

        if(mBind != null) {
            try {
                context.unbindService(srvConn);
            }catch(Exception e){
                //Log.e("unbind2"," error:"+e);
            }
        }

    }

    public void tracker_registration(){
        try{
            mBind.addmac_connect();
        }catch(Exception e){
            //Log.e("test","tracker_registration error"+e);
        }
    }

    public void add_tracker(String name, int kind){
        try{
            mBind.addmac(name, kind);
        }catch(Exception e){

        }
    }

    public void add_cancel(){
        try{
            mBind.add_cancel();
        }catch(Exception e){

        }
    }

    public void Trackers_delete(String macaddress){
        try{
            mBind.deletemac(macaddress);
        }catch(Exception e){

        }
    }
}


