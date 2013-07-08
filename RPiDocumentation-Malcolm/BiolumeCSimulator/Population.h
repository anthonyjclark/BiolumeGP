//
//  Population.h
//  Biolume
//
//  Created by Malcolm Doering on 5/22/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef Biolume_Population_h
#define Biolume_Population_h


// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include "Biolume.h"
#include "Constants.h"


// ----------------------------------------------------------------------------
// Population structure and utility functions
// ----------------------------------------------------------------------------

// Population struct
//
typedef struct population_t
{
    int size;
    Biolume biolumes[X_SIZE][Y_SIZE];
    
} *Population;


// Population creation function
//
Population populationCreate(void);


// Population initialization function
//
void populationInitialize(Population p);


// Population destroy function
//
void populationDestroy(Population p);


// Population step function
//
void populationStep(Population p);


// Population reproduce function
//
void populationReproduce(Population p);


// Population print function
//
void populationPrint(Population p);


#endif
