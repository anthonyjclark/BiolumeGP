
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
			on_vals[6] = CC.ON;
			b.set_display(on_vals);
			return;
		}
	},
	
	// turn LED0 off
	LED0_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[0] = CC.OFF;
			off_vals[1] = CC.OFF;
			off_vals[2] = CC.OFF;
			off_vals[6] = CC.OFF;
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
			on_vals[7] = CC.ON;
			b.set_display(on_vals);
			return;
		}
	},
	
	// turn LED1 off
	LED1_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[3] = CC.OFF;
			off_vals[4] = CC.OFF;
			off_vals[5] = CC.OFF;
			off_vals[7] = CC.OFF;
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
			on_vals[9] = CC.ON;
			b.set_display(on_vals);
			b.speaker_on(true);
			return;
		}
	},
	
	// turn speaker off
	SPEAKER_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.get_display();
			off_vals[8] = CC.OFF;
			off_vals[9] = CC.OFF;
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
			if (!b.getSensorDetected(CC.MOTION, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_MOTION {
		public void execute(Biolume b) {
			if (b.getSensorDetected(CC.MOTION, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_SOUND {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(CC.SOUND, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_SOUND {
		public void execute(Biolume b) {
			if (b.getSensorDetected(CC.SOUND, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_TOUCH {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(CC.TOUCH, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_TOUCH {
		public void execute(Biolume b) {
			if (b.getSensorDetected(CC.TOUCH, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_CO2 {
		public void execute(Biolume b) {
			if (!b.getSensorDetected(CC.CO2, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_N_CO2 {
		public void execute(Biolume b) {
			if (b.getSensorDetected(CC.CO2, CC.LONG)) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_LO_ENERGY {
		public void execute(Biolume b) {
			if (b.get_energy() < CC.ENERGY_MAX/2) b.skipNext();
			return;
		}
	},
	
	// conditionally execute next instruction
	IF_HI_ENERGY {
		public void execute(Biolume b) {
			if (b.get_energy() >= CC.ENERGY_MAX/2) b.skipNext();
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
			double repro_prob = (b.get_energy() / CC.ENERGY_MAX) * (CC.REPRO_PROB_MAX - CC.REPRO_PROB_MIN) + CC.REPRO_PROB_MIN;
			if (repro_prob > Biolume.RAND.nextDouble()) {
				b.parent_reproduce();
			}			
			return;
		}
	};
	
	// public method to access each instruction
	public abstract void execute(Biolume b);
	
	public static final int INST_SET_SIZE = Instruction.values().length;
}
