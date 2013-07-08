//
// Biolume class implementation file
// Malcolm Doering
//


#include "Biolume.h"

using namespace std;

////////////////////////////////////////////////////////////////////////////
// Biolume instruction function definitions
////////////////////////////////////////////////////////////////////////////

// Execute the instructions in the genes.
// Applies energy cost, executes the current instruction, and increments the pc by 1.
//
void Biolume::executeInstructions()
{
	int i;
    
    // check for skip
    if (skip) 
        skip = 0;
    
    else
    {
        // pay the energy cost of execution
        energy = -1.0 * ENERGY_DECAY;
        
        // update the previous display buffer
        for (i=0; i<NUM_ACT; i++)
            displayPrevious[i] = displayCurrent[i];
        
        // execute next instruction
        switch (genes[pc])
        {
                // no operation
            case NOP:
                nop();
                break;
                
                // set the first LED to on
            case LED0_ON:
                LED0On();
                break;
                
                // set the first LED to off
            case LED0_OFF:
                LED0Off();
                break;
                
                // toggle the first LED between off and the previous state
            case LED0_TOGGLE:
                LED0Toggle();
                break;
                
                // set the second LED to on
            case LED1_ON:
                LED1On();
                break;
                
                // set the second LED to off
            case LED1_OFF:
                LED1Off();
                break;
                
                // toggle the second LED between off and the previous state
            case LED1_TOGGLE:
                LED1Toggle();
                break;
                
                // set the speaker to on
            case SOUND_ON:
                speakerOn();
                break;
                
                // set the speaker to off
            case SOUND_OFF:
                speakerOff();
                break;
                
                // jump to label
            case JUMP:
                jumpToLabel();
                break;
                
                // set the jump point to the next instruction in the genes
            case LABEL:
                setLabel();
                break;
                
                // emit the data stored in buffer
            case MESSAGE_SEND:
                // TODO: send a message to manager
                break;
                
                //place the message buffer data into the display buffer
            case MESSAGE_RECEIVE:
                messageToBuffer();
                break;
                
                // place the current display data into the display buffer
            case BUFFER_SET_DATA:
                displayToBuffer();
                break;
                
                // set the current display data to what is in the display buffer
            case BUFFER_GET_DATA:
                bufferToDisplay();
                break;
                
                // get motion camera sensor data and set condition flag
            case IF_MOTION:
                ifMotion();
                break;
                
            case IF_N_MOTION:
                ifNotMotion();
                break;
                
                // get microphone data and set condition flag                
            case IF_SOUND:
                ifSound();
                break;
                
                // get microphone data and set condition flag                
            case IF_N_SOUND:
                ifNotSound();
                break;

                // get touch sensor data and set condition flag
            case IF_TOUCH:
                ifTouch();
                break;
                
                // get touch sensor data and set condition flag
            case IF_N_TOUCH:
                ifNotTouch();
                break;
                
                // get CO2 sensor data and set condition flag
            case IF_CO2:
                ifCO2();
                break;
                
                // get CO2 sensor data and set condition flag
            case IF_N_CO2:
                ifNotCO2();
                break;
                
            case IF_HIGH_ENERGY:
                ifHighEnergy();
                break;
                
            case IF_LOW_ENERGY:
                ifLowEnergy();
                break;
                
                // determine reproduction based on probability and send message to manager
            case REPRODUCE:
                reproduceCheck();
                break;
                
            default:
                break;
        }
    }
    
    // TODO - set actuators based on values in the current display array
    
    // increment circular PC
    pc = (pc + 1) % size;
};


// For the most part all the instruction functions do right now is print a message that they are executing.
// These function will have to be filled in later once sensors/actuators are working.

// No-operation
//
void Biolume::nop()
{
    // do nothing
    
    printf("nop\n");
    
    return;
};


// Turn on LED 0
//
void Biolume::LED0On()
{
    displayCurrent[LED0] = genes[EXECUTABLE_GENOME_SIZE_MAX + LED0];
    
    printf("LED0On\n");
};


// Turn off LED 0
//
void Biolume::LED0Off()
{
    displayCurrent[LED0] = 0;
    
    printf("LED0Off\n");
};


// Toggle LED 0
//
void Biolume::LED0Toggle()
{
    if (displayPrevious[LED0] != 0)
        displayCurrent[LED0] = genes[EXECUTABLE_GENOME_SIZE_MAX + LED0];
    
    else
        displayCurrent[LED0] = 0;
        
    printf("LED0Toggle\n");
};


// Turn on LED 1
//
void Biolume::LED1On()
{
    displayCurrent[LED1] = genes[EXECUTABLE_GENOME_SIZE_MAX + LED1];
    
    printf("LED1On\n");
};


