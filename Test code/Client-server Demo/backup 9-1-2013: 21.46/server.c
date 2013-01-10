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
#include <pthread.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <time.h>

#define SERVER_PORT 7000
#define NAME_SERVER_PORT 9001
#define STORAGE_SERVER_PORT 9002
#define PEER_PORT 9000
#define BUFFSIZE 256
#define ARRAY_LENGTH 33
#define MAX_DOWNLOADS 8
#define IP_ADDRESSES 8

struct sockaddr_in gv_address; // struct sockeaddr_in defined in socket.h
char *parameters[ARRAY_LENGTH];
char *ip_addresses[IP_ADDRESSES];
char *storageserver;
char *nameserver;
char *ip_address = "192.168.1.1\n";
char messagebuffer[BUFFSIZE];
char message_buffer[BUFFSIZE];
char *message;

int ss, ns, p = 0;

struct start {
	char *filename;
	char *filesize;
	char *blockcount;
	char *download_info;
};
struct start start_parameters[MAX_DOWNLOADS];

int init_socket(void);
int init_server_sockets(char*, int);
int init_outgoing_socket(char*, int);
void listen_and_handle(int s);
void handle(int* sPtr);
void handle_name_server(int);
void handle_storage_server(int);
void close_socket(int);
void function_start(void);

int init_socket(void) {	 
	int rv, s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
		//exit(EXIT_FAILURE);
	}
	gv_address.sin_family = AF_INET;
	gv_address.sin_addr.s_addr = INADDR_ANY;
	gv_address.sin_port = htons(SERVER_PORT);

	rv = bind(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error binding to socket");
		//exit(EXIT_FAILURE);
	}
	
	return s;
}

int init_server_sockets(char *server, int port) {	 
	int rv, s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
		//exit(EXIT_FAILURE);
	}

	gv_address.sin_family = AF_INET; 
	gv_address.sin_addr.s_addr = inet_addr(server);
	//printf("Nameserver IP = %s \n", nameserver_ip);
	//gv_address.sin_addr.s_addr = inet_addr ("192.168.8.111");
	gv_address.sin_port = htons(port);

	rv = connect(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error connecting to server socket");
		//exit(EXIT_FAILURE);
	}
	
	return s;
}

int init_outgoing_socket(char *peer, int port) {	 
	int rv, s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
	}

	gv_address.sin_family = AF_INET; 
	gv_address.sin_addr.s_addr = inet_addr(peer);
	gv_address.sin_port = htons(port);

	rv = connect(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error connecting to server socket");
	}
	
	return s;
}

void close_socket(int s) {	 
	int rv = close(s);
	if (rv < 0) {
		perror("Error closing socket");
		//exit(EXIT_FAILURE);
	}
}

void listen_and_handle(int ms) {
	int rv, cs;
	pthread_t main_thread; /* array of thread variables */

	rv = listen(ms, 2);
	if (rv < 0) {
		perror("Error listening to socket");
		//exit(EXIT_FAILURE);
	}

	unsigned int addr_len = sizeof(gv_address);
	cs = accept(ms, (struct sockaddr *)&gv_address, &addr_len);
	if (cs < 0) {
		printf("Error accept");
		//exit(EXIT_FAILURE);
	}

	rv = pthread_create (&main_thread, NULL, (void *)&handle, (void *)&cs);
	if (rv != 0) {
		perror("Error creating thread");
		//exit(EXIT_FAILURE);
	}

	rv = pthread_join(main_thread, NULL);
	if (rv != 0) {
		perror("Error listening to socket");
		//exit(EXIT_FAILURE);
	}
}

