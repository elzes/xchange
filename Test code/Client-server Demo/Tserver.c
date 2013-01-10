#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <pthread.h>    /* POSIX Threads */

/**********************************************************************************
 *  #define
 **********************************************************************************/

#define PORT 7000
#define BUFFSIZE 256
#define MAX_CLIENTS 3

/**********************************************************************************
 *   global data
 **********************************************************************************/

struct sockaddr_in gv_address; // struct sockeaddr_in defined in socket.h

/**********************************************************************************
 *  function prototypes
 **********************************************************************************/

int init_socket(void);
void listen_and_handle(int s);
void close_socket(int);
void handle(int* sPtr);

/* -----------------------------------------------------------------------------------------------------------
 * Function:	init_socket
 * Purpose:	creates a socket and binds it to port PORT
 * Arguments: 	none
 * Return value:    Success: returns socket descriptor of new socket
 *		    Failure: none
 *------------------------------------------------------------------------------------------------------------
 */

int init_socket(void)
{	 
	int rv; // return value
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

/* -----------------------------------------------------------------------------------------------------------
 * Function:	close_socket
 * Purpose:	closes a socket
 * Arguments: 	socket desciptor
 * Return value:    Success: none
 *		    Failure: none
 *------------------------------------------------------------------------------------------------------------
 */

void close_socket(int s)
{	 
	int rv = close(s);
	if (rv < 0) {
		perror("Error closing socket");
		exit(EXIT_FAILURE);
	}
}

/* -----------------------------------------------------------------------------------------------------------
 * Function:	listen and handle on a socket
 * Purpose:	listen for incoming connetions; spawn thread for each incoming connection
 * Arguments: 	master socket desciptor
 * Return value: Success: none
 *		 Failure: none
 *------------------------------------------------------------------------------------------------------------
 */


void listen_and_handle(int ms)
{
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


/* -----------------------------------------------------------------------------------------------------------
 * Function:	handle a socket (receive and send)
 * Purpose:	if a string "STOP" is received then close connection,
 *		else echoe received string back
 * Arguments: 	socket desciptor, thread id
 * Return value: Success: none
 *		 Failure: none
 *------------------------------------------------------------------------------------------------------------
 */

void handle(int* sPtr)
{
	int rv = 0; // return value
	int s = *sPtr;

	// create and init a char buffer
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';

	int stop_received = 0;
	int nr_bytes_recv = 0;

	while (! stop_received) {
		// receive data in buffer
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			exit(EXIT_FAILURE);
		}

		buffer[nr_bytes_recv]= '\0'; // close string
		printf("Server received : %s", buffer);
	
		if(strncmp(buffer, "STOP", 4) == 0) {
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
	
		} else {
			// echo message
			rv = send(s, buffer, nr_bytes_recv,0);
			if (rv < 0) {
				perror("Error sending");
				exit(EXIT_FAILURE);
			}

		}
	}
}

/* -----------------------------------------------------------------------------------------------------------
 * Function:	main
 * Purpose:     -
 * Arguments:	none
 * Return value: Success: 0
 *		 Failure: none
 *------------------------------------------------------------------------------------------------------------
 */

int main()
{
	int ms; // master socket descriptor

	ms = init_socket();
	listen_and_handle(ms);
	close_socket(ms);
	return(0);
}
