
public class StepBiolumesThread extends Thread{

	// ------------------------------------------------------------------------
	// Thread Data
	// ------------------------------------------------------------------------
	// is this thread currently running
	private boolean pause;
	
	// delay between steps (in ms)
	private int delay;
	
	// how many times have the Biolumes been stepped
	private int steps_count;
	
	// maximum number of steps to perform
	private int steps_max;
	
	// the Biolume population
	private Simulator simulator;
	
	// ------------------------------------------------------------------------
	// Thread Constructor
	// ------------------------------------------------------------------------
	public StepBiolumesThread(int delay_ms, int max, Simulator sim) {
		this.pause = true;
		this.delay = delay_ms;
		this.steps_count = 0;
		this.steps_max = max;
		this.simulator = sim;
	}
	
	// ------------------------------------------------------------------------
	// Thread start method
	// ------------------------------------------------------------------------
	@Override
	public synchronized void start() {
		// call "Threads" start method
		super.start();
	}
	
	// ------------------------------------------------------------------------
	// Thread run method
	// ------------------------------------------------------------------------
	@Override
	public void run() {
		
		// this threads loop
		while (this.steps_count < this.steps_max) {
			
			if (!this.pause){
				// increment the step counter
				this.steps_count++;
				
				// step the Biolumes
				this.simulator.step_population();
				
				// set this thread to sleep until Biolumes should step again
				try {
					sleep(delay);
				}
				// exception catch is required in Java
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void toggle_pause() {
		this.pause = !this.pause;
	}
	
	public boolean is_paused() {
		return this.pause;
	}

	public void change_delay(int new_delay_ms) {
		this.delay = new_delay_ms;
	}
	
}