void handle(int* sPtr) {
	int rv = 0, s = *sPtr, nr_bytes_recv = 0, nr = 0;
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';
	pthread_t start;

	while(1) {
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);
		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			//exit(EXIT_FAILURE);
		}
		buffer[nr_bytes_recv]= '\0';
		printf("%s", buffer);

		char *rest; 
        char *token; 
        char *ptr = buffer; 

        // loop till strtok_r returns NULL.
        while((token = strtok_r(ptr, "|", &rest))) {
        	if(strncmp(token, "STOP", 4) == 0) {
        		ptr = rest;
        	}
        	else {
	        	parameters[nr] = token;
	           	ptr = rest;    
	       		printf("Arraynr %d : %s\n", nr, parameters[nr]);
	       		nr++;
	       	}
        }

        // SET NAMESERVER IP & STORAGESERVER IP
        // Parameters: SETTINGS|nameserverip|storageserverip
        if(strncmp(buffer, "SET", 3) == 0) {
        	nameserver = malloc(sizeof(ip_address));
        	storageserver = malloc(sizeof(ip_address));
        	strncpy(nameserver, parameters[1], 20);
        	strncpy(storageserver, parameters[2], 20);

			printf("nameserver %s\n", nameserver);
			printf("storageserver %s\n", storageserver);

        	if (rv < 0) {
        		message = "FAIL\n";
        		rv = send(s, message, nr_bytes_recv,0);
				perror("Error sending");
				//exit(EXIT_FAILURE);
			}
			else {
				message = "OK\n";
				rv = send(s, message, nr_bytes_recv,0);
			}
        	nr = 0;
        }

        // START ROUTER DOWNLOAD
        // Parameters:  START|filename|filesize|block_count|download_info_string
        if(strncmp(buffer, "START", 5) == 0) {
			int i, j = 1;
			for(i = 0; i < MAX_DOWNLOADS; i++) {
				start_parameters[i].filename = parameters[j];
				j++;
				start_parameters[i].filesize = parameters[j];
				j++;
				start_parameters[i].blockcount = parameters[j];
				j++;
				start_parameters[i].download_info = parameters[j];
				j++;
			}

			for(i = 0; i < MAX_DOWNLOADS; i++) {
				if(start_parameters[i].filename == NULL) {
					break;
				}
				else {
					printf("Struct %d:\n Filename = %s, Filesize = %s, Blockcount = %s, Download_info = %s.\n\n", i, start_parameters[i].filename, start_parameters[i].filesize, start_parameters[i].blockcount, start_parameters[i].download_info);
				}
			}

        	printf("--> Start Router Download....\n");
        	if (rv < 0) {
        		message = "FAIL\n";
        		rv = send(s, message, nr_bytes_recv,0);
				perror("Error sending");
				//exit(EXIT_FAILURE);
			}
			else {
				message = "OK\n";
				rv = send(s, message, nr_bytes_recv,0);
			}
			nr = 0;

			rv = pthread_create (&start, NULL, (void *)&function_start, NULL);
        	if (rv != 0) {
				perror("Error creating thread");
				//exit(EXIT_FAILURE);
			}
        }

		else if(strncmp(buffer, "STOP", 4) == 0) {
			nr = 0;
			// Cancel the start-thread
			rv = pthread_cancel(start);
    		if (rv != 0) {
        		perror("pthread_cancel");
        	}

			printf("--> Stop Router Download...\n");
			if (rv < 0) {
        		message = "FAIL\n";
        		rv = send(s, message, nr_bytes_recv,0);
				perror("Error sending");
				//exit(EXIT_FAILURE);
			}
			else {
				 
				strcpy(messagebuffer, "OK");
				int i;
				for(i = 0; i < MAX_DOWNLOADS; i++) {
					if(start_parameters[i].filename == NULL) {
						break;
					} 
					else {
						strcat(messagebuffer, "|");
						strcat(messagebuffer, start_parameters[i].filename);
						strcat(messagebuffer, "|");
						strcat(messagebuffer, start_parameters[i].filesize);
						strcat(messagebuffer, "|");
						strcat(messagebuffer, start_parameters[i].blockcount);
						strcat(messagebuffer, "|");
						strcat(messagebuffer, start_parameters[i].download_info);
					}
				}
				rv = send(s, messagebuffer, BUFFSIZE,0);
			}

			if(start_parameters[0].filename != NULL) {
        		int y;
        		for(y = 0; y < MAX_DOWNLOADS; y++) {
        			start_parameters[y].filename = NULL;
					start_parameters[y].filesize = NULL;
					start_parameters[y].blockcount = NULL;
					start_parameters[y].download_info = NULL;
        		}
        	}

        	int z;
        	for(z = 0; z < ARRAY_LENGTH; z++) {
        		parameters[z] = NULL;
        	}
		}
	}		
}

