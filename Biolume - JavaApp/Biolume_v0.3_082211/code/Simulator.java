/**
 * Simulator.java
 * @authors: Tony J. Clark, Daniel J. Couvertier, Adam Ford
 * @date: August 1, 2011
 * @description: This file describes the Biolume Simulator which is in charge
 * of handling Biolume execution, human interaction, and the interface between
 * them.
 */

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import processing.core.*;

public class Simulator extends PApplet {

    /**************************************************************************
     * Control area parameters.
     */
	
	private static final long serialVersionUID = 1L;
	
	// Summary display elements for Biolume. 
	public static final int X_DISP = CC.B_WIDTH + CC.GUTTER;
	public static final int Y_DISP = CC.B_HEIGHT + CC.GUTTER;
	// Summary display elements for Biolume arena.
	public static final int ARENA_SIZE_X = CC.X_SIZE * X_DISP;
	public static final int ARENA_SIZE_Y = CC.Y_SIZE * Y_DISP;
	
	// Display elements for control buttons. 
	public static final int PLAY_PAUSE_X = ARENA_SIZE_X;
	public static final int PLAY_PAUSE_Y = 0;
	public static final int SLOWER_X = ARENA_SIZE_X + CC.PICKER_SIZE/2 - CC.CONTROL_SIZE/2;
	public static final int SLOWER_Y = PLAY_PAUSE_Y;
	public static final int FASTER_X = ARENA_SIZE_X + CC.PICKER_SIZE - CC.CONTROL_SIZE;
	public static final int FASTER_Y = PLAY_PAUSE_Y;
	public static final int RESTART_X = ARENA_SIZE_X + CC.PICKER_SIZE - CC.CONTROL_SIZE;
	// restart_y depends on the final size, which we don't know yet.
	public static final int DEENERGIZE_X = ARENA_SIZE_X + CC.PICKER_SIZE - CC.CONTROL_SIZE;
	// deenergize_y depends on restart_y, which we don't know yet. 
	
	//Display elements for color picker.
	public static final int PICKER_X = ARENA_SIZE_X;
	public static final int PICKER_Y = PLAY_PAUSE_Y + CC.CONTROL_SIZE + CC.GUTTER;
	
	// Display elements for global pressure targets.
	public static final int TARGET0_X = ARENA_SIZE_X + CC.PICKER_SIZE/2 - CC.B_WIDTH/2; 
	public static final int TARGET0_Y = PLAY_PAUSE_Y + CC.CONTROL_SIZE + CC.GUTTER + CC.PICKER_SIZE + CC.GUTTER; 
	public static final int TARGET0_LIGHT_X = TARGET0_X - CC.AVATAR_SIZE;
	public static final int TARGET0_LIGHT_Y = TARGET0_Y + CC.B_HEIGHT/2 - CC.AVATAR_SIZE/2;
	public static final int TARGET1_X = TARGET0_X + CC.B_WIDTH/2;
	public static final int TARGET1_Y = TARGET0_Y; 
	public static final int TARGET1_LIGHT_X = TARGET1_X + CC.B_WIDTH/2;
	public static final int TARGET1_LIGHT_Y = TARGET0_LIGHT_Y;
	
	// Display elements for info box.
	public static final int INFO_X = ARENA_SIZE_X;
	public static final int INFO_Y = PLAY_PAUSE_Y + CC.CONTROL_SIZE + CC.GUTTER;
	
    /**************************************************************************
     * Simulator parameters.
     */
	
	// Thread that steps Biolumes at a particular rate.
	private StepBiolumesThread step_thread;
	
	// The population of Biolumes. 
	private Biolume[][] population;
	
	// Biolume images.
	public static PImage repro;
	public static PImage sound;
	public static PImage battery0;
	public static PImage battery1;
	public static PImage battery2;
	public static PImage battery3;
	public static PImage battery4;
	
	// Control images. 
	private static PImage play;
	private static PImage pause;
	private static PImage faster;
	private static PImage slower;
	private static PImage restart;
	private static PImage deenergize;
	private static PImage pressure_on;
	private static PImage pressure_off;
	private static PImage close;
	private static PImage visitor;
	private static PImage zoom;
	
	// Global pressures management parameters and flags.
	private ColorPicker cp;
	private float[] led0_target = new float[3];
	private float[] led1_target = new float[3];
	private boolean LED0_pressure_on = false;
	private boolean LED1_pressure_on = false;
	private boolean led0_selected = false;
	private boolean led1_selected = false;
	private boolean cp_selected = false;
	private boolean cursor_in_arena = true;
	private boolean info_box_on = false;
	
	// Starting speed of the simulation.
	private int step_delay_ms = CC.DEFAULT_STEP_DELAY_MS;
	
