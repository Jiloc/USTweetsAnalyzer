package com.github.jiloc.USTweetsAnalyzer;

public class Main {
	public volatile boolean keepOn = true;

	class ShutDownHandler extends Thread {

		Thread th;

		public ShutDownHandler(Thread t) {
			th = t;
		}

		@Override
		public void run() {
			System.out.println("\nControl+C caught. We clean up before quitting...");

			keepOn = false;

			while (th.isAlive()) {
				System.out.println("Main thread is still alive. Waiting...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Here we have to close db and stream
			System.out.println("OK cleaned up. Quitting...");
		}
	}

	public void doProcessing() throws InterruptedException {
		ShutDownHandler sdh = new ShutDownHandler(Thread.currentThread());
		Runtime.getRuntime().addShutdownHook(sdh);

		System.out.println("program is started");
		

	}

	public static void main(String[] args) throws InterruptedException {
		new Main().doProcessing();
	}
}
