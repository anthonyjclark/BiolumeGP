//
// Biolume driver file
// Malcolm Doering
//


#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "Biolume.h"
#include "Constants.h"

using namespace std;


int main(int argc, char *argv[])
{
	Biolume biolume;
	
	biolume.run(atoi(argv[1]), atoi(argv[2]));
}
