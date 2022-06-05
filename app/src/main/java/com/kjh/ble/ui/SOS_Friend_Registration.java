package com.kjh.ble.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.kjh.ble.service.ServiceManage;

import com.kjh.ble.R;

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

public class SOS_Friend_Registration extends AppCompatActivity {
    static Context sos_friend_registration_context;
    private DBHelper dbHelper;
    private int position;
    private String kind;
    private ArrayList<SOS> sos_array;

    private EditText name_edittext;
    private EditText phone_edittext;
    private EditText id_edittext;
    private String name_string = "";
    private String phone_number_string = "";
    private String contry_code_string;
    private String app_id_string="";

    private String unique_id;
    private String encode_unique_id;
    private String encode_phone_number;
    private String past_phone_number;
    private String encode_past_phone_number;

    ServiceManage serviceManage;

    private String flag = "";

    private int sms_permission;
    private int contact_permission;
    private int MY_PERMISSION_REQUEST_STORAGE = 1;
    private boolean first_flag = false;

    private Button confirm_btn;

    private boolean search_btn_flag = false;

    Window win;
    RelativeLayout app_id_layout;
    RelativeLayout app_id_small_layout;
    private boolean popup_flag = false;
    private boolean popup_first_flag = false;

    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    //TextView country_code_textview;
    private boolean registration_flag = false;

    RelativeLayout loading_layout;
    private boolean loading_thread_flag = false;

    String sdPath;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

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


        win = getWindow();
        //win.setContentView(R.layout.activity_sos_friend_registration2);
        win.setContentView(R.layout.activity_sos_friend_registration);


        //setContentView(R.layout.activity_sos_friend_registration);

        sos_friend_registration_context = this;

        Intent getintent = getIntent();
        position = getintent.getIntExtra("position",-1);
        kind = getintent.getStringExtra("kind");
        flag = getintent.getStringExtra("flag");

        check_permission();

        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        sos_array = dbHelper.getAllSOS();

        TextView title_text = (TextView)findViewById(R.id.sos_friend_registration_title);
        if(kind.equals("edit")){
            title_text.setText("SMS 연락처 수정");
        }

        name_edittext = (EditText)findViewById(R.id.sos_friend_registration_name_edittext);
        phone_edittext = (EditText)findViewById(R.id.sos_friend_registration_phone_edittext);

        //country_code_textview = (TextView)findViewById(R.id.sos_friend_registration_country_code);
        confirm_btn = (Button)findViewById(R.id.sos_friend_registration_btn);

        TextView text1 = (TextView)findViewById(R.id.sos_friend_registration_text1);


        //country_code_textview = (TextView)findViewById(R.id.sos_friend_registration_country);

        serviceManage = new ServiceManage(getApplicationContext());




        //country_code_textview.setText("+82");

        /*
        try{
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            PhoneNumberUtil phoneNumberUtil1 = PhoneNumberUtil.getInstance();

            country_code_textview.setText("+"+phoneNumberUtil1.getCountryCodeForRegion(tm.getSimCountryIso().toUpperCase()));

            if(country_code_textview.length()<2){
                country_code_textview.setText("+82");
            }
        }catch(Exception e){
            country_code_textview.setText("+52");
        }

        country_code_textview.setOnClickListener(country_code_click);

         */


        switch (position) {
            case 0:
                name_edittext.setText(sos_array.get(0).getName());
                try {
                    phone_edittext.setText(sos_array.get(0).getPhone_number());
                } catch (Exception e) {
                    //phone_edittext.setText(sos_array.get(0).getPhone_number());
                }
                break;
            case 1:
                name_edittext.setText(sos_array.get(1).getName());
                try {
                    phone_edittext.setText(sos_array.get(1).getPhone_number());
                } catch (Exception e) {
                    //phone_edittext.setText(sos_array.get(1).getPhone_number());
                }
                break;
            case 2:
                name_edittext.setText(sos_array.get(2).getName());
                try {
                    phone_edittext.setText(sos_array.get(2).getPhone_number());
                } catch (Exception e) {
                    //phone_edittext.setText(sos_array.get(2).getPhone_number());
                }
                break;
        }

