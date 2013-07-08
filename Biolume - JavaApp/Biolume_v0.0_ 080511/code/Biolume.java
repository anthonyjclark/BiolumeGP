
import java.awt.Point;
import java.util.Random;

import processing.core.PConstants;

public class Biolume {
	
	/**************************************************************************
	 * Definitions 
	 */
	// number of instructions in the genome
	private static final int EXE_SIZE_MAX = 20;
	private static final int EXE_SIZE_MIN = 5;
	private static final int EXE_SIZE_INIT = 10;

	// mutation rates ( http://www.krl.caltech.edu/avida/manual/mutation.html )
	private static final double MUT_PROB_COPY = (1.0 / EXE_SIZE_INIT);
	private static final double MUT_PROB_INSERT = (MUT_PROB_COPY / 2.2);
	private static final double MUT_PROB_DELETE = (MUT_PROB_COPY / 2.2);

	// max genome size (including varaiables and insertion space)
	private static final int GENOME_SIZE_MAX = (EXE_SIZE_MAX + Variable.NUM_VARS);
	
	// number of steps to skip while reproducing
	public static final int REPRO_COUNT = 3;
	
	// random number generator
	public static final Random RAND = new Random();
	
	
	/**************************************************************************
	 * Biolume Data
	 */
	// this Biolume's address
	private Point address = new Point();
	
	// size of this Biolume's genome
    private int exe_size;
    
    // current position in the program
    private int pc;
    
    // flag used for condition instructions
    private boolean skipNext;
    
    // flag to show if individual is a child (1) or a parent (0)
    private boolean is_child;
    
    // Biolume genome, including instructions and variables
    private int[] genome = new int[GENOME_SIZE_MAX];
    
    // number of steps since the last time this Biolume was replaced 
    private int age;
    
    // counter used to determine a Biolume's reproduction status
    private int repro_cnt;

	// amount of energy acquired by this Biolume
    private double energy;

	// current status of the LEDs and speaker for this Biolume
    private int[] display_current = new int[Variable.NUM_VARS];
    
    // previous status of the LEDs and speaker
    private int[] display_previous = new int[Variable.NUM_VARS];
    
    // a buffer for the display and messaging system
    private int[] display_buffer = new int[Variable.NUM_VARS];

	// a buffer used for messaging other Biolumes
    private int[] message_buffer = new int[Variable.NUM_VARS]; 
    
    // a reference to the processing application
    private Simulator sim;
    
    private boolean touchDetected = false;
	private boolean motionDetected = false;
    private boolean soundDetected = false;
    private boolean CO2Detected = false;
    
    /**************************************************************************
     * Biolume methods 
     */
    
	/**
	 * Biolume initialization
	 * @param parent: 	a reference to the Processing application
	 * @param x:		the x coordinate of this Biolume
	 * @param y:		the y coordinate of this Biolume
	 */
	Biolume(Simulator parent, int x, int y) {
		
		// set the parent to the parent processing application
		this.sim = parent;
		
		// set this Biolume's address
		this.address.x = x;
		this.address.y = y;
		
		// call the reset for this Biolume
		this.reset();
	}
	
	
	/**************************************************************************
	 * Biolume activity methods
	 */

	/**
	 * step the Biolume through a single instruction
	 */
	public void step() {
		
		// if this Biolume is reproducing do not execute any instructions
		if (this.repro_cnt > 0) {
			this.repro_cnt--;
			return;
		}
		
		// check for conditional skip
		if (this.skipNext) {
			incPC();
			this.skipNext = false;
		}
		
		// increment the program counter
		incPC();
		
		// advance this Biolume's age
		this.age++;
		
		// execute the current instruction
		Instruction.values()[this.genome[this.pc]].execute(this);
		
		// decay the energy used to execute the instruction
		this.add_energy(-1.0 * Instruction.ENERGY_DECAY); 	
	}
	
	/**************************************************************************
	 * Biolume Simulation methods
	 */
	
