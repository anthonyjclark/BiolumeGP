//
//  Population.c
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

#include "Population.h"
#include "Biolume.h"


// ----------------------------------------------------------------------------
// Population structure and utility functions
// ----------------------------------------------------------------------------

// Population creation function
//
Population populationCreate(void)
{
    // allocate memory for the Population struct
    Population newPopulation = malloc(sizeof(struct population_t));
    
    // allocate memory for the biolumes
    int i, j;
    for (i=0; i<X_SIZE; i++)
        for (j=0; j<Y_SIZE; j++)
        {
            newPopulation->biolumes[i][j] = biolumeCreate((void*)newPopulation);
        }
    
    newPopulation->size = X_SIZE * Y_SIZE;
    
    return newPopulation;
};


// Population initialization function
//
void populationInitialize(Population p)
{
    // initialize the biolumes
    int i, j;
    for (i=0; i<X_SIZE; i++)
        for (j=0; j<Y_SIZE; j++)
        {
            biolumeInitialize(p->biolumes[i][j], i, j);
        }
};


// Population destroy function
//
void populationDestroy(Population p)
{
    // free memory
    int i, j;
    for (i=0; i<X_SIZE; i++)
        for (j=0; j<Y_SIZE; j++)
        {
            biolumeDestroy(p->biolumes[i][j]);
        }
    
    free(p);
};


// Population step function
//
void populationStep(Population p)
{
    int i, j;
    for (i=0; i<X_SIZE; i++)
        for (j=0; j<Y_SIZE; j++)
            biolumeStep(p->biolumes[i][j]);
};


// Population reproduce function
// When there is a new offspring, it replaces the oldest member of the population
void populationReproduce(Population p)
{
    int i, j, k, l;
    int elderIndex[2];
    
    // check each biolume for reproduction flag
    for (i=0; i<X_SIZE; i++)
        for (j=0; j<Y_SIZE; j++)
            if (p->biolumes[i][j]->reproduce)
            {
                // find the first replaceable biolume
                for (k=0; k<X_SIZE; k++)
                    for (l=0; l<Y_SIZE; l++)
                        if (p->biolumes[k][l]->age > 0)
                        {
                            elderIndex[0] = k;
                            elderIndex[1] = l;
                            break;
                        }
                
                // continue to search for the oldest
                for (; k<X_SIZE; k++)
                    for (l+=1; l<Y_SIZE; l++)
                        if (p->biolumes[k][l]->age > p->biolumes[elderIndex[0]][elderIndex[1]]->age)
                        {
                            elderIndex[0] = k;
                            elderIndex[1] = l;
                        }
                
                // copy parent into new biolume
                
                // clear data and buffers, set location, create random genome
                biolumeInitialize(p->biolumes[elderIndex[0]][elderIndex[1]], elderIndex[0], elderIndex[1]);
                
                // replace random genome with the genome of the parent
                biolumeReplaceGenome(p->biolumes[elderIndex[0]][elderIndex[1]], p->biolumes[i][j]);
                
                // mutate
                biolumeMutate(p->biolumes[elderIndex[0]][elderIndex[1]]);
                
                // clear reproduction bool of the parent
                p->biolumes[i][j]->reproduce = 0;
            }
};


// Population print function
//
void populationPrint(Population p)
{
    int i, j;
    
    printf("\n\n\n\n\n");
    
    for (i=0; i<X_SIZE; i++)
    {
        for (j=0; j<Y_SIZE; j++)
            printf("|-------------");
        printf("|\n|");
        
        for (j=0; j<Y_SIZE; j++)
        {
            biolumePrint(p->biolumes[i][j]);
            printf("|");
        }
        printf("\n");
        
        
    }
    
    for (i=0; i<Y_SIZE; i++)
        printf("|-------------");
    printf("|\n");
};



















