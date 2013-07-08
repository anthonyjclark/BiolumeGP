
#ifndef BIOLUME_H
#define BIOLUME_H


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "Random.h"

// ----------------------------------------------------------------------------
// Definitions
// ----------------------------------------------------------------------------

// number of instructions for each biolume
#define NUM_INST_MAX        20
#define NUM_INST_MIN        05
#define NUM_INST_INIT       10

// mutation rates ( http://www.krl.caltech.edu/avida/manual/mutation.html )
#define MUT_PROB_COPY       (1.0 / NUM_INST_INIT)
#define MUT_PROB_INSERT     (MUT_PROB_COPY / 2.2)
#define MUT_PROB_DELETE     (MUT_PROB_COPY / 2.2)

// reproduction
#define REPRO_PROB_MAX      0.8
#define REPRO_PROB_MIN      0.1
#define ENERGY_MAX          100

// biolume actuators
#define OFF                 0
enum { LED0, LED1, SPEAKER, NUM_VARS };

// max genome size (including varaiables and insertion space)
#define GENOME_SIZE_MAX     (NUM_INST_MAX + NUM_VARS)


// ----------------------------------------------------------------------------
// Instructions
// ----------------------------------------------------------------------------
enum Instructions {

    // led0 instructions
    LED0_ON, LED0_OFF, LED0_TOGGLE,
    
    // led1 instructions
    LED1_ON, LED1_OFF, LED1_TOGGLE,
    
    // sound instructions
    SOUND_ON, SOUND_OFF,

    // message instructions
    BUFFER_SET_DATA, BUFFER_GET_DATA,
    MESSAGE_SEND, MESSAGE_RECEIVE,
    
    // conditional instructions
    IF_MOTION, IF_SOUND, IF_TOUCH, IF_CO2,
    
    // reproduce this biolume with some chance
    REPRODUCE,
    
    // total number of unique instructions
    NUM_INST
};


// ----------------------------------------------------------------------------
// Biolume structure and utility functions
// ----------------------------------------------------------------------------
typedef struct biolume_t {
    
    // biolume GP data
    short unsigned int size;        // size of this biolume's genome
    short unsigned int pc;          // current position in the program
    short unsigned int skip;        // bool used for condition instructions
    unsigned char genome[GENOME_SIZE_MAX];
    
    // biolume evolution data
    unsigned int age;               // steps for this biolume
    short int reproduce;            // is this biolume ready to reproduce

    // biolume hardware
    int energy;                     // amount of energy acquired by biolume
    char display[NUM_VARS];         // status of LEDs and speaker
    char buffer[NUM_VARS];          // buffer used for messages
    
} Biolume;

// ----------------------------------------------------------------------------
void initialize_biolume(Biolume* b) {
    int i;

    // initial genome size
    b->size = NUM_INST_INIT;
    
    // start program counter at the beginning
    b->pc = 0;
    
    // initialize to random instructions
    for (i = 0; i < b->size; i++) {
        b->genome[i] = getRandomInt(NUM_INST);
    }
    
    // initialize to random variables
    for (i = 0; i < NUM_VARS; i++) {
        b->genome[NUM_INST_MAX + i] = getRandomInt(127);
    }
    
    // initialize data to 0 (or false)
    b->skip = 0;
    b->age = 0;
    b->reproduce = 0;
    b->energy = 0;
    for (i = 0; i < NUM_VARS; i++) b->display[i] = 0;
    for (i = 0; i < NUM_VARS; i++) b->buffer[i] = 0;

}

