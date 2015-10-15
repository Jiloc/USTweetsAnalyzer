package com.github.jiloc.USTweetsAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Main {

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
                        System.out.println("begin loading index");
             
                    Directory dir = null;
                        try {
                            dir = SimpleFSDirectory.open(new File("tweet_index"));
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        TopFeatures topf = new TopFeatures(dir);
                        topf.printTopFeatures();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
			System.out.println("Cleaned up. Quitting...");
		}
	}

	public void doProcessing() throws InterruptedException {
		Streamer stream = null;
		try {
			stream = new Streamer();
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
                
                 
	}
}
