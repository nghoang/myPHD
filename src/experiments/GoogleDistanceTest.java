package experiments;

import algorithms.GoogleSimilarityDistance;

public class GoogleDistanceTest {
	public static void main(String[] args)
	{
		GoogleSimilarityDistance g = new GoogleSimilarityDistance();
		System.out.println(g.Similarity("hiv", "patient","health"));
	}
}
