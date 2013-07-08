/**
 * Biolume.java
 * @authors: Tony J. Clark, Daniel J. Couvertier, Adam Ford
 * @date: July 28, 2011
 * @description: This file describes a Biolume unit. In the physical world, a
 * Biolume will be a sculptural robot which will be placed along walls with
 * more of its kind as an interactive, evolving exhibition. It has multiple
 * sensors such as touch, motion, sound and CO2, while also having several
 * actuators such as a speaker and two LEDs which can exhibit every color in
 * the RGB colorscale. Also, Biolumes will have the capacity to communicate
 * with one another. In this Simulator, Biolumes are reduced to the sensors and
 * actuators in a mouse-interactive environment.  
 */

import java.awt.Point; 
import java.util.Random;

public class Biolume {

    /**************************************************************************
     * Biolume parameters.
     */
	
	// Random number generator.
	public static final Random RAND = new Random();
	
	// Max genome size (including variables and insertion space).
	private static final int GENOME_SIZE_MAX = (CC.EXE_SIZE_MAX + Variable.NUM_VARS);
	
    // Biolume genome, including instructions and variables.
    private int[] genome = new int[GENOME_SIZE_MAX];
	
	// size of this Biolume's executable genome.
    private int exe_size;

	// This Biolume's location.
	private Point location = new Point();
	
    // Pointers.
    private int pc;
    private int label;
    
    // Flag (relevant only in the simulator, for display purposes).
    private boolean is_offspring;
    private boolean alt_step;
    
    // Number of steps since this Biolume was replaced.
    private int age;
    
	// Energy acquired by this Biolume.
    private double energy;
    
    // counter used to determine a Biolume's reproduction status
    private int repro_countdown;
    
    // Biolume sensor countdown timers.
    private int[] sensor_countdowns = new int[CC.NUM_SENSORS];

	// Current status of the LEDs and speaker.
    private int[] display_current = new int[Variable.NUM_VARS];
    
    // Previous status of the LEDs and speaker.
    private int[] display_previous = new int[Variable.NUM_VARS];
    
    // Buffer for the display and messaging system.
    private int[] display_buffer = new int[Variable.NUM_VARS];

	// Buffer used for messaging other Biolumes.
    private int[] message_buffer = new int[Variable.NUM_VARS]; 
    
    // Reference to the processing application.
    private Simulator sim;
    
    /**************************************************************************
     * Biolume methods. 
     */
    
	/**
	 * Biolume constructor method. 
	 * @param parent:	A reference to the Processing application.
	 * @param x:	The x coordinate of this Biolume.
	 * @param y:	The y coordinate of this Biolume.
	 */
	Biolume(Simulator parent, int x, int y) {
		
		// Set the parent to the parent processing application.
		this.sim = parent;
		
		// Set this Biolume's address.
		this.location.x = x;
		this.location.y = y;
		
		// Set this Biolume's initial genome size.
	    this.exe_size = CC.EXE_SIZE_INIT;
	    
	    // Initialize the genome to random instructions.
	    for (int i = 0; i < this.exe_size; i++)
	        this.genome[i] = RAND.nextInt(Instruction.INST_SET_SIZE);
	    this.makeViable(); // Make sure the Biolume is viable.
	    
	    // Initialize to random variables.
	    for (int i = CC.EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++)
	    	this.genome[i] = RAND.nextInt(CC.MAX_VAR);
	    
	    this.reset();
	}
	
	
	/**************************************************************************
	 * Biolume activity and flow control management methods.
	 */

	/**
	 * Step this Biolume through a single instruction. Biolumes that are
	 * reproducing may not execute any instructions. First, a Biolume must pay
	 * the cost of executing an instruction, as specified by CC.ENERGY_DECAY.
	 * If the previous instruction was a conditional and the condition was not
	 * met, an instruction is skipped. Afterwards, the instruction is executed
	 * and its effects are expressed. Finally, all counters are updated. 
	 */
	public void step() {
		
		alt_step = !alt_step;
		
		// If this Biolume is reproducing do not execute any instructions.
		if (this.repro_countdown > 0) {
			this.repro_countdown--;
			return;
		}
		
		// Pay the energy cost of execution.
		this.addEnergy(-1.0 * CC.ENERGY_DECAY); 
		
		// execute the current instruction
		Instruction.values()[this.genome[this.pc]].execute(this);
			
		// update sensors
		for (int i = 0; i < CC.NUM_SENSORS; i++)
			if (this.sensor_countdowns[i] != 0) this.sensor_countdowns[i]--;
		
		this.age++;	// advance this Biolume's age
		incPC(); // increment the program counter	
	}
	
