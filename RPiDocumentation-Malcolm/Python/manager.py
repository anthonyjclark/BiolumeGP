
""" A Manager initiates running of biolumes at the ip addresses listed in
a biolume_info.csv file. It receives and sends messages to and from Biolumes 
to facilitate communication and reproduction.

"""

__author__ = "Malcolm Doering <doeringm@msu.edu>"
__credits__ = "Malcolm Doering"

import subprocess
import socket
import time
import random
import sys
import os
import string
import constants


def get_external_ip():
	"""Get the external ip address of this computer."""
	
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.connect(('google.com', 80))
	ip = sock.getsockname()[0]
	sock.close()
	return ip


class Manager:
	"""Manage communication and reproduction of Biolumes.
	
	Properties:
	
	biolumeInfo
		A dict mapping a iolume id number to a struct containing information about that biolume
	localHost
		The ip address of this computer
	localPort
		The port used to listen to incoming connections from Biolumes
	serverSocket
		The socket used to listen to incoming connections from Biolumes
	remotePort
		The port Biolumes are using to lister for connections from the Manager
	clientSocket
		The socket used to send messages to Biolumes
	
	"""
	
	class BiolumeInfo:
		"""Stores information about a single Biolume from the biolume_info file.
		
		Properties:
		
			ipAddress
				The ip address of the Biolume
			x
				The Biolume's x coordinate (location)
			y
				The Biolume's y coordinate (location)
		
		"""
		
		def __init__(self, ipAddress, x, y):
			"""Constructor for the BiolumeInfo class."""
			
			self.ipAddress = ipAddress
			self.x = x
			self.y = y
	
	
	def __init__(self):
		"""Constructor for Manager class."""
		
		self.biolumeInfo = {}
		self.localHost = get_external_ip()
		self.localPort = 9999
		self.serverSocket = -1
		self.remotePort = 9998
		self.clientSocket = -1
		
		random.seed() # seed with current time
	
	
	def quit_manager(self):
		"""Close the server socket and exit."""
		
		# tell the biolumes to quit
		quitMessage = self.create_quit_message()
		for key in self.biolumeInfo:
			self.send_message_to_biolume(key, quitMessage)
		
		self.serverSocket.close()
		sys.exit(0)
	
	
	def read_biolume_info(self):
		""" Read in the Biolume ip addresses and locations from a file."""
		
		# read the file
		file = open(constants.BIOLUME_INFO_FILENAME, "r")
		file.readline() # ignore the first line
		data = file.readlines()
		file.close()
		
		# store info in a dict
		id = 0
		for line in data:
			info = line.split(',')
			self.biolumeInfo[id] = self.BiolumeInfo(info[0], info[1], info[2])
			id += 1
	
	
	def start_server(self):
		"""Start the server for listening for connections from Biolumes."""
		
		# open a server socket to listen for connections from biolumes
		self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) # reuse the socket even if in TIME_WAIT state
		self.serverSocket.bind((self.localHost, self.localPort))
		self.serverSocket.listen(5)
	
	
	def start_biolumes(self):
		"""Run Biolumes on the address in the biolumeInfo dict."""
		
		# key is biolume id, value is biolume info
		for key in self.biolumeInfo:
			# create the command used to start the biolume on a remote computer
			command = "sshpass -p 'biolume' ssh pi@" + self.biolumeInfo[key].ipAddress + " python /home/pi/biolume_tests/biolumeSerial.py " + self.localHost + " " + str(key) + " -ds"
			
			#command = "python biolume.py " + self.localHost + " " + str(key)
			print command
			
			# start the managee/biolume code on the remote RPi
			# somehow do error checking on this...
			#subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
	
	
	def mutate(self, genes):
		"""Apply Mutations to a list of genes.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume. The first item in the list is the number of genes that are executable.
				
		"""
		
		# convert to ints
		converted = [int(gene) for gene in genes]
		
		converted = self.deletion_mutation(converted)
		converted = self.instructions_copy_mutation(converted)
		converted = self.variables_copy_mutation(converted)
		converted = self.insertion_mutation(converted)
		
		# convert to strings
		genes = [str(gene) for gene in converted]
		
		return genes
	
	
	def instructions_copy_mutation(self, genes):
		"""Apply the copy mutation to the instructions.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume
				
		"""
		
		# genes[0] is the executable size of the genome
		for index in range(genes[0]):
			if (random.random() < constants.MUTATION_PROB_COPY):
				# skip index 0
				genes[index+1] = random.randrange(0, constants.Instructions.NUM_INST)
				
		return genes
	
	
	def variables_copy_mutation(self, genes):
		"""Apply the copy mutation to the variables.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume
		
		"""
		
		# genes[0] is the executable size of the genome
		for index in range(constants.Actuators.NUM_ACTUATORS):
			if (random.random() < constants.MUTATION_PROB_COPY):
				# increment index by 1 to account for genes[0] which contains executable size
				genes[constants.EXECUTABLE_GENOME_MAX_SIZE+index+1] += int(random.gauss(0, 1) * 10 - 5)
				if (genes[constants.EXECUTABLE_GENOME_MAX_SIZE+index+1] > constants.VARIABLE_MAX):
					genes[constants.EXECUTABLE_GENOME_MAX_SIZE+index+1] = constants.VARIABLE_MAX
				elif (genes[constants.EXECUTABLE_GENOME_MAX_SIZE+index+1] < 0):
					genes[constants.EXECUTABLE_GENOME_MAX_SIZE+index+1] = 0
		
		return genes
	
	
	def deletion_mutation(self, genes):
		"""Apply the deletion mutation to the genes of a Biolume.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume
				
		"""
		
		# genes[0] is the executable size of the genome
		if (random.random() < constants.MUTATION_PROB_DELETE) and (genes[0] > constants.EXECUTABLE_GENOME_MIN_SIZE):
			deletionLocation = random.randrange(0, genes[0])
			
			# keep the number of genes the same
			genes.insert(constants.EXECUTABLE_GENOME_MAX_SIZE, genes[constants.EXECUTABLE_GENOME_MAX_SIZE])
			
			# decrement the executable genome size by 1 
			genes[0] -=  1
			# remove a gene but skip index 0
			genes.pop(deletionLocation+1)
		
		return genes
	
	
	def insertion_mutation(self, genes):
		"""Apply the insertion mutation to the genes of a Biolume.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume
				
		"""
		
		# genes[0] is the executable size of the genome
		if (random.random() < constants.MUTATION_PROB_INSERT) and (genes[0] < constants.EXECUTABLE_GENOME_MAX_SIZE):
			insertionLocation = random.randrange(0, genes[0])
			
			# keep the number of genes the same
			genes.pop(constants.EXECUTABLE_GENOME_MAX_SIZE)
			
			# increment the executable genome size by 1
			genes[0] += 1
			# insert a gene but skip index 0
			genes.insert(insertionLocation+1, random.randrange(0, constants.Instructions.NUM_INST))
		
		return genes
	
	
	def make_genes_viable(self, genes):
		"""Make sure that there is a reproduce instruction in the executable portion of the genes.
		
		Parameters:
		
			genes
				A list of the genes of a Biolume
				
		"""
		
		# TODO
		return genes
	
	
	def send_message_to_biolume(self, biolumeId, message):
		"""Send a message to a Biolume.
		
		Parameters:
		
			biolumeId
				The id of the recipient Biolume
			message
				A string containing the message to send to the Biolume
		
		"""
		
		self.clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		
		try:
			self.clientSocket.connect((self.biolumeInfo[int(biolumeId)].ipAddress, self.remotePort))
			self.clientSocket.sendall(message)
		except:
			print "*** failed to send to " + str(biolumeId) + " ***"
			print sys.exc_info()[0]
			pass
			
		self.clientSocket.close()
	
	
	def create_quit_message(self):
		"""Create a message that tells a Biolume to exit."""
		
		return "manager,quit"
	
	
	def process_genes_message(self, genesMessage):
		"""Process a genesMessage from a Biolume and send the genes to a different random Biolume.
		
		TODO: Send message to a Biolume neighboring the sender.
		
		Parameters:
		
			genesMessage:
				A string containing the genes information of a reproducing Biolume
		
		"""
		
		words = repr(genesMessage).strip(string.punctuation).split(',')
		
		# do mutations on the genes
		genes = words
		senderId = int(genes.pop(0)) # get rid of id
		genes.pop(0) # get rid of message type
		mutated = self.mutate(genes)
		
		# create the message to be sent to a neighboring Biolume
		newGenesMessage = "manager,genes"
		for gene in mutated:
			newGenesMessage += "," + gene
		
		# send genes to a neighboring biolume
		numBiolumes = len(self.biolumeInfo)
		if numBiolumes > 1:
			# find a random biolume that is not the sender of the genes
			recipient = random.randrange(numBiolumes)
			while recipient == senderId:
				recipient = random.randrange(numBiolumes)
			
			self.send_message_to_biolume(recipient, newGenesMessage)
			print "genes message sent to " + str(recipient)
		
		else:
			self.send_message_to_biolume(senderId, newGenesMessage)
			print "genes message sent to " + str(senderId)
	

	def process_display_message(self, displayMessage):
		"""Process a Biolume's displayMessage and send it to all other.
		
		TODO: Send message only to neighboring Biolumes.
		
		Parameters:
			
			displayMessage
				A string continaining the display information of a Biolume
				
		"""
		
		words = repr(displayMessage).strip(string.punctuation).split(',')
		
		# send the display info to all biolumes neighboring the sender
		senderId = int(words.pop(0))
		displayMessage = "manager"
		for word in words:
			displayMessage += "," + word
		
		if len(self.biolumeInfo) > 1:
			# send the display info to all neighboring biolumes
			for key in self.biolumeInfo:
				if key != senderId:
					self.send_message_to_biolume(key, displayMessage)
					print "display message sent to " + str(key)
				
		else:
			self.send_message_to_biolume(senderId, displayMessage)
			print "display message sent to " + str(senderId)
	
	
	def parse_message(self, message):
		"""Parse a message from a Biolume.
		
		Parameters:
		
			message
				A string message from a Biolume
				
		"""
		
		words = repr(message).strip(string.punctuation).split(',')
		
		# take appropriate action depending on the type of message
		if (words[1] == "genes"):
			self.process_genes_message(message)
		elif (words[1] == "display"):
			self.process_display_message(message)
		
		# other message types...		
	
	
	def handle_biolume_connection(self, connection, address):
		"""Handle an incoming connection from a Biolume.
		
		This should be run on the child process after forking.
		
		Parameters:
			connection
				The socket receiving a message from a Biolume
			address
				The remote ip and port that the connected socket is connected to
		
		"""
		
		# this is the child - deal with the new connection
		print "Connected to ", address
		
		# receive the incoming message
		message = connection.recv(1024)
		connection.close()
		
		print "received: ", message
		
		self.parse_message(message)
		
		sys.exit(0)
	
		
	def run(self):
		"""Run the Manager."""
		
		m.read_biolume_info()
		m.start_server()
		m.start_biolumes()
		
		# listen for incoming conections from biolumes
		while True:
			connection, address = self.serverSocket.accept()
			
			# fork when there is a connection
			pid = os.fork()
			if pid:
				# this is the parent - continue listening
				continue
			elif pid == 0:
				self.handle_biolume_connection(connection, address)
		
		self.serverSocket.close()


if __name__ == "__main__":
	"""If this is run as main, run the Manager program."""
	
	try:
		m = Manager()
		m.run()
	
	except (KeyboardInterrupt):
		m.quit_manager()
		pass
	
	
