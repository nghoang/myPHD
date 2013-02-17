package algorithms;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;

import utility.Utilities;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POSTagging {
	public String modelFile;
	static MaxentTagger tagger;
	public TransactionDataset documents;
	public Vector<String> data;

	public List<List<HasWord>> TagSentenceCorpus(String content) {
		if (tagger == null) {
			try {
				tagger = new MaxentTagger(modelFile);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		List<List<HasWord>> sentences = tagger.tokenizeText(new StringReader(
				content));
		return sentences;
	}

	public void ExtractTextToTransaction(String folder, String modelPath) {
		this.modelFile = modelPath;
		Vector<String> files = Utilities.GetFileInFolder(folder);
		List<List<HasWord>> sentences;
		ArrayList<TaggedWord> tSentence;
		documents = new TransactionDataset();
		TransactionItem tranItem;
		TaggedWord word;
		Transaction document;
		int max = files.size();
		int count = 1;
		for (String file : files) {
			document = new Transaction();
			System.out.println(count + "/" + max);
			count++;
			sentences = TagSentenceCorpusFromFile(folder + file);
			for (List<HasWord> sentence : sentences) {
				tSentence = TaggingWords(sentence);
				for (int i = 0; i < tSentence.size(); i++) {
					word = tSentence.get(i);
					if (word.tag().equals("NN") && !word.tag().equals(",")
							&& !word.tag().equals(".")
							&& !word.tag().equals(";")) {
						tranItem = new TransactionItem(word.value());
						if (!document.IsContains(tranItem))
							document.AddItem(tranItem);
					}
				}
			}
			documents.AddRecord(document);
		}
	}
	
	public void ExtractTextToTransaction2(String folder, String modelPath) {
		this.modelFile = modelPath;
		Vector<String> files = Utilities.GetFileInFolder(folder);
		List<List<HasWord>> sentences;
		ArrayList<TaggedWord> tSentence;
		documents = new TransactionDataset();
		TransactionItem tranItem;
		TaggedWord word;
		Transaction document;
		int max = files.size();
		int count = 1;
		for (String file : files) {
			document = new Transaction();
			System.out.println(count + "/" + max);
			count++;
			sentences = TagSentenceCorpusFromFile(folder + file);
			for (List<HasWord> sentence : sentences) {
				tSentence = TaggingWords(sentence);
				for (int i = 0; i < tSentence.size(); i++) {
					word = tSentence.get(i);
					if ((word.tag().startsWith("NN") || 
							word.tag().startsWith("VB")) && !word.tag().equals(",")
							&& !word.tag().equals(".")
							&& !word.tag().equals(";")) {
						if (word.value().toLowerCase().equals("is") ||
								word.value().toLowerCase().equals("are") ||
								word.value().toLowerCase().equals("s") ||
								word.value().toLowerCase().equals("were") ||
								word.value().toLowerCase().equals("was") ||
								word.value().toLowerCase().equals("has") ||
								word.value().toLowerCase().equals("have") ||
								word.value().toLowerCase().equals("will") ||
								word.value().toLowerCase().equals("shall") ||
								word.value().toLowerCase().equals("should") ||
								word.value().toLowerCase().equals("would") ||
								word.value().toLowerCase().equals("may") ||
								word.value().toLowerCase().equals("be") ||
								word.value().toLowerCase().equals("might"))
							continue;
						tranItem = new TransactionItem(word.value());
						if (!document.IsContains(tranItem))
							document.AddItem(tranItem);
					}
				}
			}
			documents.AddRecord(document);
		}
	}

	public ArrayList<TaggedWord> TaggingWords(List<HasWord> sentence) {
		return tagger.tagSentence(sentence);
	}

	public List<List<HasWord>> TagSentenceCorpusFromFile(String file) {
		return this.TagSentenceCorpus(ClearDocument(Utilities
				.readFileAsString(file)));
	}

	public static String ClearDocument(String doc) {
		doc = doc.replaceAll("[^a-zA-Z0-9\\.,;\n\t\\-\\(\\)\\[\\]{}]", " ");
		return doc;
	}
}
