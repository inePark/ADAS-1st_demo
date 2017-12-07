/*
 * Copyright 2017 The Android AVN Project
 *
 *      Korea Electronics Technology Institute
 *
 *      http://keti.re.kr/
 *
 */
package com.example.android.bluetoothlegatt.com;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.android.bluetoothlegatt.Common.CommonData;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.com.bio_status.bio_activity;
import com.example.android.bluetoothlegatt.com.map.map_main_activity;
import com.example.android.bluetoothlegatt.com.music.music_main_activity;

public class DeviceControlActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "SM";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final int DUMP = -1;

    private static final boolean D = true;

    private TextView mConnectionState;
    private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

    private TextView mDataTextView;
    private ScrollView mDataScrollView;

    //TODO: GTO code
    //TODO: Band read data
    public static String b_gyro1;
    public static String b_gyro2;
    public static String b_gyro3;

    public static String b_acc1;
    public static String b_acc2;
    public static String b_acc3;

    public static int b_ac_x;
    public static int b_ac_y;
    public static int b_ac_z;

    public static int b_gy_x;
    public static int b_gy_y;
    public static int b_gy_z;

    public static String b_sleep_bpm;
    public static String b_discount_bpm;

    public static int ldw;

    private TimerTask myTask;
    private Timer timer;

    public int [] recv_sensors;
    public int result_;

    //TODO: CAN read data
    public static int final_can;	// 마지막 CAN data 값 저장 <전역번수>
    public static int before_can;	// 마지막 이전 CAN data 값 저장 <전역번수>
    public static int before_wheel;
    public static int final_wheel;
    public static int cal_316;
    public static int cal_2B0;
    public static int door_state;
    public static int door_opcl;

    //TODO: EXTRA_DATA -> HEX
    public static byte[] packet;


    // TODO: URBAN
    private Button mManboSyncBtn;
    private Button mPPGSyncBtn;
    private Button mSleepSyncBtn;
    private Button mPPGBtn;
    private Button mBaroBtn;

    // TODO: EXERCISE
    private Button mEXStartBtn;
    private Button mEXStopBtn;
    private Button mEXSyncBtn;
    private Button mEXUpdateBtn;

    // TODO: SETTING
    private Button mConnectionBtn;
    private Button mRTCBtn;
    private Button mUserProfileBtn;
    private Button mLanguageBtn;
    private Button mUnitBtn;
    private Button mVersionBtn;
    private Button mUserPPGBtn;
    private Button mSleepTimeBtn;
    private Button mPPGIntervalBtn;
    private Button mEXDisplayItemBtn;

    // TODO: NOTI
    private Button mCallBtn;
    private Button mAcceptCallBtn;
    private Button mSMSBtn;
    private Button mGoalBtn;
    private Button mAppNotiBtn;

    private String mDeviceName;
    private String mDeviceAddress;

    //final String band_41a6 = "CD:25:C5:30:41:A6";
    final String band_41a6_1 = "CD25C53041A6";

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    AccSlidingCollection asc = new AccSlidingCollection();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mDeviceName.equals("H_Band"))    {

                final Intent mIntent = intent;
                //*********************//
                if (action.equals(mBluetoothLeService.ACTION_GATT_CONNECTED)) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            mBluetoothLeService.mState = mBluetoothLeService.UART_PROFILE_CONNECTED;
                        }
                    });
                }

                //*********************//
                if (action.equals(mBluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            mBluetoothLeService.mState = mBluetoothLeService.UART_PROFILE_DISCONNECTED;
                            mBluetoothLeService.close();
                            //setUiState();

                        }
                    });
                }


                //*********************//
                if (action.equals(mBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {

                    mBluetoothLeService.enableTXNotification();

                }
                //*********************//
                if (action.equals(mBluetoothLeService.ACTION_DATA_AVAILABLE)) {

                    final byte[] txValue = intent.getByteArrayExtra(mBluetoothLeService.EXTRA_DATA);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                String text = new String(txValue, "UTF-8");
                                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                //Log.d (TAG, currentDateTimeString + " " + text);
                                Log.d (TAG, stringToHex0x(text));


                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                }
                //*********************//
                if (action.equals(mBluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                    Log.d(TAG, "Device doesn't support UART. Disconnecting");
                    mBluetoothLeService.disconnect();
                }

            }

            else    {

                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    updateConnectionState(R.string.connected);
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    updateConnectionState(R.string.disconnected);
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the user interface.


                    displayGattServices(mBluetoothLeService.getSupportedGattServices());

                    final BluetoothGattCharacteristic notifyCharacteristic = getNottifyCharacteristic();
                    if (notifyCharacteristic == null) {
                        Toast.makeText(getApplication(), "gatt_services can not supported", Toast.LENGTH_SHORT).show();
                        mConnected = false;
                        return;
                    }
                    final int charaProp = notifyCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(
                                notifyCharacteristic, true);
                    }

                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                    byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    displayData(packet);
                }
            }



        }

    };

    public static String stringToHex0x(String s) {  // 헥사 접두사 "0x" 붙이는 버전
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("0x%02X ", (int) s.charAt(i));
        }

        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.gatt_services_characteristics);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataTextView = (TextView) findViewById(R.id.send_data_tv);
        mDataScrollView = (ScrollView) findViewById(R.id.sd_scroll);

        mDataTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    mDataScrollView.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mDataScrollView.smoothScrollBy(0, 800);
                        }
                    }, 100);
                }
            }
        });


        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        myTask = new TimerTask() {
            public void run() {
                final ImageView gesture = (ImageView)findViewById(R.id.image3);
                final Button case1 = (Button)findViewById(R.id.case1);
                final ImageView center_basic = (ImageView)findViewById(R.id.center_basic);

                case1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String band_41a6 = "F5:F4:E4:8A:45:EB";
                        String band_0cb9 = "D7:E9:E2:0B:A7:7E";

                        if (ldw == 0) {
                            System.out.println("ldw status : 0");
                            center_basic.setImageResource(R.drawable.center_ldw);
                        }
                        else {
                            center_basic.setImageResource(R.drawable.center);
                        }

                        if (mDeviceAddress.equals(band_41a6)) {
                            if (cal_316 >= 10 || cal_2B0 >= 10) {
                                gesture.setImageResource(R.drawable.gesture_step_3);
                            }
                            else if (cal_316 == -10 || cal_2B0 == -10) {
                                gesture.setImageResource(R.drawable.gesture_step_3);
                            }
                            else if (final_can == 0 && final_wheel ==0){
                                gesture.setImageResource(R.drawable.gesture_icon);
                            }
                            else if (final_can == 1 && final_wheel ==0){
                                gesture.setImageResource(R.drawable.gesture_icon);
                            }
                            else {
                                gesture.setImageResource(R.drawable.gesture_step_2);
                            }
                        }

                        else if (mDeviceAddress.equals(band_0cb9)) {
                            if (cal_316 >= 10 || cal_2B0 >= 10) {
                                gesture.setImageResource(R.drawable.gesture_step_3);
                            }
                            else if (cal_316 == -10 || cal_2B0 == -10) {
                                gesture.setImageResource(R.drawable.gesture_step_3);
                            }
                            else if (final_can == 0 && final_wheel ==0){
                                gesture.setImageResource(R.drawable.gesture_icon);
                            }
                            else if (final_can == 1 && final_wheel ==0){
                                gesture.setImageResource(R.drawable.gesture_icon);
                            }
                            else {
                                gesture.setImageResource(R.drawable.gesture_step_2);
                            }
                        }

                        else {
                            gesture.setImageResource(R.drawable.none_gesture_icon);
                        }
                    }
                });

                final_can();
                before_can();
                final_wheel();
                before_wheel();
                can_gap();
                ldw();

                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                TextView textView = (TextView)findViewById(R.id.textView);
                                Typeface typeFace = Typeface.createFromAsset(getAssets(), "DS-DIGIB.TTF");
                                textView.setTypeface(typeFace);
                                String band_41a6 = "CD:25:C5:30:41:A6";
                                String band_0cb9 = "FA:9A:08:4E:0C:B9";

                                if (ldw==0) {
                                    textView.setVisibility(View.VISIBLE);
                                    center_basic.setImageResource(R.drawable.center_ldw);
                                }
                                else {
                                    textView.setVisibility(View.INVISIBLE);
                                }

                                if (mDeviceAddress.equals(band_41a6)) {
                                    case1.performClick();
                                }
                                else if (mDeviceAddress.equals(band_0cb9)) {
                                    case1.performClick();
                                }
                                else {
                                    case1.performClick();
                                }
                            }
                        });
                    }
                }).start();
            }
        };
        timer = new Timer();

        timer.schedule(myTask, 0, 500);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mManboSyncBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_SYNC_START_REQ);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(byte[] packet) {
        if (packet != null) {
            byte msgType = packet[1];

            switch(msgType) {
                // TODO: URBAN

                default:
                    break;

            }
            //autoScrollView(getStringPacket(packet));
            getStringPacket(packet);
        }
    }

    private String getStringPacket(byte[] packet) {

        if (packet [1] != -32) { //exception handling
            return null;
        }

        String hexString = "";

        for (byte b : packet) {  						//readBuf -> Hex
            hexString += Integer.toString((b & 0xF0) >> 4, 16);
            hexString += Integer.toString(b & 0x0F, 16);
        }

        recv_sensors = new int [6];

        // TODO : GYRO X Y Z HEX
        b_gyro1 = hexString.substring(6, 10);
        b_gyro2 = hexString.substring(10, 14);
        b_gyro3 = hexString.substring(14, 18);

        // TODO : ACC X Y Z HEX
        b_acc1 = hexString.substring(18, 22);
        b_acc2 = hexString.substring(22, 26);
        b_acc3 = hexString.substring(26, 30);

        //TODO : Band BPM information
        b_sleep_bpm = hexString.substring(32, 34);
        b_discount_bpm = hexString.substring(34, 36);

        // TODO : ACC X Y Z Decimal
        b_ac_x = (short) Integer.parseInt(b_acc1, 16);
        b_ac_y = (short) Integer.parseInt(b_acc2, 16);
        b_ac_z = (short) Integer.parseInt(b_acc3, 16);

        // TODO : GYRO X Y Z Decimal
        b_gy_x = (short) Integer.parseInt(b_gyro1, 16);
        b_gy_y = (short) Integer.parseInt(b_gyro2, 16);
        b_gy_z = (short) Integer.parseInt(b_gyro3, 16);

        recv_sensors [0] = b_ac_x;
        recv_sensors [1] = b_ac_y;
        recv_sensors [2] = b_ac_z;

        recv_sensors [3] = b_gy_x;
        recv_sensors [4] = b_gy_x;
        recv_sensors [5] = b_gy_x;

        StringBuilder sb = new StringBuilder(packet.length * 2);

        result_ = asc.SlidingCollectionInterface (recv_sensors);
        if (result_ != DUMP) {
            Log.d (TAG, "In IF branch");

            asc.gesture_count ++;

            switch (result_)	{

                case LEFT:
                        System.out.println("+++++++++++++++++ LEFT +++++++++++++++++");
                        if (final_can == 1 && final_wheel == 0) {
                            System.out.println("difference 2");
                            Runtime runtime0 = Runtime.getRuntime();
                            Process process0;
                            String res0 = "input keyevent 21";
                            try {
                                process0 = runtime0.exec(res0); //2번 실행해야 되는 경우가 있음
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Log.e("Process Manager", "Unable to execute top command");
                            }
                        }
                        else {
                            System.out.println("*************************** No event ***************************");
                            Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            LinearLayout view = (LinearLayout) toast.getView();
                            ImageView image = new ImageView(getApplicationContext());
                            image.setImageResource(R.drawable.warning);
                            view.addView(image, 0);
                            toast.show();
                        }

                    break;

                case RIGHT:
                        System.out.println("+++++++++++++++++ RIGHT +++++++++++++++++");
                       if (final_can == 1 && final_wheel == 0) {
                           Runtime runtime1 = Runtime.getRuntime();
                           Process process1;
                           String res = "input keyevent 22";
                           try {
                               process1 = runtime1.exec(res); //2번 실행해야 되는 경우가 있음
                           } catch (IOException e) {
                               // TODO Auto-generated catch block
                               e.printStackTrace();
                               Log.e("Process Manager", "Unable to execute top command");
                           }
                       }
                       else {
                           System.out.println("*************************** No event ***************************");
                           Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                           toast.setGravity(Gravity.CENTER, 0, 0);
                           LinearLayout view = (LinearLayout) toast.getView();
                           ImageView image = new ImageView(getApplicationContext());
                           image.setImageResource(R.drawable.warning);
                           view.addView(image, 0);
                           toast.show();
                       }

                    break;

                case FRONT:
                    //sb.append ("[[[ FRONT ]\n");
                    System.out.println("+++++++++++++++++ FRONT +++++++++++++++++");
                    if (final_can == 1 && final_wheel == 0) {
                        Runtime runtime2 = Runtime.getRuntime();
                        Process process2;
                        String res2 = "input keyevent 66";
                        try {

                            process2 = runtime2.exec(res2); //2번 실행해야 되는 경우가 있음
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.e("Process Manager", "Unable to execute top command");
                        }
                    }
                    else {
                        System.out.println("*************************** No event ***************************");
                        Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout view = (LinearLayout) toast.getView();
                        ImageView image = new ImageView(getApplicationContext());
                        image.setImageResource(R.drawable.warning);
                        view.addView(image, 0);
                        toast.show();
                    }

                    break;

                case UP:
                    System.out.println("+++++++++++++++++ UP +++++++++++++++++");
                    break;

                case CLOCK:
                    System.out.println("+++++++++++++++++ CLOCK +++++++++++++++++");
                    if (cal_316 >=10 || cal_2B0 >= 10) {
                        System.out.println("*************************** No event ***************************");
                        Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout view = (LinearLayout) toast.getView();
                        ImageView image = new ImageView(getApplicationContext());
                        image.setImageResource(R.drawable.warning);
                        view.addView(image, 0);
                        toast.show();
                    }
                    else if (cal_316 == -10 || cal_2B0 == -10) {
                        System.out.println("*************************** No event ***************************");
                        Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout view = (LinearLayout) toast.getView();
                        ImageView image = new ImageView(getApplicationContext());
                        image.setImageResource(R.drawable.warning);
                        view.addView(image, 0);
                        toast.show();
                    }
                    else {
                        Runtime runtime4 = Runtime.getRuntime();
                        Process process4;
                        String res4 = "input keyevent 24";
                        try {

                            process4 = runtime4.exec(res4); //2번 실행해야 되는 경우가 있음
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.e("Process Manager", "Unable to execute top command");
                        }
                    }

                    break;

                case ANTI_CLOCK:
                    //sb.append (" **** ANTI CLOCK\n");
                    System.out.println("+++++++++++++++++ ANTI CLOCK +++++++++++++++++");
                    if (cal_316 >=10 || cal_2B0 >= 10) {
                        System.out.println("*************************** No event ***************************");
                        Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout view = (LinearLayout) toast.getView();
                        ImageView image = new ImageView(getApplicationContext());
                        image.setImageResource(R.drawable.warning);
                        view.addView(image, 0);
                        toast.show();
                    }
                    else if (cal_316 == -10 || cal_2B0 == -10) {
                        System.out.println("*************************** No event ***************************");
                        Toast toast = Toast.makeText(this, "                                                                                    .", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout view = (LinearLayout) toast.getView();
                        ImageView image = new ImageView(getApplicationContext());
                        image.setImageResource(R.drawable.warning);
                        view.addView(image, 0);
                        toast.show();
                    }
                    else {
                        Runtime runtime5 = Runtime.getRuntime();
                        Process process5;
                        String res5 = "input keyevent 25";
                        try {

                            process5 = runtime5.exec(res5); //2번 실행해야 되는 경우가 있음
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.e("Process Manager", "Unable to execute top command");
                        }
                    }

                    break;

                default:
            }
        }
        return sb.toString();
    }

    private BluetoothGattCharacteristic getNottifyCharacteristic(){
        BluetoothGattCharacteristic notifyCharacteristic = null;
        if(mGattCharacteristics == null || mGattCharacteristics.size() == 0){
            return null;
        }
        for (int i = 0; i < mGattCharacteristics.size() ; i++) {
            for (int j = 0; j < mGattCharacteristics.get(i).size() ; j++) {
                notifyCharacteristic =  mGattCharacteristics.get(i).get(j);
                if(notifyCharacteristic.getUuid().equals(BluetoothLeService.FFF4_RATE_MEASUREMENT)){
                    return notifyCharacteristic;
                }
            }
        }
        return null;
    }

    private BluetoothGattCharacteristic getWriteGattCharacteristic(){
        BluetoothGattCharacteristic writeGattCharacteristic = null;
        if(mGattCharacteristics == null || mGattCharacteristics.size() == 0){
            return null;
        }

        for (int i = 0; i < mGattCharacteristics.size() ; i++) {
            for (int j = 0; j < mGattCharacteristics.get(i).size() ; j++) {
                writeGattCharacteristic =  mGattCharacteristics.get(i).get(j);
                if(writeGattCharacteristic. getUuid().equals(BluetoothLeService.FFF3_RATE_MEASUREMENT)){
                    return writeGattCharacteristic;
                }
            }
        }
        return null;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_SEND_PACKET);
        return intentFilter;
    }

    //TODO move activity ImgButon
    public void onClickMap(View v) {        //Map info Activity     //Map Button

        final Intent i = new Intent(this, map_main_activity.class);
        startActivity(i);

    }

    public void onClickMusic(View v) {        //Music info Activity     //Music Button

        final Intent i = new Intent(this, music_main_activity.class);
        startActivity(i);

    }

    public void onClickBio(View v) {        //Read to CAN DATA Activity     //CAN Button

        final Intent i = new Intent(this, bio_activity.class);
        startActivity(i);

    }

    //TODO ＃Read to CAN DATA Function
    //TODO read to last CAN DATA(Speed)
    private void final_can ()	{

        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./final_can.sh";
            process1 = runtime1.exec(cmd);

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));

            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {

                String data_1 = line1.substring(19, 21);
                int hex1 = Integer.parseInt(data_1, 16);

                final_can = hex1;
            }

            br.close();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }

    }

    //TODO read to last before CAN DATA(Speed)
    private void before_can ()	{

        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./before_can.sh";
            process1 = runtime1.exec(cmd);

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));

            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {

                String b_data_1 = line1.substring(19, 21);
                int b_hex1 = Integer.parseInt(b_data_1, 16);

                before_can = b_hex1;
            }

            br.close();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }

    }

    //TODO read to last CAN DATA(Wheel)
    private void final_wheel ()	{

        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {

            String cmd = "./wheel_final.sh";
            process1 = runtime1.exec(cmd);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));

            String line1 = "";

            if ((line1 = br.readLine()) != null)
            {
            String b_data_1 = line1.substring(19, 21);

                int b_hex1 = Integer.parseInt(b_data_1, 16);

                 final_wheel = b_hex1;

            }

            br.close();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }

    }

    //TODO read to last before CAN DATA(Wheel)
    private void before_wheel ()	{

        Runtime runtime1 = Runtime.getRuntime();
        Process process1;
        try
        {

           String cmd = "./wheel_before.sh";
           process1 = runtime1.exec(cmd);

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));

            String line1 = "";

            if ((line1 = br.readLine()) != null)
            {
            String b_data_1 = line1.substring(19, 21);
                int b_hex1 = Integer.parseInt(b_data_1, 16);


                before_wheel = b_hex1;

            }

            br.close();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }

    }

    //TODO read to last CAN DATA(MN_band info)
    private void door_state ()	{
        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./door_state.sh";
            process1 = runtime1.exec(cmd);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));
            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {
                String b_data_1 = line1.substring(19, 21);      // normal hex data
                String b_data_2 = line1.substring(22, 24);      // error hex data
                int b_hex1 = Integer.parseInt(b_data_1, 16);    // normal int data
                int b_hex2 = Integer.parseInt(b_data_2, 16);    // error int data

                door_state = b_hex1;  // 533#00 , 533#01 <- door state
                door_opcl = b_hex2;   // 533#01.00 , 533#01.01 <- open, close state

            }

            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //TODO read to last CAN DATA(ucomm_band info)
    private void door_state_ucomm ()	{
        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./ucomm_band.sh";
            process1 = runtime1.exec(cmd);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));
            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {
                String b_data_1 = line1.substring(19, 21);      // normal hex data
                String b_data_2 = line1.substring(22, 24);      // error hex data
                int b_hex1 = Integer.parseInt(b_data_1, 16);    // normal int data
                int b_hex2 = Integer.parseInt(b_data_2, 16);    // error int data

                door_state = b_hex1;  // 533#00 , 533#01 <- door state
                door_opcl = b_hex2;   // 533#01.00 , 533#01.01 <- open, close state

            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void can_gap() {
        cal_316 = final_can - before_can;
        cal_2B0 = final_wheel - before_wheel;

        System.out.println("현재 스피드 : " + final_can);
        System.out.println("이전 스피드 : " + before_can);
        System.out.println("현재 조향각 : " + final_wheel);
        System.out.println("이전 조향각 : " + before_wheel);

    }

    private void ldw() {
        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./ldw.sh";
            process1 = runtime1.exec(cmd);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));
            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {
                String return_ldw = line1.substring(0, 1);
                int b_hex11 = Integer.parseInt(return_ldw, 16);    // normal int data

                ldw = b_hex11;

            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static final int FRONT = 0;
    public static final int BACK = FRONT + 1;	//1
    public static final int RIGHT = BACK + 1;   //2
    public static final int LEFT = RIGHT + 1;	//3
    public static final int UP = LEFT + 1;	//4
    public static final int DOWN = UP + 1;	//5
    public static final int	CLOCK = DOWN + 1;	//6
    public static final int ANTI_CLOCK = CLOCK + 1;	//7
    public static final int LOW_CLOCK = ANTI_CLOCK + 1;	//8
    public static final int LOW_ANTI = LOW_CLOCK + 1;	//9
    public static final int UNKNOWN_ = 99;

    public static final int GESTURE_NUM = LOW_ANTI + 1;	//10


}
