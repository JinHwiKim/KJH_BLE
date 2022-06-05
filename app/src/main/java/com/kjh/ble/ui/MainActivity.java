package com.kjh.ble.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.kjh.ble.R;
import com.kjh.ble.service.ServiceManage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BluetoothManager bluetoothManager; //블루투스매니저 선언(API 21이상에서 사용)
    BluetoothAdapter mBluetoothAdapter; //블루투스어댑터 선언

    private int MY_PERMISSION_REQUEST_STORAGE=1;

    private SeekBar seek;
    private TextView start_btn_text;
    int originalProgress;

    ServiceManage servicemanage;

    DBHelper dbHelper;
    ArrayList<Device> deviceArrayList = new ArrayList<>();

    TextView null_text;
    LinearLayout setting_layout;
    Button registration_btn;

    static Context mainContext;

    Switch phone_sound;
    Switch ble_sound;

    TextView status_text;

    private String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private List permissionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActivityCompat.requestPermissions(this,permissions,MY_PERMISSION_REQUEST_STORAGE);
        checkPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }else{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {

                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableIntent);

                } else if (mBluetoothAdapter == null) {

                }
            }else {
                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();

                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {

                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableIntent);

                } else if (mBluetoothAdapter == null) {

                }
            }
        }

        mainContext = this;

        null_text = (TextView)findViewById(R.id.main_null_textview);
        setting_layout = (LinearLayout) findViewById(R.id.device_setting_layout);
        registration_btn = (Button)findViewById(R.id.main_registration_btn);

        phone_sound = (Switch)findViewById(R.id.sos_android_sound_switch);
        ble_sound = (Switch)findViewById(R.id.sos_device_sound_switch);
        status_text = (TextView)findViewById(R.id.status_text);

        registration_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deviceArrayList.size()!=0){
                    Log.e("deviceArrayList,",":"+deviceArrayList.get(0).getMac());
                    servicemanage.Trackers_delete(deviceArrayList.get(0).getMac());

                    dbHelper.device_delete(deviceArrayList.get(0).getMac());

                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }else{
                    startActivity(new Intent(MainActivity.this, Registration.class));
                }
            }
        });

        findViewById(R.id.sos_call_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SOS_Friend.class).putExtra("flag","activity"));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        servicemanage = new ServiceManage(MainActivity.this);

        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        deviceArrayList = dbHelper.getAllDeviceData();

        if(deviceArrayList.size()!=0){
            if(deviceArrayList.get(0).getAlarm_from_phone()==1){
                phone_sound.setChecked(true);
            }

            if(deviceArrayList.get(0).getAlarm_from_tracker()==1){
                ble_sound.setChecked(true);
            }

            status_text.setText("연결됨");
            null_text.setVisibility(View.GONE);
            registration_btn.setText("BLE Device 삭제");
        }else{
            status_text.setText("등록되지 않음");
            setting_layout.setVisibility(View.GONE);
        }

    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 1){
                deviceArrayList = dbHelper.getAllDeviceData();


                if(deviceArrayList.size()!=0){
                    null_text.setVisibility(View.GONE);
                    setting_layout.setVisibility(View.VISIBLE);
                    registration_btn.setText("BLE Device 삭제");
                }else{
                    null_text.setVisibility(View.VISIBLE);
                    setting_layout.setVisibility(View.GONE);
                    registration_btn.setText("BLE Device 등록");
                }
            }else if(msg.what == 2){

            }else if(msg.what == 3) {

            }else if(msg.what == 4){

            } else if(msg.what == 5){

            }
        }
    };

    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<>();

        for(String pm : permissions){
            result = ContextCompat.checkSelfPermission(getApplicationContext(), pm);
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(pm);
            }
        }
        if(!permissionList.isEmpty()){
            return false;
        }
        return true;
    }

    //배열로 선언한 권한에 대해 사용자에게 허용 요청
    public void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, (String[]) permissionList.toArray(new String[permissionList.size()]), MY_PERMISSION_REQUEST_STORAGE);
    }

    //요청한 권한에 대한 결과값 판단 및 처리
    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //우선 requestCode가 아까 위에 final로 선언하였던 숫자와 맞는지, 결과값의 길이가 0보다는 큰지 먼저 체크
        if(requestCode == MY_PERMISSION_REQUEST_STORAGE && (grantResults.length >0)) {
            for(int i=0; i< grantResults.length; i++){
                //grantResults 가 0이면 사용자가 허용한 것 / -1이면 거부한 것
                //-1이 있는지 체크하여 하나라도 -1이 나온다면 false를 리턴
                if(grantResults[i] == -1){
                    return false;
                }
            }
        }
        return true;
    }
}