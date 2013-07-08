//
// Biolume class interface file
// Malcolm Doering
//

#ifndef BIOLUME_
#define BIOLUME_


#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>

#include "Constants.h"


////////////////////////////////////////////////////////////////////////////
// Biolume class
//	This class is a beginning to what the final biolume class has to be. It currently does
//		not do mutation after reproduction, most instruction functions do nothing, and
//		a lot of other work needs to be done to flesh out the class.
//
//		More details about functions are provided in the Biolume.cpp file.
////////////////////////////////////////////////////////////////////////////

class Biolume
{
	private:
	
	////////////////////////////////////////////////////////////////////////////
	// Biolume instructions
	////////////////////////////////////////////////////////////////////////////

	enum Instructions
	{
		// nop instruction
		NOP,
		
		// led0 instructions
		LED0_ON, LED0_OFF, LED0_TOGGLE,
		
		// led1 instructions
		LED1_ON, LED1_OFF, LED1_TOGGLE,
		
		// sound instructions
		SOUND_ON, SOUND_OFF,
		
		// jump and set jump point instructions
		JUMP, LABEL,
		
		// message instructions
		BUFFER_SET_DATA, BUFFER_GET_DATA,
		MESSAGE_SEND, MESSAGE_RECEIVE,
		
		// conditional instructions
		IF_MOTION, IF_N_MOTION, IF_SOUND, IF_N_SOUND, IF_TOUCH, IF_N_TOUCH, IF_CO2, IF_N_CO2,
		IF_HIGH_ENERGY, IF_LOW_ENERGY,
		
		// reproduce this biolume with some chance
		REPRODUCE,
		
		// total number of unique instructions
		NUM_INST
	};
	
	
	////////////////////////////////////////////////////////////////////////////
	// Biolume actuators
	////////////////////////////////////////////////////////////////////////////

	enum { LED0, LED1, SPEAKER, NUM_ACT };
	
	
	////////////////////////////////////////////////////////////////////////////
	// Biolume data
	////////////////////////////////////////////////////////////////////////////
	
	int size;				// size of this biolume's executable genes
	int pc;					// current position in the program		
	int skip;				// bool used for condition instructions
	int label;				// to be used with the JUMP instruction
	int genes[GENOME_SIZE];	// the genes (instructions and actuator values)
	
	float energy;					// amount of energy acquired by biolume
	char displayCurrent[NUM_ACT];	// current status of the LEDs and speaker
	char displayPrevious[NUM_ACT];	// previous status of the LEDs and speaker
	char displayBuffer[NUM_ACT];	// buffer for the display and messaging system
	char messageBuffer[NUM_ACT];    // buffer used for messaging other Biolumes

	// Neighbors' IDs - for sending messages
	// This information should be put into a table for lookup by index.
	
	// Data for server to receive genes
	int serverSocketFD;
	int serverNewSocketFD;
	int serverPortNumber;
	socklen_t incomingClientLength;
	struct sockaddr_in serverAddress;
	struct sockaddr_in incomingClientAddress;
	
	// Data for client to send genes
	int clientSocketFD;
	int clientPortNumber;
	struct sockaddr_in otherBiolumeServerAddress;
	struct hostent *otherBiolumeHostInformation;
	
	// Other stuff for sending and receiving using sockets
	int genesBuffer[GENOME_SIZE+1];	// stores the genes and the executable genome size
	int messageSize;
	
	
	////////////////////////////////////////////////////////////////////////////
	// Biolume instruction function declarations
	////////////////////////////////////////////////////////////////////////////

	// Execute the instructions in the genes.
	// Applies energy cost, executes the current instruction, and increments the pc by 1.
	//
	void executeInstructions();

	void nop();

	void LED0On();

	void LED0Off();

	void LED0Toggle();

	void LED1On();

	void LED1Off();

	void LED1Toggle();

	void speakerOn();

	void speakerOff();

	void jumpToLabel();

	void setLabel();

	void displayToBuffer();

	void bufferToDisplay();

	void broadcastNeighborhood();

	void messageToBuffer();

	void ifMotion();

	void ifNotMotion();

	void ifSound();

	void ifNotSound();

	void ifTouch();

	void ifNotTouch();

	void ifCO2();

	void ifNotCO2();

	void ifLowEnergy();

	void ifHighEnergy();

	void reproduceCheck();
	
	
	////////////////////////////////////////////////////////////////////////////
	// Server functions
	////////////////////////////////////////////////////////////////////////////
	
	// Setup server
	//
	int setupServer();

	// Check for new genes
	//
	int serverCheckForNewGenes();
	
	// Set server port number
	//
	void setServerPortNumber(int portNumber);
	
	////////////////////////////////////////////////////////////////////////////
	// Client functions
	////////////////////////////////////////////////////////////////////////////
	
	// Connect and send genes
	//
	int clientConnectAndSendGenes();
	
	// Set client port number
	//
	void setClientPortNumber(int portNumber);
	
	// Copy genes to genesBuffer
	//
	void copyGenesToGenesBuffer();
	
	
	////////////////////////////////////////////////////////////////////////////
	// Biolume function declarations for setup and running
	////////////////////////////////////////////////////////////////////////////
	
	// Initialize biolume data
	//
	void clearBiolumeData();
	
	// Randomize biolume's genes
	//
	void randomizeGenes(int initialGenomeSize);
	
	
	public:
	
	// Run biolume
	//
	int run(int portA, int portB);
};


#endif
