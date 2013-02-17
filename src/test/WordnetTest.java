package test;

import java.util.Vector;

import org.junit.Test;

import utility.WordNetLib;

public class WordnetTest {
	
	WordNetLib wn = new WordNetLib("C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");
	@Test
	public void TestGetRelatedTerms()
	{
		System.out.println("Start TestGetRelatedTerms");
		Vector<String> terms = wn.GetRelated3("film", 25);
		System.out.println(terms.toString());
	}
}
