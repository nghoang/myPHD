package experiments;

import java.io.File;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

import utility.Inflector;
import utility.Utilities;
import utility.WordNetLib;
import AppParameters.AppConst;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import InformationRetrieval.NamedEntitiesRecognition;
import algorithms.COAT2;
import algorithms.CValue;

public class AnonymizeDocuments {
	TransactionDataset dataset;

	public static void main(String[] args) {
		//AnonymizeDocs();
	}

	public static void AnonymizeDocs(String datasetName, String data_folder,
			String nerred_data_folder, String anonymized_data_folder,
			String result_folder, int action_id, int k, String privacyConstraint) {
		
		String data_set_file = result_folder + "dataset.txt";

		// this is regex expression to parse data set file. each transaction is
		// a line in ds file. This parser has 2 groups.
		// First group is transaction id, and 2nd is item list. This default
		// parser is used for my ds file. (data.txt)
		String reg = "(\\d+);(.*)";
		// this is separator of each item in item list.
		String separator = ";";

		// because number of items are large, so I want to save domain in file
		// instead of saving in memory and I also wand feed that file to other
		// process such as calculating utility constraints.
		// so Please specify domain file location to save them
		String domain_file = result_folder + "/domain.txt";

		// as COAT algorithm requires Privacy Constraints (PC) and Utility
		// Constraints (UC).
		// Please specify their location to save.
		String privacy_constraint_file = result_folder
				+ "privacy_constraint.txt";
		String utility_constraint_file = result_folder
				+ "utility_constraint.txt";

		// and this is location of result file.
		String result_file = result_folder + "result.txt";
		// you need to install Wordnet and point its location to this function.
		// We need wordnet to construct Utility constraints.
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);// mac

		// this parameter is used to generate privacy constraints. if any
		// transaction not a subset of k-1 different other transaction, we will
		// extract them as privacy constraint.
		//int k = 3;
		
		// this parameter is used to generate utility constraints. It will
		// extract z different near by terms to group with given term to create
		// an utility constraint.
		// Rule is given a term, it searches and adds all sibling terms and
		// parent terms. then from those parent terms, it goes down to see other
		// terms.
		// I ran recursively that function until it reaches to z different terms
		// or stops when there is no other related term which is detected by
		// wordnet
		int z = 50;
		AnonymizeDocuments app = new AnonymizeDocuments();