        /*
        if(position != -1) {
            String number = phone_edittext.getText().toString();
            TextView country_text = (TextView) findViewById(R.id.sos_friend_registration_country_code);

            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber phonenumber = phoneNumberUtil.parse(number, null);
                String country = phoneNumberUtil.getRegionCodeForNumber(phonenumber);
                String phone_number = phoneNumberUtil.getNationalSignificantNumber(phonenumber);
                country_text.setText("+" + phoneNumberUtil.getCountryCodeForRegion(country));
                phone_edittext.setText(phone_number);
            } catch (Exception e) {
                if (number.charAt(0) == '0') {
                    phone_edittext.setText(number.substring(1));
                } else {
                    phone_edittext.setText(number);
                }
            }
        }*/
        if(position != -1){
            String number = phone_edittext.getText().toString();
            phone_edittext.setText(number);
        }



        name_edittext.requestFocus();
        //키보드 보이게 하는 부분

        name_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i) {
                    case EditorInfo.IME_ACTION_NEXT:
                        phone_edittext.requestFocus();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        phone_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i) {
                    case EditorInfo.IME_ACTION_NEXT:
                        id_edittext.requestFocus();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });


        name_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(name_edittext.getText().length() > 0){
                    findViewById(R.id.sos_friend_registration_editx1_btn).setVisibility(View.VISIBLE);

                }else{
                    findViewById(R.id.sos_friend_registration_editx1_btn).setVisibility(View.GONE);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        phone_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(phone_edittext.getText().length() > 0){
                    findViewById(R.id.sos_friend_registration_editx2_btn).setVisibility(View.VISIBLE);

                }else{
                    findViewById(R.id.sos_friend_registration_editx2_btn).setVisibility(View.GONE);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        name_edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    findViewById(R.id.sos_friend_registration_editx1_btn).setVisibility(View.GONE);
                }else{
                    if(name_edittext.getText().length() > 0){
                        findViewById(R.id.sos_friend_registration_editx1_btn).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        phone_edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    findViewById(R.id.sos_friend_registration_editx2_btn).setVisibility(View.GONE);
                }else{
                    if(phone_edittext.getText().length() > 0){
                        findViewById(R.id.sos_friend_registration_editx2_btn).setVisibility(View.VISIBLE);
                    }
                }
            }
        });



        findViewById(R.id.sos_friend_registration_back_btn).setOnClickListener(back_btn_click);
        findViewById(R.id.sos_friend_registration_search_btn).setOnClickListener(search_btn_click);
        //findViewById(R.id.sos_friend_registration_btn).setOnClickListener(registration_btn_click);

        confirm_btn.setOnClickListener(registration_btn_click);



        findViewById(R.id.sos_friend_registration_editx1_btn).setOnClickListener(editx1_btn_click);
        findViewById(R.id.sos_friend_registration_editx2_btn).setOnClickListener(editx2_btn_click);

        findViewById(R.id.sos_friend_registration_editx1_btn).setVisibility(View.GONE);
        findViewById(R.id.sos_friend_registration_editx2_btn).setVisibility(View.GONE);

        //country_code_textview.setOnClickListener(country_code_click);

    }

    Button.OnClickListener editx1_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            name_edittext.setText("");
            name_edittext.requestFocus();
        }
    };

    Button.OnClickListener editx2_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            phone_edittext.setText("");
            phone_edittext.requestFocus();
        }
    };

    Button.OnClickListener editx3_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            id_edittext.setText("");
            id_edittext.requestFocus();
        }
    };

    /*
    Button.OnClickListener country_code_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(id_edittext.getWindowToken(), 0);
            }catch(Exception e){

            }

            Intent intent = new Intent(getApplicationContext(), SOS_Friend_Country.class);

            startActivityForResult(intent, 1000);
        }
    };

     */



    Button.OnClickListener back_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //finish_function();sos_registration_new_cancel
            finish_function();
        }
    };


    public void check_permission(){
        sms_permission = ContextCompat.checkSelfPermission(SOS_Friend_Registration.this, Manifest.permission.SEND_SMS);

        if(sms_permission == -1){
            first_flag = true;
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                //설명 보여주는 코드
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_STORAGE);
            }else {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_STORAGE);
            }
        }else{
            InputMethodManager imm = (InputMethodManager) getSystemService(sos_friend_registration_context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

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

    public void go_to_sms(){
        Intent intent = new Intent(getApplicationContext(), SOS_Friend_sms.class);
        intent.putExtra("flag",flag);
        intent.putExtra("kind","registration");
        startActivity(intent);
        finish();
    }

    Button.OnClickListener search_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            contact_permission =  ContextCompat.checkSelfPermission(SOS_Friend_Registration.this, Manifest.permission.READ_CONTACTS);

            try{
                InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(id_edittext.getWindowToken(), 0);
            }catch(Exception e){

            }

            search_btn_flag = true;

            if(contact_permission == -1){
                if(ActivityCompat.shouldShowRequestPermissionRationale(SOS_Friend_Registration.this, Manifest.permission.READ_CONTACTS)){
                    //설명 보여주는 코드
                    ActivityCompat.requestPermissions(SOS_Friend_Registration.this,new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSION_REQUEST_STORAGE);
                }else {
                    ActivityCompat.requestPermissions(SOS_Friend_Registration.this,new String[]{android.Manifest.permission.READ_CONTACTS},MY_PERMISSION_REQUEST_STORAGE);
                }
            }else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        }
    };

    Button.OnClickListener registration_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(id_edittext.getWindowToken(), 0);
            }catch(Exception e){

            }

            if(name_edittext.getText().toString().length() != 0 && phone_edittext.getText().toString().length() != 0) {
                confirm_function();
            }

        }
    };

    public void confirm_function(){
        SOS sos = new SOS(phone_edittext.getText().toString(), name_edittext.getText().toString());
        dbHelper.addSOS(sos);

        finish();
    }


    public void complete(){
        finish_function();
    }

    public void modify_complete(){
        finish_function();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK)
        {
            switch (requestCode){
                case 0:
                    Cursor cursor = getContentResolver().query(data.getData(),
                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    cursor.moveToFirst();
                    String name = cursor.getString(0);        //0은 이름을 얻어옵니다.
                    String number = cursor.getString(1);   //1은 번호를 받아옵니다.
                    cursor.close();

                    phone_edittext.setText(number);
                    name_edittext.setText(name);


                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            contact_permission =  ContextCompat.checkSelfPermission(SOS_Friend_Registration.this, Manifest.permission.READ_CONTACTS);

            if(contact_permission == -1 && first_flag){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
                    //설명 보여주는 코드
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSION_REQUEST_STORAGE);
                }else {
                    ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_CONTACTS},MY_PERMISSION_REQUEST_STORAGE);
                }
            }

            first_flag = false;
        }else{

        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && !first_flag){
            if(search_btn_flag) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        }
    }


    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            //device_array = new ArrayList<>();

            if(message.what == 10){

                try {
                    registration_flag = false;
                    loading_thread_flag = false;
                }catch(Exception e){
                    //Log.e("loading_stop"," error:"+e);
                }
            }
        }
    };

    @Override
    protected  void onDestroy(){
        try{
            InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(id_edittext.getWindowToken(), 0);
        }catch(Exception e){

        }

        try {
            serviceManage.unbind(getApplicationContext());
        }catch(Exception e){

        }

        super.onDestroy();
    }

}