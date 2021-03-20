package structures;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Corpus {
	LinkedList<ReviewDoc> m_collection; // a list of review documents
	
	HashMap<String, Integer> m_dictionary; // dictionary of observed words in this corpus, word -> frequency
										   // you can use this structure to prepare the curve of Zipf's law

	HashMap<String, Integer> f_dictionary; // how many files that contains the keywords token, file frequency,
	
	public Corpus() {
		m_collection = new LinkedList<ReviewDoc>();
		m_dictionary = new HashMap<String, Integer>();
		f_dictionary = new HashMap<String, Integer>();
	}
	
	public int getCorpusSize() {
		return m_collection.size();
	}
	
	public int getDictionarySize() {
		return m_dictionary.size();	
	}
	
	public void addDoc(ReviewDoc doc) {
		m_collection.add(doc);
		
		/**
		 * INSTRUCTOR'S NOTE: based on the BoW representation of this document, you can update the m_dictionary content
		 * to maintain some global statistics here 
		 */
		// get all tokens from doc
		// check existance in the m_dictionary
		// if exist, add the new count, if not, add the new token, and its count

//		System.out.println(doc.m_BoW);
		for (String token : doc.m_BoW.keySet()){
			if (m_dictionary.containsKey(token)){
				m_dictionary.put(token,m_dictionary.get(token) + doc.counts(token));
			}else {
				m_dictionary.put(token, doc.counts(token));
			}
			//System.out.println(m_dictionary);
			// Update the file numbers that contains the tokens
			if(f_dictionary.containsKey(token)){
				f_dictionary.put(token,f_dictionary.get(token) + 1);
			}else {
				f_dictionary.put(token,1);
			}
		}
	}
	
	public ReviewDoc getDoc(int index) {
		if (index < getCorpusSize())
			return m_collection.get(index);
		else
			return null;
	}
	
	public int getWordCount(String term) {
		if (m_dictionary.containsKey(term))
			return m_dictionary.get(term);
		else
			return 0;
	}

	public void WriteCSV(String outputfname){
		//TODO add code to write the dictionary to csv file
		try {
			FileWriter csvWriter = new FileWriter(outputfname);
			csvWriter.write("token,token_count,file_count\n");
			// loop over the dictionary and write the dictionary
			for (String token : m_dictionary.keySet()){
				csvWriter.write(String.format("%s,%d,%d\n",token,m_dictionary.get(token),f_dictionary.get(token)));
			}
			csvWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}

	}
	void setWordCount(String term, int count) {
		m_dictionary.put(term, count);
	}
}