	// Final size-related calculations that need to be settled here. 
	private int min_height = TARGET0_Y + CC.B_HEIGHT + CC.CONTROL_SIZE*2 + CC.GUTTER*3;
	private int restart_y, deenergize_y;

    /**************************************************************************
     * Display management methods.
     */
	
	/**
	 * This is the PApplet setup method as called by Processing. Here, the
	 * population is initialized; images and the color picker are imported; and
	 * the initial layout is prepared for drawing. The image for a visitor will
	 * be selected randomly at startup. 
	 */
	public void setup() {
	    
		// Determine the height by the largest requirement.
		if (ARENA_SIZE_Y > min_height) min_height = ARENA_SIZE_Y;
		restart_y = min_height - CC.GUTTER - CC.CONTROL_SIZE;
		deenergize_y = min_height - CC.GUTTER*2 - CC.CONTROL_SIZE*2;
		
		// Set the window display size.
		size(ARENA_SIZE_X + CC.PICKER_SIZE + CC.GUTTER, min_height);
		
		// Set default font.	
		PFont font = createFont("Serif", 15);
		textFont(font, 15);
		
		// Create ColorPicker (changes color mode to RGB, so change it after).
		cp = new ColorPicker( PICKER_X, PICKER_Y, CC.PICKER_SIZE, CC.PICKER_SIZE, 255, this);  
	
		// Set color mode to Hue/Saturation/Brightness (after ColorPicker).
		colorMode(HSB, CC.MAX_VAR);
		
		// Set MSU green and white for the global pressure targets.
		led0_target[0] = CC.MAX_VAR*23/50; led0_target[1] = CC.MAX_VAR*13/20; led0_target[2] = CC.MAX_VAR*27/100;
		led1_target[0] = 0; led1_target[1] = 0; led1_target[2] = CC.MAX_VAR;
		
		// Add Biolume images.
		Simulator.sound = loadImage("../media/sound.png"); Simulator.sound.resize( CC.B_WIDTH/2, CC.B_HEIGHT/2);
		Simulator.repro = loadImage("../media/repro.png"); Simulator.repro.resize(CC.B_WIDTH, CC.B_HEIGHT);
		Simulator.battery0 = loadImage("../media/batteries/battery0.png"); Simulator.battery0.resize(CC.B_WIDTH, CC.B_HEIGHT *9/16);
		Simulator.battery1 = loadImage("../media/batteries/battery1.png"); Simulator.battery1.resize(CC.B_WIDTH, CC.B_HEIGHT *9/16);
		Simulator.battery2 = loadImage("../media/batteries/battery2.png"); Simulator.battery2.resize(CC.B_WIDTH, CC.B_HEIGHT *9/16);
		Simulator.battery3 = loadImage("../media/batteries/battery3.png"); Simulator.battery3.resize(CC.B_WIDTH, CC.B_HEIGHT *9/16);
		Simulator.battery4 = loadImage("../media/batteries/battery4.png"); Simulator.battery4.resize(CC.B_WIDTH, CC.B_HEIGHT *9/16);
		
		// Add control images.
		Simulator.play = loadImage("../media/control/play.png"); Simulator.play.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.pause = loadImage("../media/control/pause.png"); Simulator.pause.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.faster = loadImage("../media/control/faster.png"); Simulator.faster.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.slower = loadImage("../media/control/slower.png"); Simulator.slower.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.restart = loadImage("../media/control/restart.png"); Simulator.restart.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.deenergize = loadImage("../media/control/deenergize.png"); Simulator.deenergize.resize(CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		Simulator.pressure_on = loadImage("../media/on.png"); Simulator.pressure_on.resize(CC.AVATAR_SIZE, CC.AVATAR_SIZE);
		Simulator.pressure_off = loadImage("../media/off.png"); Simulator.pressure_off.resize(CC.AVATAR_SIZE, CC.AVATAR_SIZE);
		Simulator.close = loadImage("../media/close.png");
		Simulator.zoom = loadImage("../media/zoom.png"); Simulator.zoom.resize(CC.AVATAR_SIZE, CC.AVATAR_SIZE);
		
		// Select a random visitor image.
		Random true_rand_gen = new Random();
		int visitor_id = true_rand_gen.nextInt(10);
		if (visitor_id == 8 || visitor_id == 9) visitor_id = true_rand_gen.nextInt(10); // make fun icons rare
		String visitor = "../media/visitors/visitor" + visitor_id + ".png";
		Simulator.visitor = loadImage(visitor); Simulator.visitor.resize(CC.AVATAR_SIZE, CC.AVATAR_SIZE);
	
		// Set PRNG seed.
		Biolume.RAND.setSeed(0);
		
		this.initializePopulation();
		
		// Set up the thread that steps the Biolumes.
		step_thread = new StepBiolumesThread(CC.DEFAULT_STEP_DELAY_MS, Integer.MAX_VALUE, this);
		step_thread.start();
	
	}

	/**
	 * This method is the PApplet draw method as called by Processing. Its job
	 * is to constantly redraw changed elements in the simulator. If the info
	 * box is not being displayed, then the background, Biolume population, 
	 * control buttons, color picker, LED0 and LED1 pressure buttons, and the
	 * step count are drawn. If the info box is being displayed, there is no 
	 * need to redraw anything because the system is paused. 
	 */
	public void draw() {
		
		// Always reset the text size, as other methods may alter it.
		textSize(15);
		
		if (!info_box_on) {
			
			// Draw the background.
			background(100);
			
			// Draw the Biolumes.
			this.drawPopulation();
			
			// Draw the control buttons.
			if (this.step_thread.isPaused()) image(Simulator.play, PLAY_PAUSE_X, PLAY_PAUSE_Y, CC.CONTROL_SIZE, CC.CONTROL_SIZE);
			else image(Simulator.pause, PLAY_PAUSE_X, PLAY_PAUSE_Y, CC.CONTROL_SIZE, CC.CONTROL_SIZE);
			image(Simulator.slower, SLOWER_X, SLOWER_Y, CC.CONTROL_SIZE,CC .CONTROL_SIZE);
			image(Simulator.faster, FASTER_X, FASTER_Y, CC.CONTROL_SIZE, CC.CONTROL_SIZE);
			image(Simulator.restart, RESTART_X, restart_y, CC.CONTROL_SIZE, CC.CONTROL_SIZE);
			image(Simulator.deenergize, DEENERGIZE_X, deenergize_y, CC.CONTROL_SIZE, CC.CONTROL_SIZE);
		
			// Draw the Color Picker.
			cp.render();
			
			// Draw the LED0 pressure buttons.
			if (led0_selected) {
				fill(0,0,CC.MAX_VAR);
				rect(TARGET0_X - CC.B_WIDTH/16, TARGET0_Y - CC.B_HEIGHT/16, CC.B_WIDTH/2 + CC.B_WIDTH/16, CC.B_HEIGHT + CC.B_HEIGHT/8);
				if (cp_selected) led0_target = get_HSB(cp.getColor(), CC.MAX_VAR);
			}
			fill(led0_target[0], led0_target[1], led0_target[2]);
			rect(TARGET0_X, TARGET0_Y, CC.B_WIDTH/2, CC.B_HEIGHT);
			if(LED0_pressure_on) image(Simulator.pressure_on, TARGET0_LIGHT_X, TARGET0_LIGHT_Y);
			else image(Simulator.pressure_off, TARGET0_LIGHT_X, TARGET0_LIGHT_Y);
		
			// Draw the LED1 pressure buttons.
			if (led1_selected) {
				fill(0,0,CC.MAX_VAR);
				rect(TARGET1_X, TARGET0_Y - CC.B_HEIGHT/16, CC.B_WIDTH/2 + CC.B_WIDTH/16, CC.B_HEIGHT + CC.B_HEIGHT/8);
				if (cp_selected) led1_target = get_HSB(cp.getColor(), CC.MAX_VAR);
			}
			fill(led1_target[0], led1_target[1], led1_target[2]);
			rect(TARGET1_X, TARGET1_Y, CC.B_WIDTH/2, CC.B_HEIGHT);
			if(LED1_pressure_on) image(Simulator.pressure_on, TARGET1_LIGHT_X, TARGET1_LIGHT_Y);
			else image(Simulator.pressure_off, TARGET1_LIGHT_X, TARGET1_LIGHT_Y);
			
			// Draw the step count.
			fill(0, 0, CC.MAX_VAR);
			text("Step: " + this.step_thread.getSteps(), (float)ARENA_SIZE_X, 
					(float)(min_height - CC.GUTTER - 15)); // 15 is textsize.
		}
	}
	
	/**
	 * Updates the display of each Biolume including LED colors, whether its
	 * speaker is on, and its current energy level. 
	 */
	public void drawPopulation() {

		for (int i = 0; i < CC.X_SIZE; i++) 
			for (int j = 0; j < CC.Y_SIZE; j++) 
				this.population[i][j].display();
	}
	
	/**
	 * This method draws a box containing relevant information. The box may
	 * only appear if the system is paused because it covers most of the
	 * control buttons. 
	 * Currently, this box is only used to show Biolume information.
	 * @param text:	The text to be displayed in the information box.
	 */
	public void drawInfoBox(String text) {
		
		// Draw the actual info box.
		fill(0,0,CC.MAX_VAR);
		rect(INFO_X, INFO_Y, this.getSize().width - INFO_X - CC.GUTTER, this.getSize().height - INFO_Y - CC.GUTTER);
		textSize(12);
		
		// Draw the info box's close button.
		fill(0,0,0);
		text(text, INFO_X + 5, INFO_Y); 
		image(Simulator.close, this.getSize().width - CC.GUTTER - CC.CLOSE_SIZE, INFO_Y, CC.CLOSE_SIZE, CC.CLOSE_SIZE);
		
		info_box_on = true;
	}
	
	/**
	 * This is a helper method for translating RGB colors to HSB. 
	 * @param color:	The RGB color that is to be translated.
	 * @param scale:	The scale that is to be used with the HSB color mode.
	 * @return:	The HSB value of the input color.
	 */
	private float[] get_HSB(Color color, int scale) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hsb[0] *= scale;
		hsb[1] *= scale;
		hsb[2] *= scale;
		return hsb;
	}
	
