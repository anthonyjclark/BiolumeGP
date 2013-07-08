
import java.awt.Point; 
import java.util.Random;

public class Biolume {
	
	/**************************************************************************
	 * Definitions 
	 */
	// number of instructions in the genome
	public static final int EXE_SIZE_MAX = 20;
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
    private int label;
    
    // flags
    private boolean skipNext;
    //private boolean soundLastStep; 
    private boolean is_child; 
    private boolean speaker_on; 
    //private boolean longTermTouchDetected = false;
	//private boolean longTermMotionDetected = false;
    //private boolean longTermSoundDetected = false;
    //private boolean longTermCO2Detected = false;
    //private boolean shortTermTouchDetected = false;
	//private boolean shortTermMotionDetected = false;
    //private boolean shortTermSoundDetected = false;
    //private boolean shortTermCO2Detected = false;
    
    // Biolume sensor countdown timers.
    private int[] sensor_countdowns = new int[Variable.NUM_SENSORS];
    
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
		
		// pay the energy cost of execution
		this.add_energy(-1.0 * Instruction.ENERGY_DECAY); 
		
		// check for conditional skip
		if (this.skipNext) {
			incPC();
			this.skipNext = false;
		}
		
		// execute the current instruction
		Instruction.values()[this.genome[this.pc]].execute(this);
		
		// generate sound if necessary
		if (this.speaker_on) this.generate_sound();
		
		// update sensors
		for (int i = 0; i < Variable.NUM_SENSORS; i++)
			if (this.sensor_countdowns[i] != 0) this.sensor_countdowns[i]--;
		
		this.age++;	// advance this Biolume's age
		incPC(); // increment the program counter	
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
			
