Malcolm Doering
8-28-2012

This directory contains the Biolume class files. This code can reproduce and send its genes to another biolume running on a different computer (with a few modifications - see below).

To run the code first compile with "make biolume" then run "./biolume <server port number> <client port number>". To run two biolumes on the same machine open two terminals. In one terminal run the biolume with two different port numbers, e.g. "./biolume 3000 3001". Then in the other terminal run biolume but with the port numbers swithed - "./biolume 3001 3000". This should allow the two biolumes to send there genomes to eachother upon reproduction (although, they both will have the same genes since they started with the same seed).
This code does not actually work over more than one computer in its current state. It also does not support other important features such as mutation during reproductions. It is more of a skeleton which needs to be built on.

Here is the message I sent to Prof. McKinley that describes the code:
" The way this code works is with sockets. Each biolume works as both a server and a client. During initialization the biolume opens up a server socket and begins to listen for incoming connections. After executing an instruction the biolume checks for any incoming connections. These connections will come from other biolumes that are executing a 'reproduce' instruction, during which they send their genes to a neighboring biolume. If there is a connection, the biolume reads in new genes, assigns its genes to the new genes, and then clears all its information (program counter, etc) and restarts running with the new genes.
 Actually, the code I wrote currently only will work with two biolumes running on the same machine. But with some simple changes (just changing the port numbers and IP addresses) the code can run biolumes on separate computers, and hopefully more than two at a time. (I have not yet thought much about what will happen if two biolumes try to send their genes to the same biolume at the same time).

There are a few things I am wondering about reproduction for the biolumes:
1) What should happen if there are multiple incoming connections (I.e., multiple biolumes trying to send genes to the same biolume at the same time - I mentioned this above). Maybe it would be best to just use the last connection to come in?
2) How will the biolumes know the IP addresses of their neighbors? Will this information come from some sort of 'manager' program with global information, running separate from all the biolumes but connected to them?
3) How will a biolume determine which neighbor to send its genes to? I think in the simulation that the neighboring biolume with the lowest energy level was replaced. But biolumes do not have information about their neighbors' energy levels. Once again, I wonder if a manager program should be used here. "