    /**************************************************************************
     * Biolume management methods.
     */	
	
	/**
	 * This method instantly clears out all existing Biolumes (if any existed
	 * in the first place) and replaces them with a newly generated population.
	 *  The genomes of the new Biolumes will have a length of CC.EXE_SIZE_MIN
	 *  and their genomes will be composed of randomly selected instructions. 
	 */
	public void initializePopulation() {
		this.population = new Biolume[CC.X_SIZE][CC.Y_SIZE];
		for (int i = 0; i < CC.X_SIZE; i++) 
			for (int j = 0; j < CC.Y_SIZE; j++) 
				this.population[i][j] = new Biolume(this, i, j);
	}
	
	/**
	 * This method steps every Biolume through its next instruction. The order
	 * in which the Biolumes execute is randomly generated at every step. After
	 * executing: if global pressures are on, the Biolumes are evaluated and 
	 * awarded energy by how close they are to the target display; otherwise, 
	 * interactive pressures rule, and the Biolumes are rewarded for sensing 
	 * CO2 generated by a stationary visitor, and sound generated by other
	 * Biolumes. 
	 */
	public void stepPopulation() {
		
	    // Step each Biolume through the next instruction in random order.
		ArrayList<Point> to_go = new ArrayList<Point>();
	    for (int i = 0; i < CC.X_SIZE; i++) 
	    	for (int j = 0; j < CC.Y_SIZE; j++) 
	    		to_go.add(new Point(i,j));
	    while (to_go.size() > 0) {
	    	int next = Biolume.RAND.nextInt(to_go.size());
			population[to_go.get(next).x][to_go.get(next).y].step();
			to_go.remove(next);
	    }
	    
	    // Apply global pressures if they are on.
	    if(this.LED0_pressure_on || LED1_pressure_on) 
	    	if(this.LED0_pressure_on && LED1_pressure_on) globalColorPressure(led0_target, led1_target, 2);
	    	else if (this.LED0_pressure_on) globalColorPressure(led0_target, null, 1);
	    	else globalColorPressure(null, led1_target, 1);
	    else { // No global pressures, apply interactive features (sound, CO2).
	    	
	    	// Sound check. 
	    	for (int i = 0; i < CC.X_SIZE; i++)
	    		for (int j = 0; j < CC.Y_SIZE; j++)
	    			if (population[i][j].speakerIsOn() && !population[i][j].isReproducing())
	    				this.sensor(i, j, CC.SOUND_RANGE, CC.SOUND);
	    	
			// Ensure CO2 is awarded to Biolumes around stationary visitor.
			int x_coor = mouseX/X_DISP;
			int y_coor = mouseY/Y_DISP;
			if (x_coor < CC.X_SIZE && y_coor < CC.Y_SIZE) 				
				this.sensor(x_coor, y_coor, CC.CO2_RANGE, CC.CO2);
	    }
	    	
	}
	
