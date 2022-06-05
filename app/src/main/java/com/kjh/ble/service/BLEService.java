package com.kjh.ble.service;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static java.lang.Thread.sleep;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kjh.ble.R;
import com.kjh.ble.ui.DBHelper;
import com.kjh.ble.ui.Device;
import com.kjh.ble.ui.SOS;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class BLEService extends Service {
    BluetoothManager bluetoothManager; //블루투스매니저 선언(API 21이상에서 사용)
    BluetoothAdapter mBluetoothAdapter; //블루투스어댑터 선언
    private String packagename;

    private ArrayList<BluetoothDevice> scandevice = new ArrayList<>();
    private ArrayList<Integer> scandevicerssi = new ArrayList<>();

    private boolean addmacflag = false; //mac추가 등록 여부를 판단하기 위한 flag, flag가 true면 등록된 mac이외에 추가로 검색되는것을 연결한다.
    public int scan_count = 0;
    int maxnum;

    UUID ccb0uuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    UUID ccb4uuid = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    UUID ccb5uuid = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    UUID batteryservice = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    UUID batterycharacter = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private boolean add_flag = false;

    public static final String mBroadcastbeepingexit = "mBroadcastbeepingexit";
    public static final String mBroadcastservice = "mBroadcastservice";
    public static final String mBroadcastmConnectionState = "mBroadcastmConnectionState";
    public static final String mBroadcastBluetoothOn = "mBroadcastBluetoothOn";
    public static final String mBroadcastcharacter = "mBroadcastcharacter";
    public static final String mBroadcasta1 = "mBroadcasta1";
    public static final String mBroadcastf0 = "mBroadcastf0";
    public static final String mBroadcast01 = "mBroadcast01";
    public static final String mBroadcastDelete = "mBroadcastDelete";
    public static final String mBroadcastaddtracker = "mBroadcastaddtracker";
    public static final String mBroadcastaddmactimeout = "mBroadcastaddmactimeout";
    public static final String mBroadcastsetTrackername = "mBroadcastsetTrackername";
    public static final String mBroadcast_setting_change = "mBroadcast_setting_change";
    public static final String mBroadcast_service_destroy = "mBroadcast_service_destroy";
    public static final String mBroadcastSearchresult = "mBroadcastSearchresult";
    public static final String mBroadcastUpdate = "mBroadcastUpdate";
    public static final String mBroadcast_airplane = "android.intent.action.AIRPLANE_MODE";
    public static final String mBroadcastSearchState = "mBroadcastSearchState";
    public static final String mBroadcastfcmtest = "mBroadcastfcmtest";

    private boolean logout_flag = false;

    //연결상태 관련 변수 define
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private ArrayList<Integer> reconnect_step2_time = new ArrayList<>();
    private ArrayList<Integer> reconnect_step2_delay = new ArrayList<>();
    private ArrayList<Boolean> first_connect = new ArrayList<>();
    private ArrayList<Boolean> re_connect = new ArrayList<>();

    ArrayList<Boolean> reconnect_delay = new ArrayList<>();

    private int reconnect_step1_count = 0;

    boolean deleteflag = false;

    private volatile LinkedList<bleRequest> procQueue;
    private volatile LinkedList<bleRequest> nonBlockQueue;
    private static BLEService mThis = null;

    private final Lock lock = new ReentrantLock();

    public final static String ACTION_DATA_NOTIFY = "com.example.ti.ble.common.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "com.example.ti.ble.common.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.ti.ble.common.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.example.ti.ble.common.EXTRA_UUID";
    public final static String EXTRA_STATUS = "com.example.ti.ble.common.EXTRA_STATUS";

    DBHelper dbHelper;
    ArrayList<Device> deviceArrayList = new ArrayList<>();
    ArrayList<ServiceDevice> bleArrayList = new ArrayList<>();

    public int device_connect_state = 0;

    MyLocation safety;

    ArrayList<SOS> sos_list = new ArrayList<>();
    private MediaPlayer siren_sound;

    WindowManager m_WindowManager;
    View m_View;
    View m_View_sospopup;

    private boolean sos_flag = false;

    private int mapthread_count = 30;
    private boolean mapthread_flag = false;
    mapThread mapthread;

    public BLEService() {
        procQueue = new LinkedList<bleRequest>();
        nonBlockQueue = new LinkedList<bleRequest>();

        mThis = this;
    }


    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, final int status) {
        if(!logout_flag) {
            final Intent intent = new Intent(action);
            intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
            intent.putExtra(EXTRA_STATUS, status);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            //sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    //서비스 생성 이후 실행, 타 클래스에서 StartService실행시 불려진다.
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        packagename = intent.getStringExtra("packagename");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                //mBluetoothAdapter.enable();
            } else if (mBluetoothAdapter == null) {
                // stopSelf();

            }
        }else {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                //mBluetoothAdapter.enable();
            } else if (mBluetoothAdapter == null) {
                // stopSelf();
            }
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            //PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(BLEservice.this, 0, getPackageManager().getLaunchIntentForPackage(pakagename), PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setOngoing(true)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(icon)
                            .setGroup("service_noti")
                            .setContentTitle("BLE");


            mBuilder.setContentText(getString(R.string.service_running));



            Intent intent2 = new Intent();
            intent2.setClassName(packagename, packagename+".MainActivity");
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(getApplicationContext(),
                            1001,
                            intent2,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            startForeground(19890501, mBuilder.build());
        }else{
            NotificationChannel channel = new NotificationChannel(
                    "BLE_01",
                    "BLE",
                    NotificationManager.IMPORTANCE_MAX
            );
            channel.setSound(null, null);
            channel.setShowBadge (false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), "BLE_01");
            builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(icon)
                    .setContentTitle("BLE")
                    .setGroup("service_noti")
                    .setSound(null);

            builder.setContentText(getString(R.string.service_running));


            Intent intent2 = new Intent();
            intent2.setClassName(packagename, packagename+".MainActivity");
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(getApplicationContext(),
                            1001,
                            intent2,
                            PendingIntent.FLAG_IMMUTABLE
                    );
            builder.setContentIntent(resultPendingIntent);
            startForeground(19890501, builder.build());
        }


        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        deviceArrayList = dbHelper.getAllDeviceData();

        if(deviceArrayList.size()!=0){

            bleArrayList.add(new ServiceDevice(deviceArrayList.get(0).getMac(),deviceArrayList.get(0).getAlarm_from_phone(), deviceArrayList.get(0).getAlarm_from_tracker(), deviceArrayList.get(0).getSms_text(), null));

            if(mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //UUID[] uuids = {ccb0uuid};
                    //mBluetoothAdapter.startLeScan(uuids, leScanCallback);
                    mBluetoothAdapter.startLeScan(leScanCallback);
                } else {
                    ParcelUuid uuid = ParcelUuid.fromString(ccb0uuid.toString());
                    //byte[] bytes = {};
                    ArrayList<ScanFilter> filterList = new ArrayList<ScanFilter>();
                    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(uuid).build();
                    //ScanFilter filter = new ScanFilter.Builder().setServiceData(uuid,bytes).build();
                    filterList.add(filter);
                    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

                    mBluetoothAdapter.startLeScan(leScanCallback);
                }


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        } else {
                            //bluetoothLeScanner.stopScan(scanCallback);
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        }
                    }
                }, 7000);


            }
            for(int i =0; i<bleArrayList.size(); i++) {
                //mBluetoothAdapter.getRemoteDevice(macaddress.get(i)).connectGatt(this, false, mGattCallback);
                if(mBluetoothAdapter.isEnabled()) {

                    bleArrayList.get(i).setBluetoothGatt(mBluetoothAdapter.getRemoteDevice(bleArrayList.get(i).getMac()).connectGatt(this, false, mGattCallback));

                }
            }
        }


        //sosthread = new sosThread();

        return START_NOT_STICKY;
        //return START_STICKY;
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    //nearby
                    boolean scan_flag = true;

                    Log.e("service"," leScanCallback:"+device.getAddress());
                    /*
                    if (device.getName() != null) {
                        for (int i = 0; i < trackers.size(); i++) {
                            if (trackers.get(i).getTracker_macaddress().equals(device.getAddress())) {
                                scan_flag = false;

                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(mBroadcastSearchState);
                                broadcastIntent.putExtra("searchflag", "search");
                                broadcastIntent.putExtra("macaddress", device.getAddress());
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                                //break;
                            }
                        }
                    }

                     */

                    if(addmacflag && scan_flag) {
                        boolean temp = false;
                        for (int i = 0; i < scandevice.size(); i++) {
                            if (scandevice.get(i).getAddress().equals(device.getAddress())) {
                                if (scandevicerssi.get(i) < rssi)
                                    scandevicerssi.set(i, rssi);
                                temp = true;
                                break;
                            }
                        }
                        if (!temp) {
                            scandevice.add(device);
                            scandevicerssi.add(rssi);
                        }

                        try{
                            int temp_value=-100;
                            maxnum =0;
                            for(int i=0; i<scandevicerssi.size(); i++){
                                if(scandevicerssi.get(i)>temp_value) {
                                    temp_value = scandevicerssi.get(i);
                                    maxnum = i;
                                }
                            }

                            Log.d("scandevice",""+scandevice.get(maxnum));


                        }catch(Exception e){
                            //Log.e("service"," add error:"+e);
                        }

                    }

                }
            };



    //연결 시도된 gatt에 대한 callback function
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.e("Service", "status :" + status + ", newState :" + newState+", macaddress :"+gatt.getDevice().getAddress());

                    //연결되었을때
                    if (newState == BluetoothProfile.STATE_CONNECTED && status != 133) {

                        if (addmacflag && scandevice.get(maxnum).getAddress().equals(gatt.getDevice().getAddress())) {
//                            gatt.getDevice().createBond();

                            Device device = new Device(gatt.getDevice().getAddress(), 1, 1, "위급상황입니다. 도와주세요.");

                            //dbHelper.addDevice(device);

                            //deviceArrayList = dbHelper.getAllDeviceData();
                            bleArrayList.add(new ServiceDevice(device.getMac(), device.getAlarm_from_phone(), device.getAlarm_from_tracker(), device.getSms_text(), gatt));

                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction(mBroadcastaddtracker);
                            broadcastIntent.putExtra("success_flag",true);
                            broadcastIntent.putExtra("new_macaddress",gatt.getDevice().getAddress());
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                        }

                        gatt.discoverServices();

                        //연결이 끊겼을때
                    }else if (newState == BluetoothProfile.STATE_DISCONNECTED ) {
                        if(status != 133) {
                            //bleArrayList.get(0).setTrackers_state(STATE_DISCONNECTED);
                            try {
                                //BluetoothDevice device = bluetoothGatt.get(i).getDevice();
                                BluetoothDevice device = bleArrayList.get(0).getBluetoothGatt().getDevice();
                                gatt.close();

                                try{
                                    bleArrayList.get(0).getBluetoothGatt().close();
                                }catch(Exception e){

                                }

                                //bleArrayList.get(0).setBluetoothGatt_hash(0);

                                sleep(200);
                                //if (trackers.get(i).getTrackers_used() == true && updating_deviceinfo == false)
                                device.connectGatt(BLEService.this, false, mGattCallback);
                            } catch (Exception e) {
                                Log.d("disconnect", " error :" + e);
                            }
                        }else {

                        }

                        //연결 중일때
                    /*
                        중요한 이슈!: 블루투스 기기가 연결중일때 Service가 종료되어 버리면(약 1~2초사이) 해당 Ble는 PhoneDevice와 연결되나
                                   이를 관리하는 gatt서버가 없기때문에 ble는 연결되었지만 이에 대한 정보를 찾을 수 없는 경우가 있다. 이를 방지하기
                                   위해서는 Service종료시 혹은 해당 device 삭제시 conneting중인 device에 대해서는 잠시 delay를 주거나, 연결 실패
                                   혹은 연결성공을 기다렸다가 마저 해당기능을 수행하여야 한다.
                    */
                    } else if (newState == BluetoothProfile.STATE_CONNECTING) {

                    }


                }

                //서비스 검색 성공한경우
                @Override
                public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BluetoothGattCharacteristic characteristic = gatt.getService(ccb0uuid).getCharacteristic(ccb5uuid);
                        BluetoothGattDescriptor descriptor = gatt.getService(ccb0uuid).getCharacteristic(ccb5uuid).getDescriptors().get(0);
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean error = gatt.writeDescriptor(descriptor);

                        int count = 0;
                        for (count = 0; count < deviceArrayList.size(); count++) {
                            if (gatt.getDevice().getAddress().equals(deviceArrayList.get(count).getMac())) {
                                break;
                            }
                        }

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(mBroadcastcharacter);
                        broadcastIntent.putExtra("characteristic", true);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
                        Log.e("characteristic"," call");

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                boolean gatt_check = false;
                                int count = -1;
                                Log.e("mBroadcast01","try");

                                BluetoothGattCharacteristic characteristic = gatt.getService(ccb0uuid).getCharacteristic(ccb4uuid);
                                characteristic.setValue(new byte[]{(byte) 0x01});

                                if (gatt.writeCharacteristic(characteristic)) {


                                        //trackers.get(count).setTrackers_app_state(STATE_CONNECTED);
                                        device_connect_state = 1;
                                        //reconnect_delay.set(count, false);

                                        Intent broadcastIntent = new Intent();
                                        broadcastIntent.setAction(mBroadcastmConnectionState);
                                        broadcastIntent.putExtra("success_flag", "true");
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);


                                    }

                                    if(scandevice.size() != 0 && gatt.getDevice().getAddress().equals(scandevice.get(maxnum).getAddress())) {
                                        Intent broadcastIntent = new Intent();
                                        broadcastIntent.setAction(mBroadcast01);
                                        broadcastIntent.putExtra("01", true);
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                                        Log.e("mBroadcast01"," call");

                                        addmacflag = false;
                                    }
                            }
                        }, 3000);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BluetoothGattCharacteristic characteristic2 = gatt.getService(ccb0uuid).getCharacteristic(ccb4uuid);
                                characteristic2.setValue(new byte[]{(byte) 0xa1});
                                if (gatt.writeCharacteristic(characteristic2)) {
                                    Log.d("0xa1", "success");

                                } else {
                                    Log.d("0xa1", "false");
                                    if (scandevice.size() == 0 || !gatt.getDevice().getAddress().equals(scandevice.get(maxnum).getAddress())) {
                                        if (bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == 2)
                                            handler.postDelayed(this, 500);
                                    }
                                }

                                if (scandevice.size() != 0 && gatt.getDevice().getAddress().equals(scandevice.get(maxnum).getAddress())) {
                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction(mBroadcasta1);
                                    broadcastIntent.putExtra("a1", true);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                                    Log.e("mBroadcasta1"," call");
                                }
                            }
                        }, 3600);

                    } else {
                        //Log.w("통신", "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    //if (blocking)unlockBlockingThread(status);
                    if(!logout_flag) {
                        if (nonBlockQueue.size() > 0) {
                            lock.lock();
                            for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                                bleRequest req = nonBlockQueue.get(ii);
                                if (req.characteristic == characteristic) {
                                    req.status = bleRequestStatus.done;
                                    nonBlockQueue.remove(ii);
                                    break;
                                }
                            }
                            lock.unlock();
                        }
                        broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
                    }else{
                        stopSelf();
                    }
                }

                @Override
                public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
                    Log.d("onDescriptorWrite",descriptor.getCharacteristic().getUuid()+ " status:"+status);

                    if (descriptor.getCharacteristic().getUuid().toString().equals("0000ccb5-0000-1000-8000-00805f9b34fb")) {
                        BluetoothGattCharacteristic characteristic = gatt.getService(batteryservice).getCharacteristic(batterycharacter);
                        BluetoothGattDescriptor descriptor2 = gatt.getService(batteryservice).getCharacteristic(batterycharacter).getDescriptors().get(0);
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean error2 = gatt.writeDescriptor(descriptor2);
                    }
                }


                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if(!logout_flag) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            //Log.e("onCharacteristicRead", "" + characteristic.toString());
                        }
                    }else{
                        stopSelf();
                    }
                }

                //Ble Click감지를 위한
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.d("MyService", "BLE STICK IS CLICKED");
                    if(!logout_flag) {
                        if (characteristic.getUuid().equals(ccb5uuid)) {
                            if(byteArrayToHexString(characteristic.getValue()).length()==4){
                                add_flag = false;


                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(mBroadcastf0);
                                broadcastIntent.putExtra("f0", byteArrayToHexString(characteristic.getValue()));
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
                            }

                            if (byteArrayToHexString(characteristic.getValue()).equals("DD")) {
                                Log.d("MyService", "BLE STICK IS CLICKED");



                            }else if(byteArrayToHexString(characteristic.getValue()).equals("CC")){

                                if(!sos_flag){
                                    sos_flag = true;

                                    try{
                                        safety.exit_locaion();
                                    }catch(Exception e){

                                    }
                                    safety = null;


                                    sos_list = dbHelper.getAllSOS();
                                    deviceArrayList = dbHelper.getAllDeviceData();

                                    if(sos_list.size() != 0) {
                                        try {
                                            safety = MyLocation.getInstance();
                                            safety.setMac("emergency");
                                            safety.getLocation(getApplicationContext(), null);
                                        } catch (Exception e) {
                                            Log.d("MyService", "SafetyCertify Error:" + e);
                                        }
                                    }


                                    try{
                                        siren_sound.stop();
                                    }catch(Exception e){
                                        //Log.e("siren",":"+e);
                                    }

                                    try{
                                        siren_sound.release();
                                    }catch(Exception e){
                                        //Log.e("siren",":"+e);
                                    }

                                    siren_sound = MediaPlayer.create(BLEService.this, R.raw.siren_bfirst);

                                /*
                                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                current_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (trackers.get(i).getSos_phone_sound_volume() * 0.15), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                 */

                                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) 7, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                                    siren_sound.start();

                                    Message message = new Message();
                                    message.what = 16;
                                    handler.sendMessage(message);

                                    if(mapthread_flag){
                                        mapthread_count = 2;
                                    }else{
                                        try {
                                            mapthread = new mapThread();
                                            mapthread_count = 2;
                                            mapthread.start();
                                        }catch(Exception e){
                                            Log.d("mapthread",":"+e);
                                        }

                                    }
                                }


                            }else if(byteArrayToHexString(characteristic.getValue()).equals("CD")){

                                sos_flag = false;

                            }else if (byteArrayToHexString(characteristic.getValue()).equals("EE") || byteArrayToHexString(characteristic.getValue()).equals("FF")) {
                                Log.d("MyService", "ALARM AUTO OFF");
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(mBroadcastbeepingexit);
                                broadcastIntent.putExtra("str", "exit");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                            }else if (byteArrayToHexString(characteristic.getValue()).equals("14")) {


                            }else if(byteArrayToHexString(characteristic.getValue()).equals("00")){

                            }
                        }  else {

                        }
                    }else{
                        stopSelf();

                    }
                }
            };

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 1){

            }else if(msg.what == 2){

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(sos_list.size() != 0) {
                            /*
                            try {
                                MyLocation safety = MyLocation.getInstance();
                                safety.setMac("emergency");
                                safety.getLocation(getApplicationContext(), locationResult);
                            } catch (Exception e) {
                                Log.d("MyService", "SafetyCertify Error:" + e);
                            }

                             */


                            Location gps = null;
                            try{
                                gps = safety.getGps_location();
                            }catch(Exception e){
                                Log.e("what2 gps",":"+e);
                            }

                            Log.e("gps",":"+gps.getLatitude());
                            Log.e("gps",":"+gps.getLongitude());

                            safety.exit_locaion();

                            Message message = new Message();
                            message.what = 4;
                            handler.sendMessage(message);

                        }
                    }
                },0);

            }else if(msg.what == 3) {

            }else if(msg.what == 4){
                final String phone_number = sos_list.get(0).getPhone_number().replace(" ", "");
                deviceArrayList = dbHelper.getAllDeviceData();

                String send_message;

                if(deviceArrayList.get(0).getSms_text()!=null && !deviceArrayList.get(0).getSms_text().equals("")){
                    send_message = deviceArrayList.get(0).getSms_text();
                }else{
                    send_message = "위급상황입니다. 도와주세요.";
                }

                // 문자 보내는 상태를 감지하는 PendingIntent
                String SENT = "SMS_SENT";
                PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), FLAG_MUTABLE);
                final ArrayList<PendingIntent> sentPIs = new ArrayList<>();
                sentPIs.add(PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), FLAG_MUTABLE));

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> parts = smsManager.divideMessage(send_message);
                    smsManager.sendMultipartTextMessage(phone_number, null, parts, sentPIs, null);
                }catch(Exception e){
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone_number, null, send_message, null, null);
                }

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> parts = smsManager.divideMessage("ec2-52-78-78-27.ap-northeast-2.compute.amazonaws.com/map.php?key=EG6AV2ZA");
                    smsManager.sendMultipartTextMessage(phone_number, null, parts, sentPIs, null);
                }catch(Exception e){
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone_number, null, "ec2-52-78-78-27.ap-northeast-2.compute.amazonaws.com/map.php?key=EG6AV2ZA", null, null);
                }
            } else if(msg.what == 5){

            } else if(msg.what == 16){
                try {
                    m_WindowManager.removeView(m_View_sospopup);
                } catch (Exception e) {
                    Log.d("window", ":" + e);
                }


                m_View_sospopup = null;

                LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                m_View_sospopup = mInflater.inflate(R.layout.activity_popup_onebtn, null);
                //m_View.setOnTouchListener(onTouchListener);

                int layout_parms;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
                    //layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }

                if (!isScreenOn(getApplicationContext())) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                    wakeLock.acquire(3000);
                }

                WindowManager.LayoutParams m_Params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        layout_parms,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

                m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                m_WindowManager.addView(m_View_sospopup, m_Params);


                TextView popup_title = (TextView) m_View_sospopup.findViewById(R.id.popup_one_title);
                TextView popup_text = (TextView) m_View_sospopup.findViewById(R.id.popup_one_text1);
                Button popup_btn = (Button) m_View_sospopup.findViewById(R.id.popup_one_ok);

                popup_title.setText("위급상황 발생");
                popup_text.setText("위급상황 알림 SMS 메시지를 전송하였습니다.");


                popup_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //sos_siren_popup_flag = false;

                        try{
                            siren_sound.stop();
                            siren_sound.release();
                        }catch(Exception e){
                            Log.d("siren",":"+e);
                        }

                        try{
                            m_WindowManager.removeView(m_View_sospopup);
                        }catch(Exception e){

                        }
                    }
                });
            }
        }
    };

