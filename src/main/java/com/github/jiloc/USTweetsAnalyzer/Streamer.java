/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mahmoud
 */
package com.github.jiloc.USTweetsAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.store.Directory;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class Streamer {
	private final String GEOLOCALIZATOR_PATH = "src/main/resources/tl_2014_us_state/tl_2014_us_state.shp";
	private final int MAX_TWEETS_IN_MEMORY = 10;
	private static double[][] LOCATIONS = new double[][] { 
		{ -125.32203125, 24.6085180263 }, { -66.806875, 49.0553985878 }, // Central America
		{ -163.730232375, 17.3129861307 }, { -150.918203125, 24.4630715246 }, // Hawaii
		{ -179.902107375, 49.5693693495 }, { -119.453359375, 71.5805021834 } // Alaska
	};	
	private long receivedTweets = 0;
	private final TwitterStream twitterStream;
	private final Geolocalizator geoLocalizator;
	private final Store storage;
        private  Directory dir;
	// private static String[] query = new String[]{"Alaska"};
	// private static String[] language = new String[]{"it"};
	// private static long userId=3007554729l;

    public Directory getDir() {
        return dir;
    }
	
	public Streamer() throws IOException {
		this.twitterStream = new TwitterStreamFactory().getInstance();
		this.geoLocalizator = new Geolocalizator(this.GEOLOCALIZATOR_PATH);
		this.storage = new Store();
                Directory dir = storage.getDir();
	}

	private StatusListener listener = new StatusListener() {
		
		public void onStatus(Status status) {
			if (status.getGeoLocation() != null) {
				ArrayList<String> state = geoLocalizator.getStateFromCoordinates(
					status.getGeoLocation().getLongitude(),
					status.getGeoLocation().getLatitude()
				);

				if (state != null && status.getUser().getLocation() != null) {
					this.saveEntry(state.get(0), status.getUser().getLocation());
				}
			}
		}
		
		public void saveEntry(String state, String userLocation){
			try {
				storage.Writing_Index(state, userLocation);
				receivedTweets++;
				if (receivedTweets % MAX_TWEETS_IN_MEMORY == 0){
					System.out.println(receivedTweets + " tweets received");
					storage.commit();
				}
			} catch (IOException ex) {
				Logger.getLogger(Streamer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}

		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			System.out.println("Maximum number of statuses reached.");
			stopListening();
		}

		public void onException(Exception ex) {
			ex.printStackTrace();
			stopListening();
		}

		public void onScrubGeo(long arg0, long arg1) {
			// TODO Auto-generated method stub
		}

		public void onStallWarning(StallWarning arg0) {
			// TODO Auto-generated method stub
		}
	};

	public void startListening() {
		// sample() method internally creates a thread which manipulates
		// TwitterStream
		// and calls these adequate listener methods continuously.
		this.twitterStream.addListener(listener);
		System.out.println("Opening stream. Start listening..");
		//this.twitterStream.sample(); // 1 % of the public stream

		FilterQuery fq = new FilterQuery();// the filter object
		fq.locations(LOCATIONS);
		// fq.language(language);
		// fq.track(query); //the query to track from the stream
		this.twitterStream.filter(fq);
	}
	
	public void stopListening() {
		System.out.println("Closing stream.");
        this.twitterStream.clearListeners();
        this.twitterStream.cleanUp();
        System.out.println("Closing storage.");
        this.storage.close();
	}

	public static void main(String[] args) throws TwitterException, IOException {
		Streamer stream = new Streamer();
		stream.startListening();
	}
}
