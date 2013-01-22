#define PEER_PORT 9000
#define NAME_SERVER_PORT 9001
#define STORAGE_SERVER_PORT 9002
#define IP_ADDRESSES 8
#define MAX_DOWNLOADS 8
#define BUFFSIZE 256
#define BLOCK_SIZE 64*1024
#define ARRAY_LENGTH 33

extern char *nameServerIp;
extern char *storageServerIp;
extern char *OK_RESPONSE;
extern char *SPLIT_CHAR;
extern char *FAIL_RESPONSE;
extern char *SPLIT_CHAR;
extern char *nameServerIp;
extern char *storageServerIp;

struct sockaddr_in gv_address;
struct start {
	char *filename;
	char *filesize;
	char *blockcount;
	char *download_info;
};
struct start start_parameters[MAX_DOWNLOADS];
int ns = 0, ss = 0, peer = 0;

pthread_t downloadThread;

char *SET_COMMAND = "SET";
char *START_COMMAND = "START";
char *STOP_COMMAND = "STOP";
char *OK_RESPONSE = "OK\n";
char *OK_RESPONSE_WITHOUT_LINE_END = "OK";
char *FAIL_RESPONSE = "FAIL\n";

char *parameters[ARRAY_LENGTH];
char *ip_addresses[IP_ADDRESSES];
char message_buffer[BUFFSIZE];

void handle(int *);
void handleCommand(char *, int);
int sgetline(int, char **);
void sendError(int);
void handle_storage_server(int);
void handle_name_server(int);
int init_outgoing_socket(char *, int);
void function_start(void);
void stopDownload(int fd);
extern void sendError(int fd);

void handle(int *sPtr) {
	int sockfd = *sPtr; 
    while(1) {
		char *tbuf = malloc(BUFFSIZE);
		int ret;
		ret = sgetline(sockfd, &tbuf);
		
		if (ret < 0) {
			free(tbuf);
			break;
		}
		if (ret == 0) {
		   free(tbuf);
		   continue;
		}
	   	handleCommand(tbuf, sockfd);
		free(tbuf);
	}
}

void handleCommand(char *command, int fd) {
	int rv = 0;
	printf("handleCommand: %s \n", command);
	char *rest = "";
	char *token = ""; 
	char *ptr = command;
	int parameterCount = 0;
	
	// loop till strtok_r returns NULL.
	while((token = strtok_r(ptr, SPLIT_CHAR, &rest))) {        	
		parameters[parameterCount] = token;
		ptr = rest;    
		parameterCount++;
	}
	
	if(parameterCount <= 0)
		return;
	        
	//SET|namserverip|storageserverip
	if(strncmp(parameters[0], SET_COMMAND, strlen(SET_COMMAND)) == 0) {
		if(parameterCount != 3) {
			sendError(fd);
			return;
		}

		nameServerIp = malloc(strlen(parameters[1]));
		storageServerIp = malloc(strlen(parameters[2]));
		strcpy(nameServerIp, parameters[1]);
		strcpy(storageServerIp, parameters[2]);

		printf("Nameserver IP    = %s \n", nameServerIp);
		printf("Storageserver IP = %s \n", storageServerIp);
		
		rv = send(fd, OK_RESPONSE, strlen(OK_RESPONSE), 0);
		if (rv < 0) {
			perror("Error sending");
			exit(EXIT_FAILURE);
		}
	} 

	//START|filename|filesize|block_count|info_string|filename2|filesize2|block_count2|info_string2
	else if(strncmp(parameters[0], START_COMMAND, strlen(START_COMMAND)) == 0){
		if(nameServerIp == NULL || storageServerIp == NULL){
			sendError(fd);
			return;
		}
		int i, j = 1;
		for(i = 0; i < MAX_DOWNLOADS; i++) {
			if(parameters[j] == NULL) {
				break;
			}
			else {
				start_parameters[i].filename = malloc(1024);
				strcpy(start_parameters[i].filename, parameters[j]);
				printf("Filename: %s\n", start_parameters[i].filename);
				j++;
				start_parameters[i].filesize = malloc(10000);
				strcpy(start_parameters[i].filesize, parameters[j]);
				printf("Filesize: %s\n", start_parameters[i].filesize);
				j++;
				start_parameters[i].blockcount = malloc(1024);
				strcpy(start_parameters[i].blockcount, parameters[j]);
				printf("Blockcount: %s\n", start_parameters[i].blockcount);
				j++;
				start_parameters[i].download_info = malloc(1024);
				strcpy(start_parameters[i].download_info, parameters[j]);
				printf("Download_info: %s\n", start_parameters[i].download_info);
				j++;
			}
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
			sendError(fd);
			perror("Error sending");
			return;
		}
		else {
			rv = write(fd, OK_RESPONSE, strlen(OK_RESPONSE));
		}
		rv = pthread_create(&downloadThread, NULL, (void *)&function_start, NULL);
		if (rv != 0) {
			sendError(fd);
			perror("Error creating thread");
			return;
		}
	} 

	//STOP  --> FAIL/OK|filename|latest_full_downloaded_block|filename2|latest_full_downloaded_block2
	else if(strncmp(parameters[0], STOP_COMMAND, strlen(STOP_COMMAND)) == 0) {
		printf("handleCommand:command is stop command\n");
		stopDownload(fd);
		
	} 
	else {
		printf("HandleCommand:command is unknown\n");
	}
}

