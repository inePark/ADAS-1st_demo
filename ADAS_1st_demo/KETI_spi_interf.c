#include "KETI_spi_interf.h"

int spi_fd;
char spi_tx_buf[MAX_NUM];
char spi_rx_buf[MAX_NUM];
//extern char update_file_path [256];
pid_t child_pid;

void spi_signalHandler (int sig_)	{
	
#ifdef DEBUG
		printf ("<RECV data from SPI interface>\n");
#endif


}

int spi_open_port (void)	{
		struct sigaction sa;
		
		bzero (&sa, sizeof(sa));
		sa.sa_handler = spi_signalHandler;
		sa.sa_flags = 0;
		sa.sa_restorer = NULL;
		sigaction (SIGIO, &sa, NULL);

		spi_fd = open ("/dev/ttyUSB0", O_RDWR | O_NOCTTY | O_NONBLOCK);

		if (spi_fd == -1)	{
				return -1;
		}
		else	{
				fcntl (spi_fd, F_SETOWN, getpid());
				fcntl (spi_fd, F_SETFL, FASYNC);
		}
		
		return (spi_fd);
}
int spi_set_interface_attr (int speed, int parity)	{
		
		struct termios tty;
		memset (&tty, 0, sizeof (tty));
		if (tcgetattr (spi_fd, &tty) != 0)	{
				printf ("tcgetattr\n");
				return -1;
		}

		cfsetospeed (&tty, speed);
		cfsetispeed (&tty, speed);

		tty.c_cflag = (tty.c_cflag & ~CSIZE | CS8);	//8-bit chars

		tty.c_iflag = (IGNPAR | ICRNL);	//ignore break signal
		tty.c_lflag = ICANON;	//no signaling chars, no echo
		tty.c_oflag = 0;	//no remapping, no delay
		tty.c_cc[VMIN] = 7;	//read doesn't block
		tty.c_cc[VTIME] = 0;	//0.5 sec read timeout

		tty.c_iflag &= ~(IXON | IXOFF | IXANY);	//shut off xon/xoff ctrl
		tty.c_cflag |= (CLOCAL | CREAD);	//ignore modem controls, enable reading
		tty.c_cflag &= ~CSTOPB;
		tty.c_cflag &= ~CRTSCTS;

		if (tcsetattr (spi_fd, TCSANOW, &tty) != 0)	{
				printf ("tcsetattr\n");
				return -1;
		}
		return 0;
}

void
spi_set_blocking (int should_block)	{
		struct termios tty;
		memset (&tty, 0, sizeof (tty));
		if (tcgetattr (spi_fd, &tty) != 0)	{
				printf ("tggetattr\n");
				return ;
		}
		tty.c_cc [VMIN] = should_block ? 1 : 0;
		tty.c_cc [VTIME] = 5;

		if (tcsetattr (spi_fd, TCSANOW, &tty) != 0)
				printf ("tcsetattr \n");
}
void spi_print_hex (FILE* file_, char* str, int len)	{
	int temp;
		for (temp = 0; temp < len; temp ++	)	{
			fprintf (file_, "%#X ", str [temp]);
			if (str [temp] == 0xD)
					break;
		}	
		fprintf (file_, "\n");

		return ;
}

void spi_print_string (FILE* file_, char* str, int len)	{
		static unsigned count = 1;
		if (*str == '?')
				printf ("Protocol error!\n");
		fprintf (file_, "%d. %dB[%s]\n\n", count ++,len,str);
			fflush(file_);

		return ;
}


char spi_data_interpreter (int recv_num)	{

	char recv_data [3];

	recv_data [0] = spi_rx_buf [recv_num - 5];
	recv_data [1] = spi_rx_buf [recv_num - 4];
	recv_data [2] = '\0';

	if (spi_rx_buf [7] == 'F')	{
#ifdef	DEBUG
		printf ("End touch\n");
#endif
		return BUTTON_END_SIG;
	}
	else if (strcmp (recv_data, "55") == 0)	{	//Up
		return _UP_;
	}
	else if (strcmp (recv_data, "44") == 0)	{	//Down
		return _DOWN_;
	}
	else if (strcmp (recv_data, "4C") == 0)	{	//Left
		return _LEFT_;
	}
	else if (strcmp (recv_data, "52") == 0)	{	//Right
		return _RIGHT_;
	} 
	else if (strcmp (recv_data, "4D") == 0)	{	//Middle
		return _MIDDLE_;
	}
	else	{
		printf ("Not found order\n");
		return 0;
	}

}