	/**
	 * This method places a sent message into the message buffers of the
	 * Biolumes that are neighboring the point of origin. A message contains
	 * a set of Biolume display values including the HSB values for 2 LEDs,
	 * the state of each LED, an index for a MIDI file to be played by the 
	 * speaker, and the state of the speaker.  
	 * @param origin:	The point from which the broadcast originated.
	 * @param msg:	The message that was broadcast.
	 */
	public void localBroadcast(Point origin, int[] msg) {

		Point neighbor = new Point();
		// send message to neighbors
		for (int i = origin.x - 1; i <= origin.x + 1; i++) {
			
			// check for out of array bounds (toroidal grid)
			if (i < 0) neighbor.x = i + CC.X_SIZE;
			else if (i >= CC.X_SIZE) neighbor.x = i - CC.X_SIZE;
			else neighbor.x = i;
			
			for (int j = origin.y - 1; j <= origin.y + 1; j++ ) {
				
				// check for out of array bounds (toroidal grid)
				if (j < 0) neighbor.y = j + CC.Y_SIZE;
				else if (j >= CC.Y_SIZE) neighbor.y = j - CC.Y_SIZE;
				else neighbor.y = j;
				
				// don't send message back to sender
				if (neighbor.x == origin.x && neighbor.y == origin.y) continue;
				
				// send message
				this.population[neighbor.x][neighbor.y].setMessage(msg);
			}
		}
	}	
	
