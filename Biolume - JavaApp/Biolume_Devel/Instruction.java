/**
 * Instruction.java
 * @authors: Tony J. Clark, Daniel J. Couvertier
 * @date: July 28, 2011
 * @description: This file contains all possible Biolume instructions. 
 */
public enum Instruction {
	
	/**
	 * @Instruction: No-Operation (NOP).
	 * @Description: This instruction does nothing.
	 */
	NOP {
		public void execute(Biolume b) {
			return;
		}
	},
	
	/**
	 * @Instruction: Turn LED0 On (LED0_ON).
	 * @Description: This instruction sets the display values for LED0 to the
	 * encoded HSB values for LED0. It also sets the state of LED0 to ON. 
	 */
	LED0_ON {
		public void execute(Biolume b) {
			int[] vars = b.getVariables();
			int[] on_vals = b.getDisplay();
			on_vals[Variable.LED0_H.ordinal()] = vars[Variable.LED0_H.ordinal()];
			on_vals[Variable.LED0_S.ordinal()] = vars[Variable.LED0_S.ordinal()];
			on_vals[Variable.LED0_B.ordinal()] = vars[Variable.LED0_B.ordinal()];
			on_vals[Variable.LED0_STATE.ordinal()] = CC.ON;
			b.setDisplay(on_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Turn LED0 Off (LED0_OFF).
	 * @Description: This instruction clears the display values for LED0 
	 * (technically, and only within the Simulator, it sets them to black). It
	 * also sets the state of LED0 to OFF. 
	 */
	LED0_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.getDisplay();
			off_vals[Variable.LED0_H.ordinal()] = CC.OFF;
			off_vals[Variable.LED0_S.ordinal()] = CC.OFF;
			off_vals[Variable.LED0_B.ordinal()] = CC.OFF;
			off_vals[Variable.LED0_STATE.ordinal()] = CC.OFF;
			b.setDisplay(off_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Toggle LED0 (LED0_TOGGLE).
	 * @Description: This instruction sets the display values for LED0 to their
	 * previous state, whatever that may have been.
	 */
	LED0_TOGGLE {
		public void execute(Biolume b) {
			int[] tog_vals = b.getDisplay();
			int[] pre_vals = b.getPreviousDisplay();
			tog_vals[Variable.LED0_H.ordinal()] = pre_vals[Variable.LED0_H.ordinal()];
			tog_vals[Variable.LED0_S.ordinal()] = pre_vals[Variable.LED0_S.ordinal()];
			tog_vals[Variable.LED0_B.ordinal()] = pre_vals[Variable.LED0_B.ordinal()];
			tog_vals[Variable.LED0_STATE.ordinal()] = pre_vals[Variable.LED0_STATE.ordinal()];
			b.setDisplay(tog_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Turn LED1 On (LED1_ON).
	 * @Description: This instruction sets the display values for LED1 to the
	 * encoded HSB values for LED1. It also sets the state of LED1 to ON.  
	 */
	LED1_ON {
		public void execute(Biolume b) {
			int[] vars = b.getVariables();
			int[] on_vals = b.getDisplay();
			on_vals[Variable.LED1_H.ordinal()] = vars[Variable.LED1_H.ordinal()];
			on_vals[Variable.LED1_S.ordinal()] = vars[Variable.LED1_S.ordinal()];
			on_vals[Variable.LED1_B.ordinal()] = vars[Variable.LED1_B.ordinal()];
			on_vals[Variable.LED1_STATE.ordinal()] = CC.ON;
			b.setDisplay(on_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Turn LED1 Off (LED1_OFF).
	 * @Description: This instruction clears the display values for LED1 
	 * (technically, and only within the Simulator, it sets them to black). It
	 * also sets the state of LED1 to OFF. 
	 */
	LED1_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.getDisplay();
			off_vals[Variable.LED1_H.ordinal()] = CC.OFF;
			off_vals[Variable.LED1_S.ordinal()] = CC.OFF;
			off_vals[Variable.LED1_B.ordinal()] = CC.OFF;
			off_vals[Variable.LED1_STATE.ordinal()] = CC.OFF;
			b.setDisplay(off_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Toggle LED1 (LED1_TOGGLE).
	 * @Description: This instruction sets the display values for LED1 to their
	 * previous state, whatever that may have been.
	 */
	LED1_TOGGLE {
		public void execute(Biolume b) {
			int[] tog_vals = b.getDisplay();
			int[] pre_vals = b.getPreviousDisplay();
			tog_vals[Variable.LED1_H.ordinal()] = pre_vals[Variable.LED1_H.ordinal()];
			tog_vals[Variable.LED1_S.ordinal()] = pre_vals[Variable.LED1_S.ordinal()];
			tog_vals[Variable.LED1_B.ordinal()] = pre_vals[Variable.LED1_B.ordinal()];
			tog_vals[Variable.LED1_STATE.ordinal()] = pre_vals[Variable.LED1_STATE.ordinal()];
			b.setDisplay(tog_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Turn Speaker On (SPEAKER_ON).
	 * @Description: This instruction displays the index which will be
	 * translated to a MIDI filename and played through the Biolume's Speaker.
	 * It also sets the state of the Speaker to ON. Within the Simulator, no
	 * translation is actually made since there are no MIDI files available. 
	 */
	SPEAKER_ON {
		public void execute(Biolume b) {
			int[] vars = b.getVariables();
			int[] on_vals = b.getDisplay();
			on_vals[Variable.SPEAKER.ordinal()] = vars[8];
			on_vals[Variable.SPEAKER_STATE.ordinal()] = CC.ON;
			b.setDisplay(on_vals);
			return;
		}
	},
	
	/**
	 * @Instruction: Turn Speaker Off (SPEAKER_OFF).
	 * @Description: This instruction clears the index that would be translated
	 * to a MIDI filename (technically, and only within the Simulator, the
	 * value is being set to index 0). It also sets the state of the Speaker to
	 * OFF.
	 */
	SPEAKER_OFF {
		public void execute(Biolume b) {
			int[] off_vals = b.getDisplay();
			off_vals[Variable.SPEAKER.ordinal()] = CC.OFF;
			off_vals[Variable.SPEAKER_STATE.ordinal()] = CC.OFF;
			b.setDisplay(off_vals);
		}
	},
	
	/**
	 * @Instruction: Copy current display to display buffer (BUFFER_SET_DATA).
	 * @Description: This instruction copies all current display data into the
	 * display buffer, including: LED HSB values, LED states, speaker index,
	 * and speaker state.
	 */
	BUFFER_SET_DATA {
		public void execute(Biolume b) {
			b.displayToBuffer();
		}
	},
	
	/**
	 * @Instruction: Copy display buffer to current display (BUFFER_GET_DATA).
	 * @Description: This instruction copies all data in the display buffer
	 * into the current display, including: LED HSB values, LED states, speaker
	 * index, and speaker state.
	 */
	BUFFER_GET_DATA {
		public void execute(Biolume b) {
			b.bufferToDisplay();
		}
	},
	
	/**
	 * @Instruction: Broadcast display buffer (MESSAGE_SEND).
	 * @Description: This instruction broadcasts the Biolume's display buffer
	 * to its neighbors, including: LED HSB values, LED states, speaker index,
	 * and speaker state. A neighbor must execute a MESSAGE_RETRIEVE to place
	 * this data in its own display buffer. 
	 */
	MESSAGE_SEND {
		public void execute(Biolume b) {
			b.broadcastNeighborhood();
		}
	},
	
	/**
	 * @Instruction: Save broadcast into display buffer (MESSAGE_RECEIVE).
	 * @Description: This instruction retrieves the most recent broadcast made
	 * by a neighbor and saves the data into its display buffer. The data
	 * includes: LED HSB values, LED states, speaker index, and speaker state.
	 * A neighbor must execute a MESSAGE_SEND to make its data available. 
	 */
	MESSAGE_RETRIEVE {
		public void execute(Biolume b) {
			b.messageToBuffer();
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if motion WAS sensed (IF_MOTION).
	 * @Description: This instruction checks whether motion was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped.   
	 */
	IF_MOTION {
		public void execute(Biolume b) {
			if (!b.getDetection(CC.MOTION, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if motion NOT sensed (IF_N_MOTION).
	 * @Description: This instruction checks whether motion was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be skipped. Otherwise, the next instruction will
	 * be executed in the next step.  
	 */
	IF_N_MOTION {
		public void execute(Biolume b) {
			if (b.getDetection(CC.MOTION, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if sound WAS sensed (IF_SOUND).
	 * @Description: This instruction checks whether sound was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped. 
	 * Note that this instruction operates on long term sound only, and does
	 * not discriminate them from new sounds.
	 */
	IF_SOUND {
		public void execute(Biolume b) {
			if (!b.getDetection(CC.SOUND, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if sound NOT sensed (IF_N_SOUND).
	 * @Description: This instruction checks whether sound was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be skipped. Otherwise, the next instruction will
	 * be executed in the next step.  
	 * Note that this instruction operates on long term sound only, and does
	 * not discriminate them from new sounds.
	 */
	IF_N_SOUND {
		public void execute(Biolume b) {
			if (b.getDetection(CC.SOUND, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if touch WAS sensed (IF_TOUCH).
	 * @Description: This instruction checks whether touch was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped.   
	 */
	IF_TOUCH {
		public void execute(Biolume b) {
			if (!b.getDetection(CC.TOUCH, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if touch NOT sensed (IF_N_TOUCH).
	 * @Description: This instruction checks whether touch was sensed in the
	 * last CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be skipped. Otherwise, the next instruction will
	 * be executed in the next step.  
	 */
	IF_N_TOUCH {
		public void execute(Biolume b) {
			if (b.getDetection(CC.TOUCH, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if CO2 NOT sensed (IF_N_CO2).
	 * @Description: This instruction checks whether CO2 was sensed in the last
	 * CC.LONG_TERM_DURATION steps. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped.  
	 */
	IF_CO2 {
		public void execute(Biolume b) {
			if (!b.getDetection(CC.CO2, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if CO2 NOT sensed (IF_N_CO2).
	 * @Description: This instruction checks whether CO2 was sensed in the last
	 * CC.LONG_TERM_DURATION steps. If so, the next instruction in the 
	 * Biolume's genome will be skipped. Otherwise, the next instruction will
	 * be executed in the next step.  
	 */
	IF_N_CO2 {
		public void execute(Biolume b) {
			if (b.getDetection(CC.CO2, CC.LONG)) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if energy is LOW (IF_LO_ENERGY).
	 * @Description: This instruction checks whether the Biolume's energy is at
	 * less than 50% of its capacity. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped.
	 */
	IF_LO_ENERGY {
		public void execute(Biolume b) {
			if (b.getEnergy() < CC.ENERGY_MAX/2) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Execute next instruction if energy is HIGH (IF_HI_ENERGY).
	 * @Description: This instruction checks whether the Biolume's energy is at
	 * 50% or more of its capacity. If so, the next instruction in the
	 * Biolume's genome will be executed in the next step. Otherwise, the next
	 * instruction will be skipped.
	 */
	IF_HI_ENERGY {
		public void execute(Biolume b) {
			if (b.getEnergy() >= CC.ENERGY_MAX/2) b.incPC();
			return;
		}
	},
	
	/**
	 * @Instruction: Label the next instruction as a target for a jump (LABEL).
	 * @Description: This instruction marks the next instruction in the
	 * Biolume's genome as the target for a JUMP instruction executed in the
	 * future.
	 */
	LABEL {
		public void execute(Biolume b) {
			b.setLabel();		
			return;
		}
	},
	
	/**
	 * @Instruction: Jump to the latest labeled instruction (JUMP).
	 * @Description: This instruction sets the Biolume's PC to point to the
	 * latest labeled instruction. By default, the label is set to 0 (the first
	 * instruction in the Biolume's genome). Therefore, if no LABEL is executed
	 * before the JUMP instruction, the PC will point to the first instruction.
	 */
	JUMP {
		public void execute(Biolume b) {
			b.jumpToLabel();		
			return;
		}
	},
	
	/**
	 * @Instruction: Attempt to reproduce (REPRO).
	 * @Description: This instruction attempts to reproduce the Biolume once.
	 * The probability of successfully reproducing is directly proportional to
	 * the energy accumulated by the Biolume. Also playing a factor are the 
	 * CC.REPRO_PROB_MIN/MAX user-defined parameters. The offspring will be
	 * exposed to mutations and will replace the neighbor defined by
	 * CC.REPLACEMENT_METHOD. The energy passed on from the parent to the
	 * offspring is defined by CC.REPLICATION_METHOD.
	 */
	REPRODUCE {
		public void execute(Biolume b) {
			double repro_prob = (b.getEnergy() / CC.ENERGY_MAX) * (CC.REPRO_PROB_MAX - CC.REPRO_PROB_MIN) + CC.REPRO_PROB_MIN;
			if (repro_prob > Biolume.RAND.nextDouble()) {
				b.parentReproduce();
			}			
			return;
		}
	};
	
	// Public method to access each instruction.
	public abstract void execute(Biolume b);
	
	// Calculate the size of this instruction set. 
	public static final int INST_SET_SIZE = Instruction.values().length;
}
