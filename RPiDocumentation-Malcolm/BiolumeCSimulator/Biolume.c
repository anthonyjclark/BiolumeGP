//
//  Biolume.c
//  Biolume
//
//  Created by Malcolm Doering on 5/22/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "Random.h"
#include "Biolume.h"
#include "Instructions.h"


// ----------------------------------------------------------------------------
// Biolume utility functions
// ----------------------------------------------------------------------------

// Biolume creation function
//
Biolume biolumeCreate(void* p)
{
    int i;
    
    // allocate memory for the Biolume struct
    Biolume newBiolume = malloc(sizeof(struct biolume_t));
    
    // initial genome size
    newBiolume->size = GEN_EXE_SIZE_INIT;
    
    // initialize to NOP instructions
    for (i = 0; i < newBiolume->size; i++) 
    {
        newBiolume->genome[i] = NOP;
    }
    
    biolumeReset(newBiolume);
    
    // set pointer to population
    newBiolume->population = p;
    
    return newBiolume;
};


// Biolume random initialization function
//
void biolumeInitialize(Biolume b, int x, int y)
{
    int i;
    
    // initial genome size
    b->size = GEN_EXE_SIZE_INIT;
    
    b->location[0] = x;
    b->location[1] = y;
    
    // initialize to random instructions
    for (i = 0; i < b->size; i++) 
    {
        b->genome[i] = getRandomInt(NUM_INST);
    }
    
    biolumeReset(b);
    
    // initialize to random variables
    for (i = 0; i < NUM_ACT; i++)
    {
        b->genome[GEN_EXE_SIZE_MAX + i] = getRandomInt(127);
    }
};


// Biolume destroy function
//
void biolumeDestroy(Biolume b)
{
    free(b);
};


// Biolume step function
//
void biolumeStep(Biolume b)
{
    int i;
    
    // check for skip
    if (b->skip) 
        b->skip = 0;
        
    else
    {
        // pay the energy cost of execution
        b->energy += -1.0 * ENERGY_DECAY;
        
        // update the previous display buffer
        for (i=0; i<NUM_ACT; i++)
            b->displayPrevious[i] = b->displayCurrent[i];
        
        // execute next instruction
        switch (b->genome[b->pc])
        {
                // no operation
            case NOP:
                nop(b);
                break;
                
                // set the first LED to on
            case LED0_ON:
                LED0On(b);
                break;
                
                // set the first LED to off
            case LED0_OFF:
                LED0Off(b);
                break;
                
                // toggle the first LED between off and the previous state
            case LED0_TOGGLE:
                LED0Toggle(b);
                break;
                
                // set the second LED to on
            case LED1_ON:
                LED1On(b);
                break;
                
                // set the second LED to off
            case LED1_OFF:
                LED1Off(b);
                break;
                
                // toggle the second LED between off and the previous state
            case LED1_TOGGLE:
                LED1Toggle(b);
                break;
                
                // set the speaker to on
            case SOUND_ON:
                speakerOn(b);
                break;
                
                // set the speaker to off
            case SOUND_OFF:
                speakerOff(b);
                break;
                
            case JUMP:
                jumpToLabel(b);
                break;
                
                // set the jump point to the next instruction in the genome
            case LABEL:
                setLabel(b);
                break;
                
                // emit the data stored in buffer
            case MESSAGE_SEND:
                broadcastNeighborhood((Population)b->population, b);
                break;
                
                //place the message buffer data into the display buffer
            case MESSAGE_RECEIVE:
                messageToBuffer(b);
                break;
                
                // place the current display data into the display buffer
            case BUFFER_SET_DATA:
                displayToBuffer(b);
                break;
                
                // set the current display data to what is in the display buffer
            case BUFFER_GET_DATA:
                bufferToDisplay(b);
                break;
                
                // get motion camera sensor data and set condition flag
            case IF_MOTION:
                ifMotion(b);
                break;
                
            case IF_N_MOTION:
                ifNotMotion(b);
                break;
                
                // get microphone data and set condition flag                
            case IF_SOUND:
                ifSound(b);
                break;
                
                // get microphone data and set condition flag                
            case IF_N_SOUND:
                ifNotSound(b);
                break;

                // get touch sensor data and set condition flag
            case IF_TOUCH:
                ifTouch(b);
                break;
                
                // get touch sensor data and set condition flag
            case IF_N_TOUCH:
                ifNotTouch(b);
                break;
                
                // get CO2 sensor data and set condition flag
            case IF_CO2:
                ifCO2(b);
                break;
                
                // get CO2 sensor data and set condition flag
            case IF_N_CO2:
                ifNotCO2(b);
                break;
                
            case IF_HIGH_ENERGY:
                ifHighEnergy(b);
                break;
                
            case IF_LOW_ENERGY:
                ifLowEnergy(b);
                break;
                
                // determine reproduction based on probability and set repro flag
            case REPRODUCE:
                reproduceCheck(b);
                break;
                
            default:
                break;
        }
    }
    
    // increment circular PC
    b->pc = (b->pc + 1) % b->size;
    b->age++;
};
 

// Biolume mutate function
//
void biolumeMutate(Biolume b)
{
    biolumeDeletionMutation(b);
    biolumeInstructionCopyMutation(b);
    biolumeVariablesCopyMutation(b);
    biolumeInsertionMutation(b);

};