void eog_ECU_action	(char order_)	{
	char file_name [64] = "NONE";
	char temp [128];
	
	
	switch	(order_)	{
		case _UP_:
			strcpy (file_name, FILE_DOORCLOSE);
		break;
		case _DOWN_:
			strcpy (file_name, FILE_DOOROPEN);
		break;
		case _LEFT_:
			strcpy (file_name, FILE_EMERGENCE);
		break;
		case _RIGHT_:
			strcpy (file_name, FILE_TRUNK);
		break;
		case _MIDDLE_:
			strcpy (file_name, FILE_SOUND);
		break;
	}

	sprintf (temp, "eog %s &", file_name);
	system (temp);
}

char button_press_optimiz (unsigned char button)	{
	
	static short buttonPress [BUTTON_END_SIG] = {0,0,0,0,0};

	buttonPress [button] ++;
//printf ("button: %d, arr: %d\n", button, buttonPress[button]);
	if (button == BUTTON_END_SIG)	{
		//reset button optmz
		memset (buttonPress, '\0', BUTTON_END_SIG);
	}

	else if (buttonPress [button] % 7 == 1)	{
		return button;
	}
	return _PASS_;
}


void sleep_for_term ()	{

		sleep(4);

		system ("killall eog &");

}

void* spi_data_read_function (void* par)	{

	FILE* output;
	output = fopen ("spi_data.dat", "w");
	fclose (output);
	int recv_num =-1;
	char temp_flag = 0;
	time_t	curr_time = 0, temp_time;
	unsigned char spi_order = 0;

	do {
		memset (spi_rx_buf, '\0', MAX_NUM);
		fflush(stdout);

		if ((recv_num = read (spi_fd, spi_rx_buf, MAX_NUM)) < 1)
			continue;
		
		time (&curr_time);
		if (curr_time - temp_time > 1)
			temp_flag = 0;

		spi_rx_buf [recv_num] = '\0';
		output = fopen ("spi_data.dat", "a");
		spi_print_hex (output, spi_rx_buf, recv_num);
		spi_print_string (output, spi_rx_buf, recv_num);
		fclose (output);

		temp_time = curr_time;		
		spi_order = spi_data_interpreter (recv_num);

		switch (button_press_optimiz (spi_order))	{
#ifdef MOVING_GRAPH

			case _UP_:
			printf ("UP button is pressed!\n");
			kill (child_pid, SIGUSR1);
			temp_flag = FALSE;
			break;
			case _DOWN_:
			printf ("DOWN button is pressed!\n");
			kill (child_pid, SIGUSR2);
			temp_flag = FALSE;
			break;
			case _RIGHT_:
			printf ("RIGHT button is pressed!\n");
			kill (child_pid, SIGALRM);
			temp_flag = FALSE;
			break;
			case _LEFT_:
			printf ("LEFT button is pressed!\n");
			kill (child_pid, SIGHUP);
			temp_flag = FALSE;
			break;
			case _MIDDLE_:
				printf ("MIDDLE button is pressed!\n");
				if (temp_flag == FALSE)	{
					temp_flag = TRUE;	
					kill (child_pid, SIGCONT);
				}
				else
					kill (child_pid, SIGPIPE);
				break;
#else

		
			
			case _UP_:
			printf ("UP button is pressed!\n");
			eog_ECU_action (_UP_);
			sleep_for_term ();
			break;
			case _DOWN_:
			printf ("DOWN button is pressed!\n");
			eog_ECU_action (_DOWN_);
			sleep_for_term ();
			break;
			case _RIGHT_:
			printf ("RIGHT button is pressed!\n");
			eog_ECU_action (_RIGHT_);
			sleep_for_term ();
			break;
			case _LEFT_:
			printf ("LEFT button is pressed!\n");
			eog_ECU_action (_LEFT_);
			sleep_for_term ();
			break;
			case _MIDDLE_:
			printf ("MIDDLE button is pressed!\n");
			eog_ECU_action (_MIDDLE_);
			sleep_for_term ();
			break;

		
#endif
		}

	} while (TRUE);

}


