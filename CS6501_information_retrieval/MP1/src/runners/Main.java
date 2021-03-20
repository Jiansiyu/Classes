package runners;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import edu.virginia.cs.analyzer.DocAnalyzer;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.SearchResult;
import edu.virginia.cs.index.Searcher;
import edu.virginia.cs.searcher.DocSearcher;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import structures.ReviewDoc;

public class Main {

	//The main entrance to test various functions 
	public static void main(String[] args) {
//		Q2_main(args);
		Q23_main(args);
/*		try {

			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/yelp/60", ".json");
			//analyzer.getCorpus().WriteCSV("outCount60.csv");

			System.out.format("Finish DocAnalyzer\n\n\n");


			String query = "general chicken";

			System.out.format("Brute-force index ::\n");
			//using brute-force strategy to scan through the whole corpus
			DocSearcher bruteforceSearcher = new DocSearcher(analyzer.getCorpus(), "data/models/en-token.bin");
			bruteforceSearcher.search(query);
			System.out.format("Finish Brute-force index\n\n");

			System.out.format("Start Invert index \n");
			//create inverted index
			Indexer.index("data/indices", analyzer.getCorpus());
			
			//search in the inverted index
			Searcher indexSearcher = new Searcher("data/indices");
			indexSearcher.search(query);
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/

		/*		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("./data/indices")));
			Terms terms = MultiFields.getTerms(reader,"content"); //get reference to all the indexed terms in the content field
			TermsEnum termsEnum = terms.iterator(null);
			while (termsEnum.next()!=null){//iterate through all terms
				Term t = new Term("content",termsEnum.term());//map it to the corresponding field
				System.out.format("%s\t%d\t%d\n", t, termsEnum.docFreq(), reader.totalTermFreq(t)); //print term text, DF and TTF
			}

		}catch (IOException e){
			e.printStackTrace();
		}*/
	}

