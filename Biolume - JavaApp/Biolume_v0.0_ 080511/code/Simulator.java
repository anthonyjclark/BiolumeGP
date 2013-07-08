
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import processing.core.*;

public class Simulator extends PApplet {
	
	private static final long serialVersionUID = 1L;
	
	// ------------------------------------------------------------------------
	// Simulator Definitions
	// ------------------------------------------------------------------------
	// display elements for a Biolume
	public static final int HEIGHT = 50;
	public static final int WIDTH = 50;
	public static final int GUTTER = 10;
	public static final int X_DISP = HEIGHT + GUTTER;
	public static final int Y_DISP = WIDTH + GUTTER;
	public static final int X_PICKER = 200;
	
	
	// how often should the Biolumes step
	private static final int STEP_DELAY_MS = 500; 
	
	// dimensions of the population
	private static final int X_SIZE = 15; 
	private static final int Y_SIZE = 10;
	
	// pause button
	private static final int PAUSE_X = X_SIZE * X_DISP;
	private static final int PAUSE_Y = 0;
	
	// info box
	public static final int INFO_X = PAUSE_X;
	public static final int INFO_Y = PAUSE_Y + GUTTER + HEIGHT;
	public static final int INFO_CLOSE_SIDE = 15; // image size
	
	// replacement types
	public static final int OLD_NEIGHBOR = 0; // replace the oldest neighbor
	public static final int LOW_NEIGHBOR = 1; // replace the neighbor with the lowest energy level
	
	// ------------------------------------------------------------------------
	// Simulator Data
	// ------------------------------------------------------------------------
	// a population of Biolumes
	private Biolume[][] population;
	
	// thread that steps Biolumes at a particular rate
	private StepBiolumesThread step_thread;
	
	public static PImage sound;
	public static PImage close;
	public static PImage battery0;
	public static PImage battery1;
	public static PImage battery2;
	public static PImage battery3;
	public static PImage battery4;
	public static PImage visitor;
	
	public boolean LED0_pressure = false, LED1_pressure = false;
	public ColorPicker cp0, cp1;
	
	private boolean cursorInArena = true;
	private boolean infoBoxON = false;
	
