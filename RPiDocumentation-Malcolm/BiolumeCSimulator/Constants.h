//
//  Constants.h
//  Biolume
//
//  Created by Malcolm Doering on 6/5/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef Biolume_Constants_h
#define Biolume_Constants_h


//----------------------------------------------------------------------------
// Population
//----------------------------------------------------------------------------

// dimensions
#define X_SIZE      10
#define Y_SIZE      3


//----------------------------------------------------------------------------
// Genome
//----------------------------------------------------------------------------

// biolume actuators
enum { LED0, LED1, SPEAKER, NUM_ACT };

// display states
#define OFF                 0

// executable genome size limits
#define GEN_EXE_SIZE_MAX        20
#define GEN_EXE_SIZE_MIN        05
#define GEN_EXE_SIZE_INIT       10

// max genome size (including varaiables and insertion space)
#define GENOME_SIZE_MAX     (GEN_EXE_SIZE_MAX + NUM_ACT)


//----------------------------------------------------------------------------
// Reproduction
//----------------------------------------------------------------------------

// probabilities of executing a successful reproduction process
#define REPRO_PROB_MAX      0.8
#define REPRO_PROB_MIN      0.1

// mutation rates ( http://www.krl.caltech.edu/avida/manual/mutation.html )
#define MUT_PROB_COPY       (1.0 / GEN_EXE_SIZE_INIT)
#define MUT_PROB_INSERT     (MUT_PROB_COPY / 2.2)
#define MUT_PROB_DELETE     (MUT_PROB_COPY / 2.2)


//----------------------------------------------------------------------------
// Energy Management
//----------------------------------------------------------------------------

// Specify a maximum energy that each Biolume can reach. Then, specify the 
// proportion of that maximum that is awarded for each sensed activity. 
#define ENERGY_MAX          100
//static double CO2_ENERGY = ENERGY_MAX * 0.01;
//static double NORMAL_SOUND_ENERGY = ENERGY_MAX * 0.0; 
//static double NEW_SOUND_ENERGY = ENERGY_MAX * 0.05;
//static double MOTION_ENERGY = ENERGY_MAX * 0.10; 
//static double TOUCH_ENERGY = ENERGY_MAX * 0.50;
static double ENERGY_DECAY = ENERGY_MAX * 0.01;



#endif
