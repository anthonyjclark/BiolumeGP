/**
 * Variable.java
 * @authors: Tony J. Clark, Daniel J. Couvertier
 * @date: July 28, 2011
 * @description: All Variables encoded in the Biolume genome. 
 * - Hue, Saturation and Brightness values are in the range of [0-CC.MAX_VAR). 
 * - Speaker will also range from [0-CC.MAX_VAR) for all possible sound files.
 * - The X_STATE values are intended to be booleans.
 */
public enum Variable {
	  
	// LED0 Hue, Saturation, Brightness and state.
	LED0_H, LED0_S, LED0_B, LED0_STATE,
	
	// LED1 Hue, Saturation, Brightness and state.
	LED1_H, LED1_S, LED1_B, LED1_STATE,
	
	// Sound file and speaker status
	SPEAKER, SPEAKER_STATE;
	
	// Calculate the amount of variables.
	public static final int NUM_VARS = Variable.values().length;
}