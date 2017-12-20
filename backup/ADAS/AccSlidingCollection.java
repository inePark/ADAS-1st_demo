package com.example.android.bluetoothlegatt.com;

import android.util.Log;

public class AccSlidingCollection {
    public int gesture_count = 0;

    private int bax = X_INIT;
    private int bay = Y_INIT;
    private int baz = Z_INIT;

    private int X_CALM = X_INIT;
    private int Y_CALM = Y_INIT;
    private int Z_CALM = Z_INIT;


    private int brk_term = 0;
    private boolean processFlag = false;

    public GestureRecognition GR = new GestureRecognition();

    private int [][] average_buffer = { {X_INIT, X_INIT, X_INIT, X_INIT}, {Y_INIT, Y_INIT, Y_INIT, Y_INIT}, {Z_INIT, Z_INIT, Z_INIT, Z_INIT}};
    private int [][] sensors = new int [GR.AXIS_NUM][GR.SAMPLE_NUM];

    private int counter = 0;

    ///////////////////////////////////////////////////////////////////////
    public int SlidingCollectionInterface (int [] recv)	{
        ///////////////////////////////////////////////////////////////////////
        int result_ = -1;



        if (brk_term != 0)	{		//check moving term

            brk_term --;
            AverageCalculator (recv );
            return result_;
        }

        if (!processFlag  && ThresholdCheck(recv))	{	//hit the threshold
            processFlag = SaveSlidedElement ();
        } //if
        else
            AverageCalculator (recv );


        sensors [GR.ACC_X][counter] = recv [GR.ACC_X];
        sensors [GR.ACC_Y][counter] = recv [GR.ACC_Y];
        sensors [GR.ACC_Z][counter ++] = recv [GR.ACC_Z];



        if (counter == GR.SAMPLE_NUM)	{		//collect sample to SAMPLE_NUM
            counter = 0;

            if (processFlag)	{	// hit && collect
                processFlag = false;

                result_ = GR.gestureRecogInterface (sensors, getBandStatus());

                brk_term = BRAKE_TERM;
            } //if

            bax = X_CALM;	//set the calm state value
            bay = Y_CALM;
            baz = Z_CALM;

        } //if

        return result_;
    } //function SlidingCollectionInterface



    public boolean getBandStatus () {
      //  Log.d (TAG, "<<<<< RECV2 BAND is >>> " + band_status);
        return band_status;
    }

    byte inertial_cnt = 0;
    private void setBandStatus ()  {

        if ((X_CALM > 10000) && (Z_CALM > -10000))    {
            if (inertial_cnt > STATUS_THRESHOLD)
                band_status = LEAN;
            else  inertial_cnt ++;

        }
        else    {
            if (inertial_cnt > 0)
                inertial_cnt --;
            else
                band_status = STANDARD;

        }
    }







    ///////////////////////////////////////////////////////////////////////
    private boolean ThresholdCheck	(int [] recv)	{
        ///////////////////////////////////////////////////////////////////////
        boolean reutrn_ = false;

        if (((recv [GR.ACC_X] > X_CALM + MOVE_THRES) || (recv [GR.ACC_X] <  X_CALM - MOVE_THRES)) && ( Math.abs(Math.abs(recv [GR.ACC_X]) - Math.abs(bax)) > GAP_THRES))	{
            Log.d (TAG, "--------------------- X -------------- "+counter +" "+ Math.abs(Math.abs(recv [GR.ACC_X]) - Math.abs(bax)) + " " + bax);
            reutrn_ = true;
        }
        else if ((( recv [GR.ACC_Y] >  Y_CALM + MOVE_THRES) || (recv [GR.ACC_Y] < Y_CALM - MOVE_THRES)) && (Math.abs( Math.abs(recv [GR.ACC_Y]) - Math.abs(bay)) > GAP_THRES))	{
            Log.d (TAG, "--------------------- Y -------------- "+counter+" "+ Math.abs(Math.abs(recv [GR.ACC_Y]) - Math.abs(bay))+ " " + bay);
            reutrn_ = true;
        }
        else if (((recv [GR.ACC_Z] >  Z_CALM + MOVE_THRES) || (recv [GR.ACC_Z] < Z_CALM - MOVE_THRES)) && (Math.abs( Math.abs(recv [GR.ACC_Z]) - Math.abs(baz)) > GAP_THRES))	{
            Log.d (TAG, "--------------------- Z -------------- "+counter);
            reutrn_ = true;
        }

        return reutrn_;
    } // function ThresholdCheck



