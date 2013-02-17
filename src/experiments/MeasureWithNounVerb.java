package experiments;

import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import algorithms.GoogleSimilarityDistance;
import algorithms.POSTagging;

import com.ngochoang.CrawlerLib.WebClientX;

public class MeasureWithNounVerb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] targets = {};
		POSTagging pos = new POSTagging();
		pos.ExtractTextToTransaction2("data/data2", "data/left3words-wsj-0-18.tagger");	
		TransactionDataset transactions = pos.documents;
		WebClientX client = new WebClientX();
		client.SetProxyList("proxy-list.txt");
		GoogleSimilarityDistance google = new GoogleSimilarityDistance();
		google.client = client;
		
		String result = "";

		for (Transaction tran : transactions.getRecords())
		{
			for (String target : targets)
			{
				for (TransactionItem item : tran.GetData())
				{
					//google.Similarity(w1, w2)
				}
			}
		}
	}

}
