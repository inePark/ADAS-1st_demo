#include "KETI_stream_intp.h"
#include "KETI_header.h"

#define IN_DEBUG	1
//#undef IN_DEBUG
typedef struct 	{
	char temp1;
	char temp2;
} temp_cp_struct;

typedef union	{
	short temp_short;
	temp_cp_struct tcs;
} temp_cp_union;

short input_stream [MAX_NUM+1];
unsigned short data_num;
float size_plot [2];
float location_plot [2];
short arr_sensor1 [SENSOR_AXIS][DATA_MAX];
short arr_sensor2 [SENSOR_AXIS][DATA_MAX];

enum sensors_	{
	ACC_X,
	ACC_Y,
	ACC_Z,
	GY_X,
	GY_Y,
	GY_Z
};

int X_ [6];
int Y_ [6];
int Z_ [6];
//	int X2_ [6];
//	int Y2_ [6];
//	int Z2_ [6];
int GX1_[6];
int GY1_[6];
int GZ1_[6];
//int GX2_[6];
//int GY2_[6];
//int GZ2_[6];

//-----------------------------------------------------------
int check_validation (char* input, int num)	{
//-----------------------------------------------------------
//
// Check the protocol (start bit & end bit)
//
	if ((input_stream [0] != START_INT || input_stream [num-1] != END_INT))	{
		printf ("START bit or END bit ERROR!\n");
		return ERROR_;
	}
	data_num = (unsigned short)input_stream [1];
	if (data_num < 1)	{

		printf ("Data number error! Should be higher than 1.\n");
		return ERROR_;
	}

#ifdef DEBUG
	printf (" [%d]\n", data_num);
#endif
	return NORMAL;

}

//-----------------------------------------------------------
void file_out_function 	()	{
//-----------------------------------------------------------

static char triger  = 0;
char option [2] = {0,0};

//
//	-- file print for drowing graph with GNU
//
	if (triger ++ == 0)	{
		option [0] = 'w';
	}
#ifdef HALF_SAVER
	else if (triger % 2 == 1){
		return ;
	}
#endif
	else	{
		option [0] = 'a';
	}


	FILE* out1a = fopen (FNAME_S1_ACCEL, option);
	FILE* out1g = fopen (FNAME_S1_GYRO, option);
	FILE* out2a = fopen (FNAME_S2_ACCEL, option);
	FILE* out2g = fopen (FNAME_S2_GYRO, option);
	
	short nLoop, nLoop2;
	for (nLoop = 0; nLoop < data_num; nLoop ++)	{
			fprintf (out1a, "%d\t%d\t%d\n", 		//for sensor1-accelator data
					arr_sensor1 [0][nLoop], // x-axis 
					arr_sensor1 [1][nLoop], // y-axis
					arr_sensor1 [2][nLoop]); // z-axis
			
			fprintf (out1g, "%d\t%d\t%d\n", 		//for sensor1-gyro data
					arr_sensor1 [3][nLoop], // x-axis 
					arr_sensor1 [4][nLoop], // y-axis
					arr_sensor1 [5][nLoop]); // z-axis
	
			fprintf (out2a, "%d\t%d\t%d\n", 		//for sensor2-accelator data
					arr_sensor2 [0][nLoop], // x-axis 
					arr_sensor2 [1][nLoop], // y-axis
					arr_sensor2 [2][nLoop]); // z-axis
			
			fprintf (out2g, "%d\t%d\t%d\n", 		//for sensor2-gyro data
					arr_sensor2 [3][nLoop], // x-axis 
					arr_sensor2 [4][nLoop], // y-axis
					arr_sensor2 [5][nLoop]); // z-axis
	}
	fclose (out1a);
	fclose (out1g);
	fclose (out2a);
	fclose (out2g);
//	system ("cat sensor1_accel.dat");
//	printf ("=====\n");
}



