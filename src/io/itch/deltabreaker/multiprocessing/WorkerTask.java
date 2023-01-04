package io.itch.deltabreaker.multiprocessing;

public abstract class WorkerTask {

	private boolean isFinished = false;
	
	public abstract void tick();
	
	public final void finish() {
		isFinished = true;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
}
