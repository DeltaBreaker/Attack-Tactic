package io.itch.deltabreaker.multiprocessing;

import java.util.ArrayList;

import io.itch.deltabreaker.core.PerformanceManager;
import io.itch.deltabreaker.core.Startup;

public class TaskThread implements Runnable {

	private static final long SLEEP_TIME = 250L;

	private static ArrayList<WorkerTask> tasks = new ArrayList<>();

	public PerformanceManager performanceManager;
	public boolean running = true;

	public TaskThread(PerformanceManager performanceManager) {
		this.performanceManager = performanceManager;
	}

	@Override
	public void run() {
		double time = Startup.getTime();
		double unprocessed = 0;

		while (running) {
			if (tasks.size() > 0) {
				double time2 = Startup.getTime();
				double passed = time2 - time;
				time = time2;
				unprocessed += passed;

				while (unprocessed >= Startup.frameCap) {
					unprocessed -= Startup.frameCap;
					process();
				}
			} else {
				try {
					Thread.sleep(SLEEP_TIME);
					time = Startup.getTime();
					unprocessed = 0;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void process() {
		long time = System.nanoTime();

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).isFinished()) {
				tasks.remove(i);
				i--;
			}
		}
		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).tick();
		}

		performanceManager.effectDesyncUpdates++;
		performanceManager.effectDesyncUpdateTime += (System.nanoTime() - time) / 1000000.0;
	}

	public static void process(WorkerTask task) {
		tasks.add(task);
	}

	public static int taskCount() {
		return tasks.size();
	}

}