// ----------------------------------------------------------- 
int motion_interpreter	()	{
// ----------------------------------------------------------- 
//
// Store the raw data(HEX) of BT to pre-defined array(INT)
//
	/* ---- 
	[0][N] : Nth acceleration x-axis
	[1][N] : Nth acceleration y-axis
	[2][N] : Nth acceleration z-axis
	[3][N] : Nth gyro x-axis
	[4][N] : Nth gyro y-axis
	[5][N] : Nth gyro z-axis
	----- */

//
// --- split below loop for code-optimization --//
//
	static char triger = FALSE;
	short nLoop;
	short nLoop2;
	for (nLoop = 0; nLoop < SENSOR_AXIS; nLoop ++)	{
		for (nLoop2 = 0; nLoop2 < data_num; nLoop2 ++){
			arr_sensor1 [nLoop][nLoop2] = input_stream [2+nLoop2+nLoop*data_num];
#ifdef DEBUG
			printf ("[%d.%d] %d\n", nLoop, nLoop2, arr_sensor1[nLoop][nLoop2]);
#endif
		
		}
	}
	arr_sensor1 [nLoop-1][nLoop2] = '\0';

	short s2_temp = 2 + data_num * SENSOR_AXIS;
	for (nLoop = 0; nLoop < SENSOR_AXIS; nLoop ++)	{
		for (nLoop2 = 0; nLoop2 < data_num; nLoop2 ++){
			arr_sensor2 [nLoop][nLoop2] = input_stream [s2_temp + nLoop2 + nLoop*data_num];
#ifdef DEBUG
			printf ("[%d.%d] %d\n", nLoop, nLoop2, arr_sensor2[nLoop][nLoop2]);
#endif
		}
	}
	arr_sensor2 [nLoop-1][nLoop2] = '\0';


	
	file_out_function ();

	

	return NORMAL;
}

char max_flag[6], min_flag[6];
int temp_max [6], temp_min [6];

void calc_max_min_position ()	{

	int nLoop = 0;

	for (nLoop = 0; nLoop < 6; nLoop ++)	{
		max_flag [nLoop] = -1;
		min_flag [nLoop] = -1;
		temp_max [nLoop] = 0x80000000;
		temp_min [nLoop] = 0x0FFFFFFF;
	}
	
	for (nLoop = 0; nLoop < 5; nLoop ++)	{		//fine peak point using Gyro Y-axis


		///////////// MIN
		if (temp_min[ACC_X] > X_ [nLoop])	{
			temp_min [ACC_X] = X_ [nLoop];
			min_flag [ACC_X] = nLoop;
		}
		if (temp_min[ACC_Y] > Y_ [nLoop])	{
			temp_min [ACC_Y] = Y_ [nLoop];
			min_flag [ACC_Y] = nLoop;
		}
		if (temp_min[ACC_Z] > Z_ [nLoop])	{
			temp_min [ACC_Z] = Z_ [nLoop];
			min_flag [ACC_Z] = nLoop;
		}
		if (temp_min[GY_X] > GX1_ [nLoop])	{
			temp_min [GY_X] = GX1_ [nLoop];
			min_flag [GY_X] = nLoop;
		}
		if (temp_min[GY_Y] > GY1_ [nLoop])	{
			temp_min [GY_Y] = GY1_ [nLoop];
			min_flag [GY_Y] = nLoop;
		}
		if (temp_min[GY_Z] > GZ1_ [nLoop])	{
			temp_min [GY_Z] = GZ1_ [nLoop];
			min_flag [GY_Z] = nLoop;
		}

		///////////// MAX
		if (temp_max[ACC_X] < X_ [nLoop])	{
			temp_max [ACC_X] = X_ [nLoop];
			max_flag [ACC_X] = nLoop;
		}
		if (temp_max[ACC_Y] < Y_ [nLoop])	{
			temp_max [ACC_Y] = Y_ [nLoop];
			max_flag [ACC_Y] = nLoop;
		}
		if (temp_max[ACC_Z] < Z_ [nLoop])	{
			temp_max [ACC_Z] = Z_ [nLoop];
			max_flag [ACC_Z] = nLoop;
		}
		if (temp_max[GY_X] < GX1_ [nLoop])	{
			temp_max [GY_X] = GX1_ [nLoop];
			max_flag [GY_X] = nLoop;
		}
		if (temp_max[GY_Y] < GY1_ [nLoop])	{
			temp_max [GY_Y] = GY1_ [nLoop];
			max_flag [GY_Y] = nLoop;
		}
		if (temp_max[GY_Z] < GZ1_ [nLoop])	{
			temp_max [GY_Z] = GZ1_ [nLoop];
			max_flag [GY_Z] = nLoop;
		}

	} //for

}


//---------------------------------------------------------------------
int peak_motion_decision_LR	(unsigned flag_)	{
//-------------------------------------------------------------------- 
// This function classifies Left / Right motion
//
//


	int result_ = NULL;
	printf ("<<<<<<<<<<<<<<<<<<<<, LR\n");

	calc_max_min_position ();

	if ((max_flag [ACC_X] == max_flag [GY_Z]) || (min_flag [GY_Y] == min_flag [GY_Z]) )
		result_ = LEFT_;
	else if (max_flag [GY_Y] == max_flag [GY_Z])
		result_ = RIGHT_;

	return result_;

}

