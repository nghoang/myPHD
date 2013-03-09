package test;

import rita.wordnet.RiWordnet;
import algorithms.GoogleSimilarityDistance;
import AppParameters.AppConst;
import utility.WordNetLib;

public class TestDistances {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		GoogleSimilarityDistance ngd = new GoogleSimilarityDistance();
		String t1 = "coast";
		String t2 = "hill";
		for (String t : wn.riwn.getCommonParents(t1, t2, RiWordnet.NOUN))
		{
			System.out.println(t);
			System.out.println("distance: " + ngd.Similarity(t1, t2, "", "","AND "+ t));
		}
			
	}

}
