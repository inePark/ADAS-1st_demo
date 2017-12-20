package keti.gto.android.bluetoothlegatt.ble_folder;


import java.util.Arrays;
import android.util.Log;

public class GestureRecognition {


    GestureDataBase DB = new GestureDataBase();

    private int [][] WMA_SENSOR = new int [AXIS_NUM][WMA_SAMPLE_NUM];
    private int [][] SENSOR_tmep =  new int [AXIS_NUM][SAMPLE_NUM];

    private int [][] WMA_temp = new int [AXIS_NUM][WMA_SAMPLE_NUM];

    private byte [] WEIGHTED_AXIS = new byte [AXIS_NUM];
    private byte Motionflag_ = 0;
    public int [] gaps = {0,0,0,0,0,0};
    private int [] biggestValue;
    private int result_;
    public byte band_flag = 0;
    public boolean band_leaned = false;


    public void setBandFlag (byte band)   {
        this.band_flag = band;
    }
    private void setBandLeaned (boolean band)   {
        this.band_leaned = band;
    }


    ///////////////////////////////////////////////////////////////////////
    public int gestureRecogInterface (int [][] SENSOR, boolean band_leaned_flag)	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        result_ = UNKNOWN_;
        biggestValue = new int [GESTURE_NUM];
        int nLoop, nLoop2;

        setBandLeaned (band_leaned_flag);

        //GestureDB gesture;
        normalizingFunction(SENSOR);

        weightedMovingAverage(SENSOR_tmep);

