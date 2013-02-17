package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import utility.Inflector;
import utility.Utilities;
import DataStructure.CValueTerm;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;

public class CValue {

	// private Vector<String> _rules;
	private Vector<CValueTerm> CValueTerms = new Vector<CValueTerm>();
	// private int _threshold = 0;
	private static Vector<String> stopwords;
	public static String stop_words_path = "data\\stopwords.txt";
	public TransactionDataset documents;

	// Vector<String> tCValueTerms;

	public static boolean IsStopWord(String w) {
		if (stopwords == null) {
			stopwords = new Vector<String>();
			String content = "";
				content = Utilities.readFileAsString(stop_words_path);
			String[] _stopwords = content.split("\n");
			for (String line : _stopwords) {
				if (line.trim().equals(""))
					continue;
				stopwords.add(line);
			}
		}

		for (String line : stopwords) {
			if (w.toUpperCase().equals(line.trim().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	public Vector<CValueTerm> GetTerms() {
		return CValueTerms;
	}

	// public CValue(int th) {
	// _rules = new Vector<String>();
	// /*
	// * Rules: 0: VB 1: PRP 2: IN 3: VBG 4: DT 5: JJ 6: NN 7: PRP$ 8: VBZ 9:
	// * CC A: NNS B: VBN C: NNP D: VBD E: JJR
	// */
	// _threshold = th;
	// }

	// public void AddRule(String r) {
	// _rules.add(r);
	// }

	public void ExtractNouns(String dataFolder, String modelPath)
	{
		documents = new TransactionDataset();
		POSTagging pos = new POSTagging();
		pos.modelFile = modelPath;
		Vector<String> files = Utilities.GetFileInFolder(dataFolder);
		int max = files.size();
		int count = 1;
		List<List<HasWord>> sentences;
		ArrayList<TaggedWord> tSentence;
		TaggedWord word;
		for (String file : files) {
			Transaction document = new Transaction();
			System.out.println(count + "/" + max);
			count++;
			sentences = pos.TagSentenceCorpusFromFile(dataFolder + file);
			for (List<HasWord> sentence : sentences) {
				tSentence = pos.TaggingWords(sentence);
				for (int i = 0; i < tSentence.size(); i++) {
					word = tSentence.get(i);
					if (IsStopWord(word.value().trim()))
						continue;
					if (word.tag().equals("NN") && !word.tag().equals(",")
							&& !word.tag().equals(".")
							&& !word.tag().equals(";")) {
						if (!document.IsContains(new TransactionItem(word.value().toLowerCase())))
							document.AddItem(new TransactionItem(word.value().toLowerCase()));
					}
				}
			}
			documents.AddRecord(document);
		}
	}
	
	public Vector<CValueTerm> Process(String folder, String modelPath) {
		POSTagging pos;
		List<List<HasWord>> sentences;
		ArrayList<TaggedWord> tSentence;
		documents = new TransactionDataset();
		TaggedWord word;
		Inflector inf;
		inf = new Inflector();
		pos = new POSTagging();
		pos.modelFile = modelPath;
		Vector<String> files = Utilities.GetFileInFolder(folder);
		int count = 1;
		CValueTerms = new Vector<CValueTerm>();
		int max = files.size();
		for (String file : files) {
			Transaction document = new Transaction();
			System.out.println(count + "/" + max);
			count++;
			sentences = pos.TagSentenceCorpusFromFile(folder + file);
			String currentTerm = "";
			for (List<HasWord> sentence : sentences) {
				tSentence = pos.TaggingWords(sentence);
				for (int i = 0; i < tSentence.size(); i++) {
					word = tSentence.get(i);
					if (IsStopWord(word.value().trim()))
						continue;
					if (word.tag().equals("NN") && !word.tag().equals(",")
							&& !word.tag().equals(".")
							&& !word.tag().equals(";")) {
						if (currentTerm.equals("")) {
							currentTerm += word.value();
						} else {
							currentTerm += " " + inf.singularize(word.value());
						}
					} else {
						if (!currentTerm.equals("")) {
							if (currentTerm.length() < 3) {
								currentTerm = "";
								continue;
							}
							int index = -1;
							index = CValueTerm.IsInList(CValueTerms,
									currentTerm.toLowerCase());
							if (index == -1) {
								CValueTerm term = new CValueTerm();
								term.Frequency = 1;
								term.Term = currentTerm.toLowerCase();
								CValueTerms.add(term);
								document.AddItem(new TransactionItem(term.Term));
							} else {
								CValueTerms.get(index).Frequency++;
							}
							currentTerm = "";
						}
					}
				}
			}
			documents.AddRecord(document);
		}

		for (int i = 0; i < CValueTerms.size(); i++) {
			CValueTerms.get(i).Cvalue = CalculateCValue(CValueTerms.get(i));
		}

		Vector<CValueTerm> tCValueTerms = new Vector<CValueTerm>();
		while (CValueTerms.size() > 0) {
			CValueTerm tt = FilterCValue(CValueTerms.get(0));
			if (tt != null)
				tCValueTerms.add(tt);
			else
				documents.Suppress(CValueTerms.get(0).Term);
		}
		CValueTerms = tCValueTerms;
		// nCValueTerms = new Vector<CValueTerm>();
		// for (CValueTerm t : CValueTerms) {
		// if (t.Frequency < _threshold) {
		// continue;
		// }
		// t.CalculateCValue();
		// nCValueTerms.add(t);
		// }
		// CValueTerms = nCValueTerms;
		return CValueTerms;
	}

	private CValueTerm FilterCValue(CValueTerm term) {
		Vector<CValueTerm> ContainedByTerms = new Vector<CValueTerm>();

		Double maxCV = -1D;

		for (CValueTerm t : CValueTerms) {
			if (t.Term.contains(term.Term)) {
				CValueTerm tr = new CValueTerm();
				tr.Frequency = 1;
				tr.Term = t.Term;
				tr.Cvalue = t.Cvalue;
				ContainedByTerms.add(tr);
				if (maxCV < tr.Cvalue) {
					maxCV = tr.Cvalue;
				}
			}
		}
		CValueTerm tt = null;
		for (int i = 0; i < ContainedByTerms.size(); i++) {
			if (ContainedByTerms.get(i).Cvalue == maxCV) {
				tt = new CValueTerm();
				tt.Cvalue = ContainedByTerms.get(i).Cvalue;
				tt.Term = ContainedByTerms.get(i).Term;
				tt.Frequency = ContainedByTerms.get(i).Frequency;
				// ContainedByTerms.remove(i);
				break;
			}
		}

		for (CValueTerm t1 : ContainedByTerms) {
			int i = 0;
			for (CValueTerm t2 : CValueTerms) {
				if (t1.Term.equals(t2.Term)) {
					CValueTerms.remove(i);
					break;
				}
				i++;
			}
		}
		return tt;
	}

	private Double CalculateCValue(CValueTerm term) {
		// Vector<String> ContainedTerms = new Vector<String>();

		Double res = 0D;
		Vector<CValueTerm> ContainedByTerms = new Vector<CValueTerm>();

		for (CValueTerm t : CValueTerms) {
			if (t.Term.equals(term.Term))
				continue;
			if (t.Term.contains(term.Term)) {
				CValueTerm tr = new CValueTerm();
				tr.Frequency = 1;
				tr.Term = t.Term;
				ContainedByTerms.add(tr);
			}
		}

		if (term.Term.indexOf(" ") == -1)
			return 0D;

		if (ContainedByTerms.size() > 0) {

			Double temp = 0D;
			for (CValueTerm ct : ContainedByTerms) {
				temp += ct.Frequency;
			}
			res = Math.log(term.Term.trim().split(" ").length) / Math.log(2)
					* (term.Frequency - temp / ContainedByTerms.size());
		} else {
			res = Math.log(term.Term.trim().split(" ").length) / Math.log(2)
					* term.Frequency;
		}
		return res;
	}
}