//---------------------------------------------------------------------
int peak_motion_decision_FB	(unsigned flag_)	{
//---------------------------------------------------------------------
// This function classifies Font / Back motion
//
//
	int result_ = NULL;
	printf ("<<<<<<<<<<<<<<<<<<<<, FB\n");

	calc_max_min_position ();

	if ((max_flag [ACC_X] == max_flag [ACC_Y]) || (max_flag [ACC_Y] == min_flag [GY_Z]) )
		result_ = BACK_;
	else if (min_flag [ACC_Y] == max_flag [GY_Y])
		result_ = FRONT_;

	return result_;
}

//---------------------------------------------------------------------
int peak_motion_decision_UD	(unsigned flag_)	{
//---------------------------------------------------------------------
// This function classifies Up / Down motion
//
//
	int result_ = NULL;
	printf ("<<<<<<<<<<<<<<<<<<<<, UD\n");

	calc_max_min_position ();

	if (min_flag [GY_Z] == max_flag [GY_X]) 
		result_ = DOWN_;
	else if ((max_flag [GY_X] == max_flag [GY_Y]) || (min_flag [GY_X] == min_flag [GY_Y]) )
		result_ = UP_;

	return result_;
}


//---------------------------------------------------------------------
int peak_motion_decision_CLOCK	(unsigned flag_)	{
//---------------------------------------------------------------------
// This function classifies CLOCK / ANTiCLOCK motion
//
//

	printf ("<<<<<<<<<<<<<<<<<<<<, CLOCK/UNCLOCK\n");
	int result_ = NULL;

	calc_max_min_position();

//	printf ("MAX: %d %d %d / %d %d %d\n", max_flag [ACC_X], max_flag [ACC_Y], max_flag [ACC_Z], max_flag [GY_X], max_flag [GY_Y], max_flag[GY_Z]);
//	printf ("*MIN: %d %d %d / %d %d %d\n", min_flag [ACC_X], min_flag [ACC_Y], min_flag [ACC_Z], min_flag [GY_X], min_flag [GY_Y], min_flag[GY_Z]);

	if ((max_flag [ACC_X] == max_flag [GY_Z]) || (min_flag [ACC_Z] == min_flag [GY_Z]) )
		result_ = CLOCK_;
	else if ((min_flag [ACC_Y] == max_flag [GY_Y]) || (min_flag [ACC_Z] == max_flag [GY_Z]) )
		result_ = ANTI_CLOCK_;

	return result_;
}

//yy--------------------------------- ---------------------------------
int peak_motion_decision_TURN	(unsigned flag_)	{
//-----------------------------------------------------------------
// This function classifies TURNING position as 4 motion.
//
//
//
	int result_ = NULL;
	printf ("<<<<<<<<<<<<<<<<<<<<, TURNING\n");
	
	calc_max_min_position ();

	if ((min_flag [ACC_Y] == min_flag [ACC_Z]) || (min_flag [ACC_Y] == min_flag [GY_Z]) )
		result_ = TURN_FRONT;
	else if (min_flag [GY_Y] == min_flag [GY_Z]) 
		result_ = TURN_UP;
	else if ((max_flag [ACC_Y] == min_flag [ACC_Z]) || (min_flag [GY_X] == min_flag [GY_Z]) )
		result_ = TURN_BACK;
	else if ((min_flag [ACC_X] == min_flag [GY_Y]) || (min_flag [GY_X] == min_flag [GY_Y]) )
		result_ = TURN_DOWN;

	return result_;
}