        StringBuilder sb = new StringBuilder();
        sb.append ("DATA\n");
        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{
                //    Log.d(TAG, "[" + nLoop + "][" +nLoop2 + "] " + SENSOR [nLoop][nLoop2]);
                sb.append(WMA_SENSOR [nLoop][nLoop2]);
                sb.append (" ");
            }
            sb.append ("\n");
        }
        Log.d(TAG, sb.toString());

        validAxiesClassifier();

        result_ = groupClassifier();

        switch (result_)	{
            case DUMP:
                Log.d (TAG,"-------------------- DUMP--\n"); break;
            case LEFT:
                Log.i (TAG," <<<< LEFT <<< \n");
                break;
            case RIGHT:
                Log.i (TAG," >>>> RIGHT >>\n");
                break;
            case FRONT:
                Log.i (TAG,"[[[ FFRONT ]\n");
                break;
            case BACK:
            case UP:
                Log.i (TAG," ^^^^ UP\n");
                break;
            /*case UP:
            case DOWN:
                result_ = UP;
                Log.i (TAG," ^^^^ UP\n");
                break;*/
            case CLOCK:
            case LOW_ANTI:
            case LOW_CLOCK:
                Log.i (TAG," **** CLOCK\n");
                break;
            case ANTI_CLOCK:
                Log.i (TAG," **** ANTI-CLOCK\n");
                break;
            case UNKNOWN_:
                Log.i (TAG," CAN'T DETECT\n"); 	break;
            default:
                Log.i (TAG," ELSE \n"); 	break;

        }	//switch



        return result_;
    } //function gestureRecogInterface



    ///////////////////////////////////////////////////////////////////////
    private void normalizingFunction (int [][] sensor){
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2;

        //Arrays.fill(SENSOR_tmep, 0);

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < SAMPLE_NUM; nLoop2 ++)	{

                SENSOR_tmep [nLoop][nLoop2] = (int)(((double)sensor [nLoop][nLoop2] / NORMALIZATION_FACTOR) * 1000);
                //Log.d(TAG, "["+nLoop+"/"+nLoop2+"]"+SENSOR_tmep [nLoop][nLoop2]);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////
    private void weightedMovingAverage (int [][] SENSOR)	{
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2, nLoop3;
        int temp_avr;

        // Arrays.fill(WMA_SENSOR, 0);

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{
                temp_avr = 0;
                for (nLoop3 = 0; nLoop3 < WMA_FACTOR; nLoop3 ++)	{
                    temp_avr +=  SENSOR [nLoop][nLoop2 + nLoop3];
                } //for nLoop3

                WMA_SENSOR [nLoop][nLoop2] = temp_avr / WMA_FACTOR;
            } //for nLoop2
        } //for nLoop
    }



    ///////////////////////////////////////////////////////////////////////
    private void validAxiesClassifier ()	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        int [] min_ = {10000,10000,30000, 10000, 10000, 10000};
        int [] max_ = {-10000, -10000,-1000, -10000, -10000,-1000};
        int [] minFlag = {0,0,0,0,0,0};
        int [] maxFlag = {0,0,0,0,0,0};
        int nLoop, nLoop2;

        Motionflag_ = 0;

        char temp_flag = 0;
        for (nLoop = ACC_X; nLoop <= ACC_Z; nLoop++ )	{

            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{

                if (min_[nLoop] > WMA_SENSOR [nLoop][nLoop2])	{
                    min_[nLoop] = WMA_SENSOR [nLoop][nLoop2];
                    minFlag [nLoop] |= 0x1;
                } else if (max_[nLoop] < WMA_SENSOR [nLoop][nLoop2])	{
                    max_[nLoop] = WMA_SENSOR [nLoop][nLoop2];
                    maxFlag [nLoop] |= 0x1;
                }
                minFlag[nLoop] = minFlag[nLoop] << 1;
                maxFlag[nLoop] = maxFlag[nLoop] << 1;

            } //for nLoop2

            gaps [nLoop] = max_ [nLoop] - min_ [nLoop];

           // Log.d (TAG, "GAP is "+gaps [0]+ " "+gaps [1]+ " "+gaps [2]+ " ");

            if (gaps [nLoop] > GAP_THRESHOLD)	{
                //Log.d (TAG, "GAP TH over: " + nLoop);
                Motionflag_ |= 0x1;
                temp_flag ++;
                WEIGHTED_AXIS [nLoop] = 90;

            }

            Motionflag_ = (byte) (Motionflag_ << 1);

        } //for nLoop
        if (temp_flag == 3)	{
            int nLoop3 = 0, temp_min=2000, order = 0;
            for (nLoop3=0; nLoop3 < 3; nLoop3 ++ )	{

                if (gaps [nLoop3] < temp_min)	{
                    order = nLoop3;
                    temp_min = gaps [nLoop3];
                }

            }	//for nLoop3


            switch (order)	{
                case 0:
                    Motionflag_ = 0x6;
                    break;
                case 1:
                    Motionflag_ = 0xA;
                    WEIGHTED_AXIS [order] = 10;
                    temp_flag --;
                    break;
                case 2:
                    Motionflag_ = 0xC;
                    WEIGHTED_AXIS [order] = 10;
                    temp_flag --;
                    break;

            }	//switch
        }	//if

        if (temp_flag != 0) {
            for (nLoop = ACC_X; nLoop < AXIS_NUM; nLoop++) {
                WEIGHTED_AXIS[nLoop] /= temp_flag;
            }
        }

    }	//function validAxiesClassifier


    ///////////////////////////////////////////////////////////////////////
    private int groupClassifier ()	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        result_ = DUMP;
        int nLoop;

        // Arrays.fill(biggestValue, 0);


        if (Motionflag_ == 0)	{
            result_ = DUMP;
        }
        else {
            int temp_result=0, max=0;
            switch (Motionflag_ & 0x0E)	{
                case 0x0:
                    result_ = DUMP;
                    break;
                case 0x8:	//FRONT
                    // 3rd year's havd : 0x08
                    // 2rd year's havd : 0x04
                    Log.d(TAG, "flag is 4");
                    result_ = FRONT;
                    break;

                case 0x2:	//UP
                    Log.d(TAG, "flag is 2");

                    if (band_leaned == false) {
                        result_ = UP;
//                    result_ = assignDB_FRONT ();
                    }
                    else {
                        result_ = RIGHT;
                        result_ = assignDB_RIGHT ();
                        result_ = FRONT;
                        result_ = assignDB_FRONT ();

                        for (nLoop = FRONT; nLoop <= UP; nLoop ++)	{
                            //Log.d (TAG, "Value is " + biggestValue [nLoop]);

                            if (max < biggestValue [nLoop])	{
                                max = biggestValue [nLoop];
                                result_ = nLoop;
                            }
                        }
                    }
                    break;
                case 0x4:		//SIDE
                    // 3rd year's havd : 0x04
                    // 2rd year's havd : 0x08
                    Log.d(TAG, "flag is 8");

                    if (band_leaned == false) {
                        result_ = RIGHT;
                        result_ = assignDB_RIGHT();
                        result_ = FRONT;
                        result_ = assignDB_FRONT();

                        for (nLoop = FRONT; nLoop <= UP; nLoop++) {
                            //Log.d(TAG, "Value is " + biggestValue[nLoop]);

                            if (max < biggestValue[nLoop]) {
                                max = biggestValue[nLoop];
                                result_ = nLoop;
                            }
                        }
                    }
                    else
                        result_ = UP;
                    break;

                case 0xA:
                    Log.d(TAG, "flag is A");

                    if (gaps[0] + gaps[2] < 1700)	{
                        result_ = CLOCK;
                        temp_result = assignDB_CLOCK ();
                        result_ = FRONT;
                        temp_result = assignDB_FRONT ();
                        result_ = RIGHT;
                        temp_result = assignDB_RIGHT ();

                        for (nLoop = FRONT; nLoop < GESTURE_NUM; nLoop ++)	{
                            //Log.d (TAG, "Value is" + biggestValue [nLoop]);

                            if (max < biggestValue [nLoop])	{
                                max = biggestValue [nLoop];
                                result_ = nLoop;
                            }
                        }
                    }
                    else	{
                        result_ = CLOCK;
                        result_ = assignDB_CLOCK ();
                    }

                    break;

                case 0xC:
                    Log.d(TAG, "flag is C");

                    result_ = FRONT;
                    temp_result  = assignDB_FRONT ();
                    result_ = RIGHT;
                    temp_result  = assignDB_RIGHT ();

                    for (nLoop = FRONT; nLoop < LEFT+1; nLoop ++)	{
                        //Log.d (TAG, "Value is" + biggestValue [nLoop]);

                        if (max < biggestValue [nLoop])	{
                            max = biggestValue [nLoop];
                            result_ = nLoop;
                        } //if
                    }
                    break;

                default:

                    result_ = CLOCK;
                    temp_result = assignDB_CLOCK ();
                    result_ = RIGHT;
                    temp_result = assignDB_RIGHT ();
                    result_ = FRONT;
                    temp_result = assignDB_FRONT ();

                    for (nLoop = FRONT; nLoop < GESTURE_NUM; nLoop ++)	{
                        //Log.d (TAG, "Value is" + biggestValue [nLoop]);
                        if (max < biggestValue [nLoop])	{
                            max = biggestValue [nLoop];
                            result_ = nLoop;
                        } //if
                    }

                    Log.d(TAG, "flag is else :"+ result_);

                    break;

            } //switch
        }	//else

        return result_;
    }	//function groupClassifier






    ///////////////////////////////////////////////////////////////////////
    private int assignDB_FRONT ()	{
        ///////////////////////////////////////////////////////////////////////

        switch (band_flag)  {
            case H_BAND:
                if (crossCorrelation(DB.H_BAND_FRONT_UP, DB.H_BAND_FRONT_UP_AVR, DB.H_BAND_FRONT_UP_COV) == 1)
                    result_ = BACK;
                else
                    result_ = FRONT;
                break;
            case PARTRON:
                if (crossCorrelation(DB.PARTRON_FRONT_UP, DB.PARTRON_FRONT_UP_AVR, DB.PARTRON_FRONT_UP_COV) == 1)
                    result_ = BACK;
                else
                    result_ = FRONT;
                break;
        }

        return result_;
    }

    ///////////////////////////////////////////////////////////////////////
    private int assignDB_RIGHT ()	{
        ///////////////////////////////////////////////////////////////////////
        switch (band_flag)  {
            case H_BAND:
                if (band_leaned) {
                    if (crossCorrelation(DB.H_BAND_LEFT_RIGHT_LEANED, DB.H_BAND_LEFT_RIGHT_LEANED_AVR, DB.H_BAND_LEFT_RIGHT_LEANED_COV) == 1)
                        result_ = BACK;
                    else
                        result_ = FRONT;
                }
                else    {
                    if (crossCorrelation(DB.H_BAND_LEFT_RIGHT, DB.H_BAND_LEFT_RIGHT_AVR, DB.H_BAND_LEFT_RIGHT_COV) == 1)
                        result_ = BACK;
                    else
                        result_ = FRONT;
                }
                break;
            case PARTRON:
                if (crossCorrelation(DB.PARTRON_LEFT_RIGHT, DB.PARTRON_LEFT_RIGHT_AVR, DB.PARTRON_LEFT_RIGHT_COV) == 1)
                    result_ = BACK;
                else
                    result_ = FRONT;
                break;
        }
        return result_;
    }
    ///////////////////////////////////////////////////////////////////////
    private int assignDB_CLOCK ()	{
        ///////////////////////////////////////////////////////////////////////

        switch (band_flag)  {
            case H_BAND:
                if (crossCorrelation(DB.H_BAND_CLOCK_ANTI, DB.H_BAND_CLOCK_ANTI_AVR, DB.H_BAND_CLOCK_ANTI_COV) == 1)
                    result_ = BACK;
                else
                    result_ = FRONT;
                break;
            case PARTRON:
                if (crossCorrelation(DB.PARTRON_CLOCK_ANTI, DB.PARTRON_CLOCK_ANTI_AVR, DB.PARTRON_CLOCK_ANTI_COV) == 1)
                    result_ = BACK;
                else
                    result_ = FRONT;
                break;
        }
        return result_;

    }




    ///////////////////////////////////////////////////////////////////////
    private int crossCorrelation (double [][][] db_sample, double [][] db_average, double [][] db_cov)	{
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2;

        // Arrays.fill(WMA_temp, 0);

        for (nLoop = -CROSS_FACTOR; nLoop < 1; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM + nLoop ; nLoop2 ++)	{
                WMA_temp [0][nLoop2] = WMA_SENSOR [0][-nLoop+ nLoop2];
                WMA_temp [1][nLoop2] = WMA_SENSOR [1][-nLoop+ nLoop2];
                WMA_temp [2][nLoop2] = WMA_SENSOR [2][-nLoop+ nLoop2];

            }
            compareOtherCorrelation (WMA_SAMPLE_NUM + nLoop, nLoop, db_sample, db_average, db_cov);
            // Arrays.fill(WMA_temp, 0);

        }
        for (nLoop = 1; nLoop < CROSS_FACTOR; nLoop ++)	{

            compareOtherCorrelation (WMA_SAMPLE_NUM, nLoop, db_sample, db_average, db_cov);
        }

        //printf ("%d vs. %d  ",  biggestValue [0], biggestValue [1]);

        return biggestValue [result_] > biggestValue [result_ + 1] ? 0 : 1;

    }



    ///////////////////////////////////////////////////////////////////////
    private void compareOtherCorrelation (int arrayLen, int flag_, double [][][] db_sample, double [][] db_average, double [][] db_cov)	{
        ///////////////////////////////////////////////////////////////////////

        int [][] WMA_SENSOR_local = new int [AXIS_NUM][WMA_SAMPLE_NUM];
        int nLoopLen = 0, nLoopStart = 0;//, db_start = 0, db_end = 0;
        double [] average = {0, 0, 0};
        int nLoop, nLoop2, nLoop3;
        double [][] cov =  {{0,0,0}, {0,0,0}};
        double [] cov_self = {0, 0, 0};
        double [][] corr =  {{0,0,0}, {0,0,0}};
        double [][] corr_int_local  = {{0,0,0}, {0,0,0}};

        //  Arrays.fill(WMA_SENSOR_local, 0);

        if (flag_ < 1)	{	//array copy to
            nLoopLen = arrayLen;
            nLoopStart = 0;
            WMA_SENSOR_local = WMA_temp.clone();
            //memcpy (WMA_SENSOR_local, WMA_temp, sizeof(WMA_SENSOR_local));
        }
        else	{
            nLoopLen = WMA_SAMPLE_NUM;
            nLoopStart = flag_;
            WMA_SENSOR_local = WMA_SENSOR.clone();
            //memcpy (WMA_SENSOR_local, WMA_SENSOR, izeof(WMA_SENSOR_local));
        }

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{	//average of incoming sensor value
            for (nLoop2 = 0; nLoop2 < nLoopLen ; nLoop2 ++)	{
                average [nLoop] += WMA_SENSOR_local [nLoop][nLoop2];
            } //for nLoop2

            average [nLoop] /= nLoopLen;

        } //for nLoop

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            double temp_double=0;
            for (nLoop2 = nLoopStart; nLoop2 < nLoopLen; nLoop2 ++)	{ //co-variance //분자계산
                temp_double = WMA_SENSOR_local [nLoop][nLoop2 - nLoopStart] - average [nLoop];

                for (nLoop3 = 0; nLoop3 < NUM_OF_DB; nLoop3 ++)
                    cov [nLoop3][nLoop] += (temp_double * (db_sample [nLoop3][nLoop][nLoop2] - db_average [nLoop3][nLoop]));


                cov_self [nLoop] += (temp_double * temp_double);
            } //for nLoop2

            for (nLoop3 = 0; nLoop3 < NUM_OF_DB; nLoop3 ++)
                cov [nLoop3][nLoop] /= (nLoopLen - nLoopStart);

            cov_self [nLoop] /= (nLoopLen - nLoopStart);

        } // for nLoop

        int [] tempSUM  = {0,0};

        for (nLoop = 0; nLoop <= 1; nLoop ++)	{	//correlation. //분모 계산

            for (nLoop2 = 0; nLoop2 < AXIS_NUM; nLoop2 ++)	{


                if (cov_self [nLoop2] * cov [nLoop][nLoop2] != 0)	{
                    corr [nLoop][nLoop2] = 0;
                    corr [nLoop][nLoop2] = cov [nLoop][nLoop2] / (mysqrt ((cov_self [nLoop2] * db_cov [nLoop][nLoop2])* 10000) / 100);
                }
                else
                    corr [nLoop][nLoop2] = 0;

                corr_int_local [nLoop][nLoop2] = 0;
                corr_int_local [nLoop][nLoop2] = (int)(corr [nLoop][nLoop2] *100);

                tempSUM [nLoop] += ((int)(corr_int_local [nLoop][nLoop2] * WEIGHTED_AXIS [nLoop2]) / 100);

            } //for nLoop2

            if (tempSUM[nLoop] < 20)
                tempSUM[nLoop] = 0;
            else
                tempSUM[nLoop] *= tempSUM[nLoop];


        } //for nLoop


        //------------------------------------------------ deviation --------------
        int [][] gap_mean = {{0,0,0}, {0,0,0}};
        int [][] gap_deviation = {{0,0,0}, {0,0,0}};

        int [] tempSUM_dev = {0,0};

        for (nLoop = 0; nLoop < NUM_OF_DB; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < AXIS_NUM; nLoop2 ++){
                for (nLoop3 = nLoopStart; nLoop3 < nLoopLen; nLoop3 ++){
                    gap_mean [nLoop][nLoop2] += Math.abs( (int)(db_sample [nLoop][nLoop2][nLoop3] * 1000) - (int) WMA_SENSOR_local[nLoop2][nLoop3]);
                } //for nLoop3
                gap_mean [nLoop][nLoop2] /= nLoopLen;

                for (nLoop3 = nLoopStart; nLoop3 < nLoopLen; nLoop3 ++){
                    gap_deviation [nLoop][nLoop2] += Math.abs( (int)(db_sample [nLoop][nLoop2][nLoop3] * 1000) - (int)WMA_SENSOR_local[nLoop2][nLoop3] - (int)gap_mean [nLoop][nLoop2] );
                } //for nLoop3

                gap_deviation [nLoop][nLoop2] /= nLoopLen;

                tempSUM_dev [nLoop] +=  gap_deviation [nLoop][nLoop2];

            } //for nLoop2

        } //for nLoop

        int temp_int;
        for (nLoop = 0; nLoop < NUM_OF_DB; nLoop ++)	{
            if (tempSUM [nLoop] > 0)	{
                temp_int = (int)((tempSUM [nLoop] * 1000) / tempSUM_dev [nLoop]);
                //temp_int = tempSUM[nLoop];
            }
            else	{
                temp_int = 0;
            } //else

            if(biggestValue [result_ + nLoop] < temp_int)
                biggestValue [result_ + nLoop] = temp_int;

        } //for nLoop

    }






    ///////////////////////////////////////////////////////////////////////
    double mysqrt(double d)
    ///////////////////////////////////////////////////////////////////////
    {
        int NUM_REPEAT = 16;
        int k;
        double t;
        double buf = (double)d;
        for(k=0,t=buf;k<NUM_REPEAT;k++)
        {
            if(t<1.0)
                break;
            t = (t*t+buf)/(2.0*t);
        }
        return t;
    }



    ///////////////////////////////////////////////////////////////////////
    //static parameters
    ///////////////////////////////////////////////////////////////////////
    private static final String TAG = "GestureRecognition";
    private static final int GAP_THRESHOLD = 300;

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
    public static final int DUMP = -1;
    public static final int UNKNOWN_ = 99;

    public static final int GESTURE_NUM = LOW_ANTI + 1;	//10

    public static final int AXIS_NUM = 3;
    public static final int SAMPLE_NUM = 20;

    private static final int WMA_FACTOR = 3;
    private static final int WMA_SAMPLE_NUM = SAMPLE_NUM - WMA_FACTOR + 1;
    private static final int NORMALIZATION_FACTOR = 32767;
    private static final int NUM_OF_DB = 2;

    private static final int CROSS_FACTOR = 3;

    public static final int ACC_X = 0;
    public static final int ACC_Y = 1;
    public static final int ACC_Z = 2;
    /*public static final int GY_X = 3;
    public static final int GY_Y = 4;
    public static final int GY_Z = 5;
    */


    public static final byte H_BAND = 0;
    public static final byte PARTRON = 1;
    public static final byte UCOMM = 2;




}
