//
//  Random.c
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


// Get random integer
//
int getRandomInt(int max)
{
	return rand() % max;
};


// Get random uniform
//
float getRandomUniform(void)
{
	return ((float)rand() / RAND_MAX);
};


// Box-Muller method
//
float getRandomGaussian(void)
{
	float u1, u2, s;
    
	do {
		u1 = 2.0 * getRandomUniform() - 1.0;
		u2 = 2.0 * getRandomUniform() - 1.0;
		s = u1 * u1 + u2 * u2;
	}
	while (s >= 1.0);
	
	return u1 * sqrt(-2.0 * log(s) / s);
};