//------------------------------------------------------------------
int stationary_motion_decision ()	{
/* // ------------------ stationary motion decision function --------
// to FRONT 90 degree
// to FRONT 45 degree
// to BACK 90 degree
// to BACK 45 degree
----------------------------------------- */
	enum axis_type {X, Y, Z}; 

	int nLoop = 0, average [3];
	float g_variation [3];
	average [X] = 0;
	average [Y] = 0;
	average [Z] = 0;
	g_variation [X] = 0;
	g_variation [Y] = 0;
	g_variation [Z] = 0;

	for (nLoop=0; nLoop < 5; nLoop ++)	{
		average [X] += X_ [nLoop];
		average [Y] += Y_ [nLoop];
		average [Z] += Z_ [nLoop];
		if (nLoop == 4) break;

		g_variation [X] += abs(GX1_ [nLoop] - GX1_ [nLoop + 1]);
		g_variation [Y] += abs(GY1_ [nLoop] - GY1_ [nLoop + 1]);
		g_variation [Z] += abs(GZ1_ [nLoop] - GZ1_ [nLoop + 1]);
#ifdef IN_DEBUG
//	printf ("< %d << %d %d %d >>>\n", nLoop, X_ [nLoop], Y_[nLoop], Z_[nLoop]);
#endif
	}
	
	average [X] /= 5;
	average [Y] /= 5;
	average [Z] /= 5;

	g_variation [X] /= 5;
	g_variation [Y] /= 5;
	g_variation [Z] /= 5;

#ifdef IN_DEBUG
//	printf ("<< avr < %d %d %d >>>\n", average [X], average [Y], average [Z]);
//	printf ("(( vrt ( %f %f %f )))\n", g_variation [X], g_variation [Y], g_variation [Z]);
#endif

	if (g_variation [X] > 60.0 || g_variation [Y] > 60.0 || g_variation [Z] > 60.0)	{	// move
		return NULL;
	}
	else if (average [Y] < -200 && average [Y] > -800)	{	//IDLE 
		return NULL;
	}
	else if (average [Y] < -16000 && average [Y] > -17000)	{
			return STAY_F_90;
	}
	else if (average [Y] < -7000)	{
			return STAY_F_45;
	}
	else if (average [Y] < 17000 && average [Y] > 15000)	{
			return STAY_B_90;
	}
	else if (average [Y] > 9000)	{
			return STAY_B_45;
	}
	return NULL;
}