int sgetline(int fd, char **out)  { 
    int bytesloaded = 0, ret = 0;
    char buf = ' '; 
    char *buffer = malloc(BUFFSIZE); 

    if (NULL == buffer) {
    	return -1;
    }
    do {
        ret = read(fd, &buf, 1);
        if (ret < 1) {
			free(buffer);
            return -1;
        }

        buffer[bytesloaded] = buf; 
        bytesloaded++; 

        if (buf == '\n' || buf == '\r') {
            break;
        }
    } while(1);

    int lineEndings = 0;
    if ((bytesloaded) && (buffer[bytesloaded-1] == '\n' || (buffer[bytesloaded-1] == '\r'))) {	
        bytesloaded--;
		lineEndings = 1;
	}	
    *out = strndup(buffer, strlen(buffer) - lineEndings); 
    return bytesloaded;
}


void sendError(int fd) {
	int rv;
	rv = send(fd, FAIL_RESPONSE, strlen(FAIL_RESPONSE), 0);
	if (rv < 0) {
		perror("Error sending");
		return;
	}
}

void handle_storage_server(int fd) {
	int rv = 0;
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';
	int nr_bytes_recv = 0;

	while (!nr_bytes_recv) {
		char *list = "IS STORAGE SERVER\n";
		rv = write(fd, list, strlen(list));
		if (rv < 0) {
			perror("Error sending");
		}
		nr_bytes_recv = read(fd, buffer, BUFFSIZE);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
		}

		buffer[nr_bytes_recv]= '\0';
	}
}

void handle_name_server(int fd) {
	int rv = 0;
	char *buffer = malloc(BUFFSIZE);
	buffer[0] = '\0';
	int nr_bytes_recv = 0;

	while (!nr_bytes_recv) {
		char *list = "LIST\n";
		rv = write(fd, list, strlen(list));
		if (rv < 0) {
			perror("Error sending");
		}
		nr_bytes_recv = read(fd, buffer, BUFFSIZE);

		if (nr_bytes_recv > BUFFSIZE) {
			perror("Buffer too small");
		}

		buffer[nr_bytes_recv]= '\0';
	}

	int nr = 0;
	char *rest = ""; 
    char *token = ""; 
    char *ptr = buffer; 

    while((token = strtok_r(ptr, SPLIT_CHAR, &rest))) {    	
		ip_addresses[nr] = token;
		ptr = rest;    
		printf("Nameserver -> IP-address peer: %s\n", ip_addresses[nr]);
		nr++;       	
    }
	ip_addresses[nr + 1] = '\0';
}

int init_outgoing_socket(char *address, int port) {	 
	int rv, s = socket(AF_INET, SOCK_STREAM, 0);
	if(s < 0) {
		perror("Error creating socket");
	}

	gv_address.sin_family = AF_INET; 
	gv_address.sin_addr.s_addr = inet_addr(address);
	gv_address.sin_port = htons(port);

	rv = connect(s, (struct sockaddr *)&gv_address, sizeof(gv_address));
	if (rv < 0) {
		perror("Error connecting to server socket");
	}
	
	return s;
}

