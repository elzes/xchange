/*
 * Author: 	Erwin Gijzen
 * Date:	12/18/2012.
 * 
 * This server is created to run on the Linksys WRT45GL.
 * This application is has to serve as a bittorrent client when the computer is shut down. 
 * 
 */

#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <pthread.h>  
#include <time.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include "handler.c"

#define PORT 7000
#define PEER_PORT 9000
#define MAX_CLIENTS 3

char *SPLIT_CHAR = " ";
int ms = 0;
char *nameServerIp = "";
char *storageServerIp = "";
struct sockaddr_in gv_address;

int init_socket(void);
void close_socket(int);
void listen_and_handle(int);
void exitApp();

int init_socket(void) {	 
	int rv = 0;
	int s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
		exit(EXIT_FAILURE);
	}

	gv_address.sin_family = AF_INET;
	gv_address.sin_addr.s_addr = INADDR_ANY;
	gv_address.sin_port = htons(PORT);

	rv = bind(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error binding to socket");
		exit(EXIT_FAILURE);
	}
	
	return s;
}

void close_socket(int s) {	 
	int rv = close(s);
	if (rv < 0) {
		perror("Error closing socket");
		exit(EXIT_FAILURE);
	}
}

void listen_and_handle(int ms) {
	int cs = 0;
	int rv = listen(ms, MAX_CLIENTS);
	if (rv < 0) {
		perror("listen_and_handle: Error listening to socket");
		exit(EXIT_FAILURE);
	}

	while(1) {
		unsigned int addr_len = sizeof(gv_address);
		cs = accept(ms, (struct sockaddr *)&gv_address, &addr_len);
		if (cs < 0) {
			printf("Error accept");
			exit(EXIT_FAILURE);
		}
		pthread_t main_thread;
		rv = pthread_create (&main_thread, NULL, (void *)&handle, (void *)&cs);
		if (rv != 0) {
			perror("Error creating thread");
			exit(EXIT_FAILURE);
		}
	}	
}

void exitApp() {
	close_socket(ms);
}

int main() {
	signal(SIGABRT,exitApp);
	signal(SIGTERM, exitApp);
	ms = init_socket();
	listen_and_handle(ms);
	exitApp();
	return(0);
}