    /**
     * This method increments the program counter by 1. If the program counter
     * overflows the executable genome size, it wraps around to the first
     * instruction in the genome. 
     */
	public void incPC() {
		this.pc = (this.pc + 1) % this.exe_size;
	}
	
    /**
     * This method labels the next instruction as a target for future jumps.
     * This method actually sets the label to point to the current instruction
     * because the program counter will be increased prior to the execution of
     * the next instruction.
     */
	public void setLabel() {
		this.label = this.pc;
	}
	
    /**
     * This method jumps the program counter to the latest marked label. If
     * the LABEL instruction was never executed, the jump is made to the first
     * instruction in the genome. 
     */
	public void jumpToLabel() {
		this.pc = this.label; 
	}
	
	/**
	 * This method resets this Biolume's state information, message/display
	 * buffers, sensors, timers, and turns all actuators off.   
	 */
	private void reset() {
	    
	    // Initialize state information.
	    this.pc = 0;
	    this.label = -1;
	    this.age = 0;
	    this.energy = 0;
	    this.repro_countdown = 0;
	    this.is_offspring = true;
	    
	    // Clear message/display buffers.
	    for (int i = 0; i < Variable.NUM_VARS; i++) {
	    	this.message_buffer[i] = 0;
	    	this.display_current[i] = 0;
	    	this.display_previous[i] = 0;
	    	this.display_buffer[i] = 0;
	    }
	    
	    // Clear out sensors. 
	    for (int i = 0; i < CC.NUM_SENSORS; i++)
	    	this.sensor_countdowns[i] = 0;
	    
	    // Turn off all actuators. 
        genome[CC.EXE_SIZE_MAX + Variable.LED0_STATE.ordinal()] = CC.OFF;
        genome[CC.EXE_SIZE_MAX + Variable.LED1_STATE.ordinal()] = CC.OFF;
        genome[CC.EXE_SIZE_MAX + Variable.SPEAKER_STATE.ordinal()] = CC.OFF;
	}
	
	/**************************************************************************
	 * Biolume Simulation methods.
	 */
	
	/**
	 * Update the Processing display for this Biolume. 
	 */
	public void display() {
		
		String id; 
		
		// Draw the Biolume silhouette. 
		this.sim.fill(0, 0, 0);
		this.sim.rect(this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP, CC.B_WIDTH, CC.B_HEIGHT);
		
		if (this.isReproducing()) { // Draw the reproduction cycle & countdown.
			
			this.sim.image(Simulator.repro, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP, CC.B_WIDTH, CC.B_HEIGHT);
			this.sim.fill(0, 0, CC.MAX_VAR);
			if (this.is_offspring) id = "c"; else id = "p";
			this.sim.text(this.repro_countdown + "(" + id + ")", this.location.x * Simulator.X_DISP + 14, this.location.y * Simulator.Y_DISP + 30);
		}
		else { // Draw the Biolume LEDs, speaker state, and battery. 
			
			// Draw LED0. 
			this.sim.fill(this.display_current[0], this.display_current[1], this.display_current[2]);
			this.sim.rect(this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP, CC.B_WIDTH/2, CC.B_HEIGHT);
			
			// Draw LED1. 
			this.sim.fill(this.display_current[4], this.display_current[5], this.display_current[6]);
			this.sim.rect(this.location.x  * Simulator.X_DISP+CC.B_WIDTH/2, this.location.y * Simulator.Y_DISP, CC.B_WIDTH/2, CC.B_HEIGHT);
			
			// Draw speaker status. 
			if (speakerIsOn()) this.sim.image(Simulator.sound, this.location.x * Simulator.X_DISP + CC.B_WIDTH/4, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT/8);
			
			// Draw energy. 
			if (this.getEnergy() <= 0)
				this.sim.image(Simulator.battery0, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT *9/16);
			else if (this.getEnergy() <= 0.25 * CC.ENERGY_MAX)
				this.sim.image(Simulator.battery1, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT *9/16);
			else if (this.getEnergy() <= 0.5 * CC.ENERGY_MAX)
				this.sim.image(Simulator.battery2, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT *9/16);
			else if (this.getEnergy() <= 0.75 * CC.ENERGY_MAX)
				this.sim.image(Simulator.battery3, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT *9/16);
			else // this.get_energy() <= 1.0 * Instruction.ENERGY_MAX)
				this.sim.image(Simulator.battery4, this.location.x * Simulator.X_DISP, this.location.y * Simulator.Y_DISP + CC.B_HEIGHT *9/16);
		}
	}
    
