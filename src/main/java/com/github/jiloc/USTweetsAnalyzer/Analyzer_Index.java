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
import java.util.List;
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
    
    IndexReader ir;
    IndexSearcher searcher;
    Term state_term;
    Query q_term;
    BooleanQuery boolq;
    TopDocs top;
    ScoreDoc[] hits;
    Document document;
    String state_field="STATE";
    QueryParser parser;
    String queryString="LOC:Usa";
    private final int MAX_RETRIEVED;
    private ArrayList<HashSet<String>> Ordered_STATE_LOCS;
    private Query query;
    private int max;
    private Document doc;
    private String state_value;
    
    public Analyzer_Index(Directory dir,int n) throws IOException {
        this.MAX_RETRIEVED = n;
        ir = DirectoryReader.open(dir);
        searcher =  new IndexSearcher(ir);
      
        
    }
    /**
     * search for all documents that has state 
     * Use BooleanQuery 
     * @param state
     * @throws IOException 
     */
    public void load(String state) throws IOException{
            state_term = new Term(state_field,state);
            q_term = new TermQuery(state_term);
            boolq = new BooleanQuery();
            boolq.add(q_term, BooleanClause.Occur.MUST);
            
            top = searcher.search(boolq,3);
            hits = top.scoreDocs;
              int i = 0;
              
        for(ScoreDoc entry:hits){
              i++;
              System.out.println("\nDocument Details "+i);
              document = searcher.doc(entry.doc);
            /*   System.out.println("Field"+i+": "+document.get("LOC"))*/
            List<IndexableField> fields = document.getFields();
            for(IndexableField fld:fields){  
                System.out.println(fld.name()+": "+fld.stringValue());
                
            }
            
        }
        
    }
    
    /**
     * close Directory Reader
     * @throws IOException
     */
    public void close() throws IOException{
        ir.close();
    }
    /**
     * Use QueryParser, search for all documents that match a queryString in the field LOC only.
     * Since it is the only field that can be tokenized and QueryParser works only with tokenized fields 
     * and in the end tokenize the field LOC of each document matched.     
     * @throws IOException
     * @throws ParseException 
     */
   /* public void loadTokens() throws IOException, ParseException{
      
        parser = new QueryParser(Version.LUCENE_41,"STATE",new StandardAnalyzer(Version.LUCENE_41));
        Query query = parser.parse(queryString);
        top = searcher.search(query, MAX_RETRIEVED);
        hits = top.scoreDocs;
        System.out.println("hits lenght: "+hits.length+"\n--------------------------------");
        int i = 0;
        int max=0;
        Document doc = null;
        for(ScoreDoc entry:hits){
              i++;
              System.out.println("Hit "+i+"\n---");
              document = searcher.doc(entry.doc);
              IndexableField f =  document.getField("LOC");
              String text = f.stringValue();
              System.out.println("Only The content of LOC is printed\n--- ");
              System.out.print("The whole content of LOC: "+"\""+ text+"\""+"\n---\n");
              //This part of code for tokenize the LOC string of the current document 
              StringReader reader = new StringReader(text);
              StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_41, reader);      
              CharTermAttribute charTermAttrib = tokenizer.getAttribute(CharTermAttribute.class);
              List<String> tokens = new ArrayList<String>();
              tokenizer.reset();
             while (tokenizer.incrementToken()) {
                tokens.add(charTermAttrib.toString());
                System.out.println(charTermAttrib.toString());
            }
            tokenizer.end();
            tokenizer.close(); 
            //toknization phase end here 
            System.out.println("---\nNumber of tokens: "+tokens.size()+"\n-----------------------------");
            //save the document with the maximum tokens size 
            if(tokens.size() > max){
                doc = document;
            }
        }
        System.out.println("The Document with the maximum LOC size ");
        System.out.println("STATE:"+ doc.getField(state_field).stringValue() +"\n"+"LOC:"+doc.getField("LOC").stringValue() );
        

    }*/
    //the modified loadToken() and others news methods
    public void loadTokens() throws IOException, ParseException{
         Ordered_STATE_LOCS = new ArrayList<HashSet<String>>();
        parser = new QueryParser(Version.LUCENE_41,"STATE",new StandardAnalyzer(Version.LUCENE_41));
        query = parser.parse(queryString);
        top = searcher.search(query, MAX_RETRIEVED);
        hits = top.scoreDocs;
        System.out.println("hits lenght: "+hits.length+"\n--------------------------------");
        
        int i = 0;
        max=0;
        doc = null;
        
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
              System.out.println("Only The content of LOC is printed\n--- ");
              System.out.print("The whole content of LOC: "+"\""+ text+"\""+"\n---\n");
              //This part of code for tokenize the LOC string of the current document 
              tokenize(text);
        }
        System.out.println("The Document with the maximum LOC size ");
        
        if(doc != null){
            System.out.println("STATE:"+ doc.getField(state_field).stringValue() +"\n"+"LOC:"+doc.getField("LOC").stringValue() );
        }
        
         System.out.println("ordered list: "+Ordered_STATE_LOCS.toString());

    }
    
    public  void tokenize(String text) throws IOException{
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
            sortLocs(tokens);
            
           
    }
    
    public void sortLocs(HashSet<String> tokens){
                   
                   int pos = ricercaBinaria(tokens.size());
                   Ordered_STATE_LOCS.add(pos, tokens);
                   
       
    }
    
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
        
       
        
    }
   
    
    
    
}
