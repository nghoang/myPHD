package test;

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
		System.out.println("keyboard vs heater:" + wn.GetDistance("keyboard", "heater"));
		System.out.println("keyboard vs curtain:" + wn.GetDistance("keyboard", "curtain"));
		System.out.println("keyboard vs shower:" + wn.GetDistance("keyboard", "shower"));
		

		System.out.println("keyboard vs heater:" + ngd.Similarity("keyboard", "heater"));
		System.out.println("keyboard vs curtain:" + ngd.Similarity("keyboard", "curtain"));
		System.out.println("keyboard vs shower:" + ngd.Similarity("keyboard", "shower"));
	}

}