	/**************************************************************************
	 * Getter methods.
	 */
    
    /**
     * This method returns this Biolume's age which is the amount of steps that
     * it has survived without being replaced. 
     * @return:	This Biolume's age.
     */
    public int getAge() {
    	return this.age;
    }
	
    /**
     * This method returns an array containing the ordered executable
     * instructions that compose this Biolume's genome. 
     * @return:	An array containing the instructions in this Biolume's genome.
     */
	public String [] getGenome() {
		String [] g = new String[this.exe_size];
		for (int i = 0; i < this.exe_size; i++)
			g[i] = Instruction.values()[this.genome[i]].toString();
		return g;
	}
    
	/**
	 * This method retrieves all the variables in a Biolume. Currently, and in
	 * order, these are the LED0 HSB values, the LED0 state, the LED1 HSB
	 * values, the LED1 state, the speaker MIDI index value, and the speaker
	 * state.
	 * @return: The HSB values and state for LED0 and LED1, and the speaker
	 * index and state for this Biolume.
	 */
    public int[] getVariables() {
    	int[] vars = new int[Variable.NUM_VARS];
    	for (int i = 0; i < Variable.NUM_VARS; i++) {
    		vars[i] = this.genome[i + CC.EXE_SIZE_MAX];
    	}
    	return vars;
    }
    
	/**
	 * This method returns the currently displayed values of hue, saturation
	 * and brightness for LED0.
	 * @return:	The HSB values for LED0.
	 */
	public float [] getLED0Color() {
	    float [] hsb = new float [3];
	    hsb [0] = this.display_current[Variable.LED0_H.ordinal()];
	    hsb [1] = this.display_current[Variable.LED0_S.ordinal()];
	    hsb [2] = this.display_current[Variable.LED0_B.ordinal()];    
	    return hsb;
	}
	
	/**
	 * This method returns the currently displayed values of hue, saturation
	 * and brightness for LED1.
	 * @return:	The HSB values for LED1.
	 */
	public float [] getLED1Color() {
	    float [] hsb = new float [3];
	    hsb [0] = this.display_current[Variable.LED1_H.ordinal()];
	    hsb [1] = this.display_current[Variable.LED1_S.ordinal()];
	    hsb [2] = this.display_current[Variable.LED1_B.ordinal()];    
	    return hsb;
	}
	
	/**
	 * This method returns whether LED0 is switched on or not.
	 * @return: True if LED0 is on, false if otherwise.
	 */
	public boolean LED0IsOn() {
		return this.display_current[Variable.LED0_STATE.ordinal()] == CC.ON;
	}
	
	/**
	 * This method returns whether LED1 is switched on or not.
	 * @return:	True if LED1 is on, false if otherwise.
	 */
	public boolean LED1IsOn() {
		return this.display_current[Variable.LED1_STATE.ordinal()] == CC.ON;
	}
	
	/**
	 * This method returns whether the speaker is switched on or not.
	 * @return:	True if the speaker is on, false if otherwise. 
	 */
	public boolean speakerIsOn() {
		if (this.display_current[Variable.SPEAKER_STATE.ordinal()] == CC.ON) return true;
		else return false;
	}
	
	/**************************************************************************
	 * Biolume display, buffer, and message management methods.
	 */
	
    /**
     * This method sets this Biolume's outward display. This includes the HSB
     * values and state for both LEDs, the speaker index to a MIDI file, and
     * the speaker state.
     * @param display_vals:	The values to display.
     */
    public void setDisplay(int[] display_vals) {
    	
    	for (int i = 0; i < this.display_current.length; i++) {
    		
    		// first save the current display
    		this.display_previous[i] = this.display_current[i];
    		
    		// then update the display buffer
    		this.display_current[i] = display_vals[i];
    	}

    }
    
    /**
     * This method returns this Biolume's current outward display. This
     * includes the HSB values and state for both LEDs, the speaker index to a
     * MIDI file, and the speaker state.
     * @return:	The current values being displayed.
     */
    public int[] getDisplay() {
    	return this.display_current.clone();
    }
    
