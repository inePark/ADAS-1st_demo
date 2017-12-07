#include "KETI_serial_bt.h"
#include "KETI_header.h"

int fd;
char tx_buf[MAX_NUM+1];
char rx_buf[MAX_NUM+1];

extern int stream_interpreter (char*, int);
extern void KETI_spi_interf ();

//#define TEST
#undef TEST
#undef TEST_SPI_BOARD
//#define TEST_SPI_BOARD



void signalHandler (int sig_)	{
		static char flag_ = 0;

		#ifdef DEBUG
			printf ("\n<<<<<< RECV data from DEV >>>>>>\n");
		#endif
/*
		switch (sig_)	{
			case SIGUSR1 :
			printf ("[SERIAL.C] UP\n");
		
			set_gnuplot (_UP_);
			break;
			case SIGUSR2 :
			printf ("[SERIAL.C] DOWN\n");
			set_gnuplot (_DOWN_);
			break;

			case SIGALRM:
			printf ("[SERIAL.C] RIGHT\n");
			set_gnuplot (_RIGHT_);
			break;
			case SIGHUP:
			printf ("[SERIAL.C] LEFT\n");
			set_gnuplot (_LEFT_);
			break;
			case SIGCONT:
			printf ("[SERIAL.C] SCALE-UP\n");
			set_gnuplot (_MIDDLE_);
			break;
			case SIGPIPE:
			printf ("[SERIAL.C] SCALE-DOWN\n");
			set_gnuplot (BUTTON_END_SIG);
			break;

		
		}
*/
		if (sig_ == SIGUSR1)	{
			printf ("sleep!!!\n");
			while (!flag_);
			flag_ = 0;
		}
		else if (sig_ == SIGUSR2)	{	
			printf ("Release!!!\n");
			flag_ = 1;
		}

}

int open_port (void)	{

		
		fd = open ("/dev/rfcomm0", O_RDWR | O_NOCTTY);

		if (fd == -1)	{
				perror("Can't open /dev/rfcomm");
				return -1;
		}
		else	{
				fcntl (fd, F_SETOWN, getpid());
				fcntl (fd, F_SETFL, FASYNC);
		}
		
		return (fd);
}
int set_interface_attr (int speed, int parity)	{
		struct termios tty;
		memset (&tty, NULL, sizeof (tty));
		if (tcgetattr (fd, &tty) != FALSE)	{
				printf ("Can't get attr\n");
				return -1;
		}

		cfsetospeed (&tty, speed);
		cfsetispeed (&tty, speed);

		tty.c_cflag = (tty.c_cflag & ~CSIZE | CS8);	//8-bit chars

	
		tty.c_iflag = (IGNPAR | ICRNL);	//ignore break signal
		tty.c_lflag = NULL;	//no signaling chars, no echo
		//tty.c_lflag = ICANON;	//no signaling chars, no echo
		tty.c_oflag = NULL;	//no remapping, no delay
		tty.c_cc[VMIN] = 7;	//read doesn't block
		tty.c_cc[VTIME] = NULL;	//0.5 sec read timeout

		tty.c_iflag &= ~(IXON | IXOFF | IXANY);	//shut off xon/xoff ctrl
		tty.c_cflag |= (CLOCAL | CREAD);	//ignore modem controls, enable reading
		tty.c_cflag &= ~CSTOPB;
		tty.c_cflag &= ~CRTSCTS;
	

		tcflush (fd, TCIFLUSH);

		if (tcsetattr (fd, TCSANOW, &tty) != FALSE)	{
				printf ("Can't set attr \n");
				return -1;
		}
		return NORMAL;
}

void
set_blocking (int should_block)	{


	struct termios tty;
	memset (&tty, NULL, sizeof (tty));
	if (tcgetattr (fd, &tty) != NULL)	{
			printf ("Can't set attr \n");
			return ;
	}
	tty.c_cc [VMIN] = should_block ? 1 : NULL;
	tty.c_cc [VTIME] = NULL;
	if (tcsetattr (fd, TCSANOW, &tty) != NULL)
		printf ("Can't set attr \n");
}

void exit_function ()	{
	close (fd);
	printf ("Good bye!\n");
	exit (NORMAL);
}

