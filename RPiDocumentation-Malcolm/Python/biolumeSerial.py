
"""A Biolume simply goes through its instructions and executes them. When 
a reproduce instruction is hit, the Biolume sends its genes to the Manager 
program. It is the job of the Manager program to mutate and distribute the 
genes to other Biolumes.

"""

__author__ = "Malcolm Doering <doeringm@msu.edu>"
__credits__ = "Malcolm Doering"

import socket
import time
import random
import constants
import sys
import string
import pyaudio
import wave
import audioop
import threading
from datetime import datetime
from datetime import timedelta
import cv2.cv as cv


def getExternalIp():
	"""Get the external ip of this computer."""
	
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.connect(('google.com', 80))
	ip = sock.getsockname()[0]
	sock.close()
	return ip


class Biolume:
	"""Extecute instructions and communication with the Manager program.
	
	Properties:
	
		Variables for storing Biolume information:
	
		executableGenomeSize
			The size of this Biolume's executable genome
		pc
			The current position in the program		
		skip
			A bool used for condition instructions
		label
			A bool to be used with the JUMP instruction
		genes
			The genes (instructions and actuator values)
		energy
			The amount of energy acquired by this Biolume
		displayBuffer
			A list containing the display state received from an other Biolume
		currentDisplay
			A list containing the varibales the biolume should currently be displaying through its actuators
		previousDisplay
			A list containing the display before the current display
		messageBuffer
			A list containing display messages received from other Biolumes
		
		Variables for the sensors:
		
		useSensors
			A bool stating whether or the not the sensors are disabled
		soundHeard
			A bool stating whether or not a sound was heard
		pyaudioObject
			An object created by PyAudio for sound detection
		micStream
			The stream from the microphone
		timeDetected
			The datetime that the last sound was detected
		soundMemory
			A timedelta representing how long the Biolume can remember hearing a sound
		motionDetected
			A bool stating whether or not motion was detected
		camera
			An OpenCV object used to capture frames from the camera
		imageHeight
			The height of a frame
		imageWidth
			The width of a frame
		numPixels
			The number of pixels in a frame
		colorFrame
			The frame most recently grabbed from the camera
		previousGrayFrame
			The frame previously grabbed from the camera, converted to gray
		currentGrayFrame
			The frame currently grabbed from the camera, converted to gray
		resultImage
			The resulting image after processing
		windowName
			The name of the OpenCV window for displaying the resulting image
			
		Variables for socket communications with the Manager:
		
		localHost
			The ip address of this computer
		localPort
			The port used for listening to incoming connections from the Manager
		serverSocket
			The socket used for listening to incoming connections from the Manager
		remoteHost
			The ip address of the computer running the Manager program. This must be set before calling send_message_to_manager.
		remotePort
			The port that the Manager is listening on for connections from Biolumes
		clientSocket
			The socket used for sending messages to the Manager
		id
			The id for this Biolume received from the Manger
	
	"""
	
	
	def __init__(self, id = -1, remoteHost = '', useSensors = True):
		"""Constructor for the Biolume class.
		
		Parameters:
		
			remtoteHost
				The ip address of the computer running the Manager program
			useSensors
				A boolean stating whether of the the sensors should not be disabled
		
		"""
		
		# set variables for socket communications with the manager
		self.localPort = 9998
		self.serverSocket = -1
		self.remotePort = 9999
		self.clientSocket = -1
		self.id = id
		self.remoteHost = remoteHost
		self.localHost = getExternalIp()
		
		# set variables for the Biolume organism
		self.initialize()

		# set variables for the sensors
		self.useSensors = useSensors
		self.soundHeard = False
		self.pyaudioObject = None
		self.micStream = None
		self.timeDetected = None
		self.soundMemory = None
		self.motionDetected = False
		self.camera = None
		self.imageHeight = None
		self.imageWidth = None
		self.numPixels = None
		self.colorFrame = None
		self.previousGrayFrame = None
		self.currentGrayFrame = None
		self.resultImage = None
		self.windowName = "MotionDetectionDisplay"
		
	
	def initialize(self):
		"""Initialize the variables for the Biolume organism."""
		
		random.seed()
		
		# set variables for the Biolume organism
		self.clear_biolume_data()
		self.randomize_genome()
		self.make_viable()
		
	
	def quit_biolume(self):
		"""Close sockets, shut down sensors, and exit."""
		
		self.quit_server()
		self.destroy_sound_sensor()
		
		# TODO: release camera capture
		
		sys.exit(0)
	
	
	def randomize_genome(self):
		"""Create a random genome."""
		
		self.genes = []
		
		# create random instructions
		for i in range(constants.EXECUTABLE_GENOME_MAX_SIZE):
			instruction = random.randrange(0, constants.Instructions.NUM_INST)
			self.genes.append(instruction)
				
		# create random values for the actuators 
		for i in range(constants.Actuators.NUM_ACTUATORS):
			variable = random.randrange(0, constants.VARIABLE_MAX)
			self.genes.append(variable)
	
	
	def make_viable(self):
		"""Make sure this Biolume can reproduce."""
		
		# check if this biolume is already viable. if so, return
		for index in range(self.executableGenomeSize):
			if self.genes[index] == constants.Instructions.REPRODUCE:
				return
		
		# make viable
		insertionLocation = random.randrange(0, self.executableGenomeSize)
		
		# attempt insertion mutation
		if self.executableGenomeSize < constants.EXECUTABLE_GENOME_MAX_SIZE:
			self.genes.pop(constants.EXECUTABLE_GENOME_MAX_SIZE)
			self.genes.insert(insertionLocation, constants.Instructions.REPRODUCE)
			self.executableGenomeSize += 1
		# do a point mutation if insertion mutation fails
		else:
			self.genes[insertionLocation] = constants.Instructions.REPRODUCE
	
	
	def clear_biolume_data(self):
		"""Clear the variables for the Biolume organism."""
		
		self.executableGenomeSize = constants.EXECUTABLE_GENOME_INIT_SIZE # this is the number of genes that are to be executed
		self.pc = 0
		self.skip = False
		self.label = -1
		self.genes = []
		self.energy = 0
		
		self.displayBuffer = [constants.OFF]*constants.Actuators.NUM_ACTUATORS
		self.currentDisplay = [constants.OFF]*constants.Actuators.NUM_ACTUATORS
		self.previousDisplay = [constants.OFF]*constants.Actuators.NUM_ACTUATORS
		self.messageBuffer = [constants.OFF]*constants.Actuators.NUM_ACTUATORS
	
	
	def initialize_sound_sensor(self):
		"""Initialize objects used for sensing sound."""
		
		self.pyaudioObject = pyaudio.PyAudio()
		self.micStream = self.pyaudioObject.open(format=constants.FORMAT,
						channels=constants.CHANNELS,
						rate=constants.RATE,
						input=True,
						frames_per_buffer=constants.CHUNK)
		
		self.timeDetected = datetime.now() - timedelta(seconds=constants.SOUND_MEMORY+1)
		self.soundMemory = timedelta(seconds=constants.SOUND_MEMORY)
		
	
	def destroy_sound_sensor(self):
		"""Shutdown the objects used for sensing sound."""
		
		self.micStream.stop_stream()
		self.micStream.close()
		self.pyaudioObject.terminate()
		
	
	def take_mic_reading(self):
		"""Take a reading from the microhone.
		
		If a sound with an amplitude over RMS_THRESHOLD is detected the variable soundHeard 
		is set to true. if another loud noise is not heard for SOUND_MEMORY seconds then 
		soundHeard is set back to false. Returns true if successfully read from mic, false 
		otherwise.
		
		"""
		
		try:
			# for some reason this function blocks only on certain systems
			data = self.micStream.read(constants.CHUNK)
			
			# calculate amplitude with root mean square
			rms = audioop.rms(data, 2)  # width=2 for format=paInt16
			
			#print rms
			
			if rms > constants.RMS_THRESHOLD:
				self.soundHeard = True
				self.timeDetected = datetime.now()
				
			if (datetime.now() - self.timeDetected) > self.soundMemory:
				self.soundHeard = False
		
		except IOError:
			# failed to read from mic stream
			return False
		else:
			return True
		
	
	def initialize_motion_detection(self):
		"""Initialize objects used for motion detection."""
		
		self.camera = cv.CaptureFromCAM(0)
		
		#cv.NamedWindow(self.windowName, cv.CV_WINDOW_AUTOSIZE)
		
		# lower the resolution of the camera
		height = 120
		width = 160
		cv.SetCaptureProperty(self.camera, cv.CV_CAP_PROP_FRAME_WIDTH, width)
		cv.SetCaptureProperty(self.camera, cv.CV_CAP_PROP_FRAME_HEIGHT, height)

		# set variables containing video information
		self.colorFrame = cv.QueryFrame(self.camera)
		self.imageHeight = self.colorFrame.height
		self.imageWidth= self.colorFrame.width
		self.numPixels = self.imageHeight * self.imageWidth
		depth = self.colorFrame.depth
		imageSize = cv.GetSize(self.colorFrame)

		# create image structure for processing
		self.previousGrayFrame = cv.CreateImage(imageSize, depth, 1)
		self.currentGrayFrame = cv.CreateImage(imageSize, depth, 1)
		self.resultImage = cv.CreateImage(imageSize, depth, 1)
		cv.CvtColor(self.colorFrame, self.previousGrayFrame, cv.CV_RGB2GRAY)
		
		self.previousGrayFrame = self.reduce_image_noise(self.previousGrayFrame)
		
	
	def detect_motion(self):
		"""Detect motion."""
		
		# get rid of old images in buffer
		for i in range(5):
			cv.GrabFrame(self.camera)
		
		self.colorFrame = cv.QueryFrame(self.camera)
		self.process_image()
		self.motionDetected = self.is_motion_detected2(10)
		
		#cv.ShowImage(self.windowName, self.resultImage)
		#cv.WaitKey(1)
		
	
	def reduce_image_noise(self, image):
		"""Reduce noise from image, etc.
		
		Prameters:
			
			image
				The image to be processed
				
		"""
		
		cv.Smooth(image, image, cv.CV_BLUR, 5, 5)
		#cv.MorphologyEx(image, image, None, None, cv.CV_MOP_OPEN)
		#cv.MorphologyEx(image, image, None, None, cv.CV_MOP_CLOSE)
		
		return image
	
	
	def process_image(self):
		"""Process the image from the camera in order to remove noise."""
		
		cv.CvtColor(self.colorFrame, self.currentGrayFrame, cv.CV_RGB2GRAY)
		
		# remove noise, etc.
		self.currentGrayFrame = self.reduce_image_noise(self.currentGrayFrame)
		
		# find the difference between the current and previous frame
		cv.AbsDiff(self.currentGrayFrame, self.previousGrayFrame, self.resultImage)
		
		# calculate the binary image that shows where there is change in the image
		cv.Threshold(self.resultImage, self.resultImage, 10, 255, cv.CV_THRESH_BINARY_INV)
		
		cv.Copy(self.currentGrayFrame, self.previousGrayFrame)
	
	
	def is_motion_detected1(self):
		"""Check whether or not motion is detected in the most recent image and return a boolean."""
		
		numBlack = 0	# the number of pixels that are black - showing movement
	
		# iterate through each pixel in the image
		for x in range(self.imageHeight): 
			for y in range(self.imageWidth):
				if self.resultImage[x,y] == 0.0:	# if the pixel is black
					numBlack += 1
				
		avg = (numBlack*100.0) / self.numPixels	# calculate the percent of pixels that are black
		
		# if the percent is over the threshold, movement is detected
		if avg > constants.MOTION_THRESHOLD:
			return True
		else:
			return False
			
	
	def is_motion_detected2(self, divisor):
		"""Check whether or not motion is detected in the most recent image and return a boolean.
		
		Parameters:
			divisor
				Only check every divisor'th pixel in order to speed up processing.
		
		"""
		
		numBlack = 0	# the number of pixels that are black - showing movement
	
		# iterate through the pixels in the image
		for x in range(self.imageHeight/divisor): 
			for y in range(self.imageWidth/divisor):
				if self.resultImage[x*divisor,y*divisor] == 0.0:	# if the pixel is black
					numBlack += 1
				
		avg = (numBlack*100.0) / (self.numPixels/divisor)	# calculate the percent of pixels that are black
		
		# if the percent is over the threshold, movement is detected
		if avg > constants.MOTION_THRESHOLD:
			return True
		else:
			return False
	
	
	def create_genes_message(self):
		"""Create a message containing genes infomraiton for sending to the Manager.
		
		Message format: id,messageType,executableSize,data(genes seperated by commas)
		
		"""
		
		message = str(self.id) + ",genes," + str(self.executableGenomeSize)
		
		# add the genes
		for gene in self.genes:
			message += "," + str(gene)
		
		return message

	
	def create_display_message(self):
		"""Create a message containing message buffer information for sending to the Manager, 
		which will send it to neighboring Biolumes.
		
		Message format: id,messageType,data(message buffer)
		
		"""
		
		message = str(self.id) + ",display"
		
		# add the display information from the message buffer
		for variable in self.displayBuffer:
			message += "," + str(variable)
		
		return message
		

	def send_message_to_manager(self, message):
		"""Send a message to the Manager program."""
		
		self.clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.clientSocket.connect((self.remoteHost, self.remotePort))
		self.clientSocket.sendall(message)
		self.clientSocket.close()
	
	
	def start_server(self):
		"""Open the server socket for receiving genes from the manager."""
		
		self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
		self.serverSocket.setblocking(0) # non-blocking
		self.serverSocket.bind((self.localHost, self.localPort))
		self.serverSocket.listen(5)
		
	
	def quit_server(self):
		"""Close the server socket."""
		
		self.serverSocket.close()
	
	
	def check_for_message(self):
		"""Check for and return a message from the Manager.
		
		start_server must be called before this function can be called.
		
		"""
		
		pair = None
		try:
			pair = self.serverSocket.accept()
		except socket.error as err:
			# fail silently
			return None
			
		# this only gets the first connection in the backlog
		if pair is not None:
			connection, address = pair
			data = connection.recv(1024)
			return data
		else:
			return None
	
	
	def process_genes_message(self, genesMessage):
		"""Parse a message from the Manager that contains new genes.
		
		Parameters:
			genesMessage
				A string message from the Manager that contains genes information
			
		"""
		
		words = repr(genesMessage).strip(string.punctuation).split(',')
		words.pop(0) # get rid of the sender info
		words.pop(0) # get rid of the message type
		
		# reset the biolume
		self.clear_biolume_data()
		
		# put the recieved genes into the genome
		self.executableGenomeSize = words.pop(0)
		self.genes = words
	
	
	def process_display_message(self, displayMessage):
		"""Parse a message from the Manager that contains display information.
		
		Parameters:
			display
				A string message from the Manager that contains display information of another Biolume
			
		"""
		
		words = repr(displayMessage).strip(string.punctuation).split(',')
		words.pop(0) # get rid of the sender info
		words.pop(0) # get rid of the message type
		
		self.messageBuffer = words
		
	
	def parse_message(self, message):
		"""Parse a message from the Manager.
		
		Parameters:
			message
				A string message received from the Manager
		
		"""
		
		print "received: ", message
		
		words = repr(message).strip(string.punctuation).split(',')
		if words[0] == "manager":
			
			if words[1] == "genes":
				self.process_genes_message(message)
			elif words[1] == "display":
				self.process_display_message(message)
			elif words[1] == "quit":
				self.quit_biolume()
			
			# other message types...
	
	
	def execute_instruction(self):
		"""Execute the next instruction in this Biolume's executable genome."""
		
		# check for skip
		if self.skip:
			self.skip = False
		
		# execute the next instruction
		else:
			
			# pay the energy cost of execution
			self.add_energy(-1.0*constants.ENERGY_DECAY)
			
			instruction = int(self.genes[self.pc])
			
			print "instruction: " + str(instruction)
		
			if (instruction == constants.Instructions.NOP):
				self.nop()
			elif (instruction == constants.Instructions.LED0_ON):
				self.led0_on()
			elif (instruction == constants.Instructions.LED0_OFF):
				self.led0_off()
			elif (instruction == constants.Instructions.LED0_TOGGLE):
				self.led0_toggle()
			elif (instruction == constants.Instructions.LED1_ON):
				self.led1_on()
			elif (instruction == constants.Instructions.LED1_OFF):
				self.led1_off()
			elif (instruction == constants.Instructions.LED1_TOGGLE):
				self.led1_toggle()
			elif (instruction == constants.Instructions.SOUND_ON):
				self.speaker_on()
			elif (instruction == constants.Instructions.SOUND_OFF):
				self.speaker_off()
			elif (instruction == constants.Instructions.JUMP):
				self.jump_to_label()
			elif (instruction == constants.Instructions.LABEL):
				self.set_label()
			elif (instruction == constants.Instructions.IF_MOTION):
				self.if_motion()
			elif (instruction == constants.Instructions.IF_N_MOTION):
				self.if_not_motion()
			elif (instruction == constants.Instructions.IF_SOUND):
				self.if_sound()
			elif (instruction == constants.Instructions.IF_N_SOUND):
				self.if_not_sound()
			elif (instruction == constants.Instructions.IF_TOUCH):
				self.if_touch()
			elif (instruction == constants.Instructions.IF_N_TOUCH):
				self.if_not_touch()
			elif (instruction == constants.Instructions.IF_CO2):
				self.if_co2()
			elif (instruction == constants.Instructions.IF_N_CO2):
				self.if_not_co2()
			elif (instruction == constants.Instructions.REPRODUCE):
				self.reproduce()
			elif (instruction == constants.Instructions.SEND_MESSAGE):
				self.broadcast_neighborhood()
			elif (instruction == constants.Instructions.MESS_TO_BUFF):
				self.message_to_buffer()
			elif (instruction == constants.Instructions.DIS_TO_BUFF):
				self.display_to_buffer()
			elif (instruction == constants.Instructions.BUFF_TO_DIS):
				self.buffer_to_display()
		
		# increment the program counter
		self.pc = (self.pc + 1) % int(self.executableGenomeSize)
	
	
	def nop(self):
		"""Do nothing."""
		
		pass
	
	
	def led0_on(self):
		"""Turn on LED 0."""
		
		self.previousDisplay[constants.Actuators.LED0] = self.currentDisplay[constants.Actuators.LED0]
		self.currentDisplay[constants.Actuators.LED0] = self.genes[constants.EXECUTABLE_GENOME_MAX_SIZE + constants.Actuators.LED0]
	
	
	def led0_off(self):
		"""Turn off LED 0."""
		
		self.previousDisplay[constants.Actuators.LED0] = self.currentDisplay[constants.Actuators.LED0]
		self.currentDisplay[constants.Actuators.LED0] = constants.OFF
	
	
	def led0_toggle(self):
		"""Toggle LED 0."""
		
		self.previousDisplay[constants.Actuators.LED0] = self.currentDisplay[constants.Actuators.LED0]
		
		if self.currentDisplay[constants.Actuators.LED0] == constants.OFF:
			self.currentDisplay[constants.Actuators.LED0] = self.genes[constants.EXECUTABLE_GENOME_MAX_SIZE + constants.Actuators.LED0]
		else:
			self.currentDisplay[constants.Actuators.LED0] = constants.OFF
	
	
	def led1_on(self):
		"""Turn on LED 1."""
		
		self.previousDisplay[constants.Actuators.LED1] = self.currentDisplay[constants.Actuators.LED1]
		self.currentDisplay[constants.Actuators.LED1] = self.genes[constants.EXECUTABLE_GENOME_MAX_SIZE + constants.Actuators.LED1]
	
	
	def led1_off(self):
		"""Turn off LED 1."""
		
		self.previousDisplay[constants.Actuators.LED1] = self.currentDisplay[constants.Actuators.LED1]
		self.currentDisplay[constants.Actuators.LED1] = constants.OFF
	
	
	def led1_toggle(self):
		"""Toggle LED 1."""
		
		self.previousDisplay[constants.Actuators.LED1] = self.currentDisplay[constants.Actuators.LED1]
		
		if self.currentDisplay[constants.Actuators.LED1] == constants.OFF:
			self.currentDisplay[constants.Actuators.LED1] = self.genes[constants.EXECUTABLE_GENOME_MAX_SIZE + constants.Actuators.LED1]
		else:
			self.currentDisplay[constants.Actuators.LED1] = constants.OFF
	
	
	def speaker_on(self):
		"""Turn on the speaker."""
		
		self.previousDisplay[constants.Actuators.SPEAKER] = self.currentDisplay[constants.Actuators.SPEAKER]
		self.currentDisplay[constants.Actuators.SPEAKER] = self.genes[constants.EXECUTABLE_GENOME_MAX_SIZE + constants.Actuators.SPEAKER]
	
	
	def speaker_off(self):
		"""Turn off the speaker."""
		
		self.previousDisplay[constants.Actuators.SPEAKER] = self.currentDisplay[constants.Actuators.SPEAKER]
		self.currentDisplay[constants.Actuators.SPEAKER] = constants.OFF
	
	
	def jump_to_label(self):
		"""Jump to the instruction at the index of label."""
		
		# set pc to label but subtract 1 because pc will be incremented at the end of execute_instruction
		
		# if label is set
		if self.label == 0:
			# put the pc to the end of the genome
			self.pc = self.executableGenomeSize - 1
		elif self.label != -1:
			self.pc = self.label - 1
			
		# do nothing if label is not set, ie if lable equals -1
		
	
	def set_label(self):
		"""Set the lable index."""
		
		# set the jump point to the next instruction in the genome
		self.label = (self.pc + 1) % int(self.executableGenomeSize)
	
	
	def if_motion(self):
		"""Iff motion is detected execute the next instruction."""
		
		if not self.motionDetected:
			self.skip = True
	
	
	def if_not_motion(self):
		"""Iff motion is not detected execute the next instruction."""
		
		if self.motionDetected:
			self.skip = True
	
	
	def if_sound(self):
		"""Iff sound is detected execute the next instruction."""
		
		if not self.soundHeard:
			self.skip = True
	
	
	def if_not_sound(self):
		"""Iff sound is not detected execute the next instruction."""
		
		if self.soundHeard:
			self.skip = True
	
	
	def if_touch(self):
		"""Iff touch is detected execute the next instruction."""
		
		# TODO: check sensor
		pass
	
	
	def if_not_touch(self):
		"""Iff touch is not detected execute the next instruction."""
		
		# TODO: check sensor
		pass
	
	
	def if_co2(self):
		"""Iff CO2 is detected execute the next instruction."""
		
		# TODO: check sensor
		pass
	
	
	def if_not_co2(self):
		"""Iff CO2 is not detected execute the next instruction."""
		
		# TODO: check sensor
		pass
	
	
	def reproduce(self):
		"""Send genes to the Manager."""
		
		# calculate the probability of successful reproduction...
		# if successful send genes
		genesMessage = self.create_genes_message()
		self.send_message_to_manager(genesMessage)
	
	
	def broadcast_neighborhood(self):
		"""Send the information stored in the display buffer to the Manager."""
		
		self.send_message_to_manager(self.create_display_message())
		
	
	def message_to_buffer(self):
		"""Copy the received display message to the display buffer."""
		
		self.displayBuffer = self.messageBuffer
	
	
	def display_to_buffer(self):
		"""Copy the current display to the display buffer."""
		
		self.displayBuffer = self.currentDisplay
	
	
	def buffer_to_display(self):
		"""Copy the buffer to the current display."""
		
		self.currentDisplay = self.displayBuffer
		
	
	def add_energy(self, energy):
		"""Add energy to this Biolume and make sure it doesn't exceed ENERGY_MAX or fall belpw 0.
		
		Parameters:
			
			energy
				The amount of energy to add
				
		"""
		
		self.energy += energy
		if self.energy < 0: self.energy = 0
		elif self.energy > constants.ENERGY_MAX: self.energy = constants.ENERGY_MAX
	
	
	def read_sensors(self, totalIterations, soundSubiterations):
		"""Take readings from each of the sensors.
		
		Parameters:
		
			totalIterations:
				The number of times to read from sensors
			soundSubIterations
				The number of times to read from the sound sensor each iteration of totalIterations
		
		"""
		
		for i in range(totalIterations):
			for j in range(soundSubiterations):
				# take reading from mic
				self.take_mic_reading()
			
			# take reading from camera
			self.detect_motion()
	
	
	def award_energy_for_sensors(self):
		"""Check the sensors and award this Biolume by incrementing energy accordingly."""
		
		# check sound sensor
		if self.soundHeard: self.add_energy(constants.NEW_SOUND_ENERGY)
		
		# check motion sensor
		if self.motionDetected: self.add_energy(constants.MOTION_ENERGY)
		
		# check CO2 sensor...
		
		# check touch sensor...
	
	
	def step(self):
		"""Advance the Biolume through by a step."""
		
		# check for messages from the manager
		message = self.check_for_message()
		if message is not None:
			self.parse_message(message)
		
		if self.useSensors:
			# take reading from camera
			self.detect_motion()
			
			# take reading from the camera
			self.take_mic_reading()
			self.take_mic_reading()
			
			self.award_energy_for_sensors()
		
		self.execute_instruction()
	
	
	def run(self):
		"""Run the Biolume."""
		
		# start the server for listening to the manager
		self.start_server()
		
		# initialize the sensors
		if self.useSensors:
			self.initialize_sound_sensor()
			self.initialize_motion_detection()
		
		
		self.send_message_to_manager(self.id + ",started")
		print "genes: ", self.genes
		
		#
		while True:
			
			if not self.useSensors:
				time.sleep(constants.PAUSE_TIME) # find a better way to do this
			else:
				print "motion: ", self.motionDetected
				print "sound: ", self.soundHeard
			
			self.step()
			

if __name__ == "__main__":
	"""If this is run as main, run the Biolume program.
	
	Parameters:
	
		sys.argv[1]
			The ip address of the computer running the Manager program
		sys.argv[2]
			The id to be assigned to the Biolume
		sys.argv[3]...
			Options: -ds (disable sensors)
			
	"""
	
	ipAddress = sys.argv[1]
	biolumeId = sys.argv[2]
	if "-ds" in sys.argv:
		useSensors = False
	else:
		useSensors = True
	
	try:
		b = Biolume(biolumeId, ipAddress, useSensors)
		b.run()
	
	except (KeyboardInterrupt, SystemExit):
		b.quit_biolume()
	
	
	
	
	
