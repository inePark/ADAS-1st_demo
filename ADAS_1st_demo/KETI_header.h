#ifndef _KETI_HEADER_H_
#define _KETI_HEADER_H_


#define MAX_NUM		512
#define DATA_MAX	24
#define SENSOR_AXIS	6
#define OFF	0
#define ON	1

#ifndef NULL
#define NULL	(void)0
#endif

#ifndef TRUE
#define TRUE	1
#endif

#ifndef FALSE
#define FALSE	0
#endif

#ifndef NORMAL
#define NORMAL	0
#endif

#ifndef ERROR_
#define ERROR_	-1
#endif

#define DUMP_	1
#define LEFT_ 	2
#define RIGHT_ 	3
#define UP_ 	4
#define DOWN_ 	5
#define CLOCK_ 	6
#define ANTI_CLOCK_	7
#define FRONT_	8
#define BACK_	9
#define STAY_F_90	10
#define STAY_F_45	11
#define STAY_B_90	12
#define STAY_B_45	13
#define TURN_FRONT	14
#define TURN_BACK	15
#define TURN_UP		16
#define TURN_DOWN	17

#define UNKNOWN_	99

#define START_STRING	"FEDC"
#define END_STRING		"ABCD"
#define START_INT	-292
#define END_INT		-21555

#define FNAME_S1_ACCEL	"sensor1_accel.dat"
#define FNAME_S1_GYRO	"sensor1_gyro.dat"
#define FNAME_S2_ACCEL	"sensor2_accel.dat"
#define FNAME_S2_GYRO	"sensor2_gyro.dat"

#define _UP_	0
#define _DOWN_	1
#define _LEFT_	2
#define _RIGHT_	3
#define _MIDDLE_	4
#define BUTTON_END_SIG	5
#define _PASS_	10

#define FILE_DOOROPEN	"unlock.jpg"
#define FILE_DOORCLOSE	"lock.jpg"
#define FILE_EMERGENCE	"emergency.jpg"
#define FILE_SOUND		"sound.jpg"
#define FILE_TRUNK		"trunk.jpg"

#define HALF_SAVER 1
#undef HALF_SAVER

//#define DEBUG
#undef DEBUG

//#ifdef __cplusplus
#endif