void handle_storage_server(int s) {
	//int rv = 0; // return value

	// create and init a char buffer
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';

	int nr_bytes_recv = 0;

	while (!nr_bytes_recv) {
		/*char *list = "READY\n";
		rv = send(s, list, strlen(list), 0);
		if (rv < 0) {
			perror("Error sending");
			//exit(EXIT_FAILURE);
		}*/

		// receive data in buffer
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			//exit(EXIT_FAILURE);
		}

		buffer[nr_bytes_recv]= '\0'; // close string
		printf("Storageserver: %s", buffer);
	}
}

void handle_name_server(int s) {
	printf("Nameserver: OK\n");
	int rv = 0; // return value

	// create and init a char buffer
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';

	int nr_bytes_recv = 0;

	while (!nr_bytes_recv) {
		char *list = "LIST\n";
		rv = send(s, list, strlen(list), 0);
		if (rv < 0) {
			perror("Error sending");
			//exit(EXIT_FAILURE);
		}

		// receive data in buffer
		nr_bytes_recv = recv(s, buffer, BUFFSIZE, 0);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
			//exit(EXIT_FAILURE);
		}

		buffer[nr_bytes_recv]= '\0'; // close string
	}

	int nr = 0;
	char *rest; 
    char *token; 
    char *ptr = buffer; 

    // loop till strtok_r returns NULL.
    while((token = strtok_r(ptr, "|", &rest))) {
    	if(strncmp(token, "STOP", 4) == 0) {
    		ptr = rest;
    	}
    	else {
        	ip_addresses[nr] = token;
           	ptr = rest;    
           	printf("Nameserver -> IP-address peer: %s\n", ip_addresses[nr]);
       		nr++;
       	}
    }
	ip_addresses[nr + 1] = '\0';
}

void function_start(void) {
	// Storage server
	if(ss == 0) {
		printf("STORAGESERVER INITIALIZED!\n");
		ss = init_server_sockets(storageserver, STORAGE_SERVER_PORT);
		handle_storage_server(ss);
	}	
	// Name server
	if(ns == 0) {
		printf("NAMESERVER INITIALIZED!\n");
		ns = init_server_sockets(nameserver, NAME_SERVER_PORT);
		handle_name_server(ns);
	}
	// Connect to Peer for downloading
	// router -> Peer: GET|filename|blocknr
	int i, j;
	for(i = 0; i < MAX_DOWNLOADS; i++) {
		if(start_parameters[i].filename == NULL) {
			break;
		}
		else {
			for(j = 0; j < IP_ADDRESSES; j++) {
				if(ip_addresses[j] == '\0') {
					break;
				}
				else {
					int rv = 0, nr_bytes_recv = 0;
					char *buffer = malloc(BUFFSIZE);
					//char *new_download_info = "\0";
					p = init_outgoing_socket(ip_addresses[j], PEER_PORT);
					int total = atoi(start_parameters[i].blockcount);
					int last_block = atoi(start_parameters[i].download_info);

					while(last_block <= total) {
						sprintf(message_buffer, "GET|%s|%d\n", start_parameters[i].filename, last_block);
						printf("message_buffer = %s\n", message_buffer);
						rv = send(p, message_buffer, BUFFSIZE, 0);
						if (rv < 0) {
							perror("Error sending");
							//exit(EXIT_FAILURE);
						}

						// Wait until block is received
						nr_bytes_recv = recv(p, buffer, BUFFSIZE, 0);
						if (nr_bytes_recv > BUFFSIZE) {
							perror("Buffer too small");
							//exit(EXIT_FAILURE);
						}
						buffer[nr_bytes_recv]= '\0';
						printf("%s", buffer);

						//POST|filename|filesize|blocknr
						sprintf(message_buffer, "POST|%s|%s|%d\n", start_parameters[i].filename, start_parameters[i].filesize, last_block);
						rv = send(ss, message_buffer, BUFFSIZE, 0);
						//rv = send(ss, message_buffer2, BUFFSIZE, 0);
						if (rv < 0) {
							perror("Error sending");
							//exit(EXIT_FAILURE);
						}

						last_block++;
						//snprintf(new_download_info, 10, "%d", last_block);
						//start_parameters[i].download_info = new_download_info;
					}
				}
			}
		}
	}	
}

int main() {
	int ms = init_socket();
	listen_and_handle(ms);
	close_socket(ms);
	close_socket(ns);
	close_socket(ss);
	close_socket(p);
	return(0);
}

