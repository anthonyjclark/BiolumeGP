#
# makefile for Biolume
# Malcolm Doering
#
#


biolume: driver.o Biolume.o
	g++ -Wall -o biolume driver.o Biolume.o

driver.o: driver.cpp Biolume.h Constants.h
	g++ -Wall -c driver.cpp

Biolume.o: Biolume.cpp Biolume.h Constants.h
	g++ -Wall -c Biolume.cpp

clean:
	rm -f biolume *.o