		switch (action_id) {
		case 1:
			NamedEntitiesRecognition ner = new NamedEntitiesRecognition();
			ner.RemoveNamedEntities(data_folder, nerred_data_folder);
			app.CreateTransaction(nerred_data_folder, data_set_file);
			app.LoadTransaction(data_set_file, reg, separator);
			app.CreateDomainFile(domain_file);
			break;
		case 2:
			app.LoadTransaction(data_set_file, reg, separator);
			app.PGen(privacy_constraint_file, k);
			break;
		case 3:
			app.LoadTransaction(data_set_file, reg, separator);
			app.UGen(wn, utility_constraint_file, domain_file, z);
			break;
		case 4:
			app.LoadTransaction(data_set_file, reg, separator);
			privacyConstraint = privacyConstraint.trim();
			privacyConstraint = privacyConstraint.trim();
			if (privacyConstraint.equals("") == false)
				com.ngochoang.CrawlerLib.Utilities.WriteFile(privacy_constraint_file, privacyConstraint, false);
			app.Anonymize(privacy_constraint_file, utility_constraint_file, k,
					result_file, nerred_data_folder, anonymized_data_folder);
			break;
		}

	}

	public void CreateTransaction(String data_folder, String data_set_file) {
		CValue cvalueAlgorithm = new CValue();
		// cvalueAlgorithm.Process(data_folder,
		// "data\\left3words-wsj-0-18.tagger");
		cvalueAlgorithm.ExtractNouns(data_folder,
				"data\\left3words-wsj-0-18.tagger");
		Integer i = 0;
		Utilities.WriteFile(data_set_file, "", false);
		for (Transaction t : cvalueAlgorithm.documents.getRecords()) {
			String line = i.toString();
			for (TransactionItem it : t.GetData()) {
				line += ";" + it.getItem();
			}
			Utilities.WriteFile(data_set_file, line + "\n", true);
			i++;
		}
	}

	public void LoadTransaction(String data_set_file, String reg,
			String separator) {

		String content = Utilities.readFileAsString(data_set_file);
		dataset = new TransactionDataset();
		Vector<String> ids = Utilities.SimpleRegex(reg, content, 1,
				Pattern.CASE_INSENSITIVE);
		Vector<String> item_lists = Utilities.SimpleRegex(reg, content, 2,
				Pattern.CASE_INSENSITIVE);
		Inflector inf;
		inf = new Inflector();
		for (int i = 0; i < ids.size(); i++) {
			Transaction tran = new Transaction();
			tran.setId(ids.get(i));
			String[] items = item_lists.get(i).trim().split(separator);
			for (String j : items) {
				if (!Utilities.SimpleRegexSingle("([^0-9a-zA-Z])", j.trim(), 1)
						.equals(""))
					continue;
				tran.AddItem(new TransactionItem(inf.singularize(j)
						.toLowerCase()));
			}
			dataset.AddRecord(tran);
		}
		dataset.SortBySize();
	}

	public void CreateDomainFile(String domain_file) {
		File f;
		f = new File(domain_file);
		f.delete();
		Vector<TransactionItem> domain = dataset.getDomain().GetData();
		for (TransactionItem i : domain) {
			Utilities.WriteFile(domain_file, i + "\n", true);
		}
	}

	public void PGen(String privacy_constraint_file, int k) {
		Utilities.WriteFile(privacy_constraint_file, "", false);
		for (int i = 0; i < dataset.getRecords().size(); i++) {
			int _k = 1;
			for (int j = i + 1; j < dataset.getRecords().size(); j++) {
				if (dataset.getRecords().get(j)
						.IsContainsAll(dataset.getRecords().get(i).GetData())) {
					_k++;
					if (_k >= k) {
						break;
					}
				}
			}
			if (_k < k) {
				Utilities.WriteFile(privacy_constraint_file, dataset
						.getRecords().get(i).toString()
						+ "\n", true);
			}
		}
	}

	public void UGen(WordNetLib wn, String utility_constraint_file,
			String domain_file, int max_UGen_size) {
		Utilities.WriteFile(utility_constraint_file, "", false);
		String content = Utilities.readFileAsString(domain_file);
		Vector<String> domain = new Vector<String>();
		for (String line : content.split("\n")) {
			domain.add(line.trim());
		}

		Collections.sort(domain);

		Vector<String> group;
		while (domain.size() > 0) {
			group = new Vector<String>();
			String term = domain.get(0).trim();
			System.out.println(domain.size());
			group.add(term);
			domain.remove(0);
			if (term.equals(""))
				continue;
			Vector<String> slidings = wn.GetRelated3(term, max_UGen_size);
			for (String sl : slidings) {
				int index = Collections.binarySearch(domain, sl);
				if (index > -1) {
					if (group.indexOf(domain.get(index)) == -1)
						group.add(domain.get(index));
					domain.remove(index);
				}
			}
			String print_line = group.toString();
			if (group.size() > 1) {
				print_line = print_line.replace("]", "");
				print_line = print_line.replace("[", "");
				Utilities.WriteFile(utility_constraint_file, print_line + "\n",
						true);
			}
		}
	}

	public void Anonymize(String privacy_constraint_file,
			String utility_constraint_file, int k, String result_file,
			String original_data_folder, String anononymized_folder) {
		COAT2 coat = new COAT2();
		coat.D = dataset;
		coat.LoadPrivacyContraints(privacy_constraint_file);
		coat.LoadUtilityContraints(utility_constraint_file);
		coat.SetParameters(k, 1);
		coat.run(result_file);

		Vector<String> filenames = Utilities
				.GetFileInFolder(original_data_folder);
		Vector<String> filecontents = new Vector<String>();
		for (String fn : filenames) {
			filecontents.add(Utilities.readFileAsString(original_data_folder
					+ fn));
		}

		String suppressed_by = " _____ ";
		// Inflector inf = new Inflector();

		for (int i = 0; i < filecontents.size(); i++) {
			String newData = filecontents.get(i);
			for (String suppressed_term : coat.GetSuppressedItems()) {
				newData = newData.replaceAll("(?i)[\\s\\.,;]+"
						+ suppressed_term + "[\\s\\.,;]+", suppressed_by);
			}

			filecontents.set(i, newData);
		}

		for (Vector<String> generalized_term : coat.GetGeneralizeditems()) {
			for (int i = 0; i < filecontents.size(); i++) {
				String newData = filecontents.get(i);
				for (String sub : generalized_term) {
					newData = newData.replaceAll("(?i)[\\s\\.,;]+" + sub
							+ "[\\s\\.,;]+", generalized_term.toString());
					if (!filecontents.get(i).equals(newData))
						break;
				}
				filecontents.set(i, newData);
			}
		}

		for (int i = 0; i < filecontents.size(); i++) {
			Utilities.WriteFile(anononymized_folder + filenames.get(i),
					filecontents.get(i), false);
		}
	}

}
