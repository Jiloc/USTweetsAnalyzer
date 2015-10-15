/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jiloc.USTweetsAnalyzer;

/**
 *
 * @author mahmoud
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.lucene.store.Directory;

public class TopFeatures {
    Analyzer_Index analyzer;
    HashMap<String, HashSet<String>> mscOfStates;
    HashMap<String, HashSet<String>> topFeatures;
    HashSet<String> states = new  HashSet<String>();

    public TopFeatures(Directory dir) throws IOException {
        analyzer = new Analyzer_Index(dir) ;
        mscOfStates = new HashMap<String, HashSet<String>>();
        topFeatures = topFeatures = new HashMap<String, HashSet<String>>();
        this.states=Store.states;
        computeMscforAllStates();
        computeTopFeaturesforAllStates();
    }
   public void computeMscforAllStates() throws IOException{
       for(String state: states){
           analyzer.load(state);
           mscOfStates.put(state,analyzer.getMSC());
       }
       analyzer.close();
   }
    public void computeTopFeaturesforAllStates() throws IOException{
        HashSet<String> set1 = new HashSet<String>();
        HashSet<String> set2 = new HashSet<String>();
       for(String state1: states){
           if(mscOfStates.containsKey(state1)){
               set1 = mscOfStates.get(state1);
            for(String state2: states){
                if(!(state1.equals(state2))){
                    if(mscOfStates.containsKey(state2)){
                        set2 = mscOfStates.get(state2);
                        set1.removeAll(set2);
                    }
                }
            }
            topFeatures.put(state1, set1);
           }
       }
   }
    public void printTopFeatures(){
        System.out.println("Top Features for each state");
        for(String state: topFeatures.keySet()){
            System.out.println(state+": "+topFeatures.get(state).toString());
        }
    }
}

