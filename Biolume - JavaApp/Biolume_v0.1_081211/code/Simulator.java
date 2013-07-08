
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import processing.core.*;

public class Simulator extends PApplet {
	
	private static final long serialVersionUID = 1L;
	
	// ------------------------------------------------------------------------
	// Simulator Definitions
	// ------------------------------------------------------------------------
	// display elements for a Biolume
	public static final int WIDTH = 50;
	public static final int HEIGHT = 50;
	public static final int GUTTER = 10;
	public static final int X_DISP = WIDTH + GUTTER;
	public static final int Y_DISP = HEIGHT + GUTTER;
	
	// display elements for Biolume interaction
	public static final int AVATAR_SIZE = 32;
	
	// how often should the Biolumes step
	private static final int DEFAULT_STEP_DELAY_MS = 501; 
	private static final int STEP_DELAY_CHANGE = 100; // DEFAULT % CHANGE = 0
	
	// dimensions of the population
	private static final int X_SIZE = 15; 
	private static final int Y_SIZE = 10;
	
	//display elements for color picker
	public static final int PICKER_X = X_DISP * X_SIZE;
	public static final int PICKER_Y = Y_DISP;
	public static final int PICKER_SIZE = 200;
	
	// display elements for control buttons
	private static final int PLAY_PAUSE_X = X_SIZE * X_DISP;
	private static final int PLAY_PAUSE_Y = 0;
	private static final int CONTROL_SIZE = WIDTH;
	private static final int SLOWER_X = X_DISP * X_SIZE + PICKER_SIZE/2 - CONTROL_SIZE/2;
	private static final int SLOWER_Y = PLAY_PAUSE_Y;
	private static final int FASTER_X = X_DISP * X_SIZE + PICKER_SIZE - CONTROL_SIZE;
	private static final int FASTER_Y = SLOWER_Y;
	private static final int RESTART_X = X_DISP * X_SIZE + PICKER_SIZE - CONTROL_SIZE;
	private static final int RESTART_Y = Y_DISP * Y_SIZE - GUTTER - CONTROL_SIZE;
	private static final int DEENERGIZE_X = X_SIZE * X_DISP;
	private static final int DEENERGIZE_Y = Y_DISP * Y_SIZE - GUTTER - CONTROL_SIZE;
	
	// display elements for global pressure targets
	private static final int TARGET0_X = X_DISP * X_SIZE + PICKER_SIZE/2 - WIDTH/2; 
	private static final int TARGET0_Y = PLAY_PAUSE_Y + CONTROL_SIZE + GUTTER + PICKER_SIZE + GUTTER; 
	private static final int TARGET0_LIGHT_X = TARGET0_X - AVATAR_SIZE;
	private static final int TARGET0_LIGHT_Y = TARGET0_Y + HEIGHT/2 - AVATAR_SIZE/2;
	private static final int TARGET1_X = TARGET0_X + WIDTH/2;
	private static final int TARGET1_Y = TARGET0_Y; 
	private static final int TARGET1_LIGHT_X = TARGET1_X + WIDTH/2;
	private static final int TARGET1_LIGHT_Y = TARGET0_LIGHT_Y;
	
	// info box
	public static final int INFO_X = X_DISP * X_SIZE;
	public static final int INFO_Y = PLAY_PAUSE_Y + GUTTER + CONTROL_SIZE;
	public static final int INFO_CLOSE_SIZE = 15; // image size
	
	// replacement targets
	public static final int OLDEST_NEIGHBOR = 0; // replace the oldest neighbor
	public static final int LOWEST_ENERGY_NEIGHBOR = 1; // replace the neighbor with the lowest energy level
	
	// replacement method
	public static final int ON_YOUR_OWN = 0; // the child is born with no energy
	public static final int SPLIT_ENERGY = 1; // split energy equally between parent and child
	
	
	// ------------------------------------------------------------------------
	// Simulator Data
	// ------------------------------------------------------------------------
	// a population of Biolumes
	private Biolume[][] population;
	
	// thread that steps Biolumes at a particular rate
	private StepBiolumesThread step_thread;
	
