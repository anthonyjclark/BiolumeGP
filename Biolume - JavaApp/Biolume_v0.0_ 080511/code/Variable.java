
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
	
	// sound
	SPEAKER;
	
	// number of variables
	public static final int NUM_VARS = Variable.values().length;
	public static final int MAX_VAR = 255;
	
	public static final float MAX_COLORDIST = (float) java.lang.Math.sqrt(3. * java.lang.Math.pow(MAX_VAR, 2));
}