// Turn off LED 1
//
void Biolume::LED1Off()
{
    displayCurrent[LED1] = 0;
    
    printf("LED1Off\n");
};


// Toggle LED 1
//
void Biolume::LED1Toggle()
{
    if (displayPrevious[LED1] != 0)
        displayCurrent[LED1] = genes[EXECUTABLE_GENOME_SIZE_MAX + LED1];
    
    else
        displayCurrent[LED1] = 0;
    
    printf("LED1Toggle\n");
};


// Turn on speaker
//
void Biolume::speakerOn()
{
    displayCurrent[SPEAKER] = genes[EXECUTABLE_GENOME_SIZE_MAX + SPEAKER];
    
    printf("speakerOn\n"); 
};


// Turn off speaker
//
void Biolume::speakerOff()
{
    displayCurrent[SPEAKER] = 0;
    
    printf("speakerOff\n");
};


// JUMP forward in intructions instruction
//
void Biolume::jumpToLabel()
{
    // jump to label
    // 1 is subtracted because the pc will be incremented before returning from the biolumeStep function
    if (label == -1)
        pc = label;
    
    // set label at the end of the instructions list
    else if (label == 0)
        pc = size - 1;
    
    else
        pc = label - 1;
    
    printf("jumpToLabel\n");
};


// LABEL the next instruction as the point to JUMP to
//
void Biolume::setLabel()
{
    // set the jump point to the next instruction in the genes
    label = (pc + 1) % size;
    
    printf("setLabel\n");
};


// Used by BUFFER_SET_DATA instruction
// Copy current display into the display buffer
//
void Biolume::displayToBuffer()
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        displayBuffer[i] = displayCurrent[i];
    
    printf("displayToBuffer\n");
};


// Used by BUFFER_GET_DATA instruction
// Copy display buffer to current display
//
void Biolume::bufferToDisplay()
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        displayCurrent[i] = displayBuffer[i];
    
    printf("bufferToDisplay\n");
};


// Used by MESSAGE_SEND instruction
// Sends a message to the manager
//
void Biolume::broadcastNeighborhood()
{
	// send a message to the manager / neighboring biolumes
	
	printf("broadcastNeighborhood\n");
};


// Used by MESSAGE_RECEIVE instruction
// Save received broadcast into display buffer
//
void Biolume::messageToBuffer()
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        displayBuffer[i] = messageBuffer[i];
     
     printf("messageToBuffer\n");
};


// If motion conditional
//
void Biolume::ifMotion()
{
    // TODO: if (biolume_api_camera_motion_check)
    
    printf("ifMotion\n");
};


// If not motion conditional
//
void Biolume::ifNotMotion()
{
    // TODO: if (!biolume_api_camera_motion_check)
    
    printf("ifNotMotion\n");  
};


// If sound conditional
//
void Biolume::ifSound()
{
    // TODO: if (biolume_api_microphone_check)
    
    printf("ifSound\n");
};


// If not sound conditional
//
void Biolume::ifNotSound()
{
    // TODO: if (!biolume_api_microphone_check)
    
    printf("ifNotSound\n");
};


// If touch conditional
//
void Biolume::ifTouch()
{
    // TODO: if (biolume_api_touch_sensor_check)
    
    printf("ifTouch\n");
};


// If not touch conditional
//
void Biolume::ifNotTouch()
{
    // TODO: if (!biolume_api_touch_sensor_check)
    
    printf("ifNotTouch\n"); 
};


// If CO2 conditional
//
void Biolume::ifCO2()
{
    // TODO: if (biolume_api_c02_sensor_check)
    
    printf("ifCO2\n");
};


// If not CO2 conditional
//
void Biolume::ifNotCO2()
{
    // TODO: if (!biolume_api_c02_sensor_check)
    
    printf("ifNotCO2\n");
};


// If low energy conditional
//
void Biolume::ifLowEnergy()
{
    if (energy < ENERGY_MAX / 2) 
        skip = 1;
        
    printf("ifLowEnergy\n"); 
};


// If high energy conditional
//
void Biolume::ifHighEnergy()
{
    if (energy > ENERGY_MAX / 2) 
        skip = 1;
    
	printf("ifHighEnergy\n");
};


// Reproduce instruction
//
void Biolume::reproduceCheck()
{
	printf("reproduceCheck\n");
	
	// this should only reproduce if energy is over a certain level.
	// add code later
	
	clientConnectAndSendGenes();
};


////////////////////////////////////////////////////////////////////////////
// Server functions
////////////////////////////////////////////////////////////////////////////

