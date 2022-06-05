package com.kjh.ble.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.kjh.ble.R;
import com.kjh.ble.service.ServiceManage;

import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Registration_connect extends AppCompatActivity {
    private DBHelper dbHelper;
    private ArrayList<Device> device_array = new ArrayList<>();
    private String unique_id = "";
    private String social_login = "";
    private String encode_unique_id = "";
    private String version="";

    private IntentFilter mIntentFilter;
    public static final String mBroadcastaddtracker = "mBroadcastaddtracker";
    public static final String mBroadcastcharacter = "mBroadcastcharacter";
    public static final String mBroadcasta1 = "mBroadcasta1";
    public static final String mBroadcastf0 = "mBroadcastf0";
    public static final String mBroadcast01 = "mBroadcast01";
    public static final String mBroadcastBind = "mBroadcastBind";

    ServiceManage serviceManage;

    static Context registration_connect_context;

    private timeoutThread mreconnectThread;
    private int timeout_count = 0;
    private boolean thread_flag = false;

    int device_kind;
    String device_nickname = "";

    String new_macaddress="";
    String new_macname="";
    String new_trackername="";
    int new_kind;

    private ImageView advertising_img;
    private animThread animThread;

    int permissionResult;

    Bitmap bitmap1;
    Bitmap bitmap2;

    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    private String firmware="";

    private boolean connect_flag = false;
    private boolean character_flag = false;
    private boolean x01_flag = false;
    private boolean xa1_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FileInputStream fis = openFileInput(FILE_LANGUAGE);

            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
            String str;
            str = bufferReader.readLine();
            if(str!=null)
                languageToLoad = str;

            fis.close();
        }catch (Exception e) {

        }

        if(languageToLoad.equals("") || !languageToLoad.equals("en")){
            languageToLoad = "es";
        }

        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());


        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        setContentView(R.layout.activity_registration_connect);

        registration_connect_context = this;

        Intent getintent = getIntent();
        device_kind = getintent.getIntExtra("device_kind",0);
        device_nickname = getintent.getStringExtra("device_nickname");

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 314, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 344, getResources().getDisplayMetrics());


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 안드로이드 버전 체크. 마시멜로우 이상 true, 아니면 false
            permissionResult = ContextCompat.checkSelfPermission(Registration_connect.this, android.Manifest.permission.RECORD_AUDIO);
        } else {
            permissionResult = 0;
        }


        TextView registration_connect_text = (TextView)findViewById(R.id.registration_connect_text);
        TextView registration_connect_bottom_text = (TextView)findViewById(R.id.registration_connect_bottom_text);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastaddtracker);
        mIntentFilter.addAction(mBroadcastcharacter);
        mIntentFilter.addAction(mBroadcasta1);
        mIntentFilter.addAction(mBroadcastf0);
        mIntentFilter.addAction(mBroadcast01);
        mIntentFilter.addAction(mBroadcastBind);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, mIntentFilter);

        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();


        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        version = pi.versionName;

        serviceManage = new ServiceManage(getApplicationContext());


        if(thread_flag == false) {
            mreconnectThread = new timeoutThread(true, 0);
            mreconnectThread.start();
        }

        animThread = new animThread(true);
        animThread.start();

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(mBroadcastaddtracker)) {

                //boolean addflag = intent.getBooleanExtra("success_flag",false);
                new_macaddress = intent.getStringExtra("new_macaddress");
                new_macname = intent.getStringExtra("new_macname");
                new_trackername = intent.getStringExtra("new_trackername");
                new_kind = intent.getIntExtra("new_kind",0);

                connect_flag = true;
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }else if(intent.getAction().equals(mBroadcastcharacter)) {
                boolean characteristic = intent.getBooleanExtra("characteristic",false);
                if(characteristic && connect_flag){
                    character_flag = true;
                    Message message = new Message();
                    message.what = 3;
                    handler.sendMessage(message);
                }

            }else if(intent.getAction().equals(mBroadcast01)) {
                boolean oxo1 = intent.getBooleanExtra("01",false);
                if(oxo1 && character_flag){
                    x01_flag = true;
                    Message message = new Message();
                    message.what = 4;
                    handler.sendMessage(message);
                }

            }else if(intent.getAction().equals(mBroadcasta1)) {
                boolean oxa1 = intent.getBooleanExtra("a1",false);
                if(oxa1 && x01_flag){
                    xa1_flag = true;
                    Message message = new Message();
                    message.what = 5;
                    handler.sendMessage(message);
                }

            }else if(intent.getAction().equals(mBroadcastBind)){
                try{
                    serviceManage.tracker_registration();
                }catch(Exception e){

                }
            }

        }
    };
    class animThread extends Thread {
        private boolean isPlay = false;
        private int count = 0;


        public animThread(Boolean isPlay){
            this.isPlay = isPlay;
            Message message = new Message();
            message.what = 10;
            message.arg1 = 0;
            handler.sendMessage(message);
        }

        public void stopThread(){
            isPlay = false;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    if(count==8){
                        Message message = new Message();
                        message.what = 10;
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }else if(count == 15){
                        Message message = new Message();
                        message.what = 10;
                        message.arg1 = 0;
                        handler.sendMessage(message);

                        count = 0;
                    }
                    Thread.sleep(1000);
                    count++;
                    //asyncDialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class timeoutThread extends Thread {
        private boolean isPlay = false;
        //private int count = 0;


        public timeoutThread(Boolean isPlay, int count){
            this.isPlay = isPlay;
            timeout_count = count;
        }

        public void stopThread(){
            isPlay = false;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    if(timeout_count==150){
                        timeout_count=0;

                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);

                        break;
                    }
                    Thread.sleep(100);
                    timeout_count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 1){

                character_flag = false;
                character_flag = false;
                x01_flag = false;
                xa1_flag = false;

                try {
                    mreconnectThread.stopThread();
                    animThread.stopThread();
                }catch(Exception e){

                }

                try{
                    serviceManage.add_cancel();
                }catch(Exception e){

                }

                try{
                    serviceManage.unbind(getApplicationContext());
                }catch(Exception ee){

                }

                try{
                    bitmap1.recycle();
                    bitmap2.recycle();
                }catch(Exception e){

                }

                try {
                    startActivity(new Intent(getApplicationContext(), Registration_fail.class).putExtra("device_kind",device_kind).putExtra("error","timeout").putExtra("device_name", device_nickname));
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
                    finish();
                }catch(Exception e){

                }
            }else if(msg.what == 2){
                TextView per_text = (TextView)findViewById(R.id.registration_connect_text);
                per_text.setText("20%");
                ImageView per_img = (ImageView)findViewById(R.id.registration_connect_percent);
                per_img.setImageResource(R.drawable.regi_20);
                timeout_count = 0;
            }else if(msg.what == 3){
                TextView per_text = (TextView)findViewById(R.id.registration_connect_text);
                per_text.setText("40%");
                ImageView per_img = (ImageView)findViewById(R.id.registration_connect_percent);
                per_img.setImageResource(R.drawable.regi_40);
                timeout_count = 0;
            }else if(msg.what == 4){
                TextView per_text = (TextView)findViewById(R.id.registration_connect_text);
                per_text.setText("60%");
                ImageView per_img = (ImageView)findViewById(R.id.registration_connect_percent);
                per_img.setImageResource(R.drawable.regi_60);
                timeout_count = 0;
            }else if(msg.what == 5){
                TextView per_text = (TextView)findViewById(R.id.registration_connect_text);
                per_text.setText("80%");
                ImageView per_img = (ImageView)findViewById(R.id.registration_connect_percent);
                per_img.setImageResource(R.drawable.regi_80);
                timeout_count = 0;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //new_trackername = new_trackername.replace("''","'");
                        device_array.add(new Device(new_macaddress, 1, 1, "위급상황입니다. 도와주세요."));
                        dbHelper.addDevice(device_array.get(device_array.size()-1));

                        try {
                            mreconnectThread.stopThread();
                            animThread.stopThread();
                        }catch(Exception e){

                        }

                        try{
                            serviceManage.unbind(getApplicationContext());
                        }catch(Exception ee){

                        }

                        try{
                            bitmap1.recycle();
                            bitmap2.recycle();
                        }catch(Exception e){

                        }

                        try {
                            startActivity(new Intent(getApplicationContext(), Registration_success.class));
                            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
                            finish();
                        }catch(Exception e){

                        }

                    }
                },500);

            }else if(msg.what == 6){
                TextView per_text = (TextView)findViewById(R.id.registration_connect_text);
                per_text.setText("100%");
                ImageView per_img = (ImageView)findViewById(R.id.registration_connect_percent);
                per_img.setImageResource(R.drawable.regi_100);
                timeout_count = 0;

                try{
                    mreconnectThread.stopThread();
                    animThread.stopThread();
                }catch(Exception e){

                }


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //new_trackername = new_trackername.replace("''","'");
                        device_array.add(new Device(new_macaddress, 1, 1, "위급상황입니다. 도와주세요."));

                        try{
                            serviceManage.unbind(getApplicationContext());
                        }catch(Exception ee){

                        }

                        try{
                            bitmap1.recycle();
                            bitmap2.recycle();
                        }catch(Exception e){

                        }

                        try {
                            startActivity(new Intent(getApplicationContext(), Registration_success.class));
                            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
                            finish();
                        }catch(Exception e){

                        }

                    }
                },500);

            }else if(msg.what == 10){
            }
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    //홈키 처리
    @Override public void onPause() {
        if (isApplicationSentToBackground(this)){
            // Do what you want to do on detecting Home Key being Pressed
            finish();
            try{
                ((MainActivity)MainActivity.mainContext).finish();
            }catch(Exception e){

            }
        }
        super.onPause();

    }

    //홈키 처리
    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }

        try {
            serviceManage.unbind(getApplicationContext());
        } catch (Exception e) {

        }
    }
}