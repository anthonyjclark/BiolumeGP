
/**
 * All possible instructions
 *
 */
public enum Instruction {
	
	// blank instruction
	NOP {
		public void execute(Biolume b) {
			return;
		}
	},
	
	// turn LED0 on
	LED0_ON {
		public void execute(Biolume b) {
			int[] vars = b.get_variables();
			int[] on_vals = b.get_display();
			on_vals[0] = vars[0];
			on_vals[1] = vars[1];
			on_vals[2] = vars[2];
			on_vals[6] = ON;
			b.set_display(on_vals);
			return;
		}
	},
	
	// turn LED0 off
	LED0_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[0] = OFF;
			off_vals[1] = OFF;
			off_vals[2] = OFF;
			off_vals[6] = OFF;
			b.set_display(off_vals);
			return;
		}
	},
	
	// toggle LED0
	LED0_TOGGLE {
		public void execute(Biolume b) {
			int[] tog_vals = b.get_display();
			int[] pre_vals = b.get_previous_display();
			tog_vals[0] = pre_vals[0];
			tog_vals[1] = pre_vals[1];
			tog_vals[2] = pre_vals[2];
			tog_vals[6] = pre_vals[6];
			b.set_display(tog_vals);
			return;
		}
	},
	
	// turn LED1 on
	LED1_ON {
		public void execute(Biolume b) {
			int[] vars = b.get_variables();
			int[] on_vals = b.get_display();
			on_vals[3] = vars[3];
			on_vals[4] = vars[4];
			on_vals[5] = vars[5];
			on_vals[7] = ON;
			b.set_display(on_vals);
			return;
		}
	},
	
	// turn LED1 off
	LED1_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[3] = OFF;
			off_vals[4] = OFF;
			off_vals[5] = OFF;
			off_vals[7] = OFF;
			b.set_display(off_vals);
			return;
		}
	},
	
	// toggle LED1
	LED1_TOGGLE {
		public void execute(Biolume b) {
			int[] tog_vals = b.get_display();
			int[] pre_vals = b.get_previous_display();
			tog_vals[3] = pre_vals[3];
			tog_vals[4] = pre_vals[4];
			tog_vals[5] = pre_vals[5];
			tog_vals[7] = pre_vals[7];
			b.set_display(tog_vals);
			return;
		}
	},
	
	// turn speaker on
	SPEAKER_ON {
		public void execute(Biolume b) {
			int[] vars = b.get_variables();
			int[] on_vals = b.get_display();
			on_vals[8] = vars[8];
			on_vals[9] = ON;
			b.set_display(on_vals);
			b.speaker_on(true);
			return;
		}
	},
	
	// turn speaker off
	SPEAKER_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[8] = OFF;
			off_vals[9] = OFF;
			b.set_display(off_vals);
			b.speaker_on(false);
		}
	},
	
	// set the buffer to this Biolumes current display
	BUFFER_SET_DATA {
		public void execute(Biolume b) {
			b.display_to_buffer();
		}
	},
	
	// set this Biolumes display to what is in the Buffer
	BUFFER_GET_DATA {
		public void execute(Biolume b) {
			b.buffer_to_display();
		}
	},
	
	// broadcast this Biolumes display buffer
	MESSAGE_SEND {
		public void execute(Biolume b) {
			b.broadcast_neighborhood();
		}
	},
	
	// collect message and place in the display buffer
	MESSAGE_RETRIEVE {
		public void execute(Biolume b) {
			b.message_to_buffer();
		}
	},
	
	// conditionally execute next instruction
	IF_MOTION {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(MOTION, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_MOTION {
		public void execute(Biolume b) {
			if (b.getSensorDetected(MOTION, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_SOUND {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(SOUND, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_SOUND {
		public void execute(Biolume b) {
			if (b.getSensorDetected(SOUND, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_TOUCH {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(TOUCH, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_TOUCH {
		public void execute(Biolume b) {
			if (b.getSensorDetected(TOUCH, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_CO2 {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(CO2, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_CO2 {
		public void execute(Biolume b) {
			if (b.getSensorDetected(CO2, LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_LO_ENERGY {
		public void execute(Biolume b) {
			if (b.get_energy() < ENERGY_MAX/2) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_HI_ENERGY {
		public void execute(Biolume b) {
			if (b.get_energy() >= ENERGY_MAX/2) b.skipNext();
			return;
		}
	},
	
	// label the next instruction as the target for a future jump
	LABEL {
		public void execute(Biolume b) {
			b.set_label();		
			return;
		}
	},
	
	// jump to the latest marked label
	JUMP {
		public void execute(Biolume b) {
			b.jump_to_label();		
			return;
		}
	},
	
	// reproduce this biolume with some chance
	REPRODUCE {
		public void execute(Biolume b) {
			double repro_prob = (b.get_energy() / ENERGY_MAX) * (REPRO_PROB_MAX - REPRO_PROB_MIN) + REPRO_PROB_MIN;
			if (repro_prob > Biolume.RAND.nextDouble()) {
				b.parent_reproduce();
			}			
			return;
		}
	};
	
	// public method to access each instruction
	public abstract void execute(Biolume b);
	
	// number of instructions
	public static final int INST_SET_SIZE = Instruction.values().length;
	
	// reproduction
	private static final double REPRO_PROB_MAX = 0.10; 
	private static final double REPRO_PROB_MIN = 0.01;
	public static final double ENERGY_MAX = 100.0;
	
	public static final double CO2_ENERGY = ENERGY_MAX*0.01;
	public static final double NORMAL_SOUND_ENERGY = ENERGY_MAX*0.0; 
	public static final double NEW_SOUND_ENERGY = ENERGY_MAX*0.04;
	public static final double MOTION_ENERGY = ENERGY_MAX*0.10; 
	public static final double TOUCH_ENERGY = ENERGY_MAX*0.50;
	public static final double ENERGY_DECAY = ENERGY_MAX*0.01; 
	
	public static final int TOUCH_RANGE = 0;
	public static final int MOTION_RANGE = 0;
	public static final int SOUND_RANGE = 1;
	public static final int CO2_RANGE = 2;

	// Biolume actuators
	private static final int OFF = 0;
	private static final int ON = 1;
	
	// Sensor types
	public static final int TOUCH = 0;
	public static final int MOTION = 1;
	public static final int SOUND = 2;
	public static final int CO2 = 3;
	
	// Sensor term lengths
	public static final int SHORT = 0;
	public static final int LONG = 1;
	
	// Biolume
	public static final int SENSOR_COUNTDOWN_LENGTH = Biolume.EXE_SIZE_MAX;
	public static final int NEW_SOUND_DELAY = 1; // amount of silenced steps before a sound is considered new

}
