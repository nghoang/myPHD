package experiments;

import java.util.Vector;

import utility.WordNetLib;
import AppParameters.AppConst;

import com.ngochoang.CrawlerLib.Utilities;

public class MeasureRelatedBetweenItemInOneDocument {
	public static void main(String[] args) {
		String datasetName = "data/testdata";
		String anonymizedResultFile = datasetName + "/results/result.txt";
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);

		String content = Utilities.readFileAsString(anonymizedResultFile);
		String[] lines = content.split("\n");
		for (String line : lines) {
			if (Utilities.SimpleRegexSingle("^\\d+\\[", line, 0).equals("") == false) {
				String documentId = Utilities.SimpleRegexSingle("^(\\d+)\\[",
						line, 1);
				String terms = line.replaceAll("\\[|\\]|^\\d+", "");

				Vector<String> items = new Vector<String>();

				for (String i : terms.split(",")) {
					if (i.trim().isEmpty() == false) {
						items.add(i.trim());
					}
				}

				Float[][] table = new Float[items.size()][items.size()];
				Vector<Integer> calculatingIndexes = new Vector<Integer>();
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).equals("breath") || 
							items.get(i).equals("breathing") || 
							items.get(i).equals("intake") || 
							items.get(i).equals("reaction") || 
							items.get(i).equals("healing") || 
							items.get(i).equals("sweating") || 
							items.get(i).equals("diaphoresis")) {
						if (calculatingIndexes.size() <= 6)
							calculatingIndexes.add(i);
					}
					for (int j = i; j < items.size(); j++) {
						if (i == j) {
							table[i][j] = 0F;
							continue;
						}

						Float dis = wn.GetDistance(items.get(i), items.get(j));

						table[i][j] = dis;
						table[j][i] = dis;
					}
				}

				for (int calculatingIndex : calculatingIndexes) {
					float temp1 = 0;
					float temp2 = 0;
					int from = calculatingIndex - 5;
					int to = calculatingIndex + 5;
					if (from < 0) {
						to = to - from;
						from = 0;
					}
					if (to >= table[calculatingIndex].length) {
						from = to - table[calculatingIndex].length + from;
						to = table[calculatingIndex].length - 1;
					}
					for (int j = from; j <= to; j++) {
						temp1 += table[calculatingIndex][j];
					}
					for (int j = 0; j < table[calculatingIndex].length; j++) {
						temp2 += table[calculatingIndex][j];
					}

					System.out.println("Average Distance "+ items.get(calculatingIndex) +" "
							+ documentId + ": " + temp1 / (to - from) + " "
							+ temp2 / table[calculatingIndex].length);
				}
				if (calculatingIndexes.size() > 0)
					System.out.println("============");
			}
		}
	}
}