void function_start(void) {
	if(ns == 0) {
		ns = init_outgoing_socket(nameServerIp, NAME_SERVER_PORT);
		handle_name_server(ns);
	}

	// Connect to Peer for downloading
	// router -> Peer: GET|filename|blocknr
	int curDownload;
	for(curDownload = 0; curDownload < MAX_DOWNLOADS; curDownload++) {
		if(start_parameters[curDownload].filename == NULL) {
			break;
		}
		else {
			int j;
			for(j = 0; j < IP_ADDRESSES; j++) {
				if(ip_addresses[j] == '\0') {
					break;
				}
				else {
					int rv = 0, nr_bytes_recv = 0, i = 0;
					int total = atoi(start_parameters[curDownload].blockcount);
					int info[total];
					while (i < total) {
						
						char infoChar = (char) start_parameters[curDownload].download_info[i];
						info[i] = infoChar - 48;// set to first numeric ansii code
						i++;
					}

					int blockNo = 0;
					while(blockNo < total) {
						printf("Going to download block %d status is %d\n", blockNo, info[blockNo]);
						if(info[blockNo] == 1) {
							blockNo++;
							continue;
						}
						peer = init_outgoing_socket(ip_addresses[j], PEER_PORT);
						sprintf(message_buffer, "GET %s %d\n", start_parameters[curDownload].filename, blockNo);
						printf("Request to peer %s = %s\n",ip_addresses[j], message_buffer);
						rv = send(peer, message_buffer, BUFFSIZE, 0);
						if (rv < 0) {
							perror("Error sending");
						}

						unsigned char *received = malloc(BLOCK_SIZE);
						int received_bytes;
						
						nr_bytes_recv = 0;
						while(nr_bytes_recv < BLOCK_SIZE) {
							received_bytes = recv(peer, received + nr_bytes_recv, BLOCK_SIZE - nr_bytes_recv, 0);
						
							if(received_bytes < 0) {
								break;
							}
							nr_bytes_recv += received_bytes;
							printf("%s\n", received);
						}
						
						char *file = malloc(BLOCK_SIZE + 50);
						sprintf(file, "POST %s %s %d %d\n", start_parameters[curDownload].filename, start_parameters[curDownload].filesize, blockNo * BLOCK_SIZE, nr_bytes_recv);
						
						ss = init_outgoing_socket(storageServerIp, STORAGE_SERVER_PORT);
						
						rv = send(ss, file, strlen(file), 0);
						if (rv < 0) {
							perror("Error sending");
						}
						
						char *reply = malloc(5);
						int firstResponse = recv(ss, reply, 5, 0);
						if(firstResponse < 0) {
							perror("Error receiving");
						}
						printf("sending to storageserver: %d bytes\n", nr_bytes_recv);
						printf("%s\n", received);
						rv = send(ss, received, nr_bytes_recv, 0);
						if (rv < 0) {
							perror("Error sending");
						}

						int secondResponse = recv(ss, reply, 5, 0);
						if(secondResponse < 0) {
							perror("Error receiving");
						}
						printf("Received from storageserver: %s\n", reply);
						free(reply);
						free(received);
						free(file);

						printf("Updating infoblock\n");
						info[blockNo] = 1;
						
						char *infoText = malloc(sizeof(info));//TODO fix x in front of string?
						int blockLooper;
						for(blockLooper = 0; blockLooper < (sizeof(info) / sizeof(int)); blockLooper++)
						{
							int blockStatus = info[blockLooper];
							sprintf(infoText, "%s%d", infoText, blockStatus);
						}
						if (infoText[0] == 'x') {
   							memmove(infoText, infoText+1, strlen(infoText));
   						}
						start_parameters[curDownload].download_info = infoText;
						printf("New download_info = %s\n", infoText);
						printf("Block %d downloaded\n", blockNo);
						blockNo++;
					}
				}
			}
			printf("Download file %s : finished\n", start_parameters[curDownload].filename);//TODO deze melding zie je niet
		}
	}
	printf("Download finished.\n\n");
}

void stopDownload(int fd) {
	printf("--> Stop Router Download...\n");
	int rv = pthread_cancel(downloadThread);
	if (rv != 0 && rv != 3) {
		printf("rv not 0  but %d\n", rv);
		fprintf(stderr, "Cannot cancel thread %s\n", strerror(rv));
	} else {
		char* messagebuffer = malloc(BUFFSIZE);
		strcpy(messagebuffer, OK_RESPONSE_WITHOUT_LINE_END);
		int i;
		for(i = 0; i < MAX_DOWNLOADS; i++) {
			if(start_parameters[i].filename == NULL) {
				break;
			} else {
				strcat(messagebuffer, SPLIT_CHAR);
				strcat(messagebuffer, start_parameters[i].filename);
				strcat(messagebuffer, SPLIT_CHAR);
				strcat(messagebuffer, start_parameters[i].filesize);
				strcat(messagebuffer, SPLIT_CHAR);
				strcat(messagebuffer, start_parameters[i].blockcount);
				strcat(messagebuffer, SPLIT_CHAR);
				strcat(messagebuffer, start_parameters[i].download_info);
			}
		}
		strcat(messagebuffer, "\n");
		printf("%s\n", messagebuffer);
		rv = write(fd, messagebuffer, BUFFSIZE);
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
}