	/**
	 * Update the Processing display for this Biolume 
	 */
	public void display() {
		
		String id; 
		
		// clear previous LED0 color
		this.sim.fill(0,0,0);
		this.sim.rect(this.address.x  * Simulator.X_DISP, this.address.y * Simulator.Y_DISP, Simulator.WIDTH, Simulator.HEIGHT);
		
		// draw the Biolume
		if (this.is_reproducing()) {
			// set LED0 to white
			this.sim.fill(0,0,Variable.MAX_VAR);
			this.sim.ellipseMode(PConstants.CORNER);
			this.sim.ellipse(this.address.x  * Simulator.X_DISP, this.address.y * Simulator.Y_DISP, Simulator.WIDTH, Simulator.HEIGHT);
			this.sim.fill(0,0,0);
			if (this.is_child) id = "c"; else id = "p";
			this.sim.text(Integer.toString(this.repro_cnt) + "(" + id + ")", this.address.x * Simulator.X_DISP + 15, this.address.y * Simulator.Y_DISP + 30);
		}
		else {
			// set LED0 to specified color
			this.sim.fill(this.display_current[0], this.display_current[1], this.display_current[2]);
			this.sim.rect(this.address.x  * Simulator.X_DISP, this.address.y * Simulator.Y_DISP, Simulator.WIDTH/2, Simulator.HEIGHT);
			
			this.sim.fill(this.display_current[3], this.display_current[4], this.display_current[5]);
			this.sim.rect(this.address.x  * Simulator.X_DISP+Simulator.WIDTH/2, this.address.y * Simulator.Y_DISP, Simulator.WIDTH/2, Simulator.HEIGHT);
	
			if (this.display_current[8] > Variable.MAX_VAR/2) {
			    this.sim.image(Simulator.sound, this.address.x * Simulator.X_DISP + Simulator.WIDTH/4, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT/8, Simulator.WIDTH/2, Simulator.HEIGHT/2);    
			}
			if (this.get_energy() <= 0)
				this.sim.image(Simulator.battery0, this.address.x * Simulator.X_DISP, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT *9/16, Simulator.WIDTH, Simulator.HEIGHT *9/16);
			else if (this.get_energy() <= 0.25 * Instruction.ENERGY_MAX)
				this.sim.image(Simulator.battery1, this.address.x * Simulator.X_DISP, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT *9/16, Simulator.WIDTH, Simulator.HEIGHT *9/16);
			else if (this.get_energy() <= 0.5 * Instruction.ENERGY_MAX)
				this.sim.image(Simulator.battery2, this.address.x * Simulator.X_DISP, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT *9/16, Simulator.WIDTH, Simulator.HEIGHT *9/16);
			else if (this.get_energy() <= 0.75 * Instruction.ENERGY_MAX)
				this.sim.image(Simulator.battery3, this.address.x * Simulator.X_DISP, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT *9/16, Simulator.WIDTH, Simulator.HEIGHT *9/16);
			else // this.get_energy() <= 1.0 * Instruction.ENERGY_MAX)
				this.sim.image(Simulator.battery4, this.address.x * Simulator.X_DISP, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT *9/16, Simulator.WIDTH, Simulator.HEIGHT *9/16);
		}
	}
	

	/**************************************************************************
	 * Biolume field setters and getters
	 */
	
    /**
     * @return:	the current amount of energy this Biolume has
     */
    public double get_energy() {
		return energy;
	}
    
	/**
	 * @param energy:	the amount of energy this Biolume should have
	 */
	public void set_energy(double energy) {
		this.energy = energy;
		if (this.energy > Instruction.ENERGY_MAX) this.energy = Instruction.ENERGY_MAX;
		if (this.energy < 0) this.energy = 0;
	}
	
	/**
	 * @param addition:	an additional amount of energy to give the Biolume
	 */
	public void add_energy(double addition) {
		this.energy += addition;
		if (this.energy > Instruction.ENERGY_MAX) this.energy = Instruction.ENERGY_MAX;
		if (this.energy < 0) this.energy = 0;
	}
    
    /**
     * Set this Biolume's outward display
     * @param display_vals:	the values to display
     */
    public void set_display(int[] display_vals) {
    	
    	for (int i = 0; i < this.display_current.length; i++) {
    		
    		// first save the current display
    		this.display_previous[i] = this.display_current[i];
    		
    		// then update the display buffer
    		this.display_current[i] = display_vals[i];
    	}

    }
    
    /**
     * @return:	the current values being displayed
     */
    public int[] get_display() {
    	return this.display_current.clone();
    }
    
    /**
     * place the contents of message into the display buffer 
     */
    public void message_to_buffer() {
    	
    	for (int i = 0; i < this.display_buffer.length; i++) {
    		this.display_buffer[i] = this.message_buffer[i];
    	}
    }
    
