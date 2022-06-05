package com.kjh.ble.ui;

import static com.kjh.ble.ui.MainActivity.mainContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kjh.ble.R;
import com.kjh.ble.service.ServiceManage;

public class Registration extends AppCompatActivity {
    ServiceManage serviceManage;

    progressThread pThread;
    boolean thread_status = false;


    private ImageView scan_dot_img1;
    private ImageView scan_dot_img2;
    private ImageView scan_dot_img3;
    private ImageView scan_dot_img4;
    private ImageView scan_dot_img5;
    private ImageView scan_dot_img6;
    private ImageView scan_dot_img7;
    private ImageView scan_dot_img8;
    private ImageView scan_dot_img9;
    private ImageView scan_dot_img10;

    private IntentFilter mIntentFilter;
    public static final String mBroadcastSearchresult = "mBroadcastSearchresult";
    public static final String mBroadcastaddmactimeout = "mBroadcastaddmactimeout";

    private boolean scan_callback_flag = false;

    static Context registration_context;

    ProgressBar scan_progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registration_context = this;

        scan_dot_img1 = (ImageView)findViewById(R.id.scan_dot1);
        scan_dot_img2 = (ImageView)findViewById(R.id.scan_dot2);
        scan_dot_img3 = (ImageView)findViewById(R.id.scan_dot3);
        scan_dot_img4 = (ImageView)findViewById(R.id.scan_dot4);
        scan_dot_img5 = (ImageView)findViewById(R.id.scan_dot5);
        scan_dot_img6 = (ImageView)findViewById(R.id.scan_dot6);
        scan_dot_img7 = (ImageView)findViewById(R.id.scan_dot7);
        scan_dot_img8 = (ImageView)findViewById(R.id.scan_dot8);
        scan_dot_img9 = (ImageView)findViewById(R.id.scan_dot9);
        scan_dot_img10 = (ImageView)findViewById(R.id.scan_dot10);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastSearchresult);
        mIntentFilter.addAction(mBroadcastaddmactimeout);
        LocalBroadcastManager.getInstance(mainContext).registerReceiver(mReceiver, mIntentFilter);


        findViewById(R.id.registration_scan_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.registration_scan_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    serviceManage.add_tracker("test", 0);
                } catch (Exception e) {

                }

                pThread = new progressThread();
                pThread.start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        serviceManage = new ServiceManage(Registration.this);

    }

    public void scan_success(){
        Intent intent = new Intent(getApplicationContext(), Registration_connect.class);
        intent.putExtra("device_kind", 0);
        intent.putExtra("device_nickname", "test");
        startActivity(intent);
        finish();

        try{
            serviceManage.unbind(registration_context);
        }catch(Exception e){

        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
    }

    public void scan_fail(){
        scan_progressbar.setProgress(0);
        thread_status = false;
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //연결상태가 변할때 broadcast receive(연결, 연결끊김시)
            if(intent.getAction().equals(mBroadcastSearchresult)) {
                if(!scan_callback_flag) {
                    scan_callback_flag = true;

                    Bundle bun = new Bundle();
                    bun.putString("Message", "registration_scan_success");

                    Intent popupIntent = new Intent(getApplicationContext(), PopupActivity_onebtn.class);

                    popupIntent.putExtras(bun);
                    PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_MUTABLE);
                    try {
                        pie.send();
                    } catch (PendingIntent.CanceledException ee) {
                        //LogUtil.degug(e.getMessage());
                    }
                    try {
                        pThread.progressThreadstop();
                    } catch (Exception e) {

                    }
                }
            }

            if(intent.getAction().equals(mBroadcastaddmactimeout)){
                Bundle bun = new Bundle();
                bun.putString("Message", "registration_scan_fail");

                Intent popupIntent = new Intent(getApplicationContext(), PopupActivity_onebtn.class);

                popupIntent.putExtras(bun);
                PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_MUTABLE);
                try {
                    pie.send();
                } catch (PendingIntent.CanceledException ee) {
                    //LogUtil.degug(e.getMessage());
                }
                try {
                    pThread.progressThreadstop();
                }catch(Exception e){

                }

            }

        }
    };

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            if(message.what == 1){
                switch(message.arg1){

                }
            }else if(message.what==2) {
                switch (message.arg1) {
                    case 0:
                        scan_dot_img1.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 1:
                        scan_dot_img2.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 2:
                        scan_dot_img3.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 3:
                        scan_dot_img4.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 4:
                        scan_dot_img5.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 5:
                        scan_dot_img6.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 6:
                        scan_dot_img7.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 7:
                        scan_dot_img8.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 8:
                        scan_dot_img9.setImageResource(R.drawable.scan_dot_on);
                        break;
                    case 9:
                        scan_dot_img10.setImageResource(R.drawable.scan_dot_on);
                        break;
                }
            }else if(message.what == 3){
                scan_dot_img1.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img2.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img3.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img4.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img5.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img6.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img7.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img8.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img9.setImageResource(R.drawable.scan_dot_off);
                scan_dot_img10.setImageResource(R.drawable.scan_dot_off);
            } else if(message.what == 10){
                /*
                try {
                    ((ViewManager) loading_layout.getParent()).removeView(loading_layout);
                }catch(Exception e){
                    //Log.e("loading_stop"," error:"+e);
                }

                try {
                    loading_btn_flag = false;
                }catch(Exception e){
                    //Log.e("loading_stop"," error:"+e);
                }

                 */
            }

        }
    };

    private class progressThread extends Thread {
        //private static final String TAG = "ExampleThread";
        private int i = 0;

        public progressThread() {
            // 초기화 작업
            thread_status = true;

        }
        public void progressThreadstop(){
            thread_status = false;
            Message message = new Message();
            message.what = 3;
            handler.sendMessage(message);
        }
        public void run() {
            // 스레드에게 수행시킬 동작들 구현
            while(thread_status) {
                try {
                    /*
                    if (i == 11){
                        i = 0;
                        loading_thread.loadingThread_stop();
                        break;
                    }

                     */

                    Message message = new Message();
                    message.what = 2;
                    message.arg1 = i;
                    handler.sendMessage(message);

                    Thread.sleep(1000);
                    i++;
                } catch (Exception e) {

                }
            }
        }
    }
}