//
//  main.c
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
#include <unistd.h>

#include "Biolume.h"
#include "Population.h"


// ----------------------------------------------------------------------------
// Main function
// ----------------------------------------------------------------------------
int main (int argc, const char * argv[])
{
    Population test = populationCreate();
    populationInitialize(test);
    
    //int i;
    for (;;)
    {
        
        populationPrint(test);
        
        populationStep(test);
        
        populationReproduce(test);
        
        sleep(1);
    }
    
    populationDestroy(test);
    
        
    printf("\nFinished.\n");
    
    return 0;
}

