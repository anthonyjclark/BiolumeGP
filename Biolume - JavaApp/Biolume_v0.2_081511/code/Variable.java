
// ------------------------------------------------------------------------
// Variables encoded in the genome
// ------------------------------------------------------------------------
public enum Variable {
	  
	// led0 hue, saturation, brightness
	LED0_H, LED0_S, LED0_B,
	
	// led1 hue, saturation, brightness
	LED1_H, LED1_S, LED1_B,
	
	// LED status
	LED0_ON, LED1_ON,
	
	// sound file
	SPEAKER,
	
	// speaker status
	SPEAKER_ON;
	
	public static final int NUM_VARS = Variable.values().length;
}