#
# makefile for biolume simulator written in C
# Malcolm Doering
# 


BiolumeSimulator: main.o Biolume.o Instructions.o Population.o Random.o
	gcc -Wall -o BiolumeSimulator main.o Biolume.o Instructions.o Population.o Random.o -lm

main.o: main.c Biolume.h Population.h
	gcc -Wall -c main.c

Biolume.o: Biolume.c Biolume.h Instructions.h Random.h Constants.h
	gcc -Wall -c Biolume.c

Population.o: Population.c Population.h Biolume.h Constants.h
	gcc -Wall -c Population.c

Instructions.o: Instructions.c Instructions.h Random.h Population.h Biolume.h Constants.h
	gcc -Wall -c Instructions.c

Random.o: Random.c Random.h
	gcc -Wall -c Random.c

clean:
	rm -rf BiolumeSimulator *.o