	public static void loadDoc(String[] args){
		try {

			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/yelp/60", ".json");
			System.out.format("Finish DocAnalyzer\n\n\n");
			System.out.format("Start Invert index \n");
			//create inverted index
			Indexer.index("data/indices", analyzer.getCorpus());

		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void indexer(String query){

		String seachEngine = "invertIndex";
		if (seachEngine.equals("invertIndex")){
			Searcher indexSearcher = new Searcher("data/indices");
			indexSearcher.search(query);
		}
	}

	public static void Q2_main(String[] args){
		//get the time
		try {
			//it will calculate the DF and TTF when loading
			// start the profile for the brute-force approach

			long currentTime = System.currentTimeMillis();
			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/yelp/60", ".json");
			long timeElapsed = System.currentTimeMillis() - currentTime;

			System.out.format("\n\n********************************************\n");
			System.out.format("\t[Info] Q1 TTF & DF profile in %.3f seconds\n", timeElapsed/1000.0);
			System.out.format("\n********************************************\n");
			analyzer.getCorpus().WriteCSV("outCount60.csv");
			System.out.format("Finish DocAnalyzer\n");


			System.out.format("Start Invert index \n");
			//create inverted index

			currentTime = System.currentTimeMillis();
			// start the index for the Lucene index approach
			Indexer.index("data/indices", analyzer.getCorpus());
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("data/indices")));
			Terms terms = MultiFields.getTerms(reader,"content"); //get reference to all the indexed terms in the content field
			TermsEnum termsEnum = terms.iterator(null);
			while (termsEnum.next()!=null){//iterate through all terms
				Term t = new Term("content",termsEnum.term());//map it to the corresponding field
//				System.out.format("%s\t%d\t%d\n", t, termsEnum.docFreq(), reader.totalTermFreq(t)); //print term text, DF and TTF
			}
			timeElapsed = System.currentTimeMillis() - currentTime;

			// end of Lucene profile
			System.out.format("\n\n********************************************\n");
			System.out.format("\t[Info] Lucene TTF & DF profile in %.3f seconds\n", timeElapsed/1000.0);
			System.out.format("\n********************************************\n");

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void Q23_main(String[] arg){
		///Question2.2 compare the time difference of brute-force and the Lucene search  method
		String[] queryList={
				"general chicken",
				"fried chicken",
				"BBQ sandwiches",
				"mashed potatoes",
				"Grilled Shrimp Salad",
				"lamb Shank",
				"Pepperoni pizza",
				"brussel sprout salad",
				"FRIENDLY STAFF",
				"Grilled Cheese"};

		try {

			long startTime = System.currentTimeMillis();
			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/yelp/60", ".json");
			analyzer.getCorpus().WriteCSV("outCount60.csv");


			long startBrutefoceIndex = System.currentTimeMillis();
			//using brute-force strategy to scan through the whole corpus
			long startBruteForce = System.currentTimeMillis();
			DocSearcher bruteforceSearcher = new DocSearcher(analyzer.getCorpus(), "data/models/en-token.bin");

			HashMap<String, Integer> BruteForceRes = new HashMap<>();
			for (String currquery : queryList){
				ReviewDoc[] result = bruteforceSearcher.search(currquery);
				System.out.format("\t current search [%s] -> %d\n",currquery,result.length);
				BruteForceRes.put(currquery,result.length);
			}
			long endBruteForce = System.currentTimeMillis();

			long startLucene = System.currentTimeMillis();
			//create inverted index
			Indexer.index("data/indices", analyzer.getCorpus());

			long startSearch = System.currentTimeMillis();
			//search in the inverted index
			Searcher indexSearcher = new Searcher("data/indices");
			HashMap<String,Integer> InvertIndexRes = new HashMap<>();
			for (String currquery : queryList){
				SearchResult result = indexSearcher.search(currquery);
				System.out.format("\t current search [%s] -> %d",currquery,result.numHits());
				InvertIndexRes.put(currquery,result.numHits());
			}
			long endInvert = System.currentTimeMillis();

			System.out.format("**************************************************\n");
			System.out.format("\t *Brute Force Query Time  : %.3f seconds\n",(endBruteForce-startBruteForce)/1000.0);
			System.out.format("\t *Invert Index Query Time : %.3f seconds\n",(endInvert-startLucene)/1000.0);
			System.out.format("**************************************************\n");

			System.out.format("**************************************************\n");
			System.out.format("\n\n Brute Index Result\n\n");
			BruteForceRes.entrySet().forEach(entry->{
				System.out.println(entry.getKey() + " " + entry.getValue());
			});

			System.out.format("**************************************************\n");
			System.out.format("\n\n Invert Index Result\n\n");
			InvertIndexRes.entrySet().forEach(entry->{
				System.out.println(entry.getKey() + " " + entry.getValue());
			});


		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void Q22_main(String[] args){
		//get the time
		try {
			//load the index content

			long startTime = System.currentTimeMillis();
			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/yelp/60", ".json");

			long startwritecsv1 = System.currentTimeMillis();
			analyzer.getCorpus().WriteCSV("outCount60.csv");
			long startIndex = System.currentTimeMillis();
			Indexer.index("data/indices", analyzer.getCorpus());
			long startSearch = System.currentTimeMillis();
			HashMap<String, Long> ttf_dictionary = new HashMap<>();
			HashMap<String, Integer> df_dictionary = new HashMap<>();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("data/indices")));
			Terms terms = MultiFields.getTerms(reader,"content"); //get reference to all the indexed terms in the content field
			TermsEnum termsEnum = terms.iterator(null);
			while (termsEnum.next()!=null){//iterate through all terms
				Term t = new Term("content",termsEnum.term());//map it to the corresponding field
				System.out.format("%s\t%d\t%d\n", t.text(), termsEnum.docFreq(), reader.totalTermFreq(t)); //print term text, DF and TTF

				if (ttf_dictionary.containsKey(t.text())){
					ttf_dictionary.put(t.text(),ttf_dictionary.get(t)+reader.totalTermFreq(t));
				}else {
					ttf_dictionary.put(t.text(),reader.totalTermFreq(t));
				}

				if (df_dictionary.containsKey(t.text())){
					df_dictionary.put(t.text(),df_dictionary.get(t)+termsEnum.docFreq());
				}else{
					df_dictionary.put(t.text(),termsEnum.docFreq());
				}
			}
			// write to files
			long startGenerateTable = System.currentTimeMillis();
			try {
				FileWriter csvWriter = new FileWriter("invertIndex60.csv");
				csvWriter.write("token,token_count,file_count\n");
				for (String token : ttf_dictionary.keySet()){
					csvWriter.write(String.format("%s,%d,%d\n",token,ttf_dictionary.get(token),df_dictionary.get(token)));
				}
				csvWriter.close();
			}catch (IOException e){
				e.printStackTrace();
			}
			long endtime = System.currentTimeMillis();

			System.out.format("**************************************************\n");
			System.out.format("\t * Loading Yelp 60 Document time                  : %.3f seconds\n",(startIndex-startTime)/1000.0);
			System.out.format("\t * TTF method 1 Yelp 60 Document time             : %.3f seconds\n",(startIndex-startTime)/1000.0);
			System.out.format("\t * Index Yelp 60 Document time                    : %.3f seconds\n",(startSearch-startIndex)/1000.0);
			System.out.format("\t * Generate TTF(include Index) 60 Document time   : %.3f seconds\n",(startGenerateTable-startIndex)/1000.0);
			System.out.format("\t * Create CSV file time 60 Document time          : %.3f seconds\n",(endtime- startGenerateTable)/1000.0);
			System.out.format("\t * Total Time (Load Doc, Index, TTF, Create CSV)  : %.3f seconds\n", (endtime - startTime) / 1000.0);
			System.out.format("**************************************************\n");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
