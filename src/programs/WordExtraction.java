package programs;

import java.io.IOException;
import java.util.Vector;

import DataStructure.CValueTerm;
import algorithms.COAT;
import algorithms.CValue;
import utility.Utilities;

public class WordExtraction {
	public static String dataFolder;
	static String modelData = "C:\\data\\models\\left3words-wsj-0-18.tagger";

	public static void TermExtraction() {
		CValue cvalueAlgorithm = new CValue();

		// extract three type of nouns
//		cvalueAlgorithm.AddRule("NN");
//		cvalueAlgorithm.AddRule("NNP");
//		cvalueAlgorithm.AddRule("NNS");
//		cvalueAlgorithm.AddRule("CD");
		// extracting with folder of data, and POS model

		cvalueAlgorithm.Process(dataFolder, modelData);
		int i = 0;
		Utilities.WriteFile(dataFolder + "extracted.txt", "", false);
		for (CValueTerm t : cvalueAlgorithm.GetTerms()) {
			// System.out.println(t.Term + " - " + t.Cvalue);
			Utilities.WriteFile(dataFolder + "extracted.txt", i + "#"
					+ t.Term + "#" + t.Cvalue + "\n", true);
			i++;
		}

		COAT.CreateTransactionDatasetFile(dataFolder, dataFolder
				+ "\\extracted.txt",dataFolder + "\\transactions.txt");
	}

	public static void PutDataBack(int k) {
		Vector<Vector<String>> results = LoadResult();
		LoadItems();
		Vector<String> items = iditems;
		Vector<Vector<String>> nitems = new Vector<Vector<String>>();
		for (int i = 0; i < items.size(); i++) {
			for (int j = 0; j < results.size(); j++) {
				if (results.get(j).contains(items.get(i))) {
					nitems.add(results.get(j));
					//System.out.println(i);
					break;
				}
				if (j == results.size() - 1) {
					nitems.add(new Vector<String>());
					//System.out.println(i);
				}
			}
		}

		String content = "";
		for (int i = 0; i < nitems.size(); i++) {
			if (nitems.get(i).size() == 0 || nitems.get(i).size() > 1) {
				String res = "";
				boolean isF = true;
				for (String t : nitems.get(i)) {
					if (isF)
					{
						res += GetItemById(t);
					}
					else 
					{
						res += "," + GetItemById(t);
					}
					isF = false;
					
				}
				content += textitems.get(i) + " -> " + res + "\n";
			}
		}
		Utilities.WriteFile(dataFolder + "result2_"+k+".txt", content, false);
	}

	private static String GetItemById(String id) {
		for (int i = 0; i < iditems.size(); i++) {
			if (iditems.get(i).equals(id)) {
				return textitems.get(i);
			}
		}
		return "";
	}

	static Vector<String> iditems = null;
	static Vector<String> textitems = null;

	public static void LoadItems() {

		if (iditems != null)
			return;

		iditems = new Vector<String>();
		textitems = new Vector<String>();
		String content = "";
			content = Utilities
					.readFileAsString(dataFolder + "extracted.txt");
		String[] lines = content.split("\n");
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			iditems.add(line.split("#")[0]);
			textitems.add(line.split("#")[1]);

		}
	}

	private static Vector<Vector<String>> LoadResult() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		String content = "";
			content = Utilities.readFileAsString(dataFolder + "result.txt");
		String[] lines = content.split("\n");
		for (String line : lines) {

			Vector<String> blocks = Utilities.SimpleRegex("\\[([^\\]]*)\\]",
					line, 1);
			for (String is : blocks) {
				Vector<String> result = new Vector<String>();
				for (String i : is.split(",")) {
					result.add(i.trim());
				}
				results.add(result);
			}
		}
		return results;
	}
}
