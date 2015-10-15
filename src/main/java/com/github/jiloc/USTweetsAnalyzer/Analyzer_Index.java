/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jiloc.USTweetsAnalyzer;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 *
 * @author mahmoud
 */
public class Analyzer_Index {
    
    private IndexReader ir;
    private IndexSearcher searcher;
    private Term state_term;
    private Query q_term;
    private BooleanQuery boolq;
    private TopDocs top;
    private ScoreDoc[] hits;
    private Document document;
    private String state_field="STATE";
    private QueryParser parser;
    private String queryString="LOC:Usa";
    private int MAX_RETRIEVED=3;
    private int max;
    private Document doc;
    private Query query;
   // public static Map<String,HashSet<String>> universe;
    private String state_value;
    public static ArrayList<HashSet<String>> Ordered_STATE_LOCS;
    private HashSet<HashSet<String>> minSetC;
    
    public Analyzer_Index(Directory dir) throws IOException {
        ir = DirectoryReader.open(dir);
        searcher =  new IndexSearcher(ir);
       // universe =  new HashMap<String, HashSet<String>>();
       
        
    }
    /**
     * search for all documents that has state 
     * @param state
     * @throws IOException 
     */
    public void load(String state) throws IOException{
            state_term = new Term(state_field,state);
            q_term = new TermQuery(state_term);
            boolq = new BooleanQuery();
            boolq.add(q_term, BooleanClause.Occur.MUST);
            
            top = searcher.search(boolq,MAX_RETRIEVED);
            hits = top.scoreDocs;
          
            Ordered_STATE_LOCS = new ArrayList<HashSet<String>>();
            minSetC=new HashSet<HashSet<String>>();
            buildingMinSetCovG(buildStateUniverse());
            printMSC();
    }
    
    /**
     * close Directory Reader
     * @throws IOException
     */
    public void close() throws IOException{
        ir.close();
    }
    

    public  HashSet<String> buildStateUniverse() throws IOException{
        int i = 0;
        max=0;
        doc = null;
        HashSet<String> universe_state = new HashSet<String>();
        
        for(ScoreDoc entry:hits){
              i++;
              System.out.println("Hit "+i+"\n---");
              document = searcher.doc(entry.doc);
              state_value=document.getField(state_field).stringValue();
              
            /*  if(!(universe.containsKey(state_value)))
              {
                  universe.put(document.getField(state_field).stringValue(), new HashSet<String>());
              }*/
             
              IndexableField f =  document.getField("LOC");
              String text = f.stringValue();
             // System.out.println("Only The content of LOC is printed\n--- ");
              System.out.print("The whole content of LOC: "+"\""+ text+"\""+"\n---\n");
              //This part of code for tokenize the LOC string of the current document
              HashSet tokens = tokenize(text);
              //building the universe of all features related to the corrent object
              universe_state.addAll(tokens);
             
              
        }
       // System.out.println("The Document with the maximum LOC size ");
        
       // if(doc != null){
         //   System.out.println("STATE:"+ doc.getField(state_field).stringValue() +"\n"+"LOC:"+doc.getField("LOC").stringValue() );
        //}
        
         
         return universe_state;
    }
    public void buildingMinSetCovG(HashSet<String> universe ) throws IOException{
  
        max=0;
        doc = null;
        HashSet<String> coveredFeatures = new HashSet<String>();
    
        for(ScoreDoc entry:hits){
            document = searcher.doc(entry.doc);
            IndexableField f =  document.getField("LOC");
            String text = f.stringValue();
            HashSet tokens = tokenize(text);
            
            if(coveredFeatures.containsAll(universe)){
                break;
            }
            if(coveredFeatures.isEmpty() || !coveredFeatures.containsAll(tokens)){
                 coveredFeatures.addAll(tokens);
            }
            
            minSetC.add(tokens);
            
        }
    }
    public  HashSet tokenize(String text) throws IOException{
              StringReader reader = new StringReader(text);
              StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_41, reader);      
              CharTermAttribute charTermAttrib = tokenizer.getAttribute(CharTermAttribute.class);
              HashSet tokens = new HashSet<String>();
              tokenizer.reset();
             while (tokenizer.incrementToken()) {
                tokens.add(charTermAttrib.toString());  
              //  universe.get(state_value).add(charTermAttrib.toString());
                System.out.println(charTermAttrib.toString());
            }
            tokenizer.end();
            tokenizer.close(); 
            //toknization phase end here 
            System.out.println("---\nNumber of tokens: "+tokens.size()+"\n-----------------------------");
            //save the document with the maximum tokens size 
            if(tokens.size() > max){
                max=tokens.size();
                doc = document;
            }
           // sortLocs(tokens);
            return tokens;
            
           
    }
    public void printMSC(){
       System.out.println("Minimum set cover : "+minSetC.toString());
    }
    
    /* public void loadTokens() throws IOException, ParseException{
        Ordered_STATE_LOCS = new ArrayList<HashSet<String>>();
        parser = new QueryParser(Version.LUCENE_41,"STATE",new StandardAnalyzer(Version.LUCENE_41));
        query = parser.parse(queryString);
        top = searcher.search(query, MAX_RETRIEVED);
        hits = top.scoreDocs;
        minSetC=new HashSet<HashSet<String>>();
        System.out.println("hits lenght: "+hits.length+"\n--------------------------------");
  
        buildingMinSetCovG(buildStateUniverse());
        printMSC();
    }*/
    /*
    public void sortLocs(HashSet<String> tokens){
                   
                   int pos = ricercaBinaria(tokens.size());
                   Ordered_STATE_LOCS.add(pos, tokens);
                   
       
    }*/
    /*
    public int ricercaBinaria(int size) {
        
        if(Ordered_STATE_LOCS.size() == 0){
            return 0;
        }
       System.out.println("list not emty");
        int low = 0;
        int hi = Ordered_STATE_LOCS.size()-1;
        int mid = (hi+low)/2;
        
        int midElementSize = Ordered_STATE_LOCS.get(mid).size();
        
        while(hi==(low+1) || hi==low){
               if(size > midElementSize ){
                    low=mid+1;
               }if(size < midElementSize){
                    hi=mid-1;
               }if(size == midElementSize){
                   return mid;
               }
               mid = (hi+low)/2;
               midElementSize = Ordered_STATE_LOCS.get(mid).size();
        }
        
       if(hi==low+1){
          if(Ordered_STATE_LOCS.get(low).size()>size){
              return low;
          }
          if(Ordered_STATE_LOCS.get(hi).size()>size){
              return hi;
          }else{
              return hi+1;
          }
       }
       
       if(hi==low && Ordered_STATE_LOCS.get(hi).size()>size){
            return hi;
       }else{
            return hi+1;
       }
        
       
        
    }*/
   
   
}