void motion_expression (int motion_value)	{
/*
		switch (motion_value)	{
			case ERROR_:
			printf ("Unvalid stream!\n");
			break;
			case LEFT_:
			printf ("LEFT\n");
			break;
			case RIGHT_:
			printf ("RIGHT\n");
			break;
			case UP_:
			printf ("UP\n");
		//	system ("eog up.png 2>/dev/null &");
			break;
			case DOWN_:
			printf ("DOWN\n");
			break;
			case CLOCK_:
			printf ("CLOCK\n");
		//	system ("eog clock.png 2>/dev/null &");
			break;
			case ANTI_CLOCK__:
			printf ("CLOCK_WS\n");
			break;
			default:
			printf ("Unrecognized action\n");
			break;
		}

		*/
}

/*
void * write_function ()	{

	do {
		memset (tx_buf, NULL, MAX_NUM);
		gets (tx_buf);
		write (fd, tx_buf, MAX_NUM);
		if (strncmp (tx_buf, "exit", 4) == 0)	{
			exit_function ();
			break;
		}
	} while (TRUE);

}
*/




void *  read_function ()	{
	int recv_num = 0;
	int return_value = 0;
	int counter = 0;

	static unsigned number_ = 0;
	
	do {
		memset (rx_buf, NULL, MAX_NUM);
		fflush (stdout);
		
#ifndef TEST
		//printf ("a");
		if ((recv_num = read (fd, rx_buf, MAX_NUM)) < 1)
			continue;
		rx_buf [recv_num] = '\0';
		counter ++;
#else
		rx_buf [0] = 0xFE;
		rx_buf [1] = 0xDC;
		rx_buf [3] = 0x02;
		rx_buf [52] = 0xAB;
		rx_buf [53] = 0xCD;
		
		int b;
		for (b=4; b < 52; b++)	
			rx_buf [b] = b;
		recv_num = 54;

		sleep(5);
#endif

#ifdef DEBUG
		printf (">> RECV: %s(%d) 0x%2X 0x%2X 0x%2X 0x%2X 0x%2X 0x%2X %d  ", rx_buf, recv_num, rx_buf [0], rx_buf[1], rx_buf [2], rx_buf [3], rx_buf [recv_num-2], rx_buf [recv_num-1],counter);

		if (strncmp (rx_buf, "exit", 4) == 0)	{
			exit_function ();
			break;
		}
#endif

#ifdef	HALF_SAVER
		if (number_ % 2 == 1)		{	
			printf ("RETURN~~~~~ \n" ); fflush (stdout);
		}
		else 	{
			printf ("GET DATA %d\n",number_); fflush (stdout);
		}
#else
		printf ("GET DATA %d\n", number_); fflush (stdout);
#endif


		number_ ++;
		
		return_value = stream_interpreter (rx_buf, recv_num);
		fflush (stdout);


		motion_expression (return_value);

	}	while (TRUE);
}



int main (int argc, char ** argv)	{

	FILE* file_out;
	pid_t pid;
//	pthread_t read_thread, write_thread; 
//	int iret1, iret2; 


#ifndef TEST_SPI_BOARD

	struct sigaction sa;
	bzero (&sa, sizeof(sa));
	sa.sa_handler = signalHandler;
	sa.sa_flags = NULL;
	sa.sa_restorer = NULL;
	sigaction (SIGIO, &sa, NULL);
	(void) signal (SIGUSR1, signalHandler);
	(void) signal (SIGUSR2, signalHandler);
	(void) signal (SIGALRM, signalHandler);
	(void) signal (SIGCONT, signalHandler);
	(void) signal (SIGPIPE, signalHandler);
	(void) signal (SIGHUP, signalHandler);

	

#ifndef TEST		

	while (open_port ()  == ERROR_)	{
		printf ("open_port error!\n");
		sleep(1);
	}
		

	if (! set_interface_attr (B115200, NORMAL))
		set_blocking (NORMAL);
	
	printf ("** READY to RECV/SEND! **\n");
	
	
#endif

	printf ("START");
	read_function ();

	while (TRUE)	sleep (1);
		

	close (fd);

#else
	while (1)
		sleep(5);
#endif

	return NORMAL;
}


