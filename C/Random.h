
#ifndef RANDOM_UTILS_H
#define RANDOM_UTILS_H

//------------------------------------------------------------------------------
//------------------ PRNG Utility Functions ------------------------------------
//------------------------------------------------------------------------------
static int inline getRandomInt(int max) {
	return rand() % max;
}

float getRandomUniform() {
	return ((float)rand() / RAND_MAX);
}

// Box-Muller method
float getRandomGaussian() {
	float u1, u2, s;
    
	do {
		u1 = 2.0 * getRandomUniform() - 1.0;
		u2 = 2.0 * getRandomUniform() - 1.0;
		s = u1 * u1 + u2 * u2;
	}
	while (s >= 1.0);
	
	return u1 * sqrt(-2.0 * log(s) / s);
}

#endif