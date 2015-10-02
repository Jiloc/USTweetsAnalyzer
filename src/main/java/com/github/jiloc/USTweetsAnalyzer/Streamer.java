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

   
	
        private Store store;
        
	public Streamer() throws IOException{
		this.twitterStream = new TwitterStreamFactory().getInstance();
		this.twitterStream.addListener(listener);
		this.geoLocalizator = new Geolocalizator(
			"src/main/resources/tl_2014_us_state/tl_2014_us_state.shp");
                store = new Store();
	}
	
	private StatusListener listener = new StatusListener(){
		public void onStatus(Status status)  {
                    
                        //ArrayList<String> state = geoLocalizator.getStateFromCoordinates(status.getGeoLocation().getLongitude(), 
                                                                               //status.getGeoLocation().getLatitude());
                         //String loc = status.getUser().getLocation();
                      
			if(status.getGeoLocation() != null){
                              ArrayList<String> state = geoLocalizator.getStateFromCoordinates(
                                    status.getGeoLocation().getLongitude(),
                                    status.getGeoLocation().getLatitude());
                                if(state != null && state.get(0) != null && status.getUser().getLocation()!= null){
				System.out.println(
						"getUserLocation: " + status.getUser().getLocation() + // LOC
						" getGeoLocation: " + status.getGeoLocation() + 
						" geoLocalizator: " + geoLocalizator.getStateFromCoordinates(
							status.getGeoLocation().getLongitude(), 
							status.getGeoLocation().getLatitude()));
                                
                          
                                
                                 
                             
                                    
                                    
                                    try {
                                        store.Writing_Index(state.get(0), status.getUser().getLocation());
                                    } catch (IOException ex) {
                                        Logger.getLogger(Streamer.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    
                              }
                                
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
		this.twitterStream.sample(); //questo per il 1 % per the public stream 
		
		FilterQuery fq = new FilterQuery();// the filter object
		// fq.language(language);  // we use this method to select the language of tracked stream 
		fq.locations(locations); //we need also the next line
		// fq.track(query); //the query to track from the stream 
		this.twitterStream.filter(fq);
                 
	}
        
	public Store getStore() {
           return store;
        }
        
	public static void main(String[] args) throws TwitterException, IOException{
		
		Streamer stream = new Streamer();
		stream.startListening();
               
		//Example Rest--------
		/*Twitter twitter = new TwitterFactory(cfg.build()).getInstance(); 
                
		long [] ids =twitter.getFriendsIDs(userId, -1).getIDs();
		List<User> friends = twitter.lookupUsers(ids);
		//System.out.println(twi.getName());
                System.out.println("Showing friend's of the following user");
		System.out.println(userId+": "+twitter.showUser(userId).getScreenName());
                 System.out.println("The list of friends:");
		for(User e : friends){
			System.out.println(e.getId()+": "+e.getScreenName());
		}*/
	//	for (int i = 0 ; i < ids.length; i++){
			//System.out.println(ids[i]);
		//	friends.size()
	//	}
		
	}
}
