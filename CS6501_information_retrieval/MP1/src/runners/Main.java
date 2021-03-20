package runners;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import edu.virginia.cs.analyzer.DocAnalyzer;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.SearchResult;
import edu.virginia.cs.index.Searcher;
import edu.virginia.cs.searcher.DocSearcher;
import opennlp.tools.dictionary.Index;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import structures.ReviewDoc;

public class Main {

	//The main entrance to test various functions 
	public static void main(String[] args) {
//		Q2_main(args);
		Q22_main(args);
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

	public static void Q22_main(String[] arg){
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

			System.out.format("\t[infor] Finish DocAnalyzer.. time use %.3f second\n",(System.currentTimeMillis()-startTime)/1000.0);

			String query = "general chicken";
			System.out.format("Brute-force Search Start::\n");
			//using brute-force strategy to scan through the whole corpus
			long startBruteForce = System.currentTimeMillis();
			DocSearcher bruteforceSearcher = new DocSearcher(analyzer.getCorpus(), "data/models/en-token.bin");
			for (String currquery : queryList){
				ReviewDoc[] result = bruteforceSearcher.search(currquery);
				System.out.format("\t current search [%s] -> %d\n",currquery,result.length);
			}
			System.out.format("\n\t[infor] Finish brute-force search.. time use %.3f second \n",(System.currentTimeMillis()-startBruteForce)/1000.0);

			System.out.format("Finish Brute-force index\n\n");

			System.out.format("Start Invert index \n");
			long startLucene = System.currentTimeMillis();
			//create inverted index
			Indexer.index("data/indices", analyzer.getCorpus());
			System.out.format("\t[infor] Finish Lucene index.. time use %.3f second\n",(System.currentTimeMillis()-startLucene)/1000.0);
			//search in the inverted index
			Searcher indexSearcher = new Searcher("data/indices");
			for (String currquery : queryList){
				SearchResult result = indexSearcher.search(currquery);
				System.out.format("\t current search [%s] -> %d",currquery,result.numHits());
			}
			System.out.format("\t[infor] Finish Lucene search.. time use %.3f second\n",(System.currentTimeMillis()-startLucene)/1000.0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


}
