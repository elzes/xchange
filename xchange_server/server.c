/*
 * Author: 	Erwin Gijzen & Michael de Vries.
 * Date:	12/18/2012.
 * 
 * This server is created to run on the Linksys WRT45GL.
 * This application is has to serve as a bittorrent client when the computer is shut down. 
 * 
 * In this project the server has to handle requests form a Java application:
 *
 * ** The Java application starts the "Router download". 
 *	  Receives:
 * 		- <Start> <Filename> <Filesize> <Total chunks> <Downloaded chuncks> <IP address Peer>
 *			<IP address nameserver> <IP address storageserver>
 *    Sends:
 *      - <OK> || <FAIL>.
 *
 * ** The router connects to the "nameserver" to receive IP-addresses from uploaders.
 *    Sends:
 *		- <GET><IP-addresses>
 *	  Receives:
 *    	- <IP1><IP2>..<IPn>     
 *
 * ** The router connects to the storageserver en continues downloading the file.
 *    
 *
 * ** The Java application stops the "Router download". 
 * ** 
 *
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
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

#define SERVER_PORT 7000
#define DOWNLOAD_PORT 7001
#define BUFFSIZE 256
#define MAX_CLIENTS 3

struct sockaddr_in gv_address; // struct sockeaddr_in defined in socket.h
char *start_download_parameters[8];

int init_socket(void);
int init_socket_peer(char *);
void listen_and_handle(int s);
void close_socket(int);
void handle(int* sPtr);
void function_start_download(void);

int init_socket(void) {	 
	int rv; // return value
	int s = socket(AF_INET, SOCK_STREAM, 0);
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

int init_socket_peer(char *IP_address) {
	int rv;
	int s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
		exit(EXIT_FAILURE);
	}

	gv_address.sin_family = AF_INET;
	gv_address.sin_addr.s_addr = inet_addr (IP_address);
	//gv_address.sin_addr.s_addr = inet_addr ("192.168.1.3"); 
	//gv_address.sin_addr.s_addr = inet_addr ("192.168.8.111");
	//gv_ address.sin_addr.s_addr = inet_addr ("145.37.94.37");
	//gv_address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
	gv_address.sin_port = htons(DOWNLOAD_PORT);

	rv = connect(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error connecting to server socket");
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
	int rv; // return value
	int cs; // child socket descriptor
	int i = 0;
	pthread_t tarray[2]; /* array of thread variables */

	// listen for incoming connection, MAX_CLIENTS connections pending
	rv = listen(ms, MAX_CLIENTS);
	if (rv < 0) {
		perror("Error listening to socket");
		exit(EXIT_FAILURE);
	}

	for (i=0; i < MAX_CLIENTS; i++) {
		// accept incoming connection
		unsigned int addr_len = sizeof(gv_address);
		cs = accept(ms, (struct sockaddr *)&gv_address, &addr_len);
		if (cs < 0) {
			printf("Error accept");
			exit(EXIT_FAILURE);
		}
		
		rv = pthread_create (&tarray[i], NULL, (void *)&handle, (void *)&cs);
		if (rv != 0) {
			perror("Error creating thread");
			exit(EXIT_FAILURE);
		}
	}		

	// now wait for all threads to terminate
	for (i=0; i < MAX_CLIENTS; i++) {
		rv = pthread_join(tarray[i], NULL);
		if (rv != 0) {
			perror("Error listening to socket");
			exit(EXIT_FAILURE);
		}
	}
}

void handle(int* sPtr) {
	int rv = 0; // return value
	int s = *sPtr;

	// create and init a char buffer
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';
	int nr_bytes_recv = 0;
	int stop_received = 0;

	while(! stop_received) {
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);
		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			exit(EXIT_FAILURE);
		}
		buffer[nr_bytes_recv]= '\0'; // close string
		printf("Server received : %s", buffer);

		int nr = 0;
		char *rest; // to point to the rest of the string after token extraction.
        char *token; // to point to the actual token returned.
        char *ptr = buffer; // make q point to start of hello.

        // loop till strtok_r returns NULL.
        while(token = strtok_r(ptr, " ,", &rest)) {
        	start_download_parameters[nr] = token;
            //printf("%s\n", token); // print the token returned.
            ptr = rest; // rest contains the left over part..assign it to ptr...and start tokenizing again.    
        	printf("Arraynr %d : %s\n", nr, start_download_parameters[nr]);
        	nr++;
        }

        // START ROUTER DOWNLOAD
        if(strncmp(start_download_parameters[0], "start", 5) == 0) {
        	printf("--> Router download start.\n");
        	function_start_download();
        	rv = send(s, "OK", nr_bytes_recv,0);
        	if (rv < 0) {
				perror("Error sending");
				exit(EXIT_FAILURE);
			}
        }

		else if(strncmp(buffer, "STOP", 4) == 0) {
			char *message = "Closing server socket...\n";
			rv = send(s, message, strlen(message), 0);
			if (rv < 0) {
				perror("Error sending");
				exit(EXIT_FAILURE);
			}
		
			rv = close(s);
			if (rv < 0) {
				perror("Error closing socket");
				exit(EXIT_FAILURE);
			}

			stop_received = 1;
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

void function_start_download(void) {
	int s = init_socket_peer(start_download_parameters[5]);
	printf("Functie start =) %d \n", s);
}

int main() {
	int ms; // master socket descriptor

	ms = init_socket();
	listen_and_handle(ms);
	close_socket(ms);
	return(0);
}

