package test;

import InformationRetrieval.NamedEntitiesRecognition;

public class NERTest {
	public static void main(String[] args)
	{
		NamedEntitiesRecognition ner = new NamedEntitiesRecognition();
		ner.RemoveNamedEntities("data/NER", "");
	}
}
