/**
 * CC.java (aka ConfigurationsConstants.java)
 * @authors: Daniel J. Couvertier
 * @date: August 12, 2011
 * @description: This file contains all user-defined constant values.
 * - For now, only developers will be tampering with configurations, so this
 * file should suffice. In the future, should the purpose of the simulator
 * shift to outreach or demonstrations in which non-developers would be 
 * altering the parameters, an external configuration file may be created and 
 * used instead. 
 * - Constants which should/can not be altered by the user are contained 
 * within more appropriate files.
 * - The 'Enumerations' sections are placed here simply as a point of 
 * reference. Their values have no specific significance. Feel free to add 
 * options if necessary.  
 */
public class CC {
	/**************************************************************************
	 **************************************************************************
	 ********************************* GENOME *********************************
	 **************************************************************************
	 *************************************************************************/
	// DISPLAY STATES
	public static final int OFF = 0;
	public static final int ON = 1;
	/************************** ^ ENUMERATIONS ^ *****************************/
	// Executable genome size limits.
	public static final int EXE_SIZE_MIN = 5;
	public static final int EXE_SIZE_INIT = 10;
	public static final int EXE_SIZE_MAX = 20;
	// Variable value range = [0, MAX_VAR).
	public static final int MAX_VAR = 255;
	// Max possible Euclidean distance, given MAX_VAR, for color differences.
	public static final float MAX_DIST = 
			(float) java.lang.Math.sqrt(3 * java.lang.Math.pow(MAX_VAR, 2));

	/**************************************************************************
	 **************************************************************************
	 ****************************** REPRODUCTION ******************************
	 **************************************************************************
	 *************************************************************************/
	// REPLACEMENT TARGET: Who will be replaced when a parent is to reproduce?
	public static final int NGHBR_OLDEST = 0;
	public static final int NGHBR_LOWEST_ENERGY = 1;
	// REPLICATION METHOD: How will the parent treat its new offspring? 
	public static final int ON_YOUR_OWN = 0;
	public static final int SPLIT_ENERGY = 1;
	/************************** ^ ENUMERATIONS ^ *****************************/
	// Probabilities of executing a successful reproduction process.  
	public static final double REPRO_PROB_MIN = 0.01;
	public static final double REPRO_PROB_MAX = 0.10; 
	// Replacement and replication methods. Options available in enumerations.
	public static final int REPLACEMENT_METHOD = NGHBR_LOWEST_ENERGY;
	public static final int REPLICATION_METHOD = SPLIT_ENERGY;
	// Mutation Rates (http://www.krl.caltech.edu/avida/manual/mutation.html)
	public static final double MUT_PROB_COPY = (1.0 / EXE_SIZE_INIT);
	public static final double MUT_PROB_INSERT = (MUT_PROB_COPY / 2.2);
	public static final double MUT_PROB_DELETE = (MUT_PROB_COPY / 2.2);
	// Steps to skip while reproducing (for visualization purposes only).
	public static final int REPRO_COUNTDOWN = 3;
	
	/**************************************************************************
	 **************************************************************************
	 *************************** SENSOR MANAGEMENT ****************************
	 **************************************************************************
	 *************************************************************************/
	// SENSOR TYPES
	public static final int TOUCH = 0;
	public static final int MOTION = 1;
	public static final int SOUND = 2;
	public static final int CO2 = 3;
	// SENSOR DETECTION TIME TERMS
	// SHORT = this time step; LONG = within last LONG_TERM_DURATION steps.
	public static final int SHORT = 0;
	public static final int LONG = 1;
	/************************** ^ ENUMERATIONS ^ *****************************/
	// Total amount of sensor types. 
	public static final int NUM_SENSORS = 4;
	// Range of Biolumes affected by an activity in hops.
	// 0 = only the affected organism.
	// 1 = the affected organism and it's 1-hop neighbors.
	// etc...
	public static final int TOUCH_RANGE = 0;
	public static final int MOTION_RANGE = 0;
	public static final int SOUND_RANGE = 1;
	public static final int CO2_RANGE = 2;
	// Amount of silenced steps before a sound is considered new. 
	public static final int NEW_SOUND_DELAY = 1; 
	// Amount of steps before a Biolume 'forgets' it detected an activity.
	public static final int LONG_TERM_DURATION = EXE_SIZE_MAX;
	
	/**************************************************************************
	 **************************************************************************
	 *************************** ENERGY MANAGEMENT ****************************
	 **************************************************************************
	 *************************************************************************/
	// Specify a maximum energy that each Biolume can reach. Then, specify the 
	// proportion of that maximum that is awarded for each sensed activity. 
	public static final double ENERGY_MAX = 100.0;
	public static final double CO2_ENERGY = ENERGY_MAX * 0.01;
	public static final double NORMAL_SOUND_ENERGY = ENERGY_MAX * 0.0; 
	public static final double NEW_SOUND_ENERGY = ENERGY_MAX * 0.04;
	public static final double MOTION_ENERGY = ENERGY_MAX * 0.10; 
	public static final double TOUCH_ENERGY = ENERGY_MAX * 0.50;
	public static final double ENERGY_DECAY = ENERGY_MAX * 0.01;
		
	/**************************************************************************
	 **************************************************************************
	 ********************************** GUI ***********************************
	 **************************************************************************
	 *************************************************************************/
	// Dimensions of the population.
	public static final int X_SIZE = 15; 
	public static final int Y_SIZE = 10;
	// Display elements for a Biolume.
	public static final int B_WIDTH = 50;
	public static final int B_HEIGHT = 50;
	public static final int GUTTER = 10;
	// Avatar dimensions.
	public static final int AVATAR_SIZE = 32;
	// Color picker dimensions.
	public static final int PICKER_SIZE = 200;
	// Info box close button dimensions.
	public static final int CLOSE_SIZE = 15;
	// Control buttons are: Play/Pause, Faster, Slower, Restart, Deenergize.
	// Control button dimensions (must not be greater than B_HEIGHT). 
	public static final int CONTROL_SIZE = B_WIDTH;
	// How often should the Biolumes step.
	public static final int DEFAULT_STEP_DELAY_MS = 501; 
	// Make sure that (DEFAULT-1) % CHANGE = 0
	public static final int STEP_DELAY_CHANGE = 100;
}
