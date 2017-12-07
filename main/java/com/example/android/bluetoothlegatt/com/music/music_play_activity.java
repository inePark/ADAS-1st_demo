/*
 * Copyright 2017 The Android AVN Project
 *
 *      Korea Electronics Technology Institute
 *
 *      http://keti.re.kr/
 *
 */
package com.example.android.bluetoothlegatt.com.music;

import android.app.Activity;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.com.AccSlidingCollection;
import com.example.android.bluetoothlegatt.com.BluetoothLeService;
import com.example.android.bluetoothlegatt.com.DeviceControlActivity;

import static android.content.ContentValues.TAG;

/**
 * Created by GTO on 2017-08-15.
 */

public class music_play_activity extends Activity{

    private String videoname;
    int streamType = 0;
    AccSlidingCollection asc = new AccSlidingCollection();
    public static final int DUMP = -1;

    //TODO Gesture sensing variable
    public static String b_length;
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

    public static int count;

    ImageButton btn_play, btn_stop, btn_pause, btn_volumeUp, btn_volumeDn, btn_exit;

    SeekBar seekbar;
    MediaPlayer music;
    MediaPlayer music_wjsn;
    MediaPlayer music_lvlz;

    int video_num = 0;
    ImageView imgView;
    TextView tvTitle;
    TextView tvArtist;
    private Activity activity;

    public static music_play_activity act3;

