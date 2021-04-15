package edu.virginia.cs.evaluator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import edu.virginia.cs.index.Indexer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.Searcher;

public class Evaluate {
	/**
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */

	Searcher _searcher = null;

	public static void setSimilarity(Searcher searcher, String method) {
        if(method == null)
            return;
        else if(method.equals("--ok"))
            searcher.setSimilarity(new BM25Similarity());       
        else if(method.equals("--tfidf"))
            searcher.setSimilarity(new DefaultSimilarity());
        else
        {
            System.out.println("[Error]Unknown retrieval function specified!");
            printUsage();
            System.exit(1);
        }
    }
    
    public static void printUsage()
    {
        System.out.println("To specify a ranking function, make your last argument one of the following:");        
        System.out.println("\t--ok\tOkapi BM25");
        System.out.println("\t--tfidf\tTFIDF Dot Product");
    }
    
	//Please implement P@K, MRR and NDCG accordingly
	public void evaluate(String method, String indexPath, String judgeFile) throws IOException {
		_searcher = new Searcher(indexPath);
		setSimilarity(_searcher, method);

		BufferedReader br = new BufferedReader(new FileReader(judgeFile));
		String line = null, judgement = null;
		int k = 10;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;
		while ((line = br.readLine()) != null) {
			judgement = br.readLine();

			//compute corresponding AP
			meanAvgPrec += AvgPrec(line, judgement);
			//compute corresponding P@K
			p_k += Prec(line, judgement, k);
			//compute corresponding MRR
			mRR += RR(line, judgement);
			//compute corresponding NDCG
			nDCG += NDCG(line, judgement, k);

			++numQueries;
		}
		br.close();

		System.out.println("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		System.out.println("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
		System.out.println("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
		System.out.println("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
	}

	// implement P@K, MRR and NDCG accordingly
	// buffer the data to Hashtable and then write to csv file
	// with the csv file, get the
	public void evaluateRecoder(String method, String indexPath, String judgeFile) throws IOException {
		_searcher = new Searcher(indexPath);
		setSimilarity(_searcher, method);

		BufferedReader br = new BufferedReader(new FileReader(judgeFile));
		String line = null, judgement = null;
		int k = 10;

		List<Double> avgPrecList = new ArrayList<>();
		List<Double> pkList = new ArrayList<>();
		List<Double> mRRList = new ArrayList<>();
		List<Double> nDCGList = new ArrayList<>();


		while ((line = br.readLine()) != null) {
			judgement = br.readLine();

			//compute corresponding AP
			double avgPrec = AvgPrec(line, judgement);
			//compute corresponding P@K
			double p_k = Prec(line, judgement, k);
			//compute corresponding MRR
			double mRR = RR(line, judgement);
			//compute corresponding NDCG
			double nDCG = NDCG(line, judgement, k);

			// buffer the result to the csv file
			avgPrecList.add(avgPrec);
			pkList.add(p_k);
			mRRList.add(mRR);
			nDCGList.add(nDCG);
			System.out.printf("Res : %f, %f, %f, %f",avgPrec,p_k,mRR,nDCG);
		}
		br.close();
		// write the data t csv files
		String outFilename = "evaluate_ok.csv";
		if (method.equals("--tfidf")){
			outFilename = "evaluate_tfidf.csv";
		}
		try {
			FileWriter csvWriter = new FileWriter(outFilename);
			csvWriter.write("Count,avgPrec,pk10,mRR,nDCG10\n");
			// loop on the element and write to the csv files
			for (int indexer = 0 ; indexer < avgPrecList.size(); indexer++){
				csvWriter.write(String.format("%d,%f,%f,%f,%f\n",indexer,avgPrecList.get(indexer),pkList.get(indexer),mRRList.get(indexer),nDCGList.get(indexer)));
			}
			csvWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}

	}

	double AvgPrec(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
		int i = 1;
		double avgp = 0.0;
		double numRel = 0;
		double sump = 0.0;
		//System.out.println("\nQuery: " + query);
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				//how to accumulate average precision (avgp) when we encounter a relevant document
				numRel ++;
				sump = sump + numRel/i;

		//		System.out.print("  ");
			} else {
				//how to accumulate average precision (avgp) when we encounter an irrelevant document
		//		System.out.print("X "); // indicate the document is irrelevant
			}
		//	System.out.println(i + ". " + rdoc.title());
			++i;
		}
		
		//compute average precision here
		if (numRel == 0){
			return 0.0;
		}else {
			avgp = sump/relDocs.size();
		}
		//System.out.println("Average Precision: " + avgp);
		return avgp;
	}
	
	//precision at K
	double Prec(String query, String docString, int k) {
		double p_k = 0;
		//your code for computing precision at K here
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
		int i = 1;
		double numRel = 0;
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				numRel ++;
		//		System.out.print("  ");
			} else {
		//		System.out.print("X "); // indicate the document is irrelevant
			}
		//	System.out.println(i + ". " + rdoc.title());
			++i;
			if (i > k) break;
		}

		p_k = numRel/k;
		return p_k;
	}
	
	//Reciprocal Rank
	double RR(String query, String docString) {
		double rr = 0;
		//your code for computing Reciprocal Rank here
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
		int i = 1;
		double numRel = 0;
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				numRel ++;
				break;
			} else {
		//		System.out.print("X "); // indicate the document is irrelevant
			}
		//	System.out.println(i + ". " + rdoc.title());
			++i;
		}
		if (numRel != 0){
			rr = 1.0 / i;
		}else {
			rr = 0.0;
		}
		return rr;
	}
	
	//Normalized Discounted Cumulative Gain
	double NDCG(String query, String docString, int k) {
		double ndcg = 0;
		//your code for computing Normalized Discounted Cumulative Gain here
		//idea: take the rel to be 1. for all the relevent document. and use this value to be the normalize factor

		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
		int i = 1;
		double numRel = 0;
		double DCGgt = 0.0;
		double DCGrf = 0.0;
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				numRel ++;
				DCGrf = DCGrf + 1.0/(Math.log(1.+i)/Math.log(2.));
		//		System.out.print("  ");
			} else {
		//		System.out.print("X "); // indicate the document is irrelevant
			}
		//	System.out.println(i + ". " + rdoc.title());
			++i;
			if (i > k) break;
		}

		// calculate the ground truth
		int numGT = Math.min(relDocs.size(),k);
		for (int tgIndexer = 0; tgIndexer < numGT; tgIndexer ++){
			DCGgt = DCGgt + 1.0/(Math.log(2.+tgIndexer)/Math.log(2.0));
		}

		ndcg = DCGrf/DCGgt;

		return ndcg;
	}
}