    /**
     * This method returns this Biolume's current outward display. This
     * includes the HSB values and state for both LEDs, the speaker index to a
     * MIDI file, and the speaker state.
     * @return: the previous values being displayed
     */
    public int[] getPreviousDisplay() {
    	return this.display_previous.clone();
    }
    
    /**
     * This method places the contents of a message buffer into the display
     * buffer. A message includes the HSB values and state for 2 LEDs, as well
     * as a speaker index to a MIDI file and the speaker state.
     */
    public void messageToBuffer() {
    	
    	for (int i = 0; i < this.display_buffer.length; i++) {
    		this.display_buffer[i] = this.message_buffer[i];
    	}
    }
    
    /**
     * This method places the contents of the display buffer into the a message
     * buffer. This includes the HSB values and state for 2 LEDs, as well as a
     * speaker index to a MIDI file and the speaker state.
     */
    public void bufferToMessage() {
    	
    	for (int i = 0; i < this.message_buffer.length; i++) {
    		this.message_buffer[i] = this.display_buffer[i];
    	}
    }
    
    /**
     * This method places the contents of the current outward display into the
     * display buffer. This includes the HSB values and state for the LEDs, as
     * well as a speaker index to a MIDI file and the speaker state.
     */
	public void displayToBuffer() {
		
		for (int i = 0; i < this.display_buffer.length; i++) {
			this.display_buffer[i] = this.display_current[i];
		}
	}

    /**
     * This method places the contents of the display buffer into the current
     * outward display. This includes the HSB values and state for the LEDs, as
     * well as a speaker index to a MIDI file and the speaker state.
     */
	public void bufferToDisplay() {
		
		for (int i = 0; i < this.display_current.length; i++) {
			this.display_current[i] = this.display_buffer[i];
		}
	}

	/**
	 * This method places a message in the message buffer. The message includes
	 * the HSB values and state for both LEDs, the speaker index to a MIDI
	 * file, and the speaker state.
	 * @param new_message:	The message that is placed into the message buffer.
	 */
	public void setMessage(int[] new_message) {
		
		for (int i = 0; i < this.message_buffer.length; i++) {
			this.message_buffer[i] = new_message[i];
		}
	}
	
	/**
	 * This method broadcasts the current message to all neighbors. The message
	 * includes the HSB values and state for both LEDs, the speaker index to a
	 * MIDI file, and the speaker state.
	 */
	public void broadcastNeighborhood() {
		this.sim.localBroadcast(this.location, this.message_buffer.clone());		
	}
	
	/**************************************************************************
	 * Biolume reproduction management methods.
	 */
    
    /**
     * This method returns whether this Biolume is currently in the
     * reproduction process or not. The reproduction process lasts a total of 
     * CC.REPRO_COUNTDOWN time steps for both the parent and the offspring. 
     * @return:	True if this Biolume is currently in the reproduction process,
     * false if otherwise. 
     */
    public boolean isReproducing() {
    	return (this.repro_countdown > 0);
    }
    
    /**
     * This method places this Biolume in the reproduction state as a parent.
     * The simulator is in charge of carrying out the reproduction process.
     * Here, the parent's data is sent to the simulator which will pass it on
     * to the offspring and the parent's reproduction countdown is started.
     */
    public void parentReproduce() {
    	this.sim.reproduceBiolume(this.location, this.genome, this.exe_size, this.alt_step);
    	this.repro_countdown = CC.REPRO_COUNTDOWN;
    	this.is_offspring = false;
	}
    
	/**
	 * This method replaces this Biolume with a newborn offspring. First, all
	 * parameters are reset. Next, the genome is replaced with the parent's and
	 * exposed to mutations. If the mutations would render this Biolume
	 * sterile, it is made (potentially) fertile.
	 * @param new_genome:	The parent's genome.
	 * @param new_exe_size:	The parent's number of executable instructions.
	 * @param p_odd_step: Checksum to sync reproduction countdowns. 
	 */
	public void offspringReproduce(int[] new_genome, int new_exe_size, boolean p_odd_step) {
	
		// Copy the executable genome.
	    this.exe_size = new_exe_size;
	    for (int i = 0; i < this.exe_size; i++) this.genome[i] = new_genome[i];
	    
	    // Copy the variables.
	    for (int i = CC.EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++) this.genome[i] = new_genome[i];
		
	    // Reset the state, and expose to mutations.
	    this.reset(); 
		this.mutate(); 
		
		// Set the reproduction count.
		if ( (p_odd_step && this.alt_step) || 
				(!p_odd_step && !this.alt_step) ) // Child already executed.
			this.repro_countdown = CC.REPRO_COUNTDOWN;
		else this.repro_countdown = CC.REPRO_COUNTDOWN + 1;
	}
	
