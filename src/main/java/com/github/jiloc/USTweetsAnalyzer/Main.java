package com.github.jiloc.USTweetsAnalyzer;

import java.io.IOException;
import org.apache.lucene.store.Directory;

public class Main {
    static Directory dir;
	class ShutDownHandler extends Thread {

		Thread th;
		Streamer stream;
                
		public ShutDownHandler(Thread t, Streamer stream) {
			this.th = t;
			this.stream = stream;
		}

		@Override
		public void run() {
			System.out.println("\nControl+C caught. We clean up before quitting...");

			while (th.isAlive()) {
				System.out.println("Main thread is still alive. Waiting...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// close db and stream
			this.stream.stopListening();
			System.out.println("Cleaned up. Quitting...");
		}
	}

	public void doProcessing() throws InterruptedException {
		Streamer stream = null;
		try {
			stream = new Streamer();
                        dir = stream.getDir();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stream.startListening();
		ShutDownHandler sdh = new ShutDownHandler(Thread.currentThread(), stream);
		Runtime.getRuntime().addShutdownHook(sdh);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		new Main().doProcessing();
                
                 TopFeatures topf = new TopFeatures(dir);
                 topf.printTopFeatures();
	}
}