// Biolume instruction copy mutation function
//
void biolumeInstructionCopyMutation(Biolume b)
{
    int i;
    
    // copy mutation for the instructions
    for (i=0; i<b->size; i++)
        if (getRandomUniform() < MUT_PROB_COPY)
            b->genome[i] = getRandomInt(NUM_INST);
};


// Biolume variables copy mutation function
//
void biolumeVariablesCopyMutation(Biolume b)
{
    int i;
    
    // copy mutation for the variables
    for (i = 0; i < NUM_ACT; i++) 
        if (getRandomUniform() < MUT_PROB_COPY) 
            b->genome[GEN_EXE_SIZE_MAX + i] += (getRandomGaussian() * 10 - 5);
};


// Biolume deletion mutation
//
void biolumeDeletionMutation(Biolume b)
{
    int i, deletionLocation;
    
    // deletion mutation (must not go below minimum)
    if ((getRandomUniform() < MUT_PROB_DELETE) && (b->size > GEN_EXE_SIZE_MIN)) 
    {
        // get deletion location
        deletionLocation = getRandomInt(b->size);
        
        b->size--;
        
        // shift
        for (i=deletionLocation; i<b->size; i++) 
            b->genome[i] = b->genome[i+1];
    }
};


// Biolume inesertion mutation
//
void biolumeInsertionMutation(Biolume b)
{
    int i, insertionLocation;
    char swap1, swap2;
    
    // insertion mutation (must have room for insertion)
    if ((getRandomUniform() < MUT_PROB_INSERT) && (b->size < GEN_EXE_SIZE_MAX)) 
    {
        b->size++;
        
        // get insertion location
        insertionLocation = getRandomInt(b->size);
        
        // insert
        swap1 = b->genome[insertionLocation];
        b->genome[insertionLocation] = getRandomInt(NUM_INST);
        
        // shift
        for (i=insertionLocation+1; i<b->size; i++)
        {
            swap2 = b->genome[i];
            b->genome[i] = swap1;
            swap1 = swap2;
        }
    }
};


// Biolume make viable function
//
void biolumeMakeViable(Biolume b)
{
    int i, swap1, swap2;
    
    // check if this Biolume is already viable. if so, escape
    for (i=0; i<b->size; i++)
        if (b->genome[i] == REPRODUCE)
            return;
    
    // make viable
    else
    {
        // get insertion location
        int insertionLocation = getRandomInt(b->size);
        
        // attempt insertion mutation. if it fails, perform a point mutation
        if (b->size < GEN_EXE_SIZE_MAX)
        {
            b->size++;
            
            // insert
            swap1 = b->genome[insertionLocation];
            b->genome[insertionLocation] = REPRODUCE;
            
            // shift
            for (i=insertionLocation+1; i<b->size; i++)
            {
                swap2 = b->genome[i];
                b->genome[i] = swap1;
                swap1 = swap2;
            }
        }
        
        // point mutation
        else
            b->genome[insertionLocation] = REPRODUCE;
    }
};


// Biolume replace genome function
//
void biolumeReplaceGenome(Biolume daughter, Biolume parent)
{
    // replace genome
    int i;
    for (i = 0; i < GENOME_SIZE_MAX; i++) 
    {
        daughter->genome[i] = parent->genome[i];
    }
    
    // daughter executes the same number of instructions as the parent
    daughter->size = parent->size;
};


// Biolume copy function
//
void biolumeCopy(Biolume LHS, Biolume RHS)
{
    int i;
    
    LHS->size = RHS->size;
    LHS->pc = RHS->pc;
    LHS->skip = RHS->skip;
    LHS->age = RHS->age;
    LHS->reproduce = RHS->reproduce;
    LHS->energy = RHS->energy;
    
    for (i=0; i<GEN_EXE_SIZE_MAX; i++)
        LHS->genome[i] = RHS->genome[i];
    
    for (i=0; i<NUM_ACT; i++)
    {
        LHS->displayCurrent[i] = RHS->displayCurrent[i];
        LHS->displayPrevious[i] = RHS->displayPrevious[i];
        LHS->displayBuffer[i] = RHS->displayBuffer[i];
        LHS->messageBuffer[i] = RHS->messageBuffer[i];
    }
};


// Biolume print function
//
void biolumePrint(Biolume b)
{
    int i;
    
    printf("[");
    
    for (i = 0; i < NUM_ACT; i++)
    {
        printf("%03d", b->displayCurrent[i]);
        if (i < NUM_ACT - 1) printf("|");
    }
    
    printf("]");
};


// Biolume reset function
//
void biolumeReset(Biolume b)
{
    int i;
    
    // initialize state information
    b->pc = 0;
    b->label = -1;
    b->skip = 0;
    b->age = 0;
    b->reproduce = 0;
    b->energy = 0;
    
    // clear message and display buffers
    for (i = 0; i < NUM_ACT; i++)
    {
        b->displayCurrent[i] = 0;
        b->displayPrevious[i] = 0;
        b->displayBuffer[i] = 0;
        b->messageBuffer[i] = 0; 
    }
};


// Biolume set message function
// Places a message in the message buffer
//
void biolumeSetMessage(Biolume b, char newMeassage[])
{
    int i;
    
    for (i = 0; i < NUM_ACT; i++)
        b->messageBuffer[i] = newMeassage[i];
    
};

