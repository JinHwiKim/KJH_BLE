package com.kjh.ble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SOS_Friend_sms extends AppCompatActivity {
    static Context sos_friend_sms_context;
    private DBHelper dbHelper;
    private ArrayList<Device> device_array;

    private EditText sms_edittext;

    ServiceManage serviceManage;

    private String flag = "";
    private String kind = "";

    private TextView title_text;

    private Button confirm_btn;

    private String encode_unique_id;

    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    String sms_text_string = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sos_friend_sms);

        sos_friend_sms_context = this;

        Intent getintent = getIntent();
        flag = getintent.getStringExtra("flag");
        kind = getintent.getStringExtra("kind");


        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        device_array = dbHelper.getAllDeviceData();

        title_text = (TextView)findViewById(R.id.sos_friend_sms_title_text);
        sms_edittext = (EditText)findViewById(R.id.sos_friend_sms_edittext);
        confirm_btn = (Button)findViewById(R.id.sos_friend_sms_confirm_btn);

        TextView sos_friend_sms_message_content_title = (TextView)findViewById(R.id.sos_friend_sms_message_content_title);


        //sms_edittext.setFilters(new InputFilter[]{specialCharacterFilter});


        //sms_text_string;


        sms_edittext.requestFocus();

        confirm_btn.setOnClickListener(confirm_btn_click);

        if(sms_edittext.getText().toString().length() > 0){
            confirm_btn.setEnabled(true);
        }else{
            confirm_btn.setEnabled(false);
        }

        serviceManage = new ServiceManage(getApplicationContext());


        sms_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i) {
                    case EditorInfo.IME_ACTION_DONE:
                        confirm_function();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        sms_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sms_edittext.isFocusable()) {
                    if(sms_edittext.getText().length() > 0){
                        confirm_btn.setEnabled(true);
                    }else{
                        confirm_btn.setEnabled(false);
                    }

                    try {
                        byte[] bytetext = sms_edittext.getText().toString().getBytes("KSC5601");
                        //tv.setText(String.valueOf(bytetext.length) + " / 80 바이트");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after_text = s.toString();
                try {
                    byte[] getbyte = after_text.getBytes("KSC5601");
                    if (getbyte.length > 90) {
                        s.delete(s.length()-2, s.length()-1);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.sos_friend_sms_back_btn).setOnClickListener(back_btn_click);
        //findViewById(R.id.sos_friend_sms_confirm_btn).setOnClickListener(confirm_btn_click);

    }

    Button.OnClickListener back_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish_function();
        }
    };

    public void finish_function(){
        if(flag.equals("popup")){
            Intent intent = new Intent(getApplicationContext(), SOS_Friend.class);
            intent.putExtra("flag","popup");
            startActivity(intent);
            finish();
        }else if(flag.equals("activity")){
            finish();
        }
    }

    Button.OnClickListener confirm_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //sms_edittext.

            dbHelper.device_setting(device_array.get(0).getMac(), String.valueOf(device_array.get(0).getAlarm_from_phone()), String.valueOf(device_array.get(0).getAlarm_from_tracker()), sms_edittext.getText().toString());

            confirm_function();
        }
    };



    public void confirm_function(){
        String sms_message = sms_edittext.getText().toString().replace("'","\''");

        if(sms_message.equals(sms_text_string)){
            sms_message = "";
        }

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