// ----------------------------------------------------------------------------
void step_biolume(Biolume* b) {
    int i;
    float repro_prob;
    
    // check for skip
    if (b->skip) {
        b->skip = 0;
    }
    else {
        // execute next instruction
        switch (b->genome[b->pc]) {
            
            // set the first LED to on
            case LED0_ON:
                b->display[LED0] = b->genome[NUM_INST_MAX + LED0];
                break;
            
            // set the first LED to off
            case LED0_OFF:
                b->display[LED0] = OFF;
                break;
            
            // toggle the first LED between off and the previous state
            //   - the previous state is either from the genome or the buffer
            case LED0_TOGGLE:
                // TODO: change to toggle between previous state
                if (b->display[LED0]) b->display[LED0] = OFF;
                else b->display[LED0] = b->genome[NUM_INST_MAX + LED0];
                break;
                
            // set the second LED to on
            case LED1_ON:
                b->display[LED1] = b->genome[NUM_INST_MAX + LED1];
                break;
                
            // set the second LED to off
            case LED1_OFF:
                b->display[LED1] = OFF;
                break;

            // toggle the second LED between off and the previous state
            //   - the previous state is either from the genome or the buffer
            case LED1_TOGGLE:
                // TODO: change to toggle between previous state
                if (b->display[LED1]) b->display[LED1] = OFF;
                else b->display[LED1] = b->genome[NUM_INST_MAX + LED1];
                break;
                
            // set the speaker to on
            case SOUND_ON:
                b->display[SPEAKER] = b->genome[NUM_INST_MAX + SPEAKER];
                break;
                
            // set the speaker to off
            case SOUND_OFF:
                b->display[SPEAKER] = OFF;
                break;
                
            // emit the data stored in buffer
            case MESSAGE_SEND:
                // TODO
                break;
            
            // set the buffer to the data in received message (api buffer)
            case MESSAGE_RECEIVE:
                // TODO
                break;
                
            // place the current display data into the message buffer
            case BUFFER_SET_DATA:
                for (i = 0; i < NUM_VARS; i++) b->buffer[i] = b->display[i];
                break;

            // set the display data to what is in the message buffer
            case BUFFER_GET_DATA:
                for (i = 0; i < NUM_VARS; i++) b->display[i] = b->buffer[i];
                break;
                
            // get motion camera sensor data and set condition flag
            case IF_MOTION:
                // TODO: if (biolume_api_camera_motion_check)
                b->skip = getRandomInt(2);
                break;

            // get microphone data and set condition flag                
            case IF_SOUND:
                // TODO: if (biolume_api_microphone_check)
                b->skip = getRandomInt(2);
                break;
                
            // get touch sensor data and set condition flag
            case IF_TOUCH:
                // TODO: if (biolume_api_touch_sensor_check)
                b->skip = getRandomInt(2);
                break;
                
            // get CO2 sensor data and set condition flag
            case IF_CO2:
                // TODO: if (biolume_api_c02_sensor_check)
                b->skip = getRandomInt(2);
                break;
                
            // determine reproduction based on probability and set repro flag
            case REPRODUCE:
                // reproduction probability based on energy and scaled
                repro_prob = (b->energy / (float)ENERGY_MAX) * (REPRO_PROB_MAX - REPRO_PROB_MIN) + REPRO_PROB_MIN;
                if (repro_prob > getRandomUniform()) b->reproduce = 1; // true
                break;
                
            default:
                break;
        }
    }
    
    // increment circular PC
    b->pc = (b->pc + 1) % b->size;
    b->age++;
}

// ----------------------------------------------------------------------------
void mutate_biolume(Biolume* b) {
    int i, indel;
    char swap1, swap2;
    
    // copy mutation for the instructions
    for (i = 0; i < b->size; i++) {
        if (getRandomUniform() < MUT_PROB_COPY) {
            b->genome[i] = getRandomInt(NUM_INST);
        }
    }
    
    // copy mutation for the variables
    for (i = 0; i < NUM_VARS; i++) {
        if (getRandomUniform() < MUT_PROB_COPY) {
            b->genome[NUM_INST_MAX + i] += (getRandomGaussian() * 10 - 5);
        }
    }
    
    // insertion mutation (must have room for insertion)
    if ((getRandomUniform() < MUT_PROB_INSERT) && (b->size < NUM_INST_MAX)) {
        
        b->size++;
        
        // get insertion location
        indel = getRandomInt(b->size);
        
        // insert
        swap1 = b->genome[indel];
        b->genome[indel] = getRandomInt(NUM_INST);
        
        // shift
        for (i = indel+1; i < b->size; i++) {
            swap2 = b->genome[i];
            b->genome[i] = swap1;
            swap1 = swap2;
        }
    }
    
    // deletion mutation (must not go below minimum)
    if ((getRandomUniform() < MUT_PROB_DELETE) && (b->size > NUM_INST_MIN)) {
        
        // get deletion location
        indel = getRandomInt(b->size);
        
        b->size--;
        // shift
        for (i = indel; i < b->size; i++) {
            b->genome[i] = b->genome[i+1];
        }
    }
}

// ----------------------------------------------------------------------------
void replace_biolume(Biolume* daughter, Biolume* parent) {
    int i;
    
    // replace genome
    for (i = 0; i < GENOME_SIZE_MAX; i++) {
        daughter->genome[i] = parent->genome[i];
    }
    
    // daughter executes the same number of instructions as the parent
    daughter->size = parent->size;
}

// ----------------------------------------------------------------------------
void print_biolume(Biolume* b) {
    int i;
    
    printf("[");
    for (i = 0; i < NUM_VARS; i++) {
        printf("%03d", b->display[i]);
        if (i < NUM_VARS - 1) printf("|");
    }
    printf("]");
}

#endif

