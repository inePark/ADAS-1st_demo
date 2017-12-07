package com.example.android.bluetoothlegatt.com.bio_status;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.com.AccSlidingCollection;
import com.example.android.bluetoothlegatt.com.BluetoothLeService;
import com.example.android.bluetoothlegatt.com.DeviceControlActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.example.android.bluetoothlegatt.com.DeviceControlActivity.*;

/**
 * Created by LSY1 on 2017-08-31.
 */

public class bio_activity extends Activity {

    AccSlidingCollection asc = new AccSlidingCollection();
    public static final int DUMP = -1;

    //TODO Gesture sensing variable
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

    //TODO BPM info
    public static String b_sleep_bpm;
    public static String b_discount_bpm;

    public static int b_bpm_a;
    public static int b_bpm_b;

    public static int diff;;

    public static int bio_ldw;

    public static int driving_timer;

    private TimerTask myTask;
    private Timer timer;

    //TODO BLE HEX data receive
    private boolean mConnected = false;
    public static byte[] packet;
    private TextView mConnectionState;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;


    ImageView normal_mode, sleep_mode, tired_mode;
    Button mode_btn, sleep_btn, tired_btn;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }

            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                DeviceControlActivity.packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                displayData(DeviceControlActivity.packet);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bio_status);

        myTask = new TimerTask() {
            public void run() {
                driving_time();
                ldw();
                final ImageView user_icon = (ImageView)findViewById(R.id.normal);

             new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView heart_rate = (TextView) findViewById(R.id.textView4);
                                TextView bpm = (TextView) findViewById(R.id.bpm_textView);
                                TextView bpm_fix = (TextView) findViewById(R.id.bpm_fix);
                                TextView driving_fix = (TextView) findViewById(R.id.textView5);
                                TextView driving_time = (TextView) findViewById(R.id.driving_time_textView);
                                TextView time_fix = (TextView) findViewById(R.id.time_fix);

                                Typeface typeFace = Typeface.createFromAsset(getAssets(), "DS-DIGIB.TTF");

                                heart_rate.setTypeface(typeFace);
                                bpm.setTypeface(typeFace);
                                driving_time.setTypeface(typeFace);
                                bpm_fix.setTypeface(typeFace);
                                driving_fix.setTypeface(typeFace);
                                time_fix.setTypeface(typeFace);

                                String h_bpm = Integer.toString(b_bpm_b);       //h_bpm is Partron Band BPM
                                //String diff_bpm = Integer.toString(diff);
                                String d_time = Integer.toString(driving_timer);    //d_time is CANoe 'Long driving time' button

                                bpm.setText(h_bpm);
                                driving_time.setText(d_time);
                                //System.out.println(bio_ldw);

                                if (70 <= final_can && 90 >= final_can && driving_timer == 0) {
                                    user_icon.setImageResource(R.drawable.basic_mode_blue_circle);
                                }

                                else if (70 <= final_can && 90 >= final_can && driving_timer == 1) {
                                    if (b_bpm_b < 68)
                                    {
                                        if (ldw == 0){
                                            user_icon.setImageResource(R.drawable.sleep_mode_level_ldw);
                                        }
                                        else
                                            user_icon.setImageResource(R.drawable.sleep_mode_level4);
                                    }
                                    else
                                        user_icon.setImageResource(R.drawable.sleep_mode_level2);
                                }



                                /*
                                if (70 <= final_can && 90 >= final_can && driving_timer == 1 && ldw == 0) {
                                    user_icon.setImageResource(R.drawable.sleep_mode_level_ldw);
                                    System.out.println("third else if");
                                }

                                if (70 <= final_can && 90 >= final_can && driving_timer == 1 && b_bpm_b < 68 & ldw == 0) {
                                    user_icon.setImageResource(R.drawable.sleep_mode_level4);
                                }*/

                                /*
                                if (driving_timer == 1) {
                                    if (diff < 3) {
                                        user_icon.setImageResource(R.drawable.sleep_mode_level3);  //go to Level 3
                                        //System.out.println("++++++++ User Bio Level 3 ++++++++");
                                    }
                                    else {
                                        user_icon.setImageResource(R.drawable.sleep_mode_level2);  //go to Level 3
                                        //System.out.println("++++++++ User Bio Level 2 ++++++++");
                                    }
                                }

                                else if (driving_timer == 1) {
                                        if(DeviceControlActivity.ldw == 0) {
                                            user_icon.setImageResource(R.drawable.sleep_mode_level4);  //go to Level 4
                                            //System.out.println("++++++++ User Bio Level 4 ++++++++");
                                    }
                                }

                                else {
                                    user_icon.setImageResource(R.drawable.basic_mode_blue_circle);  //go to Level 1
                                    //System.out.println("++++++++ User Bio Level 1 ++++++++");
                                }
                                */

                            }
                        });
                    }
                }).start();
            }
        };
        timer = new Timer();
        timer.schedule(myTask, 0, 500); // 0초후 첫실행, 0.1초마다 계속실행

    }


    //TODO BLE Packet receive
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.d("test", "onDstory()");
        timer.cancel();
        super.onDestroy();
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    public void displayData(byte[] packet) {

        getStringPacket(DeviceControlActivity.packet);

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

        int [] recv_sensors = new int [6];

        // TODO : GYRO X Y Z HEX
        b_gyro1 = hexString.substring(6, 10);
        b_gyro2 = hexString.substring(10, 14);
        b_gyro3 = hexString.substring(14, 18);

        // TODO : ACC X Y Z HEX
        b_acc1 = hexString.substring(18, 22);
        b_acc2 = hexString.substring(22, 26);
        b_acc3 = hexString.substring(26, 30);

        // TODO : ACC X Y Z Decimal
        b_ac_x = (short) Integer.parseInt(b_acc1, 16);
        b_ac_y = (short) Integer.parseInt(b_acc2, 16);
        b_ac_z = (short) Integer.parseInt(b_acc3, 16);

        // TODO : GYRO X Y Z Decimal
        b_gy_x = (short) Integer.parseInt(b_gyro1, 16);
        b_gy_y = (short) Integer.parseInt(b_gyro2, 16);
        b_gy_z = (short) Integer.parseInt(b_gyro3, 16);

        //TODO : Band BPM information
        b_sleep_bpm = hexString.substring(32, 34);
        b_discount_bpm = hexString.substring(34, 36);
        /*
        System.out.println("Partron band [Gesture + Sleep info] ->" + hexString );
        System.out.println("Partron band Fix BPM ->" +b_sleep_bpm );
        System.out.println("Partron band Discount BPM ->" +b_discount_bpm );
        */
        // TODO : BAND BPM info Decimal
        b_bpm_a = (short) Integer.parseInt(b_sleep_bpm, 16);
        b_bpm_b = (short) Integer.parseInt(b_discount_bpm, 16);

        diff = b_bpm_b - b_bpm_a;

        recv_sensors [0] = b_ac_x;
        recv_sensors [1] = b_ac_y;
        recv_sensors [2] = b_ac_z;

        recv_sensors [3] = b_gy_x;
        recv_sensors [4] = b_gy_x;
        recv_sensors [5] = b_gy_x;

        StringBuilder sb = new StringBuilder(packet.length * 2);

        int result_ = asc.SlidingCollectionInterface (recv_sensors);
        if (result_ != DUMP) {
            Log.d (TAG, "In IF branch");

            asc.gesture_count ++;

            switch (result_)	{

                case LEFT:
                    System.out.println("+++++++++++++++++ LEFT +++++++++++++++++");
                    break;

                case RIGHT:
                    System.out.println("+++++++++++++++++ RIGHT +++++++++++++++++");
                    break;

                case FRONT:
                    System.out.println("+++++++++++++++++ FRONT +++++++++++++++++");
                    break;

                case UP:
                    System.out.println("+++++++++++++++++ UP +++++++++++++++++");
                    break;

                case CLOCK:
                    System.out.println("+++++++++++++++++ CLOCK +++++++++++++++++");
                    break;
                case ANTI_CLOCK:
                    System.out.println("+++++++++++++++++ ANTI CLOCK +++++++++++++++++");
                    break;

                default:
            }

        }

        return sb.toString();
    }

    private void driving_time ()	{
        Runtime runtime1 = Runtime.getRuntime();
        Process process1;

        try
        {
            String cmd = "./driving_time.sh";
            process1 = runtime1.exec(cmd);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(process1.getInputStream()));
            String line1 = "";
            if ((line1 = br.readLine()) != null)
            {
                String b_data_1 = line1.substring(19, 21);      // normal hex data
                int b_hex1 = Integer.parseInt(b_data_1, 16);    // normal int data

                driving_timer = b_hex1;  // 533#00 , 533#01 <- door state
                //mn_door_close = b_hex2;   // 533#01.00 , 533#01.01 <- open, close state

            }

            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

                bio_ldw = b_hex11;

            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