	// ------------------------------------------------------------------------
	// PApplet setup method (called by Processing)
	// ------------------------------------------------------------------------
	public void setup() {
	    
	    cp0 = new ColorPicker( X_DISP * (X_SIZE + 1), 80, X_PICKER, X_PICKER, 255, this);    
	    cp1 = new ColorPicker( X_DISP * (X_SIZE + 1), 300, X_PICKER, X_PICKER, 255, this);
	
		// set PRNG seed
		Biolume.RAND.setSeed(0);
		
		// set the window display size
		size(X_DISP * (X_SIZE + 1) + X_PICKER + GUTTER, Y_DISP * Y_SIZE);
		
		// set the color mode to hue / saturation / brightness
		colorMode(HSB, Variable.MAX_VAR);
		
		// initialize all of the Biolumes
		this.population = new Biolume[X_SIZE][Y_SIZE];
		for (int i = 0; i < X_SIZE; i++) {
			for (int j = 0; j < Y_SIZE; j++) {
				this.population[i][j] = new Biolume(this, i, j);
			}
		}
		
		// setup the thread that steps the Biolumes
		step_thread = new StepBiolumesThread(STEP_DELAY_MS, Integer.MAX_VALUE, this);
		step_thread.start();
		// set default font	
		PFont font = createFont("Serif", 15);
		textFont(font, 15);
		
		// add image
		Simulator.sound = loadImage("../media/sound.png");
		Simulator.close = loadImage("../media/close.png");
		Simulator.battery0 = loadImage("../media/battery0.png");
		Simulator.battery1 = loadImage("../media/battery1.png");
		Simulator.battery2 = loadImage("../media/battery2.png");
		Simulator.battery3 = loadImage("../media/battery3.png");
		Simulator.battery4 = loadImage("../media/battery4.png");
		Simulator.visitor = loadImage("../media/visitor.png");
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
		
		if (!infoBoxON) {
			
			// set the background color
			background(100);
			// draw the Biolumes
			this.draw_population();
			
			// draw the pause button
			fill(37,255,Variable.MAX_VAR);
			rect(PAUSE_X, PAUSE_Y, WIDTH, HEIGHT);
			fill(0,0,0);
			text("PAUSE", PAUSE_X + 1, PAUSE_Y + 30);
			
			// Global Pressure LED0
			fill(0,0,255);
			text("Global", PAUSE_X + 3, PAUSE_Y + 105);
			fill(0,0,255);
			text("Pressure", PAUSE_X - 1, PAUSE_Y + 120);
			fill(0,0,255);
			text("LED #1", PAUSE_X + 5, PAUSE_Y + 135); //LED0, but normal people wouldn't understand 0-indexing.
			
			// draw the LED0 pressure on button
			float[] hsb = getHSB(cp0.getColor(), Variable.MAX_VAR);
			fill(hsb[0], hsb[1], hsb[2]);
			rect(PAUSE_X, PAUSE_Y + 144, WIDTH, HEIGHT);
			fill(0,0,255);
			text("ON", PAUSE_X + 14, PAUSE_Y + 175);
			
			// draw the LED0 pressure off button
			fill(0,255,Variable.MAX_VAR);
			rect(PAUSE_X, PAUSE_Y + 210, WIDTH, HEIGHT);
			fill(0,0,255);
			text("OFF", PAUSE_X + 11, PAUSE_Y + 240);
					
			// Global Pressure LED1
			fill(0,0,255);
			text("Global", PAUSE_X + 3, PAUSE_Y + 315);
			fill(0,0,255);
			text("Pressure", PAUSE_X - 1, PAUSE_Y + 330);
			fill(0,0,255);
			text("LED #2", PAUSE_X + 5, PAUSE_Y + 345); //LED1, but normal people wouldn't understand 0-indexing.
					
			// draw the LED1 pressure off button
			fill(0,255,Variable.MAX_VAR);
			rect(PAUSE_X, PAUSE_Y + 425, WIDTH, HEIGHT);
			fill(0,0,255);
			text("OFF", PAUSE_X + 11, PAUSE_Y + 455);
		
			// draw the LED1 pressure on button
			float[] hsb1 = getHSB(cp1.getColor(), Variable.MAX_VAR);
			fill(hsb1[0], hsb1[1], hsb1[2]);
			rect(PAUSE_X, PAUSE_Y + 359, WIDTH, HEIGHT);
			fill(0,0,255);
			text("ON", PAUSE_X + 14, PAUSE_Y + 390);
			
			cp0.render();
			cp1.render();
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

	    // step each Biolume through the next instruction
	    for (int i = 0; i < X_SIZE; i++) {
	    	for (int j = 0; j < Y_SIZE; j++) {
	    		this.population[i][j].step();
				
	    		if (i == X_SIZE-1 && j == Y_SIZE-1)
					this.population[i][j].printInst();
	    	}
	    }
	    
	    if(this.LED0_pressure || LED1_pressure) {
	    	if(this.LED0_pressure && LED1_pressure) globalColorPressure(cp0.getColor(), cp1.getColor(), 2);
	    	else if (this.LED0_pressure) globalColorPressure(cp0.getColor(), null, 1);
	    	else globalColorPressure(null, cp1.getColor(), 1);
	    }
	}
	
	// ------------------------------------------------------------------------
	// reproduce a given Biolume over its oldest neighbor
	// ------------------------------------------------------------------------
	public void reproduce_biolume(Point parent, int[] p_genome, int p_exe_size, int replacement_type) {
		
		// temporary variables used to find the correct neighbor
		Point current = new Point();
		ArrayList<Point> children = new ArrayList<Point>();
		double cur_param;
		double child_param;
		if (replacement_type == OLD_NEIGHBOR) {
			// replace the oldest neighbor
			child_param = -1;
		}
		else if (replacement_type == LOW_NEIGHBOR) {
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
				if (replacement_type == OLD_NEIGHBOR) {
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
				else if (replacement_type == LOW_NEIGHBOR) {
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
	
	private void globalColorPressure(Color pressurecolor0, Color pressurecolor1, int pressures) {
	    
	    for (int i = 0; i < X_SIZE; i++) {
		for (int j = 0; j < Y_SIZE; j++) {
			
			float fitness0, fitness1;
			
			if (this.population[i][j].LED0_isOn() && pressurecolor0 != null) {
				
				float [] presscolor0 = Color.RGBtoHSB(pressurecolor0.getRed(), pressurecolor0.getGreen(), pressurecolor0.getBlue(), null);
			    float [] currentcolor0 = this.population[i][j].getLED0_color();
				float dh0, ds0, db0, distance0;
				
				dh0 = presscolor0 [0] * Variable.MAX_VAR - currentcolor0 [0];
				ds0 = presscolor0 [1] * Variable.MAX_VAR - currentcolor0 [1];
				db0 = presscolor0 [2] * Variable.MAX_VAR - currentcolor0 [2];
				
			    // Hue is circular, thus, distances can only be as large as halfway. 
			    // To maintain scale with S and B,we multiply the actual distance by 2.
			    if (abs(dh0) > Variable.MAX_VAR/2) dh0 = Variable.MAX_VAR - abs(dh0);
			    dh0 *= 2.0;
			    
			    distance0 = sqrt(pow(dh0,2) + pow(ds0,2) + pow(db0,2));
			    fitness0 = Variable.MAX_COLORDIST - distance0;
			}
			else fitness0 = 0; // LED is off-- no reward.
		    
			if (this.population[i][j].LED1_isOn() && pressurecolor1 != null) {
				
				float [] presscolor1 = Color.RGBtoHSB(pressurecolor1.getRed(), pressurecolor1.getGreen(), pressurecolor1.getBlue(), null);
				float [] currentcolor1 = this.population[i][j].getLED1_color();
				float dh1, ds1, db1, distance1;
				
				dh1 = presscolor1 [0] * Variable.MAX_VAR - currentcolor1 [0];
				ds1 = presscolor1 [1] * Variable.MAX_VAR - currentcolor1 [1];
				db1 = presscolor1 [2] * Variable.MAX_VAR - currentcolor1 [2];
				
				// Hue is circular, thus, distances can only be as large as halfway. 
			    // To maintain scale with S and B,we multiply the actual distance by 2.
			    if (abs(dh1) > Variable.MAX_VAR/2) dh1 = Variable.MAX_VAR - abs(dh1);
			    dh1 *= 2.0;
			    
			    distance1 = sqrt(pow(dh1,2) + pow(ds1,2) + pow(db1,2));
			    fitness1 = Variable.MAX_COLORDIST - distance1;
			}
			else fitness1 = 0; // LED is off-- no reward.
				
		    //System.out.println("x,y l0-f0 l1-f1: " + i + "," + j + " " + this.population[i][j].LED0_isOn() + "-" + fitness0 + " " + this.population[i][j].LED1_isOn() + "-" + fitness1);
		    
		    this.population[i][j].set_energy(fitness0/Variable.MAX_COLORDIST * Instruction.ENERGY_MAX / pressures + fitness1/Variable.MAX_COLORDIST * Instruction.ENERGY_MAX / pressures);
		}
	    }
		
	}
	
	public void mousePressed() {
		
		// handle the mouse press over the 'pause' button
		if (mouseX > PAUSE_X && mouseX < (PAUSE_X + WIDTH) && mouseY > PAUSE_Y && mouseY < (PAUSE_Y + HEIGHT)) {
			this.step_thread.toggle_pause();
			if (!this.step_thread.is_paused()) infoBoxON = false;
		}
		else if (mouseX > (this.getSize().width - GUTTER - INFO_CLOSE_SIDE) && mouseX < (this.getSize().width - GUTTER) && mouseY > INFO_Y && mouseY < INFO_Y + INFO_CLOSE_SIDE && infoBoxON) infoBoxON = false;
		else if (mouseX > PAUSE_X && mouseX < (PAUSE_X + WIDTH) && mouseY > PAUSE_Y + 146 && mouseY < (PAUSE_Y + 148 + HEIGHT) && !infoBoxON) {
			this.LED0_pressure = true;
		}
		else if (mouseX > PAUSE_X && mouseX < (PAUSE_X + WIDTH) && mouseY > PAUSE_Y + 211 && mouseY < (PAUSE_Y + 213 + HEIGHT) && !infoBoxON) {
			this.LED0_pressure = false;
		}
		else if (mouseX > PAUSE_X && mouseX < (PAUSE_X + WIDTH) && mouseY > PAUSE_Y + 359 && mouseY < (PAUSE_Y + 365 + HEIGHT) && !infoBoxON) {
			this.LED1_pressure = true;
		}
		else if (mouseX > PAUSE_X && mouseX < (PAUSE_X + WIDTH) && mouseY > PAUSE_Y + 425 && mouseY < (PAUSE_Y + 430 + HEIGHT) && !infoBoxON) {
			this.LED1_pressure = false;
		}
		else {
			int x_coor, y_coor; 
			x_coor = mouseX/X_DISP;
			y_coor = mouseY/Y_DISP;
			if (x_coor < X_SIZE && y_coor < Y_SIZE) //Within the Biolume arena-- proceed
				if (mouseX%X_DISP <= WIDTH && mouseY%Y_DISP <= HEIGHT) // not in the gutter-- proceed
					if (!this.step_thread.is_paused()) { //We're not paused-- proceed
						rewarder(x_coor, y_coor, 0, Instruction.TOUCH_ENERGY); // Reward the Biolume for being touched. 
						population[x_coor][y_coor].setTouchDetected(true);
					}
					else { // We're paused. Show Biolume popup info. 
				
						int [] instructions = this.population[x_coor][y_coor].getGenome();
						//int lines = 10 + instructions.length;
						
						String text = "\n.:::::BIOLUME DETAILS:::::." +
								"\nx,y: " + (x_coor+1) + "," + (y_coor+1) + "\t\t\t\t\tAge: " + 
								this.population[x_coor][y_coor].get_age() + 
								"\t\t\t\t\tEnergy: " + (float)this.population[x_coor][y_coor].get_energy() +
								"\n\n.:::::SENSOR INFORMATION:::::." +
								"\nTouch Sensed: " + this.population[x_coor][y_coor].getTouchDetected() +
								"\t\t\t\t\tMotion Sensed: " + this.population[x_coor][y_coor].getMotionDetected() +
								"\nSound Sensed: " + this.population[x_coor][y_coor].getSoundDetected() +
								"\t\t\t\t\tCO2 Sensed: " + this.population[x_coor][y_coor].getCO2Detected() +
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
				cursor(Simulator.visitor, 16, 16);
				cursorInArena = true;
			}
			// handle a pass of the mouse over the Biolume. 
			if (!this.step_thread.is_paused()) //We're not paused-- proceed
			{
				//if (mouseX%X_DISP <= WIDTH && mouseY%Y_DISP <= HEIGHT) // not in the gutter-- proceed
				rewarder(x_coor, y_coor, 0, Instruction.MOTION_ENERGY); // Energies exclusive to this Biolume.
				rewarder(x_coor, y_coor, 1, Instruction.SOUND_ENERGY); // Energies for neighbors.
				rewarder(x_coor, y_coor, 2, Instruction.CO2_ENERGY);  // Energies for neighbors of neighbors.
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
		image(Simulator.close, this.getSize().width - GUTTER - INFO_CLOSE_SIDE, INFO_Y, INFO_CLOSE_SIDE, INFO_CLOSE_SIDE);
		infoBoxON = true;
	}
	
	public void rewarder(int x, int y, int range, double reward) {
		for(int i = x-range; i <= x+range; i++) 
			for(int j = y-range; j <= y+range; j++) 
				if ( (i >= 0) && (i < X_SIZE) && (j >= 0) && (j < Y_SIZE) && !population[i][j].is_reproducing()) {
					population[i][j].add_energy(reward);
					if (reward == Instruction.TOUCH_ENERGY) population[i][j].setTouchDetected(true);
					else if (reward == Instruction.MOTION_ENERGY) population[i][j].setMotionDetected(true);
					else if (reward == Instruction.SOUND_ENERGY) population[i][j].setSoundDetected(true);
					else if (reward == Instruction.CO2_ENERGY) population[i][j].setCO2Detected(true);
				}
	}
	
}