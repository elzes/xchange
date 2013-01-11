/*
 * Author: 	Erwin Gijzen
 * Date:	12/18/2012.
 * 
 * This server is created to run on the Linksys WRT45GL.
 * This application is has to serve as a bittorrent client when the computer is shut down. 
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <pthread.h>    /* POSIX Threads */
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <time.h>

#define SERVER_PORT 9001
#define NAME_SERVER_PORT 7000
#define BUFFSIZE 256
#define ARRAY_LENGTH 33
#define MAX_DOWNLOADS 8
#define IP_ADDRESSES 8

struct sockaddr_in gv_address; // struct sockeaddr_in defined in socket.h
char *parameters[ARRAY_LENGTH];
char *ip_addresses[IP_ADDRESSES];
char *nameserver_ip = "", *storageserver_ip = "";
char *message = "192.168.1.2\n";
char *ip_address = "192.168.1.2\n";
//char *ip_address = "145.37.63.132\n";
//char *ip_address = "192.168.8.111\n";

int init_socket(void);
void listen_and_handle(int s);
void handle(int* sPtr);
void close_socket(int);
void function_start(void);

int init_socket(void) {	 
	int rv, s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
		exit(EXIT_FAILURE);
	}
	gv_address.sin_family = AF_INET;
	gv_address.sin_addr.s_addr = INADDR_ANY;
	gv_address.sin_port = htons(SERVER_PORT);

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
	int rv, cs;
	pthread_t main_thread; /* array of thread variables */

	rv = listen(ms, 2);
	if (rv < 0) {
		perror("Error listening to socket");
		exit(EXIT_FAILURE);
	}

	unsigned int addr_len = sizeof(gv_address);
	cs = accept(ms, (struct sockaddr *)&gv_address, &addr_len);
	if (cs < 0) {
		printf("Error accept");
		exit(EXIT_FAILURE);
	}

	rv = pthread_create (&main_thread, NULL, (void *)&handle, (void *)&cs);
	if (rv != 0) {
		perror("Error creating thread");
		exit(EXIT_FAILURE);
	}

	rv = pthread_join(main_thread, NULL);
	if (rv != 0) {
		perror("Error listening to socket");
		exit(EXIT_FAILURE);
	}
}

void handle(int* sPtr) {
	int rv = 0, s = *sPtr, nr_bytes_recv = 0;

	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';

	while(1) {
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);
		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			exit(EXIT_FAILURE);
		}
		buffer[nr_bytes_recv]= '\0';
		printf("%s", buffer);

        // SET NAMESERVER IP & STORAGESERVER IP
        // Parameters: SETTINGS|nameserverip|storageserverip
        if(strncmp(buffer, "LIST", 4) == 0) {
			rv = send(s, ip_address, strlen(ip_address),0);
        }

		else {
			// echo message
			rv = send(s, buffer, nr_bytes_recv,0);
			if (rv < 0) {
				perror("Error sending");
				exit(EXIT_FAILURE);
			}
		}
	}		
}

int main() {
	int ms = init_socket();
	listen_and_handle(ms);
	close_socket(ms);
	return(0);
}