//-----------------------------------------------------------
int motion_detection ()	{
//-----------------------------------------------------------
	/* ---- 
	[0][N] : Nth acceleration x-axis
	[1][N] : Nth acceleration y-axis
	[2][N] : Nth acceleration z-axis
	[3][N] : Nth gyro x-axis
	[4][N] : Nth gyro y-axis
	[5][N] : Nth gyro z-axis
	---- */
	int nLoop;
	unsigned flag_ = NULL;


	//Data optimization of array
	for (nLoop = 0; nLoop < 2; nLoop ++)	{
		if (arr_sensor1 [0][nLoop] || arr_sensor1 [1][nLoop] 
			|| arr_sensor2 [0][nLoop] || arr_sensor2 [1][nLoop])
			break;
	}
	if (nLoop == 2)	 return DUMP_;


	for (nLoop = 0; nLoop < 5; nLoop ++)	{
		X_ [nLoop] = arr_sensor1 [0][nLoop];
		Y_ [nLoop] = arr_sensor1 [1][nLoop];
		Z_ [nLoop] = arr_sensor1 [2][nLoop];
		GX1_ [nLoop] = arr_sensor1 [3][nLoop];
		GY1_ [nLoop] = arr_sensor1 [4][nLoop];
		GZ1_ [nLoop] = arr_sensor1 [5][nLoop];


//NOW, WE JUST CARE ABOUT SENSOR NUMBER 1. SENSOR NUMBER 2 IS TEMPORALLY BLOCKED.
	//	X2_ [nLoop] = arr_sensor2 [0][nLoop];
	//	Y2_ [nLoop] = arr_sensor2 [1][nLoop];
	//	Z2_ [nLoop] = arr_sensor2 [2][nLoop];
	}

	unsigned peak_x = 0, peak_y = 0, peak_z = 0, peak_gx = 0, peak_gy = 0, peak_gz = 0;
	unsigned gap_x = 0, gap_y = 0, gap_z = 0,gap_gx = 0, gap_gy = 0, gap_gz = 0;
	unsigned result_ = NULL;
	flag_ = 0;
	for (nLoop = 0; nLoop < 4; nLoop ++)	{
		flag_ = flag_ << 8;
	
		peak_x = peak_x < abs(X_ [nLoop]) ? abs(X_ [nLoop]) : peak_x;
		peak_y = peak_y < abs(Y_ [nLoop]) ? abs(Y_ [nLoop]) : peak_y;
		peak_z = peak_z < abs(Z_ [nLoop]) ? abs(Z_ [nLoop]) : peak_z;
		peak_gx = peak_gx < abs(GX1_ [nLoop]) ? abs(GX1_ [nLoop]) : peak_gx;
		peak_gy = peak_gy < abs(GY1_ [nLoop]) ? abs(GY1_ [nLoop]) : peak_gy;
		peak_gz = peak_gz < abs(GZ1_ [nLoop]) ? abs(GZ1_ [nLoop]) : peak_gz;


		if (abs( X_ [nLoop] - X_ [nLoop + 1]) > 4000)	{
			gap_x = gap_x < abs (X_ [nLoop] - X_ [nLoop+1]) ? abs (X_ [nLoop] - X_ [nLoop+1]) : gap_x;
			flag_ |= 0x1;
		} 
		if (abs( Y_ [nLoop] - Y_ [nLoop + 1]) > 4000)	{
			gap_y = gap_y < abs (Y_ [nLoop] - Y_ [nLoop+1]) ? abs (Y_ [nLoop] - Y_ [nLoop+1]) : gap_y;
			flag_ |= 0x2;
		}
		if (abs( Z_ [nLoop] - Z_ [nLoop + 1]) > 4000)	{
			gap_z = gap_z < abs (Z_ [nLoop] - Z_ [nLoop+1]) ? abs (Z_ [nLoop] - Z_ [nLoop+1]) : gap_z;
			flag_ |= 0x4;
		} 
		if (abs( GX1_ [nLoop] - GX1_ [nLoop + 1]) > 4000)	{
			gap_gx = gap_gx < abs (GX1_ [nLoop] - GX1_ [nLoop+1]) ? abs (GX1_ [nLoop] - GX1_ [nLoop+1]) : gap_gx;
			flag_ |= 0x8;
		} 
		if (abs( GY1_ [nLoop] - GY1_ [nLoop + 1]) > 4000)	{
			gap_gy = gap_gy < abs (GY1_ [nLoop] - GY1_ [nLoop+1]) ? abs (GY1_ [nLoop] - GY1_ [nLoop+1]) : gap_gy;
			flag_ |= 0x10;
		}
		if (abs( GZ1_ [nLoop] - GZ1_ [nLoop + 1]) > 4000)	{
			gap_gz = gap_gz < abs (GZ1_ [nLoop] - GZ1_ [nLoop+1]) ? abs (GZ1_ [nLoop] - GZ1_ [nLoop+1]) : gap_gz;
			flag_ |= 0x20;
		} 

		if ((flag_ & 0xFF) >= 0x3E)
			flag_ |= 0x80;


	} //for

	peak_x = peak_x < abs(X_ [4]) ? abs(X_ [4]) : peak_x;
	peak_y = peak_y < abs(Y_ [4]) ? abs(Y_ [4]) : peak_y;
	peak_z = peak_z < abs(Z_ [4]) ? abs(Z_ [4]) : peak_z;
	peak_gx = peak_gx < abs(GX1_ [4]) ? abs(GX1_ [4]) : peak_gx;
	peak_gy = peak_gy < abs(GY1_ [4]) ? abs(GY1_ [4]) : peak_gy;
	peak_gz = peak_gz < abs(GZ1_ [4]) ? abs(GZ1_ [4]) : peak_gz;

#ifdef IN_DEBUG
	printf ("%08X (%d %d %d) (%d %d %d)\n", flag_, peak_x, peak_y, peak_z, peak_gz, peak_gy, peak_gz);
	printf ("GAP: (%d %d %d) (%d %d %d)\n", gap_x, gap_y, gap_z, gap_gx, gap_gy, gap_gz);

	if ((flag_ & 0x01010101) && (peak_x > 4000)) 
				printf ("**** ACC X\n");
	if ((flag_ & 0x02020202) && (peak_y > 4000)) 
				printf ("**** ACC Y\n");
	if ((flag_ & 0x04040404) && (peak_z > 20000)) 
				printf ("**** ACC Z\n");
	if ((flag_ & 0x08080808) && (peak_gx > 5000)) 
				printf ("**** GYRO X\n");
	if ((flag_ & 0x10101010) && (peak_gy > 5000)) 
				printf ("**** GYRO Y\n");
	if ((flag_ & 0x20202020) && (peak_gz > 5000)) 
				printf ("**** GYRO Z \n");
#endif

	if ((flag_ & 0xFFFFFFFF) == NULL)
		result_ = stationary_motion_decision();
	else	{ 
		//if (( flag_ & 0x3F3F3F3F) && gap_gx && gap_gy && gap_gz )	{	// CLOCK/UNCLOCK
		if (( flag_ & 0x80808080) && gap_gx && gap_gy && gap_gz )	{	// CLOCK/UNCLOCK
			result_ = peak_motion_decision_CLOCK (flag_);
			if (result_ != NULL)		return result_;
		}	
		if (((gap_gx > 20000) || (gap_gy > 20000)) && (gap_gz < 20000))	{
			result_ = peak_motion_decision_TURN (flag_);	
		}
		if ((flag_ & 0x31313131) && (peak_x > 5000) && (gap_x > 10000) && (gap_x > (gap_y + gap_z)*0.8))	{	// LEFT/RIGHT 
			result_ = peak_motion_decision_LR (flag_);	
			if (result_ != NULL)		return result_;
		}
		if ((flag_ & 0x22222222) && (peak_y > 4000) && (gap_y > 6000) && (gap_y > (gap_x + gap_z)*0.8))	{	// FRONT/BACK 
			result_ = peak_motion_decision_FB (flag_);	
			if (result_ != NULL)		return result_;
		}
		if ((flag_ & 0x1C1C1C1C) && (gap_z >20000) && (gap_z > (gap_x + gap_y)*0.8) && (gap_x <12000))
		{	// UP/DOWN 
			result_ = peak_motion_decision_UD (flag_);	
			if (result_ != NULL)		return result_;
		}
	}

	return (result_ == NULL ? UNKNOWN_ : result_);
}