    ///////////////////////////////////////////////////////////////////////
    private boolean SaveSlidedElement ()	{
        ///////////////////////////////////////////////////////////////////////
        int nLoop, nLoop2;

        if (counter > BACKUP_SAMPLE)	{ 	//bigger than 5th sample
            for (nLoop = 0; nLoop < GR.AXIS_NUM; nLoop ++)	{
                for (nLoop2 = 0; nLoop2 < BACKUP_SAMPLE; nLoop2 ++)	{
                    sensors [nLoop][nLoop2] = sensors [nLoop][counter - BACKUP_SAMPLE + nLoop2];

                } //for nLoop2
            } //for nLoop
        } //else if

        else if (counter < BACKUP_SAMPLE)	{	//smaller than 5th sample

            for (nLoop = 0; nLoop < GR.AXIS_NUM; nLoop ++)	{

                for (nLoop2 = 1; nLoop2 <= counter; nLoop2 ++)	{
                    sensors [nLoop][BACKUP_SAMPLE - nLoop2] = sensors [nLoop][counter - nLoop2];
                } //for nLoop2
                int tempInt = BACKUP_SAMPLE - counter;
                for (nLoop2 = 0; nLoop2 < tempInt; nLoop2 ++)	{
                    sensors [nLoop][tempInt - nLoop2 - 1] = sensors [nLoop][GR.SAMPLE_NUM -1 - nLoop2];

                } //for nLoop2

            } //for nLoop
        } //else if



        counter  = BACKUP_SAMPLE;

        return true;
    } //function SaveSlidedElement

    private float X_ESTt = X_INIT;
    private float Y_ESTt = Y_INIT;
    private float Z_ESTt = Z_INIT;

    private float X_Eest = 3000;
    private float Y_Eest = 3000;
    private float Z_Eest = 3000;



    private void AverageCalculator (int [] recv) {
        int ITER_VALUE = BACKUP_SAMPLE - 1;
        for (int nLoop = GR.ACC_X; nLoop <  ITER_VALUE; nLoop ++)  {
            average_buffer [GR.ACC_X][nLoop] = average_buffer [GR.ACC_X][nLoop +1];
            average_buffer [GR.ACC_Y][nLoop] = average_buffer [GR.ACC_Y][nLoop +1];
            average_buffer [GR.ACC_Z][nLoop] = average_buffer [GR.ACC_Z][nLoop +1];
        }
        average_buffer [GR.ACC_X][ITER_VALUE] = recv [GR.ACC_X];
        average_buffer [GR.ACC_Y][ITER_VALUE] = recv [GR.ACC_Y];
        average_buffer [GR.ACC_Z][ITER_VALUE] = recv [GR.ACC_Z];


        X_CALM = Y_CALM = Z_CALM = 0;
        for (int nLoop = GR.ACC_X; nLoop <=  ITER_VALUE; nLoop ++)  {
            X_CALM += average_buffer [GR.ACC_X][nLoop];
            Y_CALM += average_buffer [GR.ACC_Y][nLoop];
            Z_CALM += average_buffer [GR.ACC_Z][nLoop];;
        }
        X_CALM /=  BACKUP_SAMPLE;
        Y_CALM /=  BACKUP_SAMPLE;
        Z_CALM /=  BACKUP_SAMPLE;

       // Log.d (TAG,  "AVERAGE: " + X_CALM + " " + Y_CALM + " "+ Z_CALM);

        setBandStatus ();


    }



    ///////////////////////////////////////////////////////////////////////
    //static parameters
    ///////////////////////////////////////////////////////////////////////

    private static final String TAG = "ASC";

    private static int X_INIT = 0;
    private static int Y_INIT = -1000;
    private static int Z_INIT = -16000;

    private static int MOVE_THRES = 3000;
    private static int GAP_THRES = 5000;

    private static int BRAKE_TERM = 20;
    private static int BACKUP_SAMPLE = 4;

    private static boolean STANDARD = false;
    private static boolean LEAN = true;

    private static int STATUS_THRESHOLD = 10;

    private boolean band_status = STANDARD;

}
