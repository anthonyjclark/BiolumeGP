//
//  Instructions.c
//  Biolume
//
//  Created by Malcolm Doering on 6/5/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include <stdio.h>

#include "Instructions.h"
#include "Random.h"


// ----------------------------------------------------------------------------
// Functions used to carry out Instructions
// ----------------------------------------------------------------------------

// No-operation
//
void nop(Biolume b)
{
    // do nothing
    return;
};


// Turn on LED 0
//
void LED0On(Biolume b)
{
    b->displayCurrent[LED0] = b->genome[GEN_EXE_SIZE_MAX + LED0];
};


// Turn off LED 0
//
void LED0Off(Biolume b)
{
    b->displayCurrent[LED0] = OFF;
};


// Sets values of LED 0 to that of the previous state, whatever that may be
//
void LED0Toggle(Biolume b)
{
    if (b->displayPrevious[LED0] != OFF)
        b->displayCurrent[LED0] = b->genome[GEN_EXE_SIZE_MAX + LED0];
    
    else
        b->displayCurrent[LED0] = OFF;
};


// Turn on LED 1
//
void LED1On(Biolume b)
{
    b->displayCurrent[LED1] = b->genome[GEN_EXE_SIZE_MAX + LED1];
};


// Turn off LED 1
//
void LED1Off(Biolume b)
{
    b->displayCurrent[LED1] = OFF;
};


// Sets values of LED 1 to that of the previous state, whatever that may be
//
void LED1Toggle(Biolume b)
{
    if (b->displayPrevious[LED1] != OFF)
        b->displayCurrent[LED1] = b->genome[GEN_EXE_SIZE_MAX + LED1];
    
    else
        b->displayCurrent[LED1] = OFF;
};


// Turn on speaker
//
void speakerOn(Biolume b)
{
    b->displayCurrent[SPEAKER] = b->genome[GEN_EXE_SIZE_MAX + SPEAKER];
};


// Turn off speaker
//
void speakerOff(Biolume b)
{
    b->displayCurrent[SPEAKER] = OFF;
};


// JUMP forward in intructions instruction
//
void jumpToLabel(Biolume b)
{
    // jump to label
    // 1 is subtracted because the pc will be incremented before returning from the biolumeStep function
    if (b->label == -1)
        b->pc = b->label;
    
    // set label at the end of the instructions list
    else if (b->label == 0)
        b->pc = b->size - 1;
    
    else
        b->pc = b->label - 1;
};


// LABEL the next instruction as the point to JUMP to
//
void setLabel(Biolume b)
{
    // set the jump point to the next instruction in the genome
    b->label = (b->pc + 1) % b->size;
};


// Used by BUFFER_SET_DATA instruction
// Copy current display into the display buffer
//
void displayToBuffer(Biolume b)
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        b->displayBuffer[i] = b->displayCurrent[i];
};


// Used by BUFFER_GET_DATA instruction
// Copy display buffer to current display
void bufferToDisplay(Biolume b)
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        b->displayCurrent[i] = b->displayBuffer[i];
};


// Used by MESSAGE_SEND instruction
// Broadcasts biolume's display buffer to its neighbors
//
void broadcastNeighborhood(Population p, Biolume b)
{
    int i, j;
    int neigborLocation[2];
    
    // send message to neighbors
    for (i=b->location[0]-1; i<b->location[0]+1; i++)
    {
        // check for out of bounds (toroidal grid)
        if (i < 0) neigborLocation[0] = i + X_SIZE;
        else if (i >= X_SIZE) neigborLocation[0] = i - X_SIZE;
        else neigborLocation[0] = i;
        
        for (j=b->location[1]-1; j<b->location[1]+1; j++)
        {
            // check for out of bounds (toroidal grid)
            if (j < 0) neigborLocation[1] = j + Y_SIZE;
            else if (j >= Y_SIZE) neigborLocation[1] = j - Y_SIZE;
            else neigborLocation[1] = j;
            
            // don't send message back to sender
            if (neigborLocation[0] == b->location[0] && neigborLocation[1] == b->location[1]) continue;
            
            // send message
            biolumeSetMessage(p->biolumes[neigborLocation[0]][neigborLocation[1]], b->displayBuffer);
        }
    }
};


// Used by MESSAGE_RECEIVE instruction
// Save received broadcast into display buffer
//
void messageToBuffer(Biolume b)
{
    int i;
    for (i = 0; i < NUM_ACT; i++) 
        b->displayBuffer[i] = b->messageBuffer[i];
};


// If motion conditional
//
void ifMotion(Biolume b)
{
    // TODO: if (biolume_api_camera_motion_check)
};


// If not motion conditional
//
void ifNotMotion(Biolume b)
{
    // TODO: if (!biolume_api_camera_motion_check)
};


// If sound conditional
//
void ifSound(Biolume b)
{
    // TODO: if (biolume_api_microphone_check)
};


// If not sound conditional
//
void ifNotSound(Biolume b)
{
    // TODO: if (!biolume_api_microphone_check)
};


// If touch conditional
//
void ifTouch(Biolume b)
{
    // TODO: if (biolume_api_touch_sensor_check)
};


// If not touch conditional
//
void ifNotTouch(Biolume b)
{
    // TODO: if (!biolume_api_touch_sensor_check)
};


// If CO2 conditional
//
void ifCO2(Biolume b)
{
    // TODO: if (biolume_api_c02_sensor_check)
};


// If not CO2 conditional
//
void ifNotCO2(Biolume b)
{
    // TODO: if (!biolume_api_c02_sensor_check)
};


// If low energy conditional
//
void ifLowEnergy(Biolume b)
{
    if (b->energy < ENERGY_MAX / 2) 
        b->skip = 1;
};


// If high energy  conditional
//
void ifHighEnergy(Biolume b)
{
    if (b->energy > ENERGY_MAX / 2) 
        b->skip = 1;

};


// Reproduce instruction
//
void reproduceCheck(Biolume b)
{
    // determine reproduction based on probability and set repro flag
    // reproduction probability based on energy and scaled
    float reproProb = (b->energy / (float)ENERGY_MAX) * (REPRO_PROB_MAX - REPRO_PROB_MIN) + REPRO_PROB_MIN;
    
    if (reproProb > getRandomUniform()) 
        b->reproduce = 1;
};
