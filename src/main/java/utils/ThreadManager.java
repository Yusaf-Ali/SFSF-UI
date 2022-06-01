package utils;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager {
	/*
	 * Local thread list that stores all runners.
	 */
	private static List<Thread> runners = new ArrayList<>();

	/**
	 * Create list of threads and use this method to start running them at a
	 * specific interval.<br>
	 * Performs operation in another thread, this method is non-blocking.<br>
	 * threadList items should be registered in MainFrame.java threadList, although
	 * calling {@link #shutdown()} will handle interruption for them anyway.
	 * 
	 * @param threadList List of threads to be executed
	 * @param interval   Duration of gaps between running next thread
	 */
	public static void runTasks(List<Thread> threadList, int interval) {
		Thread runner = new Thread() {
			public void run() {
				threadList.forEach(thread -> {
					if (Thread.currentThread().isInterrupted()) {
						return;
					}
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// As this clears interrupted status of this runner thread,
						// we need to just return from here after interrupting it again.
						Thread.currentThread().interrupt();
						return;
					}
					thread.start();
				});
			};
		};
		runners.add(runner);
		runner.start();
	}

	/**
	 * Stops inner runner threads.
	 */
	public static void shutdown() {
		System.out.println("Shutdown requested");
		runners.forEach(runner -> {
			runner.interrupt();
		});
	}
}