//-----------------------------------------------------------
int stream_interpreter (char* rx_buf, int recv_num)	{
//-----------------------------------------------------------
	
	temp_cp_union temp;
	int nLoop = 0;


	//check recved raw data(hex) and num	
	for (nLoop; nLoop < recv_num; nLoop +=2)	{
		temp.tcs.temp2 = rx_buf [nLoop];
		temp.tcs.temp1 = rx_buf [nLoop+1];
		input_stream [nLoop/2] = temp.temp_short;
#ifdef DEBUG
		printf ("/%d %d/", nLoop/2, input_stream [nLoop/2]);	
fflush(stdout);
#endif
	}

	if (check_validation (rx_buf, recv_num/2) != NORMAL)	{
	
		return ERROR_;
	}
//	return (motion_interpreter () );
	if (motion_interpreter () == NORMAL)	{
		switch (motion_detection())	{
			case DUMP_: 
			printf ("-------------------- DUMP--\n"); break;
			case LEFT_: 
			printf ("~~~~~~~~~~~~~~\n");
			printf (" <<<< LEFT <<< \n"); 
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case RIGHT_:
			printf ("~~~~~~~~~~~~~~\n");
			printf (" >>>> RIGHT >>\n"); 
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case FRONT_:
			printf ("~~~~~~~~~~~~~~\n");
			printf ("[[[ FRONT ]\n");
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case BACK_:
			printf ("~~~~~~~~~~~~~~\n");
			printf (" [ BACK ]]]\n");
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case UP_:
			printf ("~~~~~~~~~~~~~~\n");
			printf (" ^^^^ UP\n"); 
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case DOWN_:
			printf ("~~~~~~~~~~~~~~\n");
			printf (" ____ DOWN\n"); 
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case CLOCK_:
			printf ("~~~~~~~~~~~~~~\n");
			printf (" **** CLOCK\n");
			printf ("~~~~~~~~~~~~~~\n");
			break;
			case ANTI_CLOCK_:
			printf ("~~~~~~~~~~~~~~~~~\n");
			printf (" **** ANTI CLOCK\n"); 
			printf ("~~~~~~~~~~~~~~~~~\n");
			break;
			case STAY_F_90:
			printf ("~~~~~~~~~~~~~~~~~~~~~\n");
			printf (" || STAY [[ FRONT 90 ||\n");
			printf ("~~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case STAY_F_45:
			printf ("~~~~~~~~~~~~~~~~~~~~~\n");
			printf (" || STAY [[ FRONT 45 ||\n");
			printf ("~~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case STAY_B_90:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" || STAY BACK ]] 90 ||\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case STAY_B_45:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" || STAY BACK ]] 45 ||\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case TURN_FRONT:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" //- TURN FRONT //\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case TURN_UP:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" //- TURN UP //\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case TURN_BACK:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" //- TURN BACK //\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case TURN_DOWN:
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			printf (" //- TURN DOWN //\n");
			printf ("~~~~~~~~~~~~~~~~~~~~\n");
			break;
			case UNKNOWN_:
			printf (" CAN'T DETECT\n"); 	break;
			defulat:
			printf ("(LOGIG ERROR)\n"); 	break;


		}	//switch

		return NORMAL;
	}	//if
	
	return ERROR_;
	
}