private class mapThread extends Thread {
    //mapthread_count = 180;

    public mapThread() {
        // 초기화 작업
        mapthread_flag = true;
    }

    public void playThreadstop(){
        mapthread_count = 30;
        mapthread_flag = false;
    }

    public void run() {
        // 스레드에게 수행시킬 동작들 구현
        while(mapthread_flag) {
            try {
                if (mapthread_count == 0) {

                    mapthread_flag = false;

                    Message message2 = new Message();
                    message2.what = 2;
                    handler.sendMessage(message2);

                    mapthread_count = 30;
                    mapthread_flag = false;
                    break;
                }
                Thread.sleep(1000);
                mapthread_count--;
            }catch(Exception e){

            }
        }
    }
}

    public static boolean isScreenOn(Context context) {
        return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }


    //bind부분
    Bind.Stub mBinder = new Bind.Stub() {
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                               double aDouble, String aString){

        }

        public boolean addmac(String _devicename, int trackerkind) {
            Log.e("service"," addmac");
            scandevice = new ArrayList<>();
            //if((macaddress!=null && macaddress.size() != CONNET_COUNT) || macaddress == null) {

            addmacflag = true;
            //scan_count=0;
            //kind_of_tracker = trackerkind;

            scandevice = new ArrayList<>();
            scandevicerssi = new ArrayList<>();

            ParcelUuid uuid = ParcelUuid.fromString(ccb0uuid.toString());
            ArrayList<ScanFilter> filterList = new ArrayList<ScanFilter>();
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(uuid).build();
            filterList.add(filter);
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

            mBluetoothAdapter.startLeScan(leScanCallback);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter.stopLeScan(leScanCallback);
                    }else {
                        mBluetoothAdapter.stopLeScan(leScanCallback);
                    }
                    if(scandevice.size()>0) {

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(mBroadcastSearchresult);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                    }else{
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(mBroadcastaddmactimeout);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
                    }
                }
            }, 10000);

            return true;
        }

        public void addmac_connect(){
            Log.e("service"," addmac_connect");
            add_flag = true;
            scandevice.get(maxnum).connectGatt(BLEService.this, false, mGattCallback);
        }

        public void add_cancel(){
            /*
            if(trackers.get(0).getTracker_macaddress().equals(scandevice.get(maxnum).getAddress())){
                //deletemac(trackers.get(0).getTrackermacaddress());
                deletemac(scandevice.get(maxnum).getAddress());
                add_flag = false;
            }

             */
            deletemac(scandevice.get(maxnum).getAddress());
            add_flag = false;
        }

        public void deletemac(String macaddress) {
            deleteflag = true;
            try {

                //dbHelper.device_delete(macaddress);
                //deviceArrayList = dbHelper.getAllDeviceData();

                for(int i=0; i<bleArrayList.size(); i++) {
                    if(bleArrayList.get(i).getMac().equals(macaddress)) {
                        if (bleArrayList.get(i).getBluetoothGatt() != null) {

                            try{
                                bleArrayList.get(i).getBluetoothGatt().close();
                                bleArrayList.remove(i);
                            }catch(Exception e){

                            }
                        }else{
                            bleArrayList.remove(i);
                        }
                        break;
                    }
                }

            }catch(Exception e){
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(mBroadcastDelete);
                broadcastIntent.putExtra("flag", false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            }

            deleteflag = false;

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(mBroadcastDelete);
            broadcastIntent.putExtra("flag", true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

            //sendBroadcast(broadcastIntent);

        }
    };
}