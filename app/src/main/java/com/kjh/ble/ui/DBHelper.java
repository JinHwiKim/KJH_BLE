package com.kjh.ble.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context; }
    /***
     * Database가 존재하지 않을 때, 딱 한번 실행된다.
     * DB를 만드는 역할을 한다.
     * @param db
     */
    @Override public void onCreate(SQLiteDatabase db) {
        // String 보다 StringBuffer가 Query 만들기 편하다.

        StringBuffer sb2 = new StringBuffer();
        sb2.append(" CREATE TABLE DEVICE_TABLE ( ");
        sb2.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb2.append(" mac TEXT, ");
        sb2.append(" alarm_from_phone INTEGER, ");
        sb2.append(" alarm_from_tracker INTEGER, ");
        sb2.append(" sms_text TEXT )");
        db.execSQL(sb2.toString());

        StringBuffer sb3 = new StringBuffer();
        sb3.append(" CREATE TABLE SOS_TABLE ( ");
        sb3.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb3.append(" phone_number TEXT, ");
        sb3.append(" name TEXT ) ");
        db.execSQL(sb3.toString());


        // SQLite Database로 쿼리 실행
        //Toast.makeText(context, "Table 생성완료", Toast.LENGTH_SHORT).show();
    }

    /**
     * Application의 버전이 올라가서
     * Table 구조가 변경되었을 때 실행된다.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();
    }
    /**
     *
     */
    public void getDB() {
        SQLiteDatabase db = getReadableDatabase();
    }


    public void addDevice(Device device){
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO DEVICE_TABLE ( ");
        sb.append(" mac, alarm_from_phone, alarm_from_tracker) ");
        sb.append(" VALUES ( ?, ?, ?) ");



        db.execSQL(sb.toString(),
                new Object[]{
                        device.getMac(),
                        device.getAlarm_from_phone(),
                        device.getAlarm_from_tracker()
                });
    }

    public void addSOS(SOS sos){
        SQLiteDatabase db = getWritableDatabase();

        StringBuffer sb = new StringBuffer();
        sb.append(" INSERT INTO SOS_TABLE ( ");
        sb.append(" phone_number, name) ");
        sb.append(" VALUES ( ?, ? ) ");


        db.execSQL(sb.toString(),
                new Object[]{
                        sos.getPhone_number(),
                        sos.getName()
                });
    }




    public ArrayList<Device> getAllDeviceData(){
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT _ID, mac, alarm_from_phone, alarm_from_tracker, sms_text FROM DEVICE_TABLE");

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), null);

        List device_list = new ArrayList();
        ArrayList<Device> device = new ArrayList<>();

        while(cursor.moveToNext()){
            device.add(new Device(cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4)));

        }

        return device;
    }

    public ArrayList<SOS> getAllSOS(){
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT _ID, phone_number, name FROM SOS_TABLE");

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sb.toString(), null);

        List device_list = new ArrayList();
        ArrayList<SOS> sos = new ArrayList<>();

        while(cursor.moveToNext()){
            sos.add(new SOS(cursor.getString(1), cursor.getString(2)));
        }

        return sos;
    }


    public void device_delete(String macaddress){
        SQLiteDatabase db = getWritableDatabase();

        //db.delete("DEVICE_TABLE","mac = "+macaddress, null);

        db.execSQL("delete from DEVICE_TABLE where mac = '"+macaddress+"'");
    }

    public void sos_delete(String phone_number){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from SOS_TABLE where phone_number = '"+phone_number+"'");
    }

    public void device_setting(String mac, String alarm_from_phone, String alarm_from_tracker, String text){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update DEVICE_TABLE set alarm_from_phone='"+alarm_from_phone+"'," +
                "alarm_from_tracker='"+alarm_from_tracker+"', sms_text='"+text+"' where mac = '"+mac+"'");
    }

    public void device_sms_setting(String mac, String sms_text){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update DEVICE_TABLE set sms_text='"+sms_text+"' where mac = '"+mac+"'");
    }

    public void sos_setting(String phone_number, String name, String before_phone_number){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update SOS_TABLE set phone_number='"+phone_number+"', name='"+name+"' where phone_number = '"+before_phone_number+"'");
    }

    public void device_all_delete(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("delete from DEVICE_TABLE");
    }

    public void sos_all_delete(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("delete from SOS_TABLE");
    }

}