	/**
	 * This method reproduces a parent Biolume over its CC.REPLACEMENT_TARGET
	 * neighbor. If the criteria for the replacement target are met by multiple
	 * neighbors, one of them is randomly chosen. If all neighbors are in the
	 * reproduction process, a Biolume will not be allowed to reproduce. The
	 * amount of energy transfered to the offpsring is determined by 
	 * CC.REPLACEMENT_METHOD.
	 * @param parent:	The parent Biolume location.
	 * @param p_genome:	The parent Biolume's genome.
	 * @param p_exe_size: The parent Biolume's executable genome size. 
	 * @param p_odd_step: Checksum to sync parent and offspring repro countdown.
	 */
	public void reproduceBiolume(Point parent, int[] p_genome, int p_exe_size, boolean p_odd_step) {
		
		// temporary variables used to find the correct neighbor
		Point current = new Point();
		ArrayList<Point> potential_targets = new ArrayList<Point>();
		double current_criterion;
		double replacement_criterion;
		
		if (CC.REPLACEMENT_TARGET == CC.NGHBR_OLDEST) {
			// replace the oldest neighbor
			replacement_criterion = -1.0;
		}
		else if (CC.REPLACEMENT_TARGET == CC.NGHBR_LOWEST_ENERGY) {
			// replace the neighbor with the lowest energy level
			replacement_criterion = CC.ENERGY_MAX;
		}
		
		// Find the neighbor to replace (randomly break ties).
		for (int i = parent.x - 1; i <= parent.x + 1; i++) {
			
			// Check for out of array bounds (toroidal grid).
			if (i < 0) current.x = i + CC.X_SIZE;
			else if (i >= CC.X_SIZE) current.x = i - CC.X_SIZE;
			else current.x = i;
			for (int j = parent.y - 1; j <= parent.y + 1; j++ ) {
				
				// Check for out of array bounds (toroidal grid).
				if (j < 0) current.y = j + CC.Y_SIZE;
				else if (j >= CC.Y_SIZE) current.y = j - CC.Y_SIZE;
				else current.y = j;
				
				// Don't replace the parent.
				if (current.x == parent.x && current.y == parent.y) continue;
				
				// Don't replace something already involved in reproduction.
				if (population[current.x][current.y].isReproducing()) continue;
				
				// Update the list of potential replacement targets.
				if (CC.REPLACEMENT_TARGET == CC.NGHBR_OLDEST) {
					current_criterion = population[current.x][current.y].getAge();
					if (current_criterion >= replacement_criterion) {
						
						// If this is the oldest, remove the rest. 
						if (current_criterion > replacement_criterion) 
							potential_targets.clear();
						
						// Add the new oldest target to the list.
						potential_targets.add((Point) current.clone());
						
						// Update the oldest age found.
						replacement_criterion = current_criterion;
					}
				}
				else if (CC.REPLACEMENT_TARGET == CC.NGHBR_LOWEST_ENERGY) {
					current_criterion = population[current.x][current.y].getEnergy();
					if (current_criterion <= replacement_criterion) {
						
						// If this has the lowest energy, remove the rest.
						if (current_criterion < replacement_criterion) 
							potential_targets.clear();
						
						// Add the new lowest energy target to the list.
						potential_targets.add((Point) current.clone());
						
						// Update the lowest energy found.
						replacement_criterion = current_criterion;
					}
				}
				// else // no legal replacement strategy selected.
			}
		}
		
		// No targets (all neighbors are in repro)? Can't repro.
		if (potential_targets.size() == 0) return;
		
		// Randomly choose a target from the list of potentials.
		Point offspring = potential_targets.get(Biolume.RAND.nextInt(potential_targets.size()));
		
		// Replace the target.
		population[offspring.x][offspring.y].offspringReproduce(p_genome, p_exe_size, p_odd_step);
		
		if (CC.REPLICATION_METHOD == CC.SPLIT_ENERGY) {
			double energy_available = population[parent.x][parent.y].getEnergy();
			population[offspring.x][offspring.y].setEnergy(energy_available/2.0);
			population[parent.x][parent.y].setEnergy(energy_available/2.0);
		}
		// else // CC.REPLICATION_METHOD == CC.ON_YOUR_OWN
	}

