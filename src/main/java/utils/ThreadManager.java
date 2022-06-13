package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.ProgressBar;
import yusaf.main.ui.MainFrame;

/**
 * Might be overkill.<br>
 * Executes operations in another thread and also provide a way to correctly
 * stop threads.<br>
 * This also uses a single ExecutorService made of 4 threads. So if there are
 * multiple calls to
 * {@link #runTasks(threadList, interval, murderableThreadsList)}, new threads
 * will run after previous threads.
 * 
 * @author yali
 *
 */
public class ThreadManager {
	/*
	 * Local thread list that stores all runners.
	 */
	private static List<Thread> runners = new ArrayList<>();
	private static ExecutorService service = Executors.newFixedThreadPool(12);

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
	public static void runTasks(List<Thread> threadList, int interval, List<Thread> murderableThreadsList) {
		Thread runner = new Thread() {
			public void run() {
				List<CompletableFuture<?>> futures = new ArrayList<>();
				double singleItemWeight = 1.0 / threadList.size();
				ProgressBar progressBar = MainFrame.getProgressBar();
				progressBar.setProgress(0);
				progressBar.setVisible(true);
				System.out.println("Starting all threads");
				threadList.forEach(thread -> {
					if (Thread.currentThread().isInterrupted()) {
						return;
					}
					CompletableFuture<Void> future = CompletableFuture.runAsync(thread, service);
					future.whenCompleteAsync((o, throwable) -> {
						if (throwable != null) {
							return;
						}
						progressBar.setProgress(singleItemWeight + progressBar.getProgress());
					});
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// As this clears interrupted status of this runner thread,
						// we need to just return from here after interrupting it again.
						Thread.currentThread().interrupt();
						return;
					}
					futures.add(future);
				});
				CompletableFuture<?> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
				all.thenAccept((unknownVariable) -> {
					System.out.println("All ended now murdering threads");
					murderableThreadsList.removeAll(threadList);
					progressBar.setVisible(false);
				});
			};
		};
		runner.start();
		runners.add(runner);
	}

	/**
	 * Stops inner runner threads.
	 */
	public static void shutdown() {
		System.out.println("Shutdown requested");
		service.shutdown();
		runners.forEach(runner -> {
			runner.interrupt();
		});
	}
}