	/**
	 * This method exposes this Biolume's executable genome to copy, insertion,
	 *  and deletion mutations. A copy mutation is a replacement of an existing
	 * instruction with a randomly selected new one. An insertion mutation
	 * inserts a randomly selected new instruction into the genome as long as
	 * the executable genome size has not reached CC.EXE_SIZE_MAX. A deletion
	 * mutation removes a randomly selected instruction from the genome as long
	 * as the executable genome size has not reached CC.EXE_SIZE_MIN. Also, the
	 * Biolume's variables (LED0 and LED1 HSB values and the speaker index
	 * value) are exposed to mutations. They are only affected by the copy 
	 * mutation rate. 
	 */
	private void mutate() {
	
		// Deletion mutation (do not go below minimum instruction count).
		if (RAND.nextDouble() < CC.MUT_PROB_DELETE && this.exe_size > CC.EXE_SIZE_MIN) {
			
			// Get deletion location.
			int index = RAND.nextInt(this.exe_size);
			
			// Shift up.
			this.exe_size--;
			for (int i = index; i < this.exe_size; i++) {
				this.genome[i] = this.genome[i+1];
			}
		}
		
		// Copy mutations.
		for (int i = 0; i < this.exe_size; i++) 
			if (RAND.nextDouble() < CC.MUT_PROB_COPY) 
				// Mutate to random instruction.
				this.genome[i] = RAND.nextInt(Instruction.INST_SET_SIZE);
		for (int i = CC.EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++)
			if (RAND.nextDouble() < CC.MUT_PROB_COPY) {
			    // add number from Gaussian
			    this.genome[i] += (int)(RAND.nextGaussian() * 20);
				
			    if (i == (CC.EXE_SIZE_MAX + Variable.LED0_H.ordinal()) || 
			    		i == (CC.EXE_SIZE_MAX + Variable.LED1_H.ordinal()) ) { // Hues
			    	// Stay in the correct range accounting for wraparound.
					this.genome[i] %= CC.MAX_VAR;
					if (this.genome[i] < 0) this.genome[i] += CC.MAX_VAR;
				}
			    else { // Saturation or Brightness.
					if (this.genome[i] > CC.MAX_VAR) this.genome[i] = CC.MAX_VAR;
					else if (this.genome[i] < 0) this.genome[i] = 0;
			    }
			}
	
		// Insertion mutation.
		if (RAND.nextDouble() < CC.MUT_PROB_INSERT)
			this.insertMutation(RAND.nextInt(this.exe_size), RAND.nextInt(Instruction.INST_SET_SIZE));
		
		this.makeViable();
	}
	
	/**
	 * This method attempts to insert the given instruction into the given
	 * position in the executable genome space of this Biolume. If the
	 * insertion would overflow the genome space (the new genome size would be
	 * greater than CC.EXE_SIZE_MAX), then the insertion fails. Otherwise, the
	 * insertion is successful. 
	 * @param index:	The target position for the insertion. 
	 * @param instruction:	The instruction to be inserted.
	 * @return:	True if the insertion was successful, false if otherwise. 
	 */
	private boolean insertMutation(int index, int instruction) {
		
		if (this.exe_size >= CC.EXE_SIZE_MAX) return false;
		
		this.exe_size++;
		
		// insert
		int swap1 = this.genome[index];
		this.genome[index] = instruction;
		
		// shift instructions down
		for (int i = index+1; i < this.exe_size; i++) {
			int swap2 = this.genome[i];
			this.genome[i] = swap1;
			swap1 = swap2;
		}
		
		return true;
	}

	/** This method checks if the genome has at least one instance of the REPRO
	 * instruction. If there is not, and the genome length is less than 
	 * CC.EXE_SIZE_MAX, a REPRO is inserted into a random location in the
	 * genome. If the length is equal to the maximum size, a random instruction
	 * is replaced by the REPRO. 
	 */
	private void makeViable() {
		
		// Check if this Biolume is already viable. If so, escape. 
		for (int i = 0; i < this.exe_size; i++)
			if (this.genome[i] == Instruction.REPRODUCE.ordinal()) return;
		
		// We're still here, so this Biolume is not viable. 
		int insertIndex = RAND.nextInt(this.exe_size);
		
		// Attempt insertion mutation. If it fails, perform a point mutation. 
		if (!this.insertMutation(insertIndex, Instruction.REPRODUCE.ordinal()))
			this.genome[insertIndex] = Instruction.REPRODUCE.ordinal();
	}	
	