// Setup server
// This function does everything necessary to set up a server for listening on port serverPortNumber.
// It returns an int which indicates whether or not there was an error.
//		0 - no error
//		1 - socket did not open
//		2 - could not bind to socket
// When successful the server will begin listening for incoming connections (from other biolumes).
//
int Biolume::setupServer()
{
	int errorFlag = 0;
	
	// initialize server for listening
	
	// get socket
	serverSocketFD = socket(AF_INET, SOCK_STREAM, 0);
	if (serverSocketFD < 0)
	{
		cout << "Error opening socket." << endl;
		errorFlag = 1;
		goto RETURN;
	}
	
	fcntl(serverSocketFD, F_SETFL, O_NONBLOCK); // make non-blocking
	
	// set information for connecting over the internet
	bzero((char*)&serverAddress, sizeof(serverAddress));
	serverAddress.sin_family = AF_INET;
	serverAddress.sin_addr.s_addr = INADDR_ANY;
	serverAddress.sin_port = htons(serverPortNumber);
	
	// bind the socket to an address
	if (bind(serverSocketFD, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) < 0)
	{
		cout << "Error on binding." << endl;
		errorFlag = 2;
		goto RETURN;
	}
		
	listen(serverSocketFD, 5);
	
	incomingClientLength = sizeof(incomingClientAddress);
	
RETURN:
	return errorFlag;
};


// Check for new genes
// This function checks for incoming connection requests.
// If there is one it will connect, recieve a message (genes information), then disconnect.
// It returns an int which indicates whether or not there was an error.
//		0 - no error
//		3 - couldn't read from socket
//		7 - there are no incoming connections (this isn't really an error since there will not always be biolumes sending messages)
// When successful the server will receive genes and executable genome size from another biolume and then set
//		this biolumes genes and size to what was recieved.
// After this the biolume data should be reset so the biolume can start running fresh from pc = 0;
//
// This function currently does not do anything if there is more that one incoming connection (ie. connections in the listen backlog are ingnored).
//		This will have to be dealt with in the future.
//
int Biolume::serverCheckForNewGenes()
{
	int errorFlag = 0;
	
	// check for an incoming connection
	serverNewSocketFD = accept(serverSocketFD, (struct sockaddr*)&incomingClientAddress, &incomingClientLength);
	if (serverNewSocketFD >= 0)
	{
		// receive the genes being sent
		bzero(genesBuffer, sizeof(genesBuffer));
		
		printf("	Receiving genes...\n");
		messageSize = read(serverNewSocketFD, genesBuffer, sizeof(genesBuffer));
		
		if (messageSize < 0)
		{
			cout << "	Error reading from the socket." << endl;
			errorFlag = 3;
			goto CLOSE;
		}
		
		for (int j=0; j<GENOME_SIZE; j++)
			genes[j] = genesBuffer[j];
		size = genesBuffer[GENOME_SIZE];
			
		printf("	Complete.\n");
	}
	else
		errorFlag = 7; // no incoming connections
	
CLOSE:
	close(serverNewSocketFD);
	return errorFlag;
};


// Set server port number
// In the future this shouldn't be used. All biolumes will use the same port number (set in Constants.h)
//
void Biolume::setServerPortNumber(int portNumber)
{
	serverPortNumber = portNumber;
};


////////////////////////////////////////////////////////////////////////////
// client functions
////////////////////////////////////////////////////////////////////////////

// Connect and send genes
// This function connects to the server on port clientPortNumber on the local host (this will have to be changed) and sends genesBuffer.
// It returns an int which indicates whether or not there was an error.
//		0 - no error
//		1 - couldn't open the socket
//		4 - host not found
//		5 - couldn't connect
//		6 - couldn't write to the socket
// When successful this function will sent the genesBuffer which contains the genes and executable genome size of this biolume to another biolume.
//
// Later make this function so that it can take a biolume index as input.
// It will use this index to look up connection information about the indexed biolume.
// Then it will send the genes to that biolume
//
int Biolume::clientConnectAndSendGenes()
{
	int errorFlag = 0;
	
	clientSocketFD = socket(AF_INET, SOCK_STREAM, 0);
	
	// set the information for connecting to the neighbor
	if (clientSocketFD < 0)
	{
		cout << "	Error opening socket." << endl;
		errorFlag = 1;
		goto CLOSE;
	}
		
	otherBiolumeHostInformation = gethostbyname("localhost"); // this will have to later be changed so that genes can be sent to any neighboring biolume
	if (otherBiolumeHostInformation == NULL)
	{
		cout << "	Host not found." << endl;
		errorFlag = 4;
		goto CLOSE;
	}
	
	bzero((char*)&otherBiolumeServerAddress, sizeof(otherBiolumeServerAddress));
	otherBiolumeServerAddress.sin_family = AF_INET;
	bcopy((char*)otherBiolumeHostInformation->h_addr, (char*)&otherBiolumeServerAddress.sin_addr.s_addr, otherBiolumeHostInformation->h_length);
	otherBiolumeServerAddress.sin_port = htons(clientPortNumber);
	
	// attempt to connect to the neighbor
	if (connect(clientSocketFD, (struct sockaddr*)&otherBiolumeServerAddress, sizeof(otherBiolumeServerAddress)) < 0) 
	{
		cout << "	Error connecting." << endl;
		errorFlag = 5;
		goto CLOSE;
	}
	else
	{
		// send the genes
		printf("	Sending genes...\n");
		messageSize = write(clientSocketFD, genesBuffer, sizeof(genesBuffer));
		
		if (messageSize < 0)
		{
			cout << "	Error writing to socket." << endl;
			errorFlag = 6;
			goto CLOSE;
		}
		
		printf("	Complete.\n");
	}
	
CLOSE:
	close(clientSocketFD);
	return errorFlag;
};


