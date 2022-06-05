package com.kjh.ble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.kjh.ble.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PopupActivity_onebtn extends Activity {
    private String Message;
    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    String sdPath;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private boolean log_fileIO_flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (!isScreenOn(getApplicationContext())) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                wakeLock.acquire(3000);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }catch(Exception e){
        }

        try {
            setContentView(R.layout.activity_popup_onebtn);

            Bundle bun = getIntent().getExtras();
            Message = bun.getString("Message");

        }catch(Exception e){
        }

        TextView popup_title = (TextView)findViewById(R.id.popup_one_title);
        TextView popup_text1 = (TextView)findViewById(R.id.popup_one_text1);
        TextView popup_text2 = (TextView)findViewById(R.id.popup_one_text2);
        Button popup_btn = (Button)findViewById(R.id.popup_one_ok);

        try {
            Typeface noto_r = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
            Typeface noto_m = Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf");


            popup_title.setTypeface(noto_m);
            popup_text1.setTypeface(noto_r);
            popup_btn.setTypeface(noto_m);
        }catch(Exception e){
        }

        try {
            if (Message.equals("registration_scan_success")) {
                popup_title.setText("스캔 성공");
                popup_text1.setText("BLE Device 스캔에 성공했습니다.\nBLE Device를 등록하시겠습니까?");
                popup_text2.setVisibility(View.GONE);
                //popup_btn.setText(getString(R.string.registration_scan_success_btn));
                //popup_btn.setText(getString(R.string.registration_scan_popup_btn));
            }else if(Message.equals("registration_scan_fail")){
                popup_title.setText("스캔 실패");
                popup_text1.setText("BLE Device 스캔에 실패 했습니다.");
                popup_text2.setVisibility(View.GONE);
            }
            /*
            if (Message.equals("email_format_fail")) {
                popup_title.setText(getString(R.string.email_format_fail_title));
                popup_text1.setText(getString(R.string.email_format_fail));
                popup_text2.setVisibility(View.GONE);
            }
            else if (Message.equalsIgnoreCase("social_agreement_email_format_fail")) {
                popup_title.setText(getString(R.string.email_format_fail_title));
                popup_text1.setText(getString(R.string.email_format_fail));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("email_change_format_fail")) {
                popup_title.setText(getString(R.string.email_format_fail_title));
                popup_text1.setText(getString(R.string.email_format_fail));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("email_exist")) {
                popup_title.setText(getString(R.string.email_exist_title));
                popup_text1.setText(getString(R.string.email_exist));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("email_password_length")) {
                popup_title.setText(getString(R.string.email_password_length_title));
                popup_text1.setText(getString(R.string.email_password_length));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("email_equal_fail")) {
                popup_title.setText(getString(R.string.email_equal_fail_title));
                popup_text1.setText(getString(R.string.email_equal_fail));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("term_check")) {
                popup_title.setText(getString(R.string.term_check_title));
                popup_text1.setText(getString(R.string.term_check));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("registration_scan_success")) {
                popup_title.setText(getString(R.string.registration_scan_success_title));
                popup_text1.setText(getString(R.string.registration_scan_success));
                popup_text2.setVisibility(View.GONE);
                //popup_btn.setText(getString(R.string.registration_scan_success_btn));
                //popup_btn.setText(getString(R.string.registration_scan_popup_btn));
            } else if (Message.equals("registration_scan_fail")) {
                popup_title.setText(getString(R.string.registration_scan_fail_title));
                popup_text1.setText(getString(R.string.registration_scan_fail));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("internet_disable")) {
                popup_title.setText(getString(R.string.internet_disable_title));
                popup_text1.setText(getString(R.string.internet_disable));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("email_change_exist")) {
                popup_title.setText(getString(R.string.email_exist_title));
                popup_text1.setText(getString(R.string.email_exist));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("login_email_nonexist")) {
                popup_title.setText(getString(R.string.login_email_nonexist_title));
                popup_text1.setText(getString(R.string.login_email_nonexist));
                popup_text2.setVisibility(View.GONE);
            }
            else if (Message.equals("login_password_wrong")) {
                popup_title.setText(getString(R.string.login_password_wrong_title));
                popup_text1.setText(getString(R.string.login_password_wrong));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("login_password_change_wrong")) {
                popup_title.setText(getString(R.string.login_password_wrong_title));
                popup_text1.setText(getString(R.string.login_password_wrong));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("login_duplicate")) {
                popup_title.setText(getString(R.string.login_duplicate_title));
                popup_text1.setText(getString(R.string.login_duplicate));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("registration_empty_nickname")) {
                popup_title.setText(getString(R.string.registration_empty_nickname_title));
                popup_text1.setText(getString(R.string.registration_empty_nickname));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("sound_volume_popup")) {
                popup_title.setText(getString(R.string.sound_volume_popup_title));
                popup_text1.setText(getString(R.string.sound_volume_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("gps_notice_popup")) {
                popup_title.setText(getString(R.string.gps_notice_popup_title));
                popup_text1.setText(getString(R.string.gps_notice_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("gps_notice_popup_card")) {
                popup_title.setText(getString(R.string.gps_notice_popup_title));
                popup_text1.setText(getString(R.string.gps_notice_popup));
                popup_text2.setVisibility(View.GONE);
            }else if (Message.equals("gps_notice_popup_tag")) {
                popup_title.setText(getString(R.string.gps_notice_popup_title));
                popup_text1.setText(getString(R.string.gps_notice_popup));
                popup_text2.setVisibility(View.GONE);
            }else if (Message.equals("gps_notice_popup_wallet")) {
                popup_title.setText(getString(R.string.gps_notice_popup_title));
                popup_text1.setText(getString(R.string.gps_notice_popup));
                popup_text2.setVisibility(View.GONE);
            }else if (Message.equals("sos_friend_registration_name_popup")) {
                //popup_title.setText(getString(R.string.sos_friend_registration_name_popup_title));
                //popup_text.setText(getString(R.string.sos_friend_registration_name_popup));
            } else if (Message.equals("sos_friend_registration_phone_popup")) {
                //popup_title.setText(getString(R.string.sos_friend_registration_phone_popup_title));
                //popup_text.setText(getString(R.string.sos_friend_registration_phone_popup));
            } else if (Message.equals("social_change_popup")) {
                popup_title.setText(getString(R.string.social_change_popup_title));
                popup_text1.setText(getString(R.string.social_change_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_change_current")) {
                popup_title.setText(getString(R.string.password_change_current_title));
                popup_text1.setText(getString(R.string.password_change_current));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_change_password_length")) {
                popup_title.setText(getString(R.string.password_change_password_length_title));
                popup_text1.setText(getString(R.string.password_change_password_length));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_change_re_error")) {
                popup_title.setText(getString(R.string.password_change_re_error_title));
                popup_text1.setText(getString(R.string.password_change_re_error));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_change_success")) {
                popup_title.setText(getString(R.string.password_change_success_title));
                popup_text1.setText(getString(R.string.password_change_success));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_find_success")) {
                popup_title.setText(getString(R.string.password_find_success_title));
                popup_text1.setText(getString(R.string.password_find_success));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_find_nonexist")) {
                popup_title.setText(getString(R.string.password_find_nonexist_title));
                popup_text1.setText(getString(R.string.password_find_nonexist));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("password_find_format")) {
                popup_title.setText(getString(R.string.password_find_format_title));
                popup_text1.setText(getString(R.string.password_find_format));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("camera_popup")) {
                popup_title.setText(getString(R.string.camera_popup_title));
                popup_text1.setText(getString(R.string.camera_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("location_null_popup")) {
                popup_title.setText(getString(R.string.location_null_popup_title));
                popup_text1.setText(getString(R.string.location_null_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("registration_duplicate_name")) {
                popup_title.setText(getString(R.string.registration_duplicate_name_title));
                popup_text1.setText(getString(R.string.registration_duplicate_name));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("bt_on_popup")) {
                //popup_title.setText(getString(R.string.bt_on_popup_title));
                //popup_text.setText(getString(R.string.bt_on_popup));
            } else if (Message.equals("gps_tracking_popup")) {
                popup_title.setText(getString(R.string.gps_tracking_popup_title));
                popup_text1.setText(getString(R.string.gps_tracking_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("gps_tracking_popup_wallet")) {
                popup_title.setText(getString(R.string.gps_tracking_popup_title));
                popup_text1.setText(getString(R.string.gps_tracking_popup));
                popup_text2.setVisibility(View.GONE);
            }else if (Message.equals("record_popup")) {
                popup_title.setText(getString(R.string.record_popup_title));
                popup_text1.setText(getString(R.string.record_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("video_record_popup")) {
                popup_title.setText(getString(R.string.video_record_popup_title));
                popup_text1.setText(getString(R.string.video_record_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("sos_friend_registration_count")) {
                popup_title.setText(getString(R.string.sos_friend_registration_count_title));
                popup_text1.setText(getString(R.string.sos_friend_registration_count));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("sos_registration_duplicate")) {
                popup_title.setText(getString(R.string.sos_registration_duplicate_title));
                popup_text1.setText(getString(R.string.sos_registration_duplicate));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("app_start_gps_popup")) {
                popup_title.setText(getString(R.string.app_start_gps_popup_title));
                popup_text1.setText(getString(R.string.app_start_gps_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("user_guide_wallet")) {
                popup_title.setText(getString(R.string.user_guide_wallet_title));
                popup_text1.setText(getString(R.string.user_guide_wallet));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("app_id_fail")) {
                popup_title.setText(getString(R.string.app_id_fail_title));
                popup_text1.setText(getString(R.string.app_id_fail));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("sos_ap_popup")) {
                popup_title.setText(getString(R.string.sos_ap_popup_title));
                popup_text1.setText(getString(R.string.sos_ap_popup));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("device_list_update")) {
                popup_title.setText(getString(R.string.device_list_update_title));
                popup_text1.setText(getString(R.string.device_list_update));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("registration_tracker_count")) {
                popup_title.setText(getString(R.string.registration_tracker_count_title));
                popup_text1.setText(getString(R.string.registration_tracker_count));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("registration_sos_count")) {
                popup_title.setText(getString(R.string.registration_sos_count_title));
                popup_text1.setText(getString(R.string.registration_sos_count));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("update2")) {
                popup_title.setText(getString(R.string.update_title));
                popup_text1.setText(getString(R.string.update2));
                popup_btn.setText(getString(R.string.update2_btn));
                popup_text2.setVisibility(View.GONE);
            } else if (Message.equals("video_recording_off")) {
                popup_title.setText(getString(R.string.video_recording_off_title));
                popup_text1.setText(getString(R.string.video_recording_off));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("memberjoin_verify_exist")){
                popup_title.setText(getString(R.string.popup_verify_phone_exist_title));
                popup_text1.setText(getString(R.string.popup_verify_phone_exist));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("memberjoin_verify_request")){
                popup_title.setText(getString(R.string.popup_verify_phone_sent_title));
                popup_text1.setText(getString(R.string.popup_verify_phone_sent));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("memberjoin_verify_invalid")){
                popup_title.setText(getString(R.string.popup_verify_phone_invalid_title));
                popup_text1.setText(getString(R.string.popup_verify_phone_invalid));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("memberjoin_success_popup")){
                popup_title.setText(getString(R.string.memberjoin_success_popup_title));
                popup_text1.setText(getString(R.string.memberjoin_success_popup));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("social_memberjoin_success_popup")){
                popup_title.setText(getString(R.string.memberjoin_success_popup_title));
                popup_text1.setText(getString(R.string.memberjoin_success_popup));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("sos_friend_complete_popup")){
                popup_title.setText(getString(R.string.sos_friend_registration_complete_title));
                popup_text1.setText(getString(R.string.sos_friend_registration_complete));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("sos_friend_registration_complete_modify")){
                popup_title.setText(getString(R.string.sos_friend_registration_complete_modify_title));
                popup_text1.setText(getString(R.string.sos_friend_registration_complete_modify));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("sos_registration_sms_complete")){
                popup_title.setText(getString(R.string.sos_friend_sms_registration_complete_title));
                popup_text1.setText(getString(R.string.sos_friend_sms_registration_complete));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("sos_friend_sms_modify_complete")){
                popup_title.setText(getString(R.string.sos_friend_sms_modify_complete_title));
                popup_text1.setText(getString(R.string.sos_friend_sms_modify_complete));
                popup_text2.setVisibility(View.GONE);
            }else if(Message.equals("device_setting_change_popup")){
                popup_title.setText(getString(R.string.device_setting_change_popup_title));
                popup_text1.setText(getString(R.string.device_setting_change_popup));
                popup_text2.setVisibility(View.GONE);
            }

             */
        }catch(Exception e){

        }

        popup_btn.setOnClickListener(new btn_click());
    }

    private class btn_click implements View.OnClickListener {

        public void onClick(View v) {
            try {
                finish();
                overridePendingTransition(0,0);

                if(Message.equals("registration_scan_success")){
                    ((Registration)Registration.registration_context).scan_success();
                }else if(Message.equals("registration_scan_fail")){
                    ((Registration)Registration.registration_context).scan_fail();
                }
                /*
                if(Message.equals("email_format_fail")){
                    ((MemberJoin)MemberJoin.memberjoin_context).rewrite(1);
                }else if(Message.equals("social_agreement_email_format_fail")){
                    //((SocialAgreement)SocialAgreement.socialagreement_context).rewrite(1);
                }else if(Message.equals("name_format_fail")){
                    ((MemberJoin)MemberJoin.memberjoin_context).rewrite(0);
                }else if(Message.equals("email_change_format_fail")){
                    ((Email_change)Email_change.email_change_context).rewrite(1);
                } else if(Message.equals("email_exist")){
                    ((MemberJoin)MemberJoin.memberjoin_context).rewrite(2);
                }else if(Message.equals("email_change_exist")){
                    ((Email_change)Email_change.email_change_context).rewrite(1);
                } else if(Message.equals("email_password_length")){
                    ((MemberJoin)MemberJoin.memberjoin_context).rewrite(3);
                }else if(Message.equals("email_equal_fail")){
                    ((MemberJoin)MemberJoin.memberjoin_context).rewrite(4);
                }else if(Message.equals("term_check")){

                }else if(Message.equals("registration_scan_success")){
                    ((Registration_scan)Registration_scan.registration_scan_context).scan_success();
                }else if(Message.equals("registration_scan_fail")){
                    ((Registration_scan)Registration_scan.registration_scan_context).scan_fail();
                }else if(Message.equals("login_email_nonexist")){
                    ((Login)Login.loginContext).rewrite(1);
                }else if(Message.equals("login_phone_nonexist")){
                    ((Login)Login.loginContext).rewrite(1);
                }else if(Message.equals("login_password_wrong")){
                    ((Login)Login.loginContext).rewrite(2);
                }else if(Message.equals("login_password_change_wrong")){
                    ((Email_change)Email_change.email_change_context).rewrite(2);
                }else if(Message.equals("login_duplicate")){
                    ((Login)Login.loginContext).login_success();
                }else if(Message.equals("registration_empty_nickname")){
                    ((Registration_nickname)Registration_nickname.registration_nickname_context).rewrite();
                }else if(Message.equals("gps_notice_popup")){
                    ((Detail_SOS_Setting)Detail_SOS_Setting.detail_sos_setting_context).gps_on();
                }else if(Message.equals("gps_notice_popup_card")){
                    ((Detail_Card_Setting)Detail_Card_Setting.detail_card_setting_context).gps_on();
                }else if(Message.equals("gps_notice_popup_tag")){
                    ((Detail_Tag_Setting)Detail_Tag_Setting.detail_tag_setting_context).gps_on();
                }else if(Message.equals("gps_notice_popup_tag")){
                    ((Detail_Wallet_Setting)Detail_Wallet_Setting.detail_wallet_setting_context).gps_on();
                }else if(Message.equals("password_change_current")){
                    ((Password_change)Password_change.password_change_context).rewrite(1);
                }else if(Message.equals("password_change_password_length")){
                    ((Password_change)Password_change.password_change_context).rewrite(2);
                }else if(Message.equals("password_change_re_error")){
                    ((Password_change)Password_change.password_change_context).rewrite(3);
                }else if(Message.equals("password_change_success")){
                    ((Password_change)Password_change.password_change_context).password_change_success();
                }else if(Message.equals("password_find_success")){
                    ((Password_find)Password_find.password_find_context).finish();
                }else if(Message.equals("password_find_nonexist")){
                    ((Password_find)Password_find.password_find_context).rewrite();
                }else if(Message.equals("password_find_format")){
                    ((Password_find)Password_find.password_find_context).rewrite();
                }else if(Message.equals("registration_duplicate_name")){
                    ((Registration_nickname)Registration_nickname.registration_nickname_context).rewrite();
                }else if(Message.equals("gps_tracking_popup")){
                    ((Detail_SOS_Setting)Detail_SOS_Setting.detail_sos_setting_context).gps_tracking_on();
                }else if(Message.equals("gps_tracking_popup_wallet")){
                    ((Detail_Wallet_Setting)Detail_Wallet_Setting.detail_wallet_setting_context).gps_tracking_on();
                }else if(Message.equals("device_list_update")){
                    ((MainActivity)MainActivity.mainContext).update_deviceinfo();
                }else if(Message.equals("update2")){
                    ((Luncher)Luncher.luncher_context).version_upgrade();
                }else if(Message.equals("sos_ap_popup")){
                    ((CustomFirebaseMessagingService)CustomFirebaseMessagingService.messageing_context).popup_close();
                }else if(Message.equals("memberjoin_success_popup")){
                    ((MemberJoin)MemberJoin.memberjoin_context).memberjoin_success_popup_ok();
                }else if(Message.equals("social_memberjoin_success_popup")){
                    ((SocialAgreement)SocialAgreement.socialagreement_context).memberjoin_success();
                }else if(Message.equals("sos_friend_complete_popup")){
                    ((SOS_Friend_Registration)SOS_Friend_Registration.sos_friend_registration_context).complete();
                }else if(Message.equals("sos_friend_registration_complete_modify")){
                    ((SOS_Friend_Registration)SOS_Friend_Registration.sos_friend_registration_context).modify_complete();
                }else if(Message.equals("sos_registration_sms_complete")){
                    ((SOS_Friend_sms)SOS_Friend_sms.sos_friend_sms_context).finish_function();
                }else if(Message.equals("sos_friend_sms_modify_complete")){
                    ((SOS_Friend_sms)SOS_Friend_sms.sos_friend_sms_context).finish_function();
                }

                 */

            } catch (Exception e) {

            }
        }
    }

    public static boolean isScreenOn(Context context) {
        return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //TODO Auto-generated method stub
        //return super.onTouchEvent(event);
        return false;
    }


    @Override
    public void onBackPressed(){
        // super.onBackPressed();
    }


}