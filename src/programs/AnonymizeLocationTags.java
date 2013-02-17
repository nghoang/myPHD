package programs;

import java.io.File;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

import utility.Inflector;
import utility.WordNetLib;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;

import algorithms.COAT2;
import utility.Utilities;

public class AnonymizeLocationTags {
	TransactionDataset dataset;

	public static void main(String[] args) {

	}

	public static void AnonymizeLocation() {
		AnonymizeLocationTags app = new AnonymizeLocationTags();

		// data set parameters. These are parameters need to process input
		// dataset (ds)
		// data set file is pointing to file content input data
		String data_set_file = "C:\\projects\\ngochoangprojects\\Java\\PHDProject\\anonymize_log_data\\data_small.txt";
		// this is regex expression to parse data set file. each transaction is
		// a line in ds file. This parser has 2 groups.
		// First group is transaction id, and 2nd is item list. This default
		// parser is used for my ds file. (data.txt)
		String reg = "(\\d+,\\d+@[^,]+,[^,]+,[^,]+),(.*)";
		// this is separator of each item in item list.
		String separator = " ";

		// because number of items are large, so I want to save domain in file
		// instead of saving in memory and I also wand feed that file to other
		// process such as calculating utility constraints.
		// so Please specify domain file location to save them
		String domain_file = "C:\\projects\\ngochoangprojects\\Java\\PHDProject\\anonymize_log_data\\domain_small.txt";

		// as COAT algorithm requires Privacy Constraints (PC) and Utility
		// Constraints (UC).
		// Please specify their location to save.
		String privacy_constraint_file = "C:\\projects\\ngochoangprojects\\Java\\PHDProject\\anonymize_log_data\\constraint_small.txt";
		String utility_constraint_file = "C:\\projects\\ngochoangprojects\\Java\\PHDProject\\anonymize_log_data\\utility_small.txt";

		// and this is location of result file.
		String result_file = "C:\\projects\\ngochoangprojects\\Java\\PHDProject\\anonymize_log_data\\result_small.txt";
		// you need to install Wordnet and point its location to this function.
		// We need wordnet to construct Utility constraints.
		WordNetLib wn = new WordNetLib(
				"C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");

		// this parameter is used to generate privacy constraints. if any
		// transaction not a subset of k-1 different other transaction, we will
		// extract them as privacy constraint.
		int k = 3;
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

		// read input to construct data
		app.ExtractToTransaction(data_set_file, reg, separator);
		// generate domain file
		app.CreateDomainFile(domain_file);
		// calculate UC and PC
		app.PGen(privacy_constraint_file, k);
		app.UGen(wn, utility_constraint_file, domain_file, z);
		// begin anonymizing
		app.Anonymize(privacy_constraint_file, utility_constraint_file, k,
				result_file);
	}

	public void ExtractToTransaction(String data_set_file, String reg,
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
			String utility_constraint_file, int k, String result_file) {
		COAT2 coat = new COAT2();
		coat.D = dataset;
		coat.LoadPrivacyContraints(privacy_constraint_file);
		coat.LoadUtilityContraints(utility_constraint_file);
		coat.SetParameters(k, 1);
		coat.run(result_file);
	}

}
