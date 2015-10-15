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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.lucene.document.Document;

/**
 *
 * @author mahmoud
 */
class ValueComparator implements Comparator {
 
	HashMap<String, HashSet<Document>> map;
 
	public ValueComparator(HashMap<String, HashSet<Document>> map) {
		this.map = map;
	}
 
	public int compare(Object keyA, Object keyB) {
		HashSet<Document> s1 = (HashSet<Document>) map.get(keyA);
		HashSet<Document> s2 = (HashSet<Document>) map.get(keyB);
                Comparable valueA = (Comparable) s1.size();
                Comparable valueB = (Comparable) s2.size();
                if(valueB.compareTo(valueA) == 0){
                    return -1;
                }else{
		return valueB.compareTo(valueA);
                }
	}
}
