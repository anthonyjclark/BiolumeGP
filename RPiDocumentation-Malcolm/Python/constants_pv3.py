
"""Constants contains a list of constants used by both the Manager class
and the Biolume class.

"""

__author__ = "Malcolm Doering <doeringm@msu.edu>"
__credits__ = "Malcolm Doering"

import pyaudio


# the file containing Biolume ip addresses and locations
BIOLUME_INFO_FILENAME = "biolume_info.csv"


# speaker, led0, led1 - this class acts like an enum
class Actuators:
	"""An enumeration of the Biolume's actuators."""
	
	LED0, LED1, SPEAKER, NUM_ACTUATORS = list(range(4))

# the value of a variable when it is turned off
OFF = 0

# time paused before executing each instruction
PAUSE_TIME = 2


# genome constants

# maximum size of the executable genes
EXECUTABLE_GENOME_MAX_SIZE = 20
EXECUTABLE_GENOME_MIN_SIZE = 5

# initial size of the executable genes
EXECUTABLE_GENOME_INIT_SIZE = 10

# maximum size of the genes (including variables)
GENOME_MAX_SIZE = EXECUTABLE_GENOME_MAX_SIZE + Actuators.NUM_ACTUATORS

# variable maximum value
VARIABLE_MAX = 256


# energy management constants

# Specify a maximum energy that each Biolume can reach. Then, specify the 
# proportion of that maximum that is awarded for each sensed activity. 

# maximum energy level
ENERGY_MAX = 100

# energy decay
ENERGY_DECAY = ENERGY_MAX * 0.01

CO2_ENERGY = ENERGY_MAX * 0.01
NORMAL_SOUND_ENERGY = ENERGY_MAX * 0
NEW_SOUND_ENERGY = ENERGY_MAX * 0.05
MOTION_ENERGY = ENERGY_MAX * 0.1
TOUCH_ENERGY = ENERGY_MAX * 0.5


# reproduction constants

# probabilities of executing a successful reproduction process
REPRODUCTION_PROB_MAX = 0.8
REPRODUCTION_PROB_MIN = 0.1

# mutation rates ( http://www.krl.caltech.edu/avida/manual/mutation.html )
MUTATION_PROB_COPY = 1.0 / EXECUTABLE_GENOME_INIT_SIZE
MUTATION_PROB_INSERT = MUTATION_PROB_COPY / 2.2
MUTATION_PROB_DELETE = MUTATION_PROB_COPY / 2.2


# the instructions available - this class acts like an enum	
class Instructions:
	"""An enumeration of available instructions."""
	
	NOP, LED0_ON, LED0_OFF, LED0_TOGGLE, LED1_ON, LED1_OFF, LED1_TOGGLE, SOUND_ON, SOUND_OFF, JUMP, LABEL, IF_MOTION, IF_N_MOTION, IF_SOUND, IF_N_SOUND, IF_TOUCH, IF_N_TOUCH, IF_CO2, IF_N_CO2, REPRODUCE, SEND_MESSAGE, MESS_TO_BUFF, DIS_TO_BUFF, BUFF_TO_DIS, NUM_INST = list(range(25))


# constants for the microphone
CHUNK = 512
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 16000 			# rate for pseye is 16000 Hz
RMS_THRESHOLD = 2000 
SOUND_MEMORY = 5		# the biolume will remember hearing a sound over RMS_THRESHOLD for this many seconds


# constants for motion detection
MOTION_THRESHOLD = 1	# percent of the camera image that must have changed for motion to be detected