    //TODO BLE HEX data receive
    private boolean mConnected = false;
    public static byte[] packet;
    private TextView mConnectionState;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;

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

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_play);

        music = MediaPlayer.create(this, R.raw.twice_signal);
        music_wjsn = MediaPlayer.create(this, R.raw.apink_five);
        music_lvlz = MediaPlayer.create(this, R.raw.girlfriend_lovewhisper);

        music.setLooping(true);
        music_wjsn.setLooping(true);
        music_lvlz.setLooping(true);

        seekbar = (SeekBar) findViewById(R.id.seekBar1);
        seekbar.setMax(music.getDuration());
        seekbar.setMax(music_wjsn.getDuration());
        seekbar.setMax(music_lvlz.getDuration());

        imgView = (ImageView) findViewById(R.id.videoView);
        tvTitle = (TextView) findViewById(R.id.title);
        tvArtist = (TextView) findViewById(R.id.Artist);

        System.gc();
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        videoname = extras.getString("video");
        video_num = extras.getInt("video_num");

        act3 = music_play_activity.this;


        if (video_num == 1) {
            imgView.setImageResource(R.drawable.gfriend);
            tvTitle.setText("Title : AVE MARIA(두손을모아)");
            tvArtist.setText("Artists : GIRL FRIEND");
        }

        else if (video_num == 2) {
            imgView.setImageResource(R.drawable.twice);
            tvTitle.setText("Title : Signal");
            tvArtist.setText("Artists : TWICE");
        }
        else if (video_num == 3) {
            imgView.setImageResource(R.drawable.gfriend);
            tvTitle.setText("Title : Holiday Night");
            tvArtist.setText("Artists : Girl's Generation(소녀시대)");
        }

        if (video_num == 0) {
            Intent intent1 = new Intent(music_play_activity.this, music_main_activity.class);
            //ANTI_intent.addFlags(ANTI_intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent1);
            Toast.makeText(getApplicationContext(), "---- 종료 종료 종료 ---- ",Toast.LENGTH_SHORT).show();
            System.out.println("--- 좋료 종료 종료 ---");

        }

        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

                if(fromUser)
                    //music_lvlz.seekTo(progress);
                    music.seekTo(progress);
                //music_wjsn.seekTo(progress);
                //music_lvlz.seekTo(progress);
                if(fromUser)
                    music_wjsn.seekTo(progress);
                if(fromUser)
                    music_lvlz.seekTo(progress);
            }

        });

        btn_play = (ImageButton) findViewById(R.id.play);
        btn_stop = (ImageButton) findViewById(R.id.stop);
        btn_pause = (ImageButton) findViewById(R.id.pause);
        //btn_volumeUp = (ImageButton) findViewById(R.id.volumeUp);
        //btn_volumeDn = (ImageButton) findViewById(R.id.volumeDn);
        btn_exit = (ImageButton) findViewById(R.id.exit);

        btn_exit.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("Exit Btn Call");
                Intent backPress = new Intent(music_play_activity.this,
                        DeviceControlActivity.class);
                startActivity(backPress);

                finish();
				/*
				--BluetoothChat.i;
				--BluetoothChat.j;

				System.out.println("Click count i : " + BluetoothChat.i);
				System.out.println("Click count j : " + BluetoothChat.j);
				*/
            }
        });
        /*
        btn_volumeUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Runtime runtime1 = Runtime.getRuntime();
                Process process1;
                String res = "input keyevent 24";
                try {

                    process1 = runtime1.exec(res); //2번 실행해야 되는 경우가 있음
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e("Process Manager", "Unable to execute top command");
                }
            }
        });

        btn_volumeDn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Runtime runtime1 = Runtime.getRuntime();
                Process process1;
                String res = "input keyevent 25";
                try {

                    process1 = runtime1.exec(res); //2번 실행해야 되는 경우가 있음
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e("Process Manager", "Unable to execute top command");
                }
            }
        });
        */

        if (video_num == 1) {
            btn_pause.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music.pause();
                }
            });
        }

        if (video_num == 2) {
            btn_pause.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music_wjsn.pause();
                }
            });
        }

        if (video_num == 3) {
            btn_pause.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music_lvlz.pause();
                }
            });
        }

        if (video_num == 1) {
            btn_play.setOnClickListener(new OnClickListener() {
                //if (video_num == 1)
                @Override
                public void onClick(View v)
                {
                    music.start();

                    Thread();
                }

            });
        }

        if (video_num == 2) {
            btn_play.setOnClickListener(new OnClickListener() {
                //if (video_num == 1)
                @Override
                public void onClick(View v)
                {
                    music_wjsn.start();

                    Thread();
                }

            });
        }

        if (video_num == 3) {
            btn_play.setOnClickListener(new OnClickListener() {
                //if (video_num == 1)
                @Override
                public void onClick(View v)
                {
                    music_lvlz.start();

                    Thread();
                }

            });
        }

        if (video_num == 1) {
            btn_stop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music.stop();
                    try {
                        // 음악을 재생할경우를 대비해 준비합니다
                        // prepare()은 예외가 2가지나 필요합니다
                        music.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 음악 진행 정도를 0, 즉 처음으로 되돌립니다
                    music.seekTo(0);
                    seekbar.setProgress(0);
                }
            });
        }

        if (video_num == 2) {
            btn_stop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music_wjsn.stop();
                    try {
                        // 음악을 재생할경우를 대비해 준비합니다
                        // prepare()은 예외가 2가지나 필요합니다
                        music_wjsn.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 음악 진행 정도를 0, 즉 처음으로 되돌립니다
                    music_wjsn.seekTo(0);
                    seekbar.setProgress(0);
                }
            });
        }

        if (video_num == 3) {
            btn_stop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    music_lvlz.stop();
                    try {
                        // 음악을 재생할경우를 대비해 준비합니다
                        // prepare()은 예외가 2가지나 필요합니다
                        music_lvlz.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 음악 진행 정도를 0, 즉 처음으로 되돌립니다
                    music_lvlz.seekTo(0);
                    seekbar.setProgress(0);
                }
            });
        }
    }

    public void Thread(){
        Runnable task = new Runnable(){
            public void run(){

                while(music.isPlaying()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    seekbar.setProgress(music.getCurrentPosition());
                }

                while(music_wjsn.isPlaying()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    seekbar.setProgress(music_wjsn.getCurrentPosition());
                }

                while(music_lvlz.isPlaying()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    seekbar.setProgress(music_lvlz.getCurrentPosition());
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
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
            //sb.append(String.valueOf(asc.gesture_count));
            //sb.append(" \t ");

            switch (result_)	{

                case LEFT:
                    System.out.println("+++++++++++++++++ LEFT +++++++++++++++++");

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

                    break;

                case RIGHT:
                    //sb.append(" <<<< SIDE >>>> \n");
                    System.out.println("+++++++++++++++++ RIGHT +++++++++++++++++");

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

                    break;

                case FRONT:
                    //sb.append ("[[[ FRONT ]\n");
                    System.out.println("+++++++++++++++++ FRONT +++++++++++++++++");

                    Runtime runtime2 = Runtime.getRuntime();
                    Process process2;
                    String res2 = "input keyevent 66";
                    try {

                        process1 = runtime2.exec(res2); //2번 실행해야 되는 경우가 있음
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e("Process Manager", "Unable to execute top command");
                    }

                    break;

                case UP:
                    //sb.append (" ^^^^ UP\n");
                    System.out.println("+++++++++++++++++ UP +++++++++++++++++");

                    Runtime runtime3 = Runtime.getRuntime();
                    Process process3;
                    String res3 = "input keyevent 4";
                    try {

                        process1 = runtime3.exec(res3); //2번 실행해야 되는 경우가 있음
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e("Process Manager", "Unable to execute top command");
                    }

                    break;

                case CLOCK:
                    //sb.append (" **** CLOCK\n");
                    System.out.println("+++++++++++++++++ CLOCK +++++++++++++++++");

                    Runtime runtime4 = Runtime.getRuntime();
                    Process process4;
                    String res4 = "input keyevent 24";
                    try {

                        process1 = runtime4.exec(res4); //2번 실행해야 되는 경우가 있음
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e("Process Manager", "Unable to execute top command");
                    }

                    break;
                case ANTI_CLOCK:
                    //sb.append (" **** ANTI CLOCK\n");
                    System.out.println("+++++++++++++++++ ANTI CLOCK +++++++++++++++++");

                    Runtime runtime5 = Runtime.getRuntime();
                    Process process5;
                    String res5 = "input keyevent 25";
                    try {

                        process1 = runtime5.exec(res5); //2번 실행해야 되는 경우가 있음
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e("Process Manager", "Unable to execute top command");
                    }

                    break;

                default:
                    //sb.append (" CAN'T DETECT\n"); 	break;

            }	//switch


            // sb.append(String.valueOf(result_));
            //sb.append("\n");
        }
        return sb.toString();
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
