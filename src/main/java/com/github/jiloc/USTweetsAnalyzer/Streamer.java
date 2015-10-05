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

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;


public class Streamer {

	private TwitterStream twitterStream;
	private Geolocalizator geoLocalizator;
	// private static String[] query = new String[]{"Alaska"};
	// private static String[] language =  new String[]{"it"};
	// private static long userId=3007554729l;
	private static double[][] locations =  new double[][]{
		{-125.32203125,24.6085180263}, {-66.806875,49.0553985878}, // Central America
		{-163.730232375,17.3129861307}, {-150.918203125,24.4630715246}, // Hawaii
		{-179.902107375, 49.5693693495}, {-119.453359375, 71.5805021834} // Alaska
	};
	
	public Streamer(){
		this.twitterStream = new TwitterStreamFactory().getInstance();
		this.twitterStream.addListener(listener);
		this.geoLocalizator = new Geolocalizator(
			"src/main/resources/tl_2014_us_state/tl_2014_us_state.shp");
	}
	
	private StatusListener listener = new StatusListener(){
		public void onStatus(Status status) {
			if(status.getGeoLocation() != null){
				System.out.println(
						"getUserLocation: " + status.getUser().getLocation() +
						" getGeoLocation: " + status.getGeoLocation() + 
						" geoLocalizator: " + geoLocalizator.getStateFromCoordinates(
							status.getGeoLocation().getLongitude(), 
							status.getGeoLocation().getLatitude()));
			}
		}
		
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
		public void onException(Exception ex) { ex.printStackTrace(); }
		
		public void onScrubGeo(long arg0, long arg1) {
			// TODO Auto-generated method stub
		}
		public void onStallWarning(StallWarning arg0) {
			// TODO Auto-generated method stub	
		}
	};
	
	public void startListening(){
		// sample() method internally creates a thread which manipulates TwitterStream 
		// and calls these adequate listener methods continuously.
		this.twitterStream.sample(); //1 % of the public stream 
		
		FilterQuery fq = new FilterQuery();// the filter object
		fq.locations(locations);
		// fq.language(language);		
		// fq.track(query); //the query to track from the stream 
		this.twitterStream.filter(fq);
	}
	
	public static void main(String[] args) throws TwitterException, IOException{
		
		Streamer stream = new Streamer();
		stream.startListening();
		
	}
}
