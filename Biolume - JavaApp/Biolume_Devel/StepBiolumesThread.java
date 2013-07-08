/**
 * StepBiolumesThread.java
 * @authors: Tony J. Clark, Daniel J. Couvertier
 * @date: July 28, 2011
 * @description: This file contains the Step Thread for the Biolume Simulator. 
 */
public class StepBiolumesThread extends Thread {

    /**************************************************************************
     * Step Thread parameters.
     */
	
	// Is this thread currently running?
	private boolean pause;
	
	// Delay between steps (in ms).
	private int delay;
	
	// How many times have the Biolumes been stepped.
	private int steps_count;
	
	// Maximum number of steps to perform.
	private int steps_max;
	
	// The Biolume Simulator.
	private Simulator simulator;
	
    /**************************************************************************
     * Step Thread methods.
     */
	
	/**
	 * Thread constructor.
	 * @param delay_ms:	The delay in miliseconds.
	 * @param max:	The maximum number of steps to perform.
	 * @param sim:	The Biolume Simulator.
	 */
	public StepBiolumesThread(int delay_ms, int max, Simulator sim) {
		this.pause = true;
		this.delay = delay_ms;
		this.steps_count = 0;
		this.steps_max = max;
		this.simulator = sim;
	}
	
	@Override
	/**
	 * Override of the thread start method.
	 */
	public synchronized void start() {
		// call "Threads" start method
		super.start();
	}

	@Override
	/**
	 * Override of the thread run method.
	 * If the thread is not paused, a step (with a duration of delay), will
	 * take place in which every Biolume executes one instruction. Otherwise, 
	 * nothing happens.  
	 * @throws:	InterruptedException error.
	 */
	public void run() {
		// The thread's loop.
		while (this.steps_count < this.steps_max) {
			if (!this.pause) {
				// increment the step counter
				this.steps_count++;
				// step the Biolumes
				this.simulator.stepPopulation();
				// set this thread to sleep until Biolumes should step again
				try {
					sleep(this.delay);
				}
				// exception catch is required in Java
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method toggles the pause on this thread. 
	 */
	public void togglePause() {
		this.pause = !this.pause;
	}
	
	/**
	 * This method returns whether this thread is paused or not.
	 * @return:	True if this thread is paused, False if otherwise.
	 */
	public boolean isPaused() {
		return this.pause;
	}

	/**
	 * This method changes the delay on this thread step.
	 * @param new_delay_ms:	The new delay, in miliseconds.
	 */
	public void changeDelay(int new_delay_ms) {
		this.delay = new_delay_ms;
	}
	
	/**
	 * This method returns the thread's steps count.
	 * @return:	The amount of steps executed. 
	 */
	public int getSteps() {
		return this.steps_count;
	}
	
	/**
	 * This method resets this thread's steps count.
	 */
	public void resetSteps() {
		this.steps_count = 0;
	}
}