			this.sim.image(Simulator.repro, this.address.x  * Simulator.X_DISP, this.address.y * Simulator.Y_DISP, Simulator.WIDTH, Simulator.HEIGHT);
			this.sim.fill(0,0,Variable.MAX_VAR);
			if (this.is_child) id = "c"; else id = "p";
			this.sim.text(Integer.toString(this.repro_cnt) + "(" + id + ")", this.address.x * Simulator.X_DISP + 15, this.address.y * Simulator.Y_DISP + 30);
		}
		else {
			// set LED0 to specified color
			this.sim.fill(this.display_current[0], this.display_current[1], this.display_current[2]);
			this.sim.rect(this.address.x  * Simulator.X_DISP, this.address.y * Simulator.Y_DISP, Simulator.WIDTH/2, Simulator.HEIGHT);
			// set LED1 to specific color
			this.sim.fill(this.display_current[3], this.display_current[4], this.display_current[5]);
			this.sim.rect(this.address.x  * Simulator.X_DISP+Simulator.WIDTH/2, this.address.y * Simulator.Y_DISP, Simulator.WIDTH/2, Simulator.HEIGHT);
			// set speaker status
			if (speaker_on) this.sim.image(Simulator.sound, this.address.x * Simulator.X_DISP + Simulator.WIDTH/4, this.address.y * Simulator.Y_DISP + Simulator.HEIGHT/8, Simulator.WIDTH/2, Simulator.HEIGHT/2);
			// energy display
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

	public void generate_sound() {
		this.sim.detector(this.address.x, this.address.y, Instruction.SOUND_RANGE, Instruction.SOUND);
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
    	this.sim.reproduce_biolume(this.address, this.genome, this.exe_size, Simulator.LOWEST_ENERGY_NEIGHBOR, Simulator.SPLIT_ENERGY);
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
		this.makeViable();
		
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
	    this.makeViable();
	    
	    // initialize to random variables
	    for (int i = EXE_SIZE_MAX; i < GENOME_SIZE_MAX; i++) {
	        this.genome[i] = RAND.nextInt(Variable.MAX_VAR);
	    }
	    
	    // initialize state information
	    this.pc = 0;
	    this.label = 0;
	    this.age = 0;
	    this.energy = 0;
	    this.repro_cnt = 0;
	    this.is_child = true;
	    this.skipNext = false;
	    this.speaker_on = false;
	    
	    for (int i = 0; i < Variable.NUM_VARS; i++) {
	    	this.message_buffer[i] = 0;
	    	this.display_current[i] = 0;
	    	this.display_previous[i] = 0;
	    	this.display_buffer[i] = 0;
	    }
	    for (int i = 0; i < Variable.NUM_SENSORS; i++)
	    	this.sensor_countdowns[i] = 0;
	}
	
	/**
	 * replace this Biolume's genome
	 * @param new_genome: replacement genome
	 */
	private void replace_genome(int[] new_genome) {
		
		// replace this genome
		for (int i = 0; i < GENOME_SIZE_MAX; i++) 
			this.genome[i] = new_genome[i];
	}
	
	/** Check if the genome has at least one instance of the REPRO istruction.
	 * If there is no REPRO instruction already present, insert one. 
	 * If there is space in the genome, perform an insertion mutation.  
	 * Otherwise, perform a point mutation. 
	 */
	private void makeViable() {
		
		// Check if the Biolume is already viable. If so, escape. 
		for (int i = 0; i < this.exe_size; i++)
			if (Instruction.values()[this.genome[i]] == Instruction.REPRODUCE) return;
		
		// We're still here, so the Biolume is not viable. 
		int insertIndex = RAND.nextInt(this.exe_size);
		
		if (this.exe_size < EXE_SIZE_MAX) { // perform an insertion mutation
			this.exe_size++;
			
			// insert
			int swap1 = this.genome[insertIndex];
			this.genome[insertIndex] = Instruction.REPRODUCE.ordinal();
			
			// shift instructions down
			for (int i = insertIndex+1; i < this.exe_size; i++) {
				int swap2 = this.genome[i];
				this.genome[i] = swap1;
				swap1 = swap2;
			}
		}
		else this.genome[insertIndex] = Instruction.REPRODUCE.ordinal(); // perform a point mutation
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
	
	public boolean getSensorDetected(int sensor_type, int term) {
		if(term == Instruction.SHORT) return (this.sensor_countdowns[sensor_type] == Instruction.SENSOR_COUNTDOWN_LENGTH);
		else if (term == Instruction.LONG) return (this.sensor_countdowns[sensor_type] != 0);
		else {
			System.out.println("Invalid timer term!");
			return false;
		}
	}
	
	public void setSensorDetected(int sensor_type) {
		if (sensor_type == Instruction.TOUCH && !this.getSensorDetected(Instruction.TOUCH, Instruction.SHORT)) {
			this.add_energy(Instruction.TOUCH_ENERGY);
			this.sensor_countdowns[Instruction.TOUCH] = Instruction.SENSOR_COUNTDOWN_LENGTH;
		}
		else if (sensor_type == Instruction.MOTION && !this.getSensorDetected(Instruction.MOTION, Instruction.SHORT)) {
			this.add_energy(Instruction.MOTION_ENERGY);
			this.sensor_countdowns[Instruction.MOTION] = Instruction.SENSOR_COUNTDOWN_LENGTH;
		}
		else if (sensor_type == Instruction.SOUND) {
			if (this.sensor_countdowns[Instruction.SOUND] < Instruction.SENSOR_COUNTDOWN_LENGTH - Instruction.NEW_SOUND_DELAY) 
					this.add_energy(Instruction.NEW_SOUND_ENERGY);
			else this.add_energy(Instruction.NORMAL_SOUND_ENERGY); // multiple sounds make more "loudness"
			this.sensor_countdowns[Instruction.SOUND] = Instruction.SENSOR_COUNTDOWN_LENGTH;
		}
		else if (sensor_type == Instruction.CO2 && !this.getSensorDetected(Instruction.CO2, Instruction.SHORT)) {
			this.add_energy(Instruction.CO2_ENERGY);
			this.sensor_countdowns[Instruction.CO2] = Instruction.SENSOR_COUNTDOWN_LENGTH;
		}
	}
	
	public void skipNext() {
		skipNext = true;
	}
	
	public void speaker_on(boolean speaker_state) {
		this.speaker_on = speaker_state;
	}
	
	public void incPC() {
		this.pc = (this.pc + 1) % this.exe_size;
	}
	
	public void set_label() {
		this.label = this.pc;
	}
	
	// all jumps are treated as jumps forward
	public void jump_to_label() {
		this.pc = this.label; 
	}
	
	public void printInst() { // for testing purposes only! @DJ
		System.out.println("current inst: " + Instruction.values()[this.genome[this.pc]]);
	}
}