	/**
	 * This method announces a sensor-triggering activity to all relevant
	 * Biolumes. 
	 * @param x: The x-coordinate of the Biolume at the center of the activity.
	 * @param y: The y-coordinate of the Biolume at the center of the activity.
	 * @param range: The affected layers of neighbors to the center Biolume.
	 * @param sensor_type:	The type of sensor triggered by the activity.
	 */
	public void sensor(int x, int y, int range, int sensor_type) {
		if (!this.LED0_pressure_on && !LED1_pressure_on) 
			for(int i = x-range; i <= x+range; i++) 
				for(int j = y-range; j <= y+range; j++) 
					if ( (i >= 0) && (i < CC.X_SIZE) && (j >= 0) && (j < CC.Y_SIZE) && !population[i][j].isReproducing())
						population[i][j].setDetection(sensor_type);
	}
		
	/**
	 * This method enforces the global pressures on each Biolume. It calculates
	 * the Eucledian distance between the displayed LED color and the 
	 * corresponding global target. The comparison is made in the HSB color
	 * mode (which requires the Hue distance to be calculated differently due
	 * to its circular nature). The reward assigned to each Biolume will fill
	 * its energy reserve at an inversely proportional rate to the calculated
	 * distance (maximum distance possible = 0% energy reward; minimum
	 * distance possible = 100% energy reward). If both global pressures are
	 * active, they are given equal weight in calculating the reward. If only
	 * one pressure is active, then the distance between the other LED and the
	 * target is ignored. 
	 * @param pressurecolor0:	The HSB values of the global target for LED0.
	 * @param pressurecolor1:	The HSB values of the global target for LED1.
	 * @param pressures:	The amount of global pressures which are active.
	 */
	private void globalColorPressure(float[] pressurecolor0, float[] pressurecolor1, int pressures) {
		for (int i = 0; i < CC.X_SIZE; i++) {
			for (int j = 0; j < CC.Y_SIZE; j++) {
			
				float fitness0, fitness1;
			
				if (this.population[i][j].LED0IsOn() && pressurecolor0 != null) {
				
				    float [] currentcolor0 = this.population[i][j].getLED0Color();
					float dh0, ds0, db0, distance0;
					
					dh0 = pressurecolor0 [0] - currentcolor0 [0];
					ds0 = pressurecolor0 [1] - currentcolor0 [1];
					db0 = pressurecolor0 [2] - currentcolor0 [2];
					
				    // Hue is circular, thus, distances can only be as large as halfway. 
				    // To maintain scale with S and B,we multiply the actual distance by 2.
				    if (abs(dh0) > CC.MAX_VAR/2) dh0 = CC.MAX_VAR - abs(dh0);
				    dh0 *= 2.0;
				    
				    distance0 = sqrt(pow(dh0,2) + pow(ds0,2) + pow(db0,2));
				    fitness0 = CC.MAX_DIST - distance0;
				}
				else fitness0 = 0; // LED is off-- no reward.
		    
				if (this.population[i][j].LED1IsOn() && pressurecolor1 != null) {
				
					float [] currentcolor1 = this.population[i][j].getLED1Color();
					float dh1, ds1, db1, distance1;
					
					dh1 = pressurecolor1 [0] - currentcolor1 [0];
					ds1 = pressurecolor1 [1] - currentcolor1 [1];
					db1 = pressurecolor1 [2] - currentcolor1 [2];
					
					// Hue is circular, thus, distances can only be as large as halfway. 
				    // To maintain scale with S and B,we multiply the actual distance by 2.
				    if (abs(dh1) > CC.MAX_VAR/2) dh1 = CC.MAX_VAR - abs(dh1);
				    dh1 *= 2.0;
				    
				    distance1 = sqrt(pow(dh1,2) + pow(ds1,2) + pow(db1,2));
				    fitness1 = CC.MAX_DIST - distance1;
				}
				else fitness1 = 0; // LED is off-- no reward.
	
				this.population[i][j].setEnergy(fitness0/CC.MAX_DIST * CC.ENERGY_MAX / pressures + fitness1/CC.MAX_DIST * CC.ENERGY_MAX / pressures);
			}
	    }	
	}
	
	/**
	 * This method instantly clears all energies accumulated by all Biolumes.
	 */
	public void clearAllEnergies() { 
		for (int i = 0; i < CC.X_SIZE; i++) 
			for (int j = 0; j < CC.Y_SIZE; j++) 
				this.population[i][j].setEnergy(0.0);
	}
	
    /**************************************************************************
     * Mouse event management methods.
     */
	
