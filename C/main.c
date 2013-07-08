
// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#include <stdio.h>
#include <time.h>

#include "Biolume.h"

// ----------------------------------------------------------------------------
// Includes
// ----------------------------------------------------------------------------
#define POP_SIZE 4


// ----------------------------------------------------------------------------
// Function prototypes
// ----------------------------------------------------------------------------
void print_population_2x2();
void reproduce_population();


// ----------------------------------------------------------------------------
// Globals
// ----------------------------------------------------------------------------
static Biolume** population;        // population of biolumes


// ----------------------------------------------------------------------------
// Main
// ----------------------------------------------------------------------------
int main(int argc, char** argv) {
    int i;
    
    // "randomly" initialize PRNG
    srand ( (unsigned int)time(NULL) );
    
    // initialize the biolumes
    population = malloc(POP_SIZE*sizeof(Biolume*));
    for (i = 0; i < POP_SIZE; i++) {
        population[i] = malloc(sizeof(Biolume));
        initialize_biolume(population[i]);
    }
    
    // step the biolumes
    for (;;) {
        
        // display the population
        print_population_2x2();
        
        // step each biolume
        for (i = 0; i < POP_SIZE; i++) step_biolume(population[i]);
        
        // reproduce the population
        reproduce_population();
        
        // delay between steps
        sleep(1);        
    }
    
    return 0;
}

// ----------------------------------------------------------------------------
void print_population_2x2() {
    
    printf("\n\n\n\n\n\n|-------------|-------------|\n|");
    print_biolume(population[0]);
    printf("|");
    print_biolume(population[1]);
    printf("|\n");
    printf("|-------------|-------------|\n|");
    print_biolume(population[2]);
    printf("|");
    print_biolume(population[3]);
    printf("|\n|-------------|-------------|\n");
    
}

// ----------------------------------------------------------------------------
void reproduce_population() {
    int i, j, elder_index;
    int to_free[POP_SIZE];
    Biolume** newpop;
    
    // set newpopulation to population
    newpop = malloc(POP_SIZE*sizeof(Biolume*));
    for (i = 0; i < POP_SIZE; i++) {newpop[i] = population[i]; to_free[i] = 0;}
    
    // check each biolume for reproduction flag
    for (i = 0; i < POP_SIZE; i++) {
        if (population[i]->reproduce) {
            
            // find the first replaceable biolume
            for (j = 0; j < POP_SIZE; j++) {
                if (population[j]->age > 0) { elder_index = j; break; }
            }
            
            // continue the search for the oldest
            for (j += 1; j < POP_SIZE; j++) {
                if (population[j]->age > population[elder_index]->age) {
                    elder_index = j;
                }
            }
            
            // copy parent into new biolume
            newpop[elder_index] = malloc(sizeof(Biolume));
            initialize_biolume(newpop[elder_index]);
            replace_biolume(newpop[elder_index], population[i]);
            mutate_biolume(newpop[elder_index]);
            
            // clear reproduction and dissallow elder being copied over again
            population[i]->reproduce = 0;
            population[elder_index]->age = 0;
            
            // mark memory for deallocation
            to_free[elder_index] = 1;
            printf("reproduction");
        }
    }
    
    // deallocate memory and set pointer to new population
    for (i = 0; i < POP_SIZE; i++) if (to_free[i]) free(population[i]);
    free(population);
    population = newpop;    
}

