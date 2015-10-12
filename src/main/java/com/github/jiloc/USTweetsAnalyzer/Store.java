/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jiloc.USTweetsAnalyzer;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;

/**
 *
 * @author mahmoud
 */
public class Store {
	private final Directory dir;
	private final Analyzer analyzer;
	private final IndexWriterConfig cfg;
	private final IndexWriter writer;
	private final Document doc;
	private final StringField s;
	private final TextField l;

	public Store() throws IOException {
		// The process of writing indexing
		dir = new SimpleFSDirectory(new File("tweet_index"));
		analyzer = new StandardAnalyzer(LUCENE_41);
		cfg = new IndexWriterConfig(LUCENE_41, analyzer);
		writer = new IndexWriter(dir, cfg);
		// a document contains a list of field(s)
		doc = new Document();

		s = new StringField("STATE", "", Field.Store.YES);
		l = new TextField("LOC", "", Field.Store.YES);

		doc.add(s);
		doc.add(l);
	}

	public void Writing_Index(String state, String loc) throws IOException {
		s.setStringValue(state);
		l.setStringValue(loc);
		writer.addDocument(doc);
	}
	
	public void commit() {
		try {
			writer.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