	public static PImage play;
	public static PImage pause;
	public static PImage faster;
	public static PImage slower;
	public static PImage restart;
	public static PImage deenergize;
	public static PImage sound;
	public static PImage close;
	public static PImage repro;
	public static PImage battery0;
	public static PImage battery1;
	public static PImage battery2;
	public static PImage battery3;
	public static PImage battery4;
	public static PImage pressureOn;
	public static PImage pressureOff;
	public static PImage zoom;
	public static PImage visitor;
	
	private ColorPicker cp;
	private float[] led0_target = new float[3];
	private float[] led1_target = new float[3];
	private boolean LED0_pressure_on = false;
	private boolean LED1_pressure_on = false;
	private boolean led0_selected = false;
	private boolean led1_selected = false;
	private boolean cp_selected = false;
	
	private boolean cursorInArena = true;
	private boolean infoBoxOn = false;
	
	private int step_delay_ms = DEFAULT_STEP_DELAY_MS;
	
	// ------------------------------------------------------------------------
	// PApplet setup method (called by Processing)
	// ------------------------------------------------------------------------
	public void setup() {
	    
	    cp = new ColorPicker( PICKER_X, PICKER_Y, PICKER_SIZE, PICKER_SIZE, 255, this);  
	
		// set PRNG seed
		Biolume.RAND.setSeed(0);
		
		// set the window display size
		size(X_DISP * X_SIZE + PICKER_SIZE + GUTTER, Y_DISP * Y_SIZE);
		
		// set the color mode to hue / saturation / brightness
		colorMode(HSB, Variable.MAX_VAR);
		// by default, let's set MSU green and white
		led0_target[0] = Variable.MAX_VAR*23/50; led0_target[1] = Variable.MAX_VAR*13/20; led0_target[2] = Variable.MAX_VAR*27/100;
		led1_target[0] = 0; led1_target[1] = 0; led1_target[2] = Variable.MAX_VAR;
		
		this.initialize_pop(); // initialize the Biolume population
		
		// setup the thread that steps the Biolumes
		step_thread = new StepBiolumesThread(DEFAULT_STEP_DELAY_MS, Integer.MAX_VALUE, this);
		step_thread.start();
		// set default font	
		PFont font = createFont("Serif", 15);
		textFont(font, 15);
		
		// add images
		Simulator.play = loadImage("../media/control/play.png"); Simulator.play.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.pause = loadImage("../media/control/pause.png"); Simulator.pause.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.faster = loadImage("../media/control/faster.png"); Simulator.faster.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.slower = loadImage("../media/control/slower.png"); Simulator.slower.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.restart = loadImage("../media/control/restart.png"); Simulator.restart.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.deenergize = loadImage("../media/control/deenergize.png"); Simulator.deenergize.resize(CONTROL_SIZE, CONTROL_SIZE);
		Simulator.sound = loadImage("../media/sound.png");
		Simulator.close = loadImage("../media/close.png");
		Simulator.repro = loadImage("../media/repro.png"); Simulator.repro.resize(WIDTH, HEIGHT);
		Simulator.battery0 = loadImage("../media/batteries/battery0.png");
		Simulator.battery1 = loadImage("../media/batteries/battery1.png");
		Simulator.battery2 = loadImage("../media/batteries/battery2.png");
		Simulator.battery3 = loadImage("../media/batteries/battery3.png");
		Simulator.battery4 = loadImage("../media/batteries/battery4.png");
		Simulator.pressureOn = loadImage("../media/on.png"); Simulator.pressureOn.resize(AVATAR_SIZE, AVATAR_SIZE);
		Simulator.pressureOff = loadImage("../media/off.png"); Simulator.pressureOff.resize(AVATAR_SIZE, AVATAR_SIZE);
		Simulator.zoom = loadImage("../media/zoom.png"); Simulator.zoom.resize(AVATAR_SIZE, AVATAR_SIZE);
		
		// select a random visitor image
		Random trueRandGen = new Random();
		int visitorID = trueRandGen.nextInt(10);
		if (visitorID == 8 || visitorID == 9) visitorID = trueRandGen.nextInt(10); // make fun icons rare
		String visitor = "../media/visitors/visitor" + visitorID + ".png";
		Simulator.visitor = loadImage(visitor); Simulator.visitor.resize(AVATAR_SIZE, AVATAR_SIZE);
	}