	/**
	 * This method handles mouse presses that occur in relevant locations.
	 * These relevant locations are:
	 * ~ PLAY/PAUSE button:	Toggles between running and pausing the system. If
	 * 						the info box was displayed, it is removed.
	 * ~ SPEED-DOWN button:	Slows down the speed by CC.STEP_DELAY_CHANGE.
	 * ~ SPEED-UP button:	Speeds up the speed by CC.STEP_DELAY_CHANGE.
	 * ~ RESTART button:	Re-seeds the population with randomly generated
	 * 						Biolumes. Also pauses the system.
	 * ~ DE-ENERGIZE button:	Resets the energy of every Biolume to 0.
	 * ~ Info Box close button:	Closes the information box.
	 * ~ Global pressure lights:	Toggle the corresponding LED's global
	 * 								pressure status (on/off).
	 * ~ Global target LEDs:	ColorPicker affects only the selected one.
	 * ~ Color picker area:	Sets the target color for the selected LED. 
	 * ~ Click a Biolume (PLAY):	Triggers corresponding sensors on the
	 * 						Biolumes for successfully achieving human
	 * 						interaction. The clicked Biolume senses Touch,
	 * 						Motion, Sound and CO2. Immediate neighbors sense
	 * 						Sound and CO2. Neighbors 2 hops away sense CO2.
	 * ~ Click a Biolume (PAUSE): Brings up the info box with the Biolume's 
	 * 						information, including location, age, energy,
	 * 						triggered sensors and genome. 
	 */
	public void mousePressed() {
		
		// Handle a mouse press over the PLAY/PAUSE button.
		if (mouseX > PLAY_PAUSE_X && mouseX < (PLAY_PAUSE_X + CC.CONTROL_SIZE) && mouseY > PLAY_PAUSE_Y && mouseY < (PLAY_PAUSE_Y + CC.CONTROL_SIZE)) {
			this.step_thread.togglePause();
			if (!this.step_thread.isPaused()) info_box_on = false;
		}
		// Handle the SPEED-DOWN button.
		else if (mouseX > SLOWER_X && mouseX < (SLOWER_X + CC.CONTROL_SIZE) && mouseY > SLOWER_Y && mouseY < (SLOWER_Y + CC.CONTROL_SIZE)) {
			step_delay_ms += CC.STEP_DELAY_CHANGE;
			this.step_thread.changeDelay(step_delay_ms);
		}
		// Handle the SPEED-UP button.
		else if (mouseX > FASTER_X && mouseX < (FASTER_X + CC.CONTROL_SIZE) && mouseY > FASTER_Y && mouseY < (FASTER_Y + CC.CONTROL_SIZE)) {
			if(step_delay_ms > 1) {
				step_delay_ms -= CC.STEP_DELAY_CHANGE;
				this.step_thread.changeDelay(step_delay_ms);
			}
		}	
		// Handle the RESTART button.
		else if (mouseX > RESTART_X && mouseX < (FASTER_X + CC.CONTROL_SIZE) && mouseY > restart_y && mouseY < (restart_y + CC.CONTROL_SIZE)) {
			if (!this.step_thread.isPaused()) this.step_thread.togglePause();
			this.step_thread.resetSteps();
			this.initializePopulation();
		}
		// Handle the DE-ENERGIZE button.
		else if (mouseX > DEENERGIZE_X && mouseX < (DEENERGIZE_X + CC.CONTROL_SIZE) && mouseY > deenergize_y && mouseY < (deenergize_y + CC.CONTROL_SIZE)) clearAllEnergies();
		// Handle the info box's close button.
		else if (mouseX > (this.getSize().width - CC.GUTTER - CC.CLOSE_SIZE) && mouseX < (this.getSize().width - CC.GUTTER) && mouseY > INFO_Y && mouseY < INFO_Y + CC.CLOSE_SIZE && info_box_on) info_box_on = false;
		// Handle global pressures, but only if the info box is NOT active.
		else if (mouseX > X_DISP * CC.X_SIZE && !info_box_on) {
			// Handle the LED0 pressure on/off button (the light).
			if (mouseX > TARGET0_LIGHT_X && mouseX < TARGET0_LIGHT_X + CC.AVATAR_SIZE && mouseY > TARGET0_LIGHT_Y && mouseY < TARGET0_LIGHT_Y + CC.AVATAR_SIZE && !info_box_on)
				if (this.LED0_pressure_on) this.LED0_pressure_on = false;
				else this.LED0_pressure_on = true;
			// Handle the LED0 target color button.
			else if (mouseX > TARGET0_X && mouseX < TARGET0_X + CC.B_WIDTH/2 && mouseY > TARGET0_Y && mouseY < TARGET0_Y + CC.B_HEIGHT) {
				led0_selected = true;
				led1_selected = false;
				cp_selected = false;
			}
			// Handle the LED1 pressure on/off button.
			else if (mouseX > TARGET1_LIGHT_X && mouseX < TARGET1_LIGHT_X + CC.AVATAR_SIZE && mouseY > TARGET1_LIGHT_Y && mouseY < TARGET1_LIGHT_Y + CC.AVATAR_SIZE) 
				if (this.LED1_pressure_on) this.LED1_pressure_on = false;
				else this.LED1_pressure_on = true;
			// Handle the LED1 target color button.
			else if (mouseX > TARGET1_X && mouseX < TARGET1_X + CC.B_WIDTH/2 && mouseY > TARGET1_Y && mouseY < TARGET1_Y + CC.B_HEIGHT) {
				led0_selected = false;
				led1_selected = true;
				cp_selected = false;
			}
			// Handle clicks on the color picker area.
			else if (mouseX > PICKER_X && mouseX < PICKER_X + CC.PICKER_SIZE && mouseY > PICKER_Y && mouseY < PICKER_Y + CC.PICKER_SIZE) cp_selected = true;
		}
		// Handle clicks on the Biolume arena.
		else {
			int x_coor, y_coor; 
			x_coor = mouseX/X_DISP;
			y_coor = mouseY/Y_DISP;
			if (x_coor < CC.X_SIZE && y_coor < CC.Y_SIZE) // Within Biolume arena-- proceed.
				if (mouseX%X_DISP <= CC.B_WIDTH && mouseY%Y_DISP <= CC.B_HEIGHT) // Not in gutter-- proceed.
					if (!this.step_thread.isPaused()) // Not paused-- proceed.
						sensor(x_coor, y_coor, CC.TOUCH_RANGE, CC.TOUCH); // Trigger touch sensor on Biolume.
					else { // We're paused. Show info box with Biolume info.
						String [] instructions = this.population[x_coor][y_coor].getGenome();
						String text = "\n.:::::BIOLUME DETAILS:::::." +
								"\nx,y: " + (x_coor+1) + "," + (y_coor+1) + "\t\t\t\t\tAge: " + 
								this.population[x_coor][y_coor].getAge() + 
								"\t\t\t\t\tEnergy: " + (float)this.population[x_coor][y_coor].getEnergy() +
								"\n\n.:::::SENSOR INFORMATION:::::." +
								"\nTouch: " + this.population[x_coor][y_coor].getDetection(CC.TOUCH, CC.LONG) +
								"\t\t\t\t\tMotion: " + this.population[x_coor][y_coor].getDetection(CC.MOTION, CC.LONG) +
								"\nSound: " + this.population[x_coor][y_coor].getDetection(CC.SOUND, CC.LONG) +
								"\t\t\t\t\tCO2: " + this.population[x_coor][y_coor].getDetection(CC.CO2, CC.LONG) +
								"\n\n.:::::BIOLUME GENOME:::::.";
						for (int i = 0; i < instructions.length; i++) {
							text = text + "\n " + (i+1) + ":\t" + instructions[i];
						}				
						drawInfoBox(text);					
					}
				//else {} // Do nothing! Click's in the gutter!		
			//else {} // Do nothing! Click's outside of the Biolume arena!
		} 
	}
	
