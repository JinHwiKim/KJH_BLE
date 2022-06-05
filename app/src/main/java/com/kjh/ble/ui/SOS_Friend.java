package com.kjh.ble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kjh.ble.R;
import com.kjh.ble.service.ServiceManage;

import org.apache.http.message.BasicNameValuePair;

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
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SOS_Friend extends AppCompatActivity {
    static Context sos_friend_context;
    private DBHelper dbHelper;
    private ArrayList<Device> device_array = new ArrayList<>();
    private ArrayList<SOS> sos_array = new ArrayList<>();

    private Button edit_btn;
    private Button edit_success_btn;

    private RelativeLayout nomal_list1;
    private ImageView nomal_list1_img;
    private TextView nomal_list1_text;
    private Button nomal_list1_edit_btn;
    private Button nomal_list1_delete_btn;

    private RelativeLayout nomal_list2;
    private ImageView nomal_list2_img;
    private TextView nomal_list2_text;
    private Button nomal_list2_edit_btn;
    private Button nomal_list2_delete_btn;

    private RelativeLayout nomal_list3;
    private ImageView nomal_list3_img;
    private TextView nomal_list3_text;
    private Button nomal_list3_edit_btn;
    private Button nomal_list3_delete_btn;

    private Button sms_edit_btn;
    private TextView sms_text_box;

    private LinearLayout exist_layout;
    private LinearLayout null_layout;

    ServiceManage serviceManage;

    private String encode_unique_id;
    private String encode_phone_number;

    private int delete_position;
    private String flag = "";

    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    private boolean loading_thread_flag = false;
    private boolean loading_btn_flag = false;
    RelativeLayout loading_layout;

    Window win;

    String sdPath;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getWindow();

        win.setContentView(R.layout.activity_sos_friend);


        sos_friend_context = this;

        Intent getintent = getIntent();
        flag = getintent.getStringExtra("flag");

        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        sos_array = dbHelper.getAllSOS();
        device_array = dbHelper.getAllDeviceData();

        edit_btn = (Button)findViewById(R.id.sos_friend_edit_btn);
        edit_success_btn = (Button)findViewById(R.id.sos_friend_edit_success_btn);

        nomal_list1 = (RelativeLayout)findViewById(R.id.nomal_list1);
        nomal_list1_img = (ImageView)findViewById(R.id.nomal_list1_img);
        nomal_list1_text = (TextView)findViewById(R.id.nomal_list1_text);
        nomal_list1_edit_btn = (Button)findViewById(R.id.nomal_list1_edit_btn);
        nomal_list1_delete_btn = (Button)findViewById(R.id.nomal_list1_delete_btn);

        nomal_list2 = (RelativeLayout)findViewById(R.id.nomal_list2);
        nomal_list2_img = (ImageView)findViewById(R.id.nomal_list2_img);
        nomal_list2_text = (TextView)findViewById(R.id.nomal_list2_text);
        nomal_list2_edit_btn = (Button)findViewById(R.id.nomal_list2_edit_btn);
        nomal_list2_delete_btn = (Button)findViewById(R.id.nomal_list2_delete_btn);

        nomal_list3 = (RelativeLayout)findViewById(R.id.nomal_list3);
        nomal_list3_img = (ImageView)findViewById(R.id.nomal_list3_img);
        nomal_list3_text = (TextView)findViewById(R.id.nomal_list3_text);
        nomal_list3_edit_btn = (Button)findViewById(R.id.nomal_list3_edit_btn);
        nomal_list3_delete_btn = (Button)findViewById(R.id.nomal_list3_delete_btn);

        sms_edit_btn = (Button)findViewById(R.id.sos_friend_sms_edit_btn);
        sms_text_box = (TextView)findViewById(R.id.sos_friend_sms_text_box);



        TextView title_text = (TextView)findViewById(R.id.sos_friend_title_text);
        TextView message_content_title = (TextView)findViewById(R.id.sos_friend_message_content_title);
        TextView sos_friend_empty_text = (TextView)findViewById(R.id.sos_friend_empty_text);
        Button btn1 = (Button)findViewById(R.id.sos_friend_registration_btn1);
        Button btn2 = (Button)findViewById(R.id.sos_friend_registration_btn2);



        String sms_text_string;

        if(device_array.get(0).getSms_text()==null || device_array.get(0).getSms_text().equals("")){
            sms_text_string = "위급상황입니다. 도와주세요.";
        }else{
            sms_text_string = device_array.get(0).getSms_text();
        }

        sms_text_box.setText(sms_text_string);

        exist_layout = (LinearLayout)findViewById(R.id.sos_friend_exist_layout);
        null_layout = (LinearLayout)findViewById(R.id.sos_friend_null_layout);

        //smartTracker = new SmartTracker(getApplicationContext());

        serviceManage = new ServiceManage(getApplicationContext());

        if(sos_array.size()==0){
            edit_btn.setVisibility(View.GONE);
            edit_success_btn.setVisibility(View.GONE);
            exist_layout.setVisibility(View.GONE);
        }else{
            null_layout.setVisibility(View.GONE);
            edit_success_btn.setVisibility(View.GONE);
            sms_edit_btn.setVisibility(View.GONE);

            if(sos_array.size()==1){
                nomal_list2.setVisibility(View.GONE);
                nomal_list3.setVisibility(View.GONE);

                nomal_list1_edit_btn.setVisibility(View.GONE);
                nomal_list1_delete_btn.setVisibility(View.GONE);


                nomal_list1_text.setText(sos_array.get(0).getName());

            }else if(sos_array.size()==2){
                nomal_list3.setVisibility(View.GONE);

                nomal_list1_edit_btn.setVisibility(View.GONE);
                nomal_list1_delete_btn.setVisibility(View.GONE);
                nomal_list2_edit_btn.setVisibility(View.GONE);
                nomal_list2_delete_btn.setVisibility(View.GONE);


                nomal_list1_text.setText(sos_array.get(0).getName());
                nomal_list2_text.setText(sos_array.get(1).getName());

            }else if(sos_array.size()==3){
                nomal_list1_edit_btn.setVisibility(View.GONE);
                nomal_list1_delete_btn.setVisibility(View.GONE);
                nomal_list2_edit_btn.setVisibility(View.GONE);
                nomal_list2_delete_btn.setVisibility(View.GONE);
                nomal_list3_edit_btn.setVisibility(View.GONE);
                nomal_list3_delete_btn.setVisibility(View.GONE);


                nomal_list1_text.setText(sos_array.get(0).getName());
                nomal_list2_text.setText(sos_array.get(1).getName());
                nomal_list3_text.setText(sos_array.get(2).getName());

            }
        }


        findViewById(R.id.sos_friend_edit_btn).setOnClickListener(edit_btn_click);
        findViewById(R.id.sos_friend_edit_success_btn).setOnClickListener(edit_success_btn_click);
        findViewById(R.id.sos_friend_registration_btn1).setOnClickListener(registration_btn);
        findViewById(R.id.sos_friend_registration_btn2).setOnClickListener(registration_btn);

        findViewById(R.id.nomal_list1_edit_btn).setOnClickListener(list1_edit_btn_click);
        findViewById(R.id.nomal_list2_edit_btn).setOnClickListener(list2_edit_btn_click);
        findViewById(R.id.nomal_list3_edit_btn).setOnClickListener(list3_edit_btn_click);

        findViewById(R.id.nomal_list1_delete_btn).setOnClickListener(list1_delete_btn_click);
        findViewById(R.id.nomal_list2_delete_btn).setOnClickListener(list2_delete_btn_click);
        findViewById(R.id.nomal_list3_delete_btn).setOnClickListener(list3_delete_btn_click);

        findViewById(R.id.sos_friend_sms_edit_btn).setOnClickListener(sms_edit_btn_click);

        findViewById(R.id.sos_friend_back_btn).setOnClickListener(back_btn_click);
    }

    Button.OnClickListener back_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(flag.equals("popup")){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("sos_registration",true);
                startActivity(intent);
                finish();
            }else if(flag.equals("activity")){
                finish();
            }
        }
    };

    Button.OnClickListener edit_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            edit_btn.setVisibility(View.GONE);
            edit_success_btn.setVisibility(View.VISIBLE);
            sms_edit_btn.setVisibility(View.VISIBLE);


            nomal_list1_edit_btn.setVisibility(View.VISIBLE);
            nomal_list2_edit_btn.setVisibility(View.VISIBLE);
            nomal_list3_edit_btn.setVisibility(View.VISIBLE);
            nomal_list1_delete_btn.setVisibility(View.VISIBLE);
            nomal_list2_delete_btn.setVisibility(View.VISIBLE);
            nomal_list3_delete_btn.setVisibility(View.VISIBLE);
        }
    };

    Button.OnClickListener edit_success_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            edit_success_btn.setVisibility(View.GONE);
            edit_btn.setVisibility(View.VISIBLE);
            sms_edit_btn.setVisibility(View.GONE);

            nomal_list1_edit_btn.setVisibility(View.GONE);
            nomal_list2_edit_btn.setVisibility(View.GONE);
            nomal_list3_edit_btn.setVisibility(View.GONE);
            nomal_list1_delete_btn.setVisibility(View.GONE);
            nomal_list2_delete_btn.setVisibility(View.GONE);
            nomal_list3_delete_btn.setVisibility(View.GONE);

        }
    };

    Button.OnClickListener list1_edit_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), SOS_Friend_Registration.class);
            intent.putExtra("position", 0);
            intent.putExtra("kind", "edit");
            intent.putExtra("flag", "activity");
            startActivity(intent);
        }
    };
    Button.OnClickListener list2_edit_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), SOS_Friend_Registration.class);
            intent.putExtra("position", 1);
            intent.putExtra("kind", "edit");
            intent.putExtra("flag", "activity");
            startActivity(intent);
        }
    };
    Button.OnClickListener list3_edit_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), SOS_Friend_Registration.class);
            intent.putExtra("position", 2);
            intent.putExtra("kind", "edit");
            intent.putExtra("flag", "activity");
            startActivity(intent);
        }
    };

    Button.OnClickListener list1_delete_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    Button.OnClickListener list2_delete_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    Button.OnClickListener list3_delete_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    Button.OnClickListener sms_edit_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), SOS_Friend_sms.class);
            intent.putExtra("flag",flag);
            intent.putExtra("kind","edit");
            startActivity(intent);
        }
    };

    Button.OnClickListener registration_btn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(sos_array.size() >= 3){
                Bundle bun = new Bundle();
                bun.putString("Message", "sos_friend_registration_count");

                Intent popupIntent = new Intent(getApplicationContext(), PopupActivity_onebtn.class);

                popupIntent.putExtras(bun);
                PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                try {
                    pie.send();
                } catch (PendingIntent.CanceledException ee) {
                    //LogUtil.degug(e.getMessage());
                }
            }else {
                Intent intent = new Intent(getApplicationContext(), SOS_Friend_Registration.class);
                intent.putExtra("position", -1);
                intent.putExtra("kind", "registration");
                intent.putExtra("flag", "activity");
                startActivity(intent);
            }
        }
    };



    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            if(message.what == 1) {
                //device_array = new ArrayList<>();
                sos_array = dbHelper.getAllSOS();

                edit_btn.setVisibility(View.VISIBLE);
                edit_success_btn.setVisibility(View.VISIBLE);
                exist_layout.setVisibility(View.VISIBLE);

                null_layout.setVisibility(View.VISIBLE);
                edit_success_btn.setVisibility(View.VISIBLE);
                sms_edit_btn.setVisibility(View.VISIBLE);

                nomal_list1.setVisibility(View.VISIBLE);
                nomal_list2.setVisibility(View.VISIBLE);
                nomal_list3.setVisibility(View.VISIBLE);

                if(sos_array.size()==0){

                    edit_btn.setVisibility(View.GONE);
                    edit_success_btn.setVisibility(View.GONE);
                    exist_layout.setVisibility(View.GONE);
                }else{
                    null_layout.setVisibility(View.GONE);
                    edit_success_btn.setVisibility(View.GONE);
                    sms_edit_btn.setVisibility(View.GONE);

                    if(sos_array.size()==1){
                        nomal_list2.setVisibility(View.GONE);
                        nomal_list3.setVisibility(View.GONE);

                        nomal_list1_edit_btn.setVisibility(View.GONE);
                        nomal_list1_delete_btn.setVisibility(View.GONE);


                        nomal_list1_text.setText(sos_array.get(0).getName());
                    }else if(sos_array.size()==2){
                        nomal_list3.setVisibility(View.GONE);

                        nomal_list1_edit_btn.setVisibility(View.GONE);
                        nomal_list1_delete_btn.setVisibility(View.GONE);
                        nomal_list2_edit_btn.setVisibility(View.GONE);
                        nomal_list2_delete_btn.setVisibility(View.GONE);


                        nomal_list1_text.setText(sos_array.get(0).getName());
                        nomal_list2_text.setText(sos_array.get(1).getName());
                    }else if(sos_array.size()==3){
                        nomal_list1_edit_btn.setVisibility(View.GONE);
                        nomal_list1_delete_btn.setVisibility(View.GONE);
                        nomal_list2_edit_btn.setVisibility(View.GONE);
                        nomal_list2_delete_btn.setVisibility(View.GONE);
                        nomal_list3_edit_btn.setVisibility(View.GONE);
                        nomal_list3_delete_btn.setVisibility(View.GONE);

                        nomal_list1_text.setText(sos_array.get(0).getName());
                        nomal_list2_text.setText(sos_array.get(1).getName());
                        nomal_list3_text.setText(sos_array.get(2).getName());
                    }
                }

            }else if(message.what == 15){
                try {
                    ((ViewManager) loading_layout.getParent()).removeView(loading_layout);
                }catch(Exception e){
                    //Log.e("loading_stop"," error:"+e);
                }

                try {
                    loading_btn_flag = false;
                    //button_flag = false;
                }catch(Exception e){
                    //Log.e("loading_stop"," error:"+e);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    @Override
    protected  void onDestroy(){
        try {
            serviceManage.unbind(getApplicationContext());
        }catch(Exception e){

        }

        super.onDestroy();
    }
}