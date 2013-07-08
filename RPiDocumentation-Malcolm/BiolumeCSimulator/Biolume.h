//
//  Biolume.h
//  Biolume
//
//  Created by Malcolm Doering on 5/22/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef Biolume_Biolume_h
#define Biolume_Biolume_h


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include "Constants.h"


// ----------------------------------------------------------------------------
// Biolume structure and utility functions
// ----------------------------------------------------------------------------

// Biolume struct
//
typedef struct biolume_t
{
    // biolume data
    
    // size of this biolume's executable genome
    int size;
    
    // current position in the program
    int pc;
    
    // bool used for condition instructions
    int skip;
    
    // to be used with the JUMP instruction
    int label;
    
    // location coordinates (x,y)
    int location[2];    
    
    // the genome (instructions and actuator values)
    int genome[GENOME_SIZE_MAX];
    
    
    // biolume evolution data
    
    // steps for this biolume
    int age;
    
    // is this biolume ready to reproduce
    int reproduce;      
    
    
    // biolume hardware
    
    // amount of energy acquired by biolume
    int energy;             
    
    // current status of the LEDs and speaker
    char displayCurrent[NUM_ACT];   
    
    // previous status of the LEDs and speaker
    char displayPrevious[NUM_ACT];  
    
    // buffer for the display and messaging system
    char displayBuffer[NUM_ACT];    
    
    // buffer used for messaging other Biolumes
    char messageBuffer[NUM_ACT];    
    
    // sensors?
    
    
    // population
    
    // a pointer to the population of which this Biolume is a part
    void* population;
    
} *Biolume;


// Biolume creation function
//
Biolume biolumeCreate(void* p);


// Biolume initialization function
//
void biolumeInitialize(Biolume b, int x, int y);


// Biolume destroy function
//
void biolumeDestroy(Biolume b);


// Biolume step function
//
void biolumeStep(Biolume b);


// Biolume mutate function
//
void biolumeMutate(Biolume b);


// Biolume instruction copy mutation function
//
void biolumeInstructionCopyMutation(Biolume b);


// Biolume variables copy mutation function
//
void biolumeVariablesCopyMutation(Biolume b);


// Biolume deletion mutation
//
void biolumeDeletionMutation(Biolume b);


// Biolume inesertion mutation
//
void biolumeInsertionMutation(Biolume b);


// Biolume make viable function
//
void biolumeMakeViable(Biolume b);


// Biolume replace genome function
//
void biolumeReplaceGenome(Biolume daughter, Biolume parent);


// Biolume copy function
//
void biolumeCopy(Biolume LHS, Biolume RHS);


// Biolume print function
//
void biolumePrint(Biolume b);


// Biolume reset function
//
void biolumeReset(Biolume b);


// Biolume set message function
// Places a message in the message buffer
//
void biolumeSetMessage(Biolume b, char newMeassage[]);


#endif
