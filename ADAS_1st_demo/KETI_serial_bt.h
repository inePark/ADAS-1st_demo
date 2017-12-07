#ifndef _KETI_SERIAL_BT_H_
#define _KETI_SERIAL_BT_H_

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <termios.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/signal.h>
#include <pthread.h>
#include "KETI_header.h"

int open_port ();
int set_interface_attr (int, int);
void set_blocking (int);

#endif