int main (int argc, char** argv)	{
		

		pthread_t read_thread;
		int iret;
		FILE* file_out;


		while (spi_open_port () < 0)	{
			printf ("[SPI interface] open_port error!\n");
			sleep(2);
		}
		
		if (! spi_set_interface_attr (B115200, 0))
			spi_set_blocking (0);
		
		size_t wn, n  = 0;

#ifdef DEBUG
		printf ("Hello this is KETI_spi_interface function!\n");
#endif
		file_out = fopen("debug", "w+");

		/* Env. setting read order */
		env_set_read (file_out);
		
		usleep(5000);
		recv_from_UCA (file_out);

		/* CAN RX start order */
		can_rx_start (file_out);

		usleep (5000);
		recv_from_UCA (file_out);

		fclose (file_out);
	
		iret = pthread_create (&read_thread, NULL, spi_data_read_function, NULL);


		while (1)	{
				file_out = fopen("debug", "a+");
				sleep(3);
				n = user_interface_ ();	
				if (can_tx_data (n) == -1 ) 
					break;
		
	/*			wn = write (spi_fd, spi_tx_buf, 27);		//TX
				spi_print_hex (file_out, spi_tx_buf, 27);
				spi_print_string (file_out, spi_tx_buf, 88);
				recv_from_UCA (file_out);
	*/		

				fclose (file_out);
		}

		close (spi_fd);
		pthread_join (read_thread, NULL);
}



void env_set_read (FILE* file_out)	{


		memset(spi_tx_buf, 0, MAX_NUM);
		spi_tx_buf [0] = ':';	//start
		spi_tx_buf [1] = 'Y';
		spi_tx_buf [2] = 0x35;	//3
		spi_tx_buf [3] = 0x39;	//9
		spi_tx_buf [4] = 0x0D;	//end

		write (spi_fd, spi_tx_buf, 5);	//TX
		spi_print_hex (file_out, spi_tx_buf, 5);


}


int recv_from_UCA (FILE* file_out)	{


		memset(spi_rx_buf, 0, MAX_NUM);
		int n = read (spi_fd, spi_rx_buf, MAX_NUM);	//RX
		spi_print_string (file_out, spi_rx_buf, n);
		spi_print_hex (file_out, spi_rx_buf, n);

		return 0;
}

void can_rx_start (FILE* file_out)	{
		

		memset(spi_tx_buf, 0, MAX_NUM);
		spi_tx_buf [0] = ':';	//start
		spi_tx_buf [1] = 'G';
		spi_tx_buf [2] = 0x31;	//1
		spi_tx_buf [3] = 0x31;	//1
		spi_tx_buf [4] = 0x41;	//chk sum1: A
		spi_tx_buf [5] = 0x39;	//chk sum2: 9
		spi_tx_buf [6] = 0xD;	//end

		write (spi_fd, spi_tx_buf, 7);		//TX
		spi_print_hex (file_out, spi_tx_buf, 7);
		
}

int user_interface_ ()	{
		
		
		return 4;

}

unsigned char check_sum()	{

	char temp = 0; 

	int i;
	for (i = 1; i < 26; i ++)	{
		temp += spi_tx_buf [i];
	}

	temp &= 0xFF;
	return temp;
}


char hex2ascii (char in)	{

		if (in == 'A' || in == 'a' || in == 10)	return 0x41;	
		else if (in == 'B' || in == 'b' || in == 11)	return 0x42;	
		else if (in == 'C' || in == 'c' || in == 12)	return 0x43;	
		else if (in == 'D' || in == 'd' || in == 13)	return 0x44;	
		else if (in == 'E' || in == 'e' || in == 14)	return 0x45;	
		else if (in == 'F' || in == 'f' || in == 15)	return 0x46;
		else
			return (in + 0x30);
}

int can_tx_data	(int order_)	{

	int size_;

	char chksum = 0;

	memset(spi_tx_buf, 0, MAX_NUM);
	spi_tx_buf [0] = ':';	//start
	spi_tx_buf [1] = 'W';
	spi_tx_buf [2] = 0x30;	//0
	spi_tx_buf [3] = 0x38;	//8
	spi_tx_buf [4] = 0x30;	//tx ID1: 0
	spi_tx_buf [5] = 0x36;	//tx ID2: 6
	spi_tx_buf [6] = 0x46;	//tx ID3: F
	spi_tx_buf [7] = 0x45;	//tx ID4: E
	
	int loop;
	for (loop =8; loop < 24; loop ++)	{
		spi_tx_buf [loop] = 0x30;
	}

	chksum = check_sum ();
	spi_tx_buf [24]= chksum & 0xF0;
	spi_tx_buf [24]= spi_tx_buf [24] >> 4;
	spi_tx_buf [24] &= 0x0F;
	spi_tx_buf [25]= chksum & 0x0F;

	spi_tx_buf [24] = hex2ascii (spi_tx_buf [24]);
	spi_tx_buf [25] = hex2ascii (spi_tx_buf [25]);

	spi_tx_buf [26] = 0x0D;

	return 27;
}