// Set client port number
// In the future this shouldn't be used. All neighbor biolume connection information should be looked up by an index.
//
void Biolume::setClientPortNumber(int portNumber)
{
	clientPortNumber = portNumber;
};


// Copy genes to genesBuffer
// This should be called before clientConnectAndSendGenes() so that the biolume's current genes and size are sent.
// Executable genome size is stored at the end of the buffer.
//
void Biolume::copyGenesToGenesBuffer()
{
	for (int i=0; i<GENOME_SIZE; i++)
		genesBuffer[i] = genes[i];
		
	genesBuffer[GENOME_SIZE] = size;
}


////////////////////////////////////////////////////////////////////////////
// Biolume function declarations for setup and running
////////////////////////////////////////////////////////////////////////////

// Initialize biolume data
// Sets the data, buffers, and displays of the biolume to zero (-1 for label).
// Does not clear the genes, size, or genesBuffer.
//
void Biolume::clearBiolumeData()
{
	// reset data
	pc = 0;
	skip = 0;
	label = -1;     
	energy = 0;
	
	// clear buffers and displays
	int i;
	for (i=0; i<NUM_ACT; i++)
	{
		displayCurrent[i] = 0;   
		displayPrevious[i] = 0;  
		displayBuffer[i] = 0;    
		messageBuffer[i] = 0;    
	}
};


// Randomize biolume's genes
// Randomizes initialGenomeSize genes of the biolume's genes.
// If the input size is too big or too small, size will be set to EXECUTABLE_GENOME_SIZE_MAX or 0, respectively.
// Lastly, this function sets size to the input executable genome size.
//
void Biolume::randomizeGenes(int initialGenomeSize)
{
	// set the executable size of the genome
	if (initialGenomeSize > EXECUTABLE_GENOME_SIZE_MAX)
		size = EXECUTABLE_GENOME_SIZE_MAX;
		
	else if (initialGenomeSize < 0)
		size = 0;
		
	else
		size = initialGenomeSize;
	
	// create random genes
	int j;
	for (j=0; j<size; j++)
		genes[j] = rand() % NUM_INST;
	for (j=EXECUTABLE_GENOME_SIZE_MAX; j<GENOME_SIZE; j++)
		genes[j] = rand() % VARIBALE_MAX;
};


// Run biolume
// Inputs: a port number for the server to use and for the client to use.
// Later this information will be provided in Constants.h and in a table of biolume connection information.
// This function sets up a biolume, sets up the server, executes instructions, and listens for incoming genes.
//
int Biolume::run(int portA, int portB)
{
	serverPortNumber = SERVER_PORT_NUMBER;
	clientPortNumber = CLIENT_PORT_NUMBER;
	
	// later get rid of this
	setServerPortNumber(portA);
	setClientPortNumber(portB);
	
	// initialize genome
	randomizeGenes(EXECUTABLE_GENOME_SIZE_MAX);
	
	// setup the server for listening to incoming connections from other biolumes
	if (setupServer() != 0)
		cout << "Error setting up the server" << endl;
	
	SETUP:
	// clear data for a fresh start
	clearBiolumeData();
	
	// get genes ready to send to other biolumes
	copyGenesToGenesBuffer();
	
	while (true)
	{
		// go through the instructions in the genome
		executeInstructions();
		
		// slow down the execution so that a human can see what is going on
		sleep(1);
		
		// check for genes from other biolumes that are reproducing
		if (serverCheckForNewGenes() == 0)
			// if there are new genes, go to setup and clear data
			goto SETUP;
	}
	
	return 0;
};