	/**
	 * This method handles mouse movements in relevant locations. 
	 * These relevant locations are:
	 * ~ Moving INTO Biolume arena:	Changes the cursor into a visitor.
	 * ~ Moving OUT of arena:	Changes cursor into arrow to facilitate 
	 * 							interaction with the control area. 
	 * ~ Moving OVER Biolumes:	Triggers corresponding sensors on the Biolumes
	 * 		for successfully achieving human interaction. The Biolume over
	 * 		which the mouse was moved senses Motion, Sound and CO2. Immediate
	 * 		neighbors sense Sound and CO2. Neighbors 2 hops away sense CO2.
	 */
	public void mouseMoved() {
		
		int x_coor = mouseX/X_DISP;
		int y_coor = mouseY/Y_DISP;
		
		if (x_coor < CC.X_SIZE && y_coor < CC.Y_SIZE) { //Within Biolume arena-- proceed.
			if (!cursor_in_arena) { //As long as the cursor is in the arena, it should be a "visitor".
				if (this.step_thread.isPaused()) cursor(Simulator.zoom, CC.AVATAR_SIZE/2, CC.AVATAR_SIZE/2);
				else cursor(Simulator.visitor, CC.AVATAR_SIZE/2, CC.AVATAR_SIZE/2);
				cursor_in_arena = true;
			}
			// Handle a pass of the mouse over the Biolume. 
			if (!this.step_thread.isPaused()) // Not paused-- proceed.
			{
				//if (mouseX%X_DISP <= WIDTH && mouseY%Y_DISP <= HEIGHT) // Not in gutter-- proceed.
				sensor(x_coor, y_coor, CC.MOTION_RANGE, CC.MOTION); // Energies only for this Biolume.
				sensor(x_coor, y_coor, CC.SOUND_RANGE, CC.SOUND); // Energies for neighbors.
				sensor(x_coor, y_coor, CC.CO2_RANGE, CC.CO2);  // Energies for 2-hop neighbors.
				//else {} // Do nothing! Click's in the gutter!		
			} 
			// else {} // Do nothing! The system is paused!
		}
		else { // Outside the Biolume arena. No need for a "visitor" here. 
			cursor(ARROW);
			cursor_in_arena = false;
		}
	}	
}