    /**
     * place the contents of the display buffer into the message
     */
    public void buffer_to_message() {
    	
    	for (int i = 0; i < this.message_buffer.length; i++) {
    		this.message_buffer[i] = this.display_buffer[i];
    	}
    }
    
	/**
	 * set the buffer to what is currently being displayed
	 */
	public void display_to_buffer() {
		
		for (int i = 0; i < this.display_buffer.length; i++) {
			this.display_buffer[i] = this.display_current[i];
		}
	}

	/**
	 * set the display to what is stored in the buffer
	 */
	public void buffer_to_display() {
		
		for (int i = 0; i < this.display_current.length; i++) {
			this.display_current[i] = this.display_buffer[i];
		}
	}


	/**
	 * broadcast the current message to all neighbors
	 */
	public void broadcast_neighborhood() {
		this.sim.local_broadcast(this.address, this.message_buffer.clone());		
	}
	
	public void set_message(int[] new_message) {
		
		for (int i = 0; i < this.message_buffer.length; i++) {
			this.message_buffer[i] = new_message[i];
		}
	}
	
    /**
     * @return: the previous values being displayed
     */
    public int[] get_previous_display() {
    	return this.display_previous.clone();
    }
    
    public int[] get_variables() {
    	int[] vars = new int[Variable.NUM_VARS];
    	for (int i = 0; i < Variable.NUM_VARS; i++) {
    		vars[i] = this.genome[i + EXE_SIZE_MAX];
    	}
    	return vars;
    }
    
    /**
     * @return:	the age of this Biolume
     */
    public int get_age() {
    	return this.age;
    }
    

	/**************************************************************************
	 * Biolume reproduction methods
	 */
    
    /**
     * @return: is this Biolume currently in the reproduction process
     */
    public boolean is_reproducing() {
    	return (this.repro_cnt > 0);
    }
    
    /**
     * place this Biolume in the reproduction state as a parent 
     */
    public void parent_reproduce() {
    	this.sim.reproduce_biolume(this.address, this.genome, this.exe_size, Simulator.LOW_NEIGHBOR);
    	this.repro_cnt = REPRO_COUNT;
    	this.is_child = false;
	}
    
	/**
	 * replace this Biolume with another
	 * @param new_genome:	the replacement genome
	 * @param new_exe_size:	the number of executed instructions
	 */
	public void child_reproduce(int[] new_genome, int new_exe_size, boolean step_after) {
		
	    // reset state information, copy and mutate
		this.reset();
		this.replace_genome(new_genome);
		this.exe_size = new_exe_size;
		this.mutate();

		while (!this.isViable()) {
			// repeat until we have a viable copy
			this.replace_genome(new_genome);
			this.exe_size = new_exe_size;
			this.mutate();
		}
		
		// set the reproduction count
		if (step_after) {
			// if this Biolume is stepped after the parent
			this.repro_cnt = REPRO_COUNT + 1;
		}
		else this.repro_cnt = REPRO_COUNT;
		
	}
	
	/**
	 * Mutate this Biolume's genome
	 */
	private void mutate() {
	
		// copy mutations
		for (int i = 0; i < this.exe_size; i++) {
			if (RAND.nextDouble() < MUT_PROB_COPY) {
				// mutate to random instruction
				this.genome[i] = RAND.nextInt(Instruction.INST_SET_SIZE);
			}
		}
		for (int i = EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++) {
			if (RAND.nextDouble() < MUT_PROB_COPY) {
			    // add number from Gaussian
			    this.genome[i] += (int)(RAND.nextGaussian() * 20);
				
			    if (i == EXE_SIZE_MAX || i == (EXE_SIZE_MAX+3) ) { // hues
				// stay in the correct range
				this.genome[i] %= Variable.MAX_VAR;
				if (this.genome[i] < 0) this.genome[i] += Variable.MAX_VAR;
			    }
			    else { // saturation or brightness
				if (this.genome[i] > Variable.MAX_VAR) this.genome[i] = Variable.MAX_VAR;
				else if (this.genome[i] < 0) this.genome[i] = 0;
			    }
			}
		}
	
		// insertion mutation (do not overflow genome space)
		if (RAND.nextDouble() < MUT_PROB_INSERT && this.exe_size < EXE_SIZE_MAX) {
			this.exe_size++;
			
			// get insertion location
			int index = RAND.nextInt(this.exe_size);
			
			// insert
			int swap1 = this.genome[index];
			this.genome[index] = RAND.nextInt(Instruction.INST_SET_SIZE);
			
			// shift instructions down
			for (int i = index+1; i < this.exe_size; i++) {
				int swap2 = this.genome[i];
				this.genome[i] = swap1;
				swap1 = swap2;
			}
		}
		
		// deletion mutation (do not go below minimum instruction count)
		if (RAND.nextDouble() < MUT_PROB_DELETE && this.exe_size > EXE_SIZE_MIN) {
			
			// get deletion location
			int index = RAND.nextInt(this.exe_size);
			
			// shift
			this.exe_size--;
			for (int i = index; i < this.exe_size; i++) {
				this.genome[i] = this.genome[i+1];
			}
		}
	}
	
