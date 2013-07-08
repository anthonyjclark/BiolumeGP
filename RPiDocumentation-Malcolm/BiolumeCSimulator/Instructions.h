//
//  Instructions.h
//  Biolume
//
//  Created by Malcolm Doering on 6/5/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef Biolume_Instructions_h
#define Biolume_Instructions_h


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include "Population.h"
#include "Biolume.h"
#include "Constants.h"


// ----------------------------------------------------------------------------
// Instructions
// ----------------------------------------------------------------------------
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


// ----------------------------------------------------------------------------
// Functions used to carry out Instructions
// ----------------------------------------------------------------------------

// No-operation
//
void nop(Biolume b);


// Turn on LED 0
//
void LED0On(Biolume b);


// Turn off LED 0
//
void LED0Off(Biolume b);


// Toggle LED 0
//
void LED0Toggle(Biolume b);


// Turn on LED 1
//
void LED1On(Biolume b);


// Turn off LED 1
//
void LED1Off(Biolume b);


// Toggle LED 1
//
void LED1Toggle(Biolume b);


// Turn on speaker
//
void speakerOn(Biolume b);


// Turn off speaker
//
void speakerOff(Biolume b);


// JUMP forward in intructions instruction
//
void jumpToLabel(Biolume b);


// LABEL the next instruction as the point to JUMP to
//
void setLabel(Biolume b);


// Used by BUFFER_SET_DATA instruction
// Copy current display into the display buffer
//
void displayToBuffer(Biolume b);


// Used by BUFFER_GET_DATA instruction
// Copy display buffer to current display
void bufferToDisplay(Biolume b);


// Used by MESSAGE_SEND instruction
// Broadcasts biolume's display buffer to its neighbors
void broadcastNeighborhood(Population p, Biolume b);


// Used by MESSAGE_RECEIVE instruction
// Save received broadcast into display buffer
//
void messageToBuffer(Biolume b);


// If motion conditional
//
void ifMotion(Biolume b);


// If not motion conditional
//
void ifNotMotion(Biolume b);


// If sound conditional
//
void ifSound(Biolume b);


// If not sound conditional
//
void ifNotSound(Biolume b);


// If touch conditional
//
void ifTouch(Biolume b);


// If not touch conditional
//
void ifNotTouch(Biolume b);


// If CO2 conditional
//
void ifCO2(Biolume b);


// If not CO2 conditional
//
void ifNotCO2(Biolume b);


// If low energy conditional
//
void ifLowEnergy(Biolume b);


// If high energy  conditional
//
void ifHighEnergy(Biolume b);


// Reproduce instruction
//
void reproduceCheck(Biolume b);


#endif
