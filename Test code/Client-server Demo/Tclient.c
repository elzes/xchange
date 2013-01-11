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

/**********************************************************************************
 *  #define
 **********************************************************************************/

#define PORT 7000
#define BUFFSIZE 256

/**********************************************************************************
 *   global data
 **********************************************************************************/

struct sockaddr_in gv_address; // struct sockeaddr_in defined in socket.h

/**********************************************************************************
 *  function prototypes
 **********************************************************************************/

int init_socket(void);
void handle(int s);
void close_socket(int);

/* -----------------------------------------------------------------------------------------------------------
 * Function:	init_socket
 * Purpose:	creates a socket and connects it to server socket
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
	gv_address.sin_addr.s_addr = inet_addr ("192.168.1.2"); 
	//gv_address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
	gv_address.sin_port = htons(PORT);

	rv = connect(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error connecting to server socket");
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
 * Function:	handle a socket (receive and send)
 * Purpose:	if a string "STOP" is send then server will close connection,
 *		else server will echo the send string back to client
 * Arguments: 	socket desciptor
 * Return value: Success: none
 *		 Failure: none
 *------------------------------------------------------------------------------------------------------------
 */

void handle(int s)
{
	int rv = 0; // return value

	printf("%s\n", "Max clients = 3");
	printf("%s\n", "Type your input; \"STOP<return>\" will stop the server and ^D will stop the client");
	// finish with ctrl-d

	// create and init a char buffer
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';

	int nr_bytes_recv = 0;

	while (!feof(stdin)) {
		fgets(buffer, BUFFSIZE, stdin);
		rv = send(s, buffer, strlen(buffer), 0);
		if (rv < 0) {
			perror("Error sending");
			exit(EXIT_FAILURE);
		}

		// receive data in buffer
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			exit(EXIT_FAILURE);
		}

		buffer[nr_bytes_recv]= '\0'; // close string
		printf("Client received : %s", buffer);
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
	int s; // master socket descriptor

	s = init_socket();
	handle(s);
	close_socket(s);
	return(0);
}