	/**************************************************************************
	 * Biolume energy management methods. 
	 */
	
	/**
	 * This method clears the energy acquired by this Biolume thus far, and
	 * supplies it with the specified amount. The amount of energy must be
	 * within the bounds of [0, CC.ENERGY_MAX].
	 * @param energy:	The energy supplied to this Biolume. 
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
		if (this.energy > CC.ENERGY_MAX) this.energy = CC.ENERGY_MAX;
		if (this.energy < 0) this.energy = 0;
	}
	
	/**
	 * This method supplies this Biolume with the specified amount of energy
	 * which is added to its current energy reserve. The amount of energy must
	 * be within the bounds of [0, CC.ENERGY_MAX].
	 * @param addition:	The energy that will be added to this Biolume's current
	 * energy reserve. 
	 */
	private void addEnergy(double addition) {
		this.energy += addition;
		if (this.energy > CC.ENERGY_MAX) this.energy = CC.ENERGY_MAX;
		if (this.energy < 0) this.energy = 0;
	}
	
	/**
     * @return:	The energy acquired by this Biolume. 
     */
    public double getEnergy() {
		return energy;
	}
	
	/**************************************************************************
	 * Sensor management methods.
	 */
	
	/**
	 * Sets the specified sensor as having detected activity and rewards the
	 * Biolume with the corresponding amount of energy as appropriate. If the
	 * sensor was already triggered in this time step, no additional reward is
	 * given (except in the case of SOUND which is cumulative). Also, in the
	 * case of SOUND, if the sound is a new sound (no sound was detected in the
	 * last time step), a different reward is assigned to this Biolume. The
	 * detection decays over CC.LONG_TERM_DURATION time steps.
	 * @param sensor_type:	The sensor for which activity was detected. 
	 */
	public void setDetection(int sensor_type) {
		if (sensor_type == CC.TOUCH && !this.getDetection(CC.TOUCH, CC.SHORT)) {
			this.addEnergy(CC.TOUCH_ENERGY);
			this.sensor_countdowns[CC.TOUCH] = CC.LONG_TERM_DURATION;
		}
		else if (sensor_type == CC.MOTION && !this.getDetection(CC.MOTION, CC.SHORT)) {
			this.addEnergy(CC.MOTION_ENERGY);
			this.sensor_countdowns[CC.MOTION] = CC.LONG_TERM_DURATION;
		}
		else if (sensor_type == CC.SOUND) {
			if (this.sensor_countdowns[CC.SOUND] < CC.LONG_TERM_DURATION - CC.NEW_SOUND_DELAY) 
					this.addEnergy(CC.NEW_SOUND_ENERGY);
			else this.addEnergy(CC.NORMAL_SOUND_ENERGY); // multiple sounds make more "loudness"
			this.sensor_countdowns[CC.SOUND] = CC.LONG_TERM_DURATION;
		}
		else if (sensor_type == CC.CO2 && !this.getDetection(CC.CO2, CC.SHORT)) {
			this.addEnergy(CC.CO2_ENERGY);
			this.sensor_countdowns[CC.CO2] = CC.LONG_TERM_DURATION;
		}
	}
	
	/**
	 * This method checks if a specific sensor detected activity in a specific
	 * term. The accepted sensor types are CC.TOUCH, CC.MOTION, CC.SOUND and
	 * CC.CO2. The accepted time terms are CC.SHORT and CC.LONG. Short term
	 * refers to the same time step. Long term refers to the last 
	 * CC.LONG_TERM_DURATION time steps. 
	 * @param sensor_type:	The requested sensor to check for detected activity. 
	 * @param term:	The requested term for checking detected activity.
	 * @return: True if the requested sensor detected activity in the requested
	 * term. False if otherwise.
	 */
	public boolean getDetection(int sensor_type, int term) {
		
		if(term == CC.SHORT)
			return (this.sensor_countdowns[sensor_type] == CC.LONG_TERM_DURATION);
		// if (term == CC.LONG) // no other options are acceptable.
			return (this.sensor_countdowns[sensor_type] != 0);
	}

}