	/**
	 * reset the Biolume to initial state (random genome)
	 */
	private void reset() {
		
		// initial genome size
	    this.exe_size = EXE_SIZE_INIT;
	    
	    // initialize to random instructions
	    for (int i = 0; i < this.exe_size; i++) {
	    	// pick a random instruction to insert
	        this.genome[i] = RAND.nextInt(Instruction.INST_SET_SIZE);
	    }
	    
	    // initialize to random variables
	    for (int i = EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++) {
	        this.genome[i] = RAND.nextInt(Variable.MAX_VAR);
	    }
	    
	    // initialize state information
	    this.pc = 0;
	    this.age = 0;
	    for (int i = 0; i < Variable.NUM_VARS; i++) {
	    	this.message_buffer[i] = 0;
	    	this.display_current[i] = 0;
	    	this.display_previous[i] = 0;
	    	this.display_buffer[i] = 0;
	    }
	    this.energy = 0;
	    this.repro_cnt = 0;
	    this.is_child = true;
	    this.skipNext = false;
	    resetSensors();
	}
	
	/**
	 * replace this Biolume's genome
	 * @param new_genome: replacement genome
	 */
	private void replace_genome(int[] new_genome) {
		
		// replace this genome
		for (int i = 0; i < GENOME_SIZE_MAX; i++) {
			this.genome[i] = new_genome[i];
		}
	}
	
	/**
	 * replace this Biolume's genome
	 */
	private boolean isViable() {
		boolean hasRepros = false;
		for (int i = 0; i < this.exe_size && !hasRepros; i++)
			if (Instruction.values()[this.genome[i]] == Instruction.REPRODUCE) hasRepros = true;
		return hasRepros;
	}
	
	public float [] getLED0_color() {
	    float [] hsb = new float [3];
	    hsb [0] = this.display_current[0];
	    hsb [1] = this.display_current[1];
	    hsb [2] = this.display_current[2];    
	    return hsb;
	}
	
	public float [] getLED1_color() {
	    float [] hsb = new float [3];
	    hsb [0] = this.display_current[3];
	    hsb [1] = this.display_current[4];
	    hsb [2] = this.display_current[5];    
	    return hsb;
	}
	
	public int [] getGenome() {
		int [] g = new int[this.exe_size];
		for (int i = 0; i < this.exe_size; i++)
			g[i] = this.genome[i];
		return g;
	}
	
	public boolean LED0_isOn() {
		return this.display_current[6] == 1;
	}
	
	public boolean LED1_isOn() {
		return this.display_current[7] == 1;
	}
	
    public boolean getTouchDetected() {
		return touchDetected;
	}


	public void setTouchDetected(boolean touchDetected) {
		this.touchDetected = touchDetected;
	}


	public boolean getMotionDetected() {
		return motionDetected;
	}


	public void setMotionDetected(boolean motionDetected) {
		this.motionDetected = motionDetected;
	}


	public boolean getSoundDetected() {
		return soundDetected;
	}


	public void setSoundDetected(boolean soundDetected) {
		this.soundDetected = soundDetected;
	}


	public boolean getCO2Detected() {
		return CO2Detected;
	}


	public void setCO2Detected(boolean cO2Detected) {
		this.CO2Detected = cO2Detected;
	}
	
	public void skipNext() {
		skipNext = true;
	}
	
	public void resetSensors() {
	    this.touchDetected = false;
	    this.motionDetected = false;
	    this.soundDetected = false;
	    this.CO2Detected = false;
	}
	
	public void incPC() {
		this.pc = (this.pc + 1) % this.exe_size;
		if (this.pc == 0) resetSensors();
	}
	
	public void printInst() { // for testing purposes only! @DJ
		System.out.println("current inst: " + Instruction.values()[this.genome[this.pc]]);
	}
}