	private float[] getHSB(Color color, int scale) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hsb[0] *= scale;
		hsb[1] *= scale;
		hsb[2] *= scale;
		return hsb;
	}

	// ------------------------------------------------------------------------
	// PApplet draw method (called by Processing)
	// ------------------------------------------------------------------------
	public void draw() {
		
		textSize(15);
		
		if (!infoBoxOn) {
			
			// set the background color
			background(100);
			// draw the Biolumes
			this.draw_population();
			
			// draw the control buttons
			if (this.step_thread.is_paused()) image(Simulator.play, PLAY_PAUSE_X, PLAY_PAUSE_Y, CONTROL_SIZE, CONTROL_SIZE);
			else image(Simulator.pause, PLAY_PAUSE_X, PLAY_PAUSE_Y, CONTROL_SIZE, CONTROL_SIZE);
			image(Simulator.slower, SLOWER_X, SLOWER_Y, CONTROL_SIZE, CONTROL_SIZE);
			image(Simulator.faster, FASTER_X, FASTER_Y, CONTROL_SIZE, CONTROL_SIZE);
			image(Simulator.restart, RESTART_X, RESTART_Y, CONTROL_SIZE, CONTROL_SIZE);
			image(Simulator.deenergize, DEENERGIZE_X, DEENERGIZE_Y, CONTROL_SIZE, CONTROL_SIZE);
		
			// draw the LED0 pressure button
			if (led0_selected) {
				fill(0,0,Variable.MAX_VAR);
				rect(TARGET0_X - WIDTH/16, TARGET0_Y - HEIGHT/16, WIDTH/2 + WIDTH/16, HEIGHT + HEIGHT/8);
				if (cp_selected) led0_target = getHSB(cp.getColor(), Variable.MAX_VAR);
			}
			fill(led0_target[0], led0_target[1], led0_target[2]);
			rect(TARGET0_X, TARGET0_Y, WIDTH/2, HEIGHT);
			if(LED0_pressure_on) image(Simulator.pressureOn, TARGET0_LIGHT_X, TARGET0_LIGHT_Y);
			else image(Simulator.pressureOff, TARGET0_LIGHT_X, TARGET0_LIGHT_Y);
		
			// draw the LED1 pressure button
			if (led1_selected) {
				fill(0,0,Variable.MAX_VAR);
				rect(TARGET1_X, TARGET0_Y - HEIGHT/16, WIDTH/2 + WIDTH/16, HEIGHT + HEIGHT/8);
				if (cp_selected) led1_target = getHSB(cp.getColor(), Variable.MAX_VAR);
			}
			fill(led1_target[0], led1_target[1], led1_target[2]);
			rect(TARGET1_X, TARGET1_Y, WIDTH/2, HEIGHT);
			if(LED1_pressure_on) image(Simulator.pressureOn, TARGET1_LIGHT_X, TARGET1_LIGHT_Y);
			else image(Simulator.pressureOff, TARGET1_LIGHT_X, TARGET1_LIGHT_Y);
			
			cp.render();
		}
	}

	// ------------------------------------------------------------------------
	// Update the display for each Biolume
	// ------------------------------------------------------------------------
	public void draw_population() {

		// Update the display for each Biolume
		for (int i = 0; i < X_SIZE; i++) 
			for (int j = 0; j < Y_SIZE; j++) 
				this.population[i][j].display();
	}
	
	// ------------------------------------------------------------------------
	// step each Biolume through the next instruction
	// ------------------------------------------------------------------------
	public void step_population() {

		// first, ensure CO2 is awarded to Biolumes around stationary visitor.
		int x_coor = mouseX/X_DISP;
		int y_coor = mouseY/Y_DISP;
		if (x_coor < X_SIZE && y_coor < Y_SIZE) 				
			detector(x_coor, y_coor, Instruction.CO2_RANGE, Instruction.CO2);  // Energies for neighbors of neighbors.
		
	    // step each Biolume through the next instruction in random order
		ArrayList<Point> to_go = new ArrayList<Point>();
	    for (int i = 0; i < X_SIZE; i++) 
	    	for (int j = 0; j < Y_SIZE; j++) 
	    		to_go.add(new Point(i,j));
	    while (to_go.size() > 0) {
	    	int next = Biolume.RAND.nextInt(to_go.size());
			population[to_go.get(next).x][to_go.get(next).y].step();
			to_go.remove(next);
	    }
	    
	    if(this.LED0_pressure_on || LED1_pressure_on) 
	    	if(this.LED0_pressure_on && LED1_pressure_on) globalColorPressure(led0_target, led1_target, 2);
	    	else if (this.LED0_pressure_on) globalColorPressure(led0_target, null, 1);
	    	else globalColorPressure(null, led1_target, 1);
	}
	
	// ------------------------------------------------------------------------
	// reproduce a given Biolume over its oldest neighbor
	// ------------------------------------------------------------------------
	public void reproduce_biolume(Point parent, int[] p_genome, int p_exe_size, int replacement_target, int replication_method) {
		
		// temporary variables used to find the correct neighbor
		Point current = new Point();
		ArrayList<Point> children = new ArrayList<Point>();
		double cur_param;
		double child_param;
		if (replacement_target == OLDEST_NEIGHBOR) {
			// replace the oldest neighbor
			child_param = -1;
		}
		else if (replacement_target == LOWEST_ENERGY_NEIGHBOR) {
			// replace the neighbor with the lowest energy level
			child_param = Instruction.ENERGY_MAX;
		}
		else {
			System.out.println("Choose an implemented replacement strategy");
			exit();
			return;
		}
		
		// find the neighbor to replace (randomly break ties)
		for (int i = parent.x - 1; i <= parent.x + 1; i++) {
			
			// check for out of array bounds (toroidal grid)
			if (i < 0) current.x = i + X_SIZE;
			else if (i >= X_SIZE) current.x = i - X_SIZE;
			else current.x = i;
			for (int j = parent.y - 1; j <= parent.y + 1; j++ ) {
				
				// check for out of array bounds (toroidal grid)
				if (j < 0) current.y = j + Y_SIZE;
				else if (j >= Y_SIZE) current.y = j - Y_SIZE;
				else current.y = j;
				
				// don't replace the parent
				if (current.x == parent.x && current.y == parent.y) continue;
				
				// don't replace something already involved in reproduction
				if (population[current.x][current.y].is_reproducing()) continue;
				
				// update the list of children
				if (replacement_target == OLDEST_NEIGHBOR) {
					cur_param = population[current.x][current.y].get_age();
					if (cur_param >= child_param) {
						
						// if the current is strictly the oldest then clear
						//    the list of children
						if (cur_param > child_param) children.clear();
						
						// add the new oldest child to the list
						children.add((Point) current.clone());
						
						// update the oldest age parameter
						child_param = cur_param;
					}
				}
				else if (replacement_target == LOWEST_ENERGY_NEIGHBOR) {
					cur_param = population[current.x][current.y].get_energy();
					
					if (cur_param <= child_param) {
						
						// if the current has strictly less energy then clear
						//    the list of children
						if (cur_param < child_param) children.clear();
						
						// add the new oldest child to the list
						children.add((Point) current.clone());
						
						// update the oldest age parameter
						child_param = cur_param;
					}
				}
				// else we have not selected an implemented replacement strategy
			}
		}
		
		// if there are no available children (all neighbors are part of a
		//    reproduction process) cannot reproduce
		if (children.size() == 0) return;
		
		// randomly choose a child from the list of potential children
		Point child = children.get(Biolume.RAND.nextInt(children.size()));
		
		// replace the child
		if ((child.x > parent.x) || (child.x == parent.x && child.y > parent.y)) {
			population[child.x][child.y].child_reproduce(p_genome, p_exe_size, true);
		}
		else {
			population[child.x][child.y].child_reproduce(p_genome, p_exe_size, false);
		}
		
		if(replication_method == SPLIT_ENERGY) {
			double energy_available = population[parent.x][parent.y].get_energy();
			population[child.x][child.y].set_energy(energy_available/2.0);
			population[parent.x][parent.y].set_energy(energy_available/2.0);
		}
	}

	public void local_broadcast(Point bc, int[] msg) {

		Point neighbor = new Point();
		// send message to neighbors
		for (int i = bc.x - 1; i <= bc.x + 1; i++) {
			
			// check for out of array bounds (toroidal grid)
			if (i < 0) neighbor.x = i + X_SIZE;
			else if (i >= X_SIZE) neighbor.x = i - X_SIZE;
			else neighbor.x = i;
			
			for (int j = bc.y - 1; j <= bc.y + 1; j++ ) {
				
				// check for out of array bounds (toroidal grid)
				if (j < 0) neighbor.y = j + Y_SIZE;
				else if (j >= Y_SIZE) neighbor.y = j - Y_SIZE;
				else neighbor.y = j;
				
				// don't send message back to sender
				if (neighbor.x == bc.x && neighbor.y == bc.y) continue;
				
				// send message
				this.population[neighbor.x][neighbor.y].set_message(msg);
			}
		}
	}
	
	private void globalColorPressure(float[] pressurecolor0, float[] pressurecolor1, int pressures) {
		
	    for (int i = 0; i < X_SIZE; i++) {
		for (int j = 0; j < Y_SIZE; j++) {
			
			float fitness0, fitness1;
			
			if (this.population[i][j].LED0_isOn() && pressurecolor0 != null) {
				
			    float [] currentcolor0 = this.population[i][j].getLED0_color();
				float dh0, ds0, db0, distance0;
				
				dh0 = pressurecolor0 [0] - currentcolor0 [0];
				ds0 = pressurecolor0 [1] - currentcolor0 [1];
				db0 = pressurecolor0 [2] - currentcolor0 [2];
				
			    // Hue is circular, thus, distances can only be as large as halfway. 
			    // To maintain scale with S and B,we multiply the actual distance by 2.
			    if (abs(dh0) > Variable.MAX_VAR/2) dh0 = Variable.MAX_VAR - abs(dh0);
			    dh0 *= 2.0;
			    
			    distance0 = sqrt(pow(dh0,2) + pow(ds0,2) + pow(db0,2));
			    fitness0 = Variable.MAX_COLORDIST - distance0;
			}
			else fitness0 = 0; // LED is off-- no reward.
		    
			if (this.population[i][j].LED1_isOn() && pressurecolor1 != null) {
				
				float [] currentcolor1 = this.population[i][j].getLED1_color();
				float dh1, ds1, db1, distance1;
				
				dh1 = pressurecolor1 [0] - currentcolor1 [0];
				ds1 = pressurecolor1 [1] - currentcolor1 [1];
				db1 = pressurecolor1 [2] - currentcolor1 [2];
				
				// Hue is circular, thus, distances can only be as large as halfway. 
			    // To maintain scale with S and B,we multiply the actual distance by 2.
			    if (abs(dh1) > Variable.MAX_VAR/2) dh1 = Variable.MAX_VAR - abs(dh1);
			    dh1 *= 2.0;
			    
			    distance1 = sqrt(pow(dh1,2) + pow(ds1,2) + pow(db1,2));
			    fitness1 = Variable.MAX_COLORDIST - distance1;
			}
			else fitness1 = 0; // LED is off-- no reward.
	
		    this.population[i][j].set_energy(fitness0/Variable.MAX_COLORDIST * Instruction.ENERGY_MAX / pressures + fitness1/Variable.MAX_COLORDIST * Instruction.ENERGY_MAX / pressures);
		}
	    }
		
	}
	
	public void mousePressed() {
		
		// handle the mouse press over the PLAY/PAUSE button
		if (mouseX > PLAY_PAUSE_X && mouseX < (PLAY_PAUSE_X + CONTROL_SIZE) && mouseY > PLAY_PAUSE_Y && mouseY < (PLAY_PAUSE_Y + CONTROL_SIZE)) {
			this.step_thread.toggle_pause();
			if (!this.step_thread.is_paused()) infoBoxOn = false;
		}
		// handle the speed-down button
		else if (mouseX > SLOWER_X && mouseX < (SLOWER_X + CONTROL_SIZE) && mouseY > SLOWER_Y && mouseY < (SLOWER_Y + CONTROL_SIZE)) {
			step_delay_ms += STEP_DELAY_CHANGE;
			this.step_thread.change_delay(step_delay_ms);
		}
		// handle the speed-up button
		else if (mouseX > FASTER_X && mouseX < (FASTER_X + CONTROL_SIZE) && mouseY > FASTER_Y && mouseY < (FASTER_Y + CONTROL_SIZE)) {
			if(step_delay_ms > 1) {
				step_delay_ms -= STEP_DELAY_CHANGE;
				this.step_thread.change_delay(step_delay_ms);
			}
		}	
		// handle the RESTART button
		else if (mouseX > RESTART_X && mouseX < (FASTER_X + CONTROL_SIZE) && mouseY > RESTART_Y && mouseY < (RESTART_Y + CONTROL_SIZE)) {
			if (!this.step_thread.is_paused()) this.step_thread.toggle_pause();
			initialize_pop();
		}
		// handle the DEENERGIZE button
		else if (mouseX > DEENERGIZE_X && mouseX < (DEENERGIZE_X + CONTROL_SIZE) && mouseY > DEENERGIZE_Y && mouseY < (DEENERGIZE_Y + CONTROL_SIZE)) {
			clear_all_energies();
		}
		// handle the close-info-box button
		else if (mouseX > (this.getSize().width - GUTTER - INFO_CLOSE_SIZE) && mouseX < (this.getSize().width - GUTTER) && mouseY > INFO_Y && mouseY < INFO_Y + INFO_CLOSE_SIZE && infoBoxOn) infoBoxOn = false;
		// handle global pressures, but only if the infobox is NOT active
		else if (mouseX > X_DISP*X_SIZE && !infoBoxOn) {
			// handle the LED0 pressure on/off button
			if (mouseX > TARGET0_LIGHT_X && mouseX < TARGET0_LIGHT_X + AVATAR_SIZE && mouseY > TARGET0_LIGHT_Y && mouseY < TARGET0_LIGHT_Y + AVATAR_SIZE && !infoBoxOn) {
				if (this.LED0_pressure_on) this.LED0_pressure_on = false;
				else this.LED0_pressure_on = true;
			}
			// handle the LED0 target color button
			else if (mouseX > TARGET0_X && mouseX < TARGET0_X + WIDTH/2 && mouseY > TARGET0_Y && mouseY < TARGET0_Y + HEIGHT) {
				led0_selected = true;
				led1_selected = false;
				cp_selected = false;
			}
			// handle the LED1 pressure on/off button
			else if (mouseX > TARGET1_LIGHT_X && mouseX < TARGET1_LIGHT_X + AVATAR_SIZE && mouseY > TARGET1_LIGHT_Y && mouseY < TARGET1_LIGHT_Y + AVATAR_SIZE) {
				if (this.LED1_pressure_on) this.LED1_pressure_on = false;
				else this.LED1_pressure_on = true;
			}
			// handle the LED1 target color button
			else if (mouseX > TARGET1_X && mouseX < TARGET1_X + WIDTH/2 && mouseY > TARGET1_Y && mouseY < TARGET1_Y + HEIGHT) {
				led0_selected = false;
				led1_selected = true;
				cp_selected = false;
			}
			// handle clicks on the color picker area
			else if (mouseX > PICKER_X && mouseX < PICKER_X + PICKER_SIZE && mouseY > PICKER_Y && mouseY < PICKER_Y + PICKER_SIZE) cp_selected = true;
		}
		// handle clicks on the Biolume arena
		else {
			int x_coor, y_coor; 
			x_coor = mouseX/X_DISP;
			y_coor = mouseY/Y_DISP;
			if (x_coor < X_SIZE && y_coor < Y_SIZE) //Within the Biolume arena-- proceed
				if (mouseX%X_DISP <= WIDTH && mouseY%Y_DISP <= HEIGHT) // not in the gutter-- proceed
					if (!this.step_thread.is_paused()) { //We're not paused-- proceed
						detector(x_coor, y_coor, Instruction.TOUCH_RANGE, Instruction.TOUCH); // Reward the Biolume for being touched. 
					}
					else { // We're paused. Show Biolume popup info. 
				
						int [] instructions = this.population[x_coor][y_coor].getGenome();
						//int lines = 10 + instructions.length;
						
						String text = "\n.:::::BIOLUME DETAILS:::::." +
								"\nx,y: " + (x_coor+1) + "," + (y_coor+1) + "\t\t\t\t\tAge: " + 
								this.population[x_coor][y_coor].get_age() + 
								"\t\t\t\t\tEnergy: " + (float)this.population[x_coor][y_coor].get_energy() +
								"\n\n.:::::SENSOR INFORMATION:::::." +
								"\nTouch:" + this.population[x_coor][y_coor].getSensorDetected(Instruction.TOUCH, Instruction.LONG) +
								"\t\t\t\t\tMotion:" + this.population[x_coor][y_coor].getSensorDetected(Instruction.MOTION, Instruction.LONG) +
								"\nSound:" + this.population[x_coor][y_coor].getSensorDetected(Instruction.SOUND, Instruction.LONG) +
								"\t\t\t\t\tCO2:" + this.population[x_coor][y_coor].getSensorDetected(Instruction.CO2, Instruction.LONG) +
								"\n\n.:::::BIOLUME GENOME:::::.";
						for (int i = 0; i < instructions.length; i++) {
							text = text + "\n " + (i+1) + ":\t" + Instruction.values()[instructions[i]];
						}				
						drawInfoBox(text);					
					}
				//else {} // Do nothing! Click's in the gutter!		
			//else {} // Do nothing! Click's outside of the Biolume arena!
		} 
	}
	
	public void mouseMoved() {
		
		int x_coor, y_coor; 
		x_coor = mouseX/X_DISP;
		y_coor = mouseY/Y_DISP;
		
		if (x_coor < X_SIZE && y_coor < Y_SIZE) { //Within the Biolume arena-- proceed
			if (!cursorInArena) { //As long as the cursor is in the arena, it should be a "visitor".
				if (this.step_thread.is_paused()) cursor(Simulator.zoom, AVATAR_SIZE/2, AVATAR_SIZE/2);
				else cursor(Simulator.visitor, AVATAR_SIZE/2, AVATAR_SIZE/2);
				cursorInArena = true;
			}
			// handle a pass of the mouse over the Biolume. 
			if (!this.step_thread.is_paused()) //We're not paused-- proceed
			{
				//if (mouseX%X_DISP <= WIDTH && mouseY%Y_DISP <= HEIGHT) // not in the gutter-- proceed
				detector(x_coor, y_coor, Instruction.MOTION_RANGE, Instruction.MOTION); // Energies exclusive to this Biolume.
				detector(x_coor, y_coor, Instruction.SOUND_RANGE, Instruction.SOUND); // Energies for neighbors.
				detector(x_coor, y_coor, Instruction.CO2_RANGE, Instruction.CO2);  // Energies for neighbors of neighbors.
				//else {} // Do nothing! Click's in the gutter!		
			} 
			// else {} // Do nothing! The system is paused!
		}
		else { // Outside the Biolume arena. No need for a "visitor" here. 
			cursor(ARROW);
			cursorInArena = false;
		}
	}
	
	public void drawInfoBox(String text) {
		fill(0,0,Variable.MAX_VAR);
		rect(INFO_X, INFO_Y, this.getSize().width - INFO_X - GUTTER, this.getSize().height - INFO_Y - GUTTER);
		textSize(12);
		fill(0,0,0);
		text(text, INFO_X + 5, INFO_Y); 
		image(Simulator.close, this.getSize().width - GUTTER - INFO_CLOSE_SIZE, INFO_Y, INFO_CLOSE_SIZE, INFO_CLOSE_SIZE);
		infoBoxOn = true;
	}
	
	public void detector(int x, int y, int range, int sensor_type) {
		if (!this.LED0_pressure_on && !this.LED1_pressure_on) 
			for(int i = x-range; i <= x+range; i++) 
				for(int j = y-range; j <= y+range; j++) 
					if ( (i >= 0) && (i < X_SIZE) && (j >= 0) && (j < Y_SIZE) && !population[i][j].is_reproducing())
						population[i][j].setSensorDetected(sensor_type);
	}
	
	public void initialize_pop() {
		this.population = new Biolume[X_SIZE][Y_SIZE];
		for (int i = 0; i < X_SIZE; i++) 
			for (int j = 0; j < Y_SIZE; j++) 
				this.population[i][j] = new Biolume(this, i, j);
	}
	
	public void clear_all_energies() {
		for (int i = 0; i < X_SIZE; i++) 
			for (int j = 0; j < Y_SIZE; j++) 
				this.population[i][j].set_energy(0.0);
	}
	
	
}