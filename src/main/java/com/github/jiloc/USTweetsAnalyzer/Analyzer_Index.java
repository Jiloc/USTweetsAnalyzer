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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
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
    private HashSet<Document> universeDocs;
    private String state_field="STATE";
   // private QueryParser parser;
   // private String queryString="LOC:Usa";
    private int MAX_RETRIEVED=10;
    //private int max;
    //private Document doc;
    //private Query query;
   // public static Map<String,HashSet<String>> universe;
   // private String state_value;
    public static ArrayList<HashSet<String>> Ordered_STATE_LOCS;

 
    private HashSet<String> mSC;

    public HashSet<String> getMSC() {
        return mSC;
    }
    private HashMap<String, HashSet<Document>> tokenDocuments;
    private TreeMap<String, HashSet<Document>> sortedTokenDocuments;
    
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
            tokenDocuments = new HashMap<String,HashSet<Document>>();
            top = searcher.search(boolq,MAX_RETRIEVED);
            hits = top.scoreDocs;
            mSC = buildingMSCG();
           
           // minSetC=new HashSet<HashSet<String>>();
            //buildingMinSetCovG(buildStateUniverse());
            //printMSC();
    }
    public HashSet<String> buildingMSCG() throws IOException{
        universeDocs=new HashSet<Document>();
        for(ScoreDoc entry:hits){
            document = searcher.doc(entry.doc);
            universeDocs.add(document);
            IndexableField f =  document.getField("LOC");
            String text = f.stringValue();
            updateTokensToDocs(tokenizeText(text));
        }
       // System.out.println(tokenDocuments.keySet().toString());
       // print(tokenDocuments);
        sortedTokenDocuments = sortByValue(tokenDocuments);
        //System.out.println("control "+sortedtokenDocuments.values().size());
   //   print(sortedTokenDocuments);
      // System.out.println(sortedMap);
        
        HashSet<String> msc =minSetCover(sortedTokenDocuments);
        
        if(msc.isEmpty()){
            System.out.println("No match");
        }else{
           //  System.out.println("minimum set cover: "+msc.toString());
        }
        return msc;
       
    }
    public HashSet minSetCover(TreeMap<String,HashSet<Document>> sortedTokenDocuments){
        HashSet msc = new HashSet<String>();
        if(sortedTokenDocuments.size() > 1){
        search1:{
        for(Entry<String,HashSet<Document>> t1 :  sortedTokenDocuments.entrySet()){
            if(!universeDocs.isEmpty()){
               // System.out.println("t1: "+t1.getKey());
            HashSet<Document> intersect1 = t1.getValue();
            intersect1.retainAll(universeDocs);
            int s1 = intersect1.size();
           // System.out.println("intersect1: "+intersect1.size());
            if(s1 > 0){
            search2:{
            for(Entry<String,HashSet<Document>> t2 :  sortedTokenDocuments.entrySet()){
            
                if(!(t2.getKey().equals(t1.getKey()))){
                       //   System.out.println("t2: "+t2.getKey());
                    HashSet<Document> intersect2 = t2.getValue();
                    intersect2.retainAll(universeDocs);
                    int s2 = intersect2.size();
                  //  System.out.println("intersect2: "+intersect2.size());
                   
                    if(s1 >= s2){
                        msc.add(t1.getKey());
                      // System.out.println("msc: "+msc.toString());
                        sortedTokenDocuments.remove(t1.getKey());
                        
                        universeDocs.removeAll(intersect1);
                        break search2;
                    }else{
                        TreeMap<String,HashSet<Document>> mp = sortedTokenDocuments;
                        mp.remove(t1.getKey());
                        minSetCover(mp);
                    }
                   
                }
              }
            }
            }else{
                sortedTokenDocuments.remove(t1.getKey());
            }
            }else{
                  break search1;
              }
           
            }
          }
        }else if (sortedTokenDocuments.size() == 1){
            for(Entry<String,HashSet<Document>> t :  sortedTokenDocuments.entrySet()){
                msc.add(t.getKey());
            }
        }
        return msc;
    }
    /**
     * Take in input a string and tokenize it into an ArrayList of strings(tokens) which is returned 
     * @param text - a string that has to be splited 
     * @return an ArrayList of strings 
     * @throws IOException 
     */
    public ArrayList<String> tokenizeText(String text) throws IOException{      
              StringReader reader = new StringReader(text);
              StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_41, reader);      
              CharTermAttribute charTermAttrib = tokenizer.getAttribute(CharTermAttribute.class);
              tokenizer.reset();
              ArrayList<String> tokens = new ArrayList<String>();
              
            while (tokenizer.incrementToken()) {
                tokens.add(charTermAttrib.toString());

            }
            tokenizer.end();
            tokenizer.close(); 
          //  System.out.println("tokenizetext: "+tokens.toString());
           return tokens;
      
    }
    /**
     * Update tokenDocuments field 
     * @param tokens 
     */
   public void updateTokensToDocs(ArrayList<String> tokens){
        
            for(String t : tokens){   
                if(tokenDocuments.containsKey(t)){
                   tokenDocuments.get(t).add(document);   
                }else{
                    HashSet s = new HashSet<String>();
                    s.add(document);
                    tokenDocuments.put(t,s);
                }
                // System.out.println("updateTokensToDocs: ");
               // print(tokenDocuments);
            }            
     }
  /**  public TreeMap<String, Integer> mapSizeDocTotoken(){
       HashMap<String, Integer> map = new HashMap<String, Integer>();
       for(String t : tokenDocuments.keySet()){
            map.put(t,tokenDocuments.get(t).size());
       }
            TreeMap tokensOrdBySize = SortByValue(map);
            return tokensOrdBySize;
       
   }*/
    
    public  TreeMap<String, HashSet<Document>> sortByValue(HashMap<String, HashSet<Document>> unsortedMap) {
	TreeMap<String, HashSet<Document>> sortedMap = new TreeMap<String, HashSet<Document>>(new ValueComparator(unsortedMap));
       // System.out.println(unsortedMap.keySet().toString());
	sortedMap.putAll(unsortedMap);
	return sortedMap;
    }
  
    /**
     * Returns a set of tokens that cover all documents computed by  a minimum set cover greedy algorithm 
     * @return a set of tokens that cover all documents 
     */
  /*  public Set<String> getMinimumSetCover(){
        return tokenDocuments.keySet();
    }
    */
    /*
    public HashMap<String, HashSet<Document>> getTokenDocuments() {
        return tokenDocuments;
    }*/

    /**
     * close Directory Reader
     * @throws IOException
     */
    public void close() throws IOException{
        ir.close();
    }
    
    public void print(Map<String,HashSet<Document>> m){
       // System.out.println(m.toString());
        for(Entry<String,HashSet<Document>> t :  m.entrySet()){
            System.out.println(t.getKey()+": "+t.getValue().size()+"------\n");
            
            
            
            
           /* for(Document d : s){
                System.out.print(d.getFields().toString());
            }*/
        }
    }
    
 
}
