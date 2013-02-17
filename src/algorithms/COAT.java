package algorithms;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import utility.Utilities;
import DataStructure.GeneralizedTransaction;
import DataStructure.GeneralizedTransactionDataSet;
import DataStructure.GeneralizedTransactionItem;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import dataset.MeshLib.MeSHUtility;

public class COAT {

	TransactionDataset D;
	int k = 5;
	double s = 0.15;
	GeneralizedTransactionDataSet gD;
	GeneralizedTransactionDataSet ogD;
	GeneralizedTransactionDataSet Ps;
	GeneralizedTransactionDataSet Us;
	GeneralizedTransactionDataSet oUs;
	String dataFolder = "";
	Double alpha = 0.01;
	Hashtable<String, String> extracted_term;
	Hashtable<String, String> extracted_term2;
	Hashtable<String, String> paths;
	MeSHUtility mesh = new MeSHUtility();
	Vector<GeneralizedTransactionItem> ss = new Vector<GeneralizedTransactionItem>();
	private Vector<Integer> pc_supports = new Vector<Integer>();
	public int countGeneralizations = 0;
	public int countSuppressions = 0;
	Hashtable<String, Double> weights = null;
	public long timetorun = 0;
	int maxUSize = 0;

	private void LoatPaths(String folder) {
		paths = new Hashtable<String, String>();
		String content = "";
			content = Utilities.readFileAsString(folder + "path_" + alpha
					+ ".txt");
		String[] lines = content.split("\n");
		for (String line : lines) {
			if (!line.trim().equals("")
					&& extracted_term.get(line.split("#")[1]) != null
					&& line.split("#")[0] != null)
				paths.put(extracted_term.get(line.split("#")[1]),
						line.split("#")[0]);
		}
	}

	private Double GetDistance(GeneralizedTransactionItem gI) {
		Vector<String> nodes = new Vector<String>();
		for (String i : gI.get_rule_data()) {
			String n = paths.get(i) + "#" + i;
			nodes = AddingNode(nodes, n);
		}

		Double diff = 0D;
		while (nodes.size() > 1) {
			diff += mesh.GetDisNode(nodes.get(0), nodes.get(1));
			nodes.remove(0);
		}

		return diff;
	}

	public void SetParameters(int k, double s, double alpha) {
		this.k = k;
		this.s = s;
		this.alpha = alpha;
	}

	public static void CreateTransactionDatasetFile(String dataFolder,
			String extractedWords, String resultFile) {
		String content = "";
		Vector<String> files;
		String[] lines;
		String fContent = "";

		Hashtable<String, String> wordlist = new Hashtable<String, String>();

			content = Utilities.readFileAsString(extractedWords);
		lines = content.split("\n");
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			wordlist.put(line.split("#")[1].toUpperCase(), line.split("#")[0]);
		}

		files = Utilities.GetFileInFolder(dataFolder);
		Utilities.WriteFile(resultFile, "", false);
		for (String file : files) {
			String tid = Utilities.SimpleRegexSingle("([0-9]*)", file, 1);
			if (tid.equals(""))
				continue;
			Vector<String> idList = new Vector<String>();
			
				fContent = Utilities.readFileAsString(dataFolder + "\\" + file);
			
			fContent = POSTagging.ClearDocument(fContent);
			fContent = Utilities.RemoveSpace(fContent);
			String[] words = fContent.split("[\\s\\.,;]");
			for (String w : words) {
				String k = wordlist.get(w.trim().toUpperCase());
				if (k != null) {
					if (!idList.contains(k)) {
						idList.add(k);
					}
				}
			}

			for (String id : idList) {
				tid += " " + id;
			}
			Utilities.WriteFile(resultFile, tid + "\n",
					true);
		}
	}

	private Double ComputeWeights(String key) {
		if (weights != null) {
			return weights.get(key);
		}
		weights = new Hashtable<String, Double>();
		extracted_term = new Hashtable<String, String>();
		extracted_term2 = new Hashtable<String, String>();

		System.out.println("Calculating weights...");
		String content = "";
		
			content = Utilities.readFileAsString(dataFolder + "extracted.txt");
		
		String[] lines = content.split("\n");
		double totalWeight = 0;
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			totalWeight += Double.parseDouble(line.split("#")[2]);
		}
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			String id = line.split("#")[0];
			String term = line.split("#")[1];
			double w = Double.parseDouble(line.split("#")[2]) / totalWeight;
			weights.put(id, w);
			extracted_term.put(term, id);
			extracted_term2.put(id, term);
			// for (int i = 0; i < D.getRecords().size(); i++) {
			// for (int j = 0; j < D.getRecords().get(i).GetData().size(); j++)
			// {
			// if (D.getRecords().get(i).GetData().get(j).getItem()
			// .equals(id)) {
			// D.getRecords().get(i).GetData().get(j).setItemWeight(w);
			// }
			// }
			// }
		}
		return weights.get(key);
	}

	private void LoadData(String file) {
		D = new TransactionDataset();
		String content = "";
		
			content = Utilities.readFileAsString(file);
		
		String[] lines = content.split("\n");
		System.out.println("Loading transactions...");
		for (String line : lines) {
			System.out.print(".");
			if (line.trim().equals(""))
				continue;
			Transaction t = new Transaction();
			t.setId(line.split(" ")[0]);
			for (int i = 1; i < line.split(" ").length; i++) {
				TransactionItem ti = new TransactionItem();
				ti.setItem(line.split(" ")[i]);
				ti.setItemWeight(ComputeWeights(line.split(" ")[i]));
				t.AddItem(ti);
			}
			D.AddRecord(t);
		}
	}

	private void LoadUtilityContraints(String file) {
		String content = "";
		
			content = Utilities.readFileAsString(file);
		
		String[] lines = content.split("\n");
		Us = new GeneralizedTransactionDataSet();
		oUs = new GeneralizedTransactionDataSet();
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			GeneralizedTransaction U = new GeneralizedTransaction();
			for (String i : line.split(" ")) {
				U.AddItem(new GeneralizedTransactionItem(i));
			}
			boolean isContained = false;
			for (int o = 0; o < Us.GetData().size(); o++) {
				if (Us.GetData().get(o).IsContainsAll(U.GetData())) {
					isContained = true;
					break;
				} else if (U.IsContainsAll(Us.GetData().get(o).GetData())) {
					if (U.GetData().size() > maxUSize) {
						maxUSize = U.GetData().size();
					}
					Us.GetData().set(o, U);
					isContained = true;
					break;
				}
			}
			if (isContained) {
				continue;
			}
			if (U.GetData().size() > maxUSize) {
				maxUSize = U.GetData().size();
			}
			Us.AddRecord(U);
			oUs.AddRecord(U);
		}
	}

	private void LoadPrivacyContraints(String file) {
		String content = "";
		
			content = Utilities.readFileAsString(file);
		
		String[] lines = content.split("\n");
		Ps = new GeneralizedTransactionDataSet();
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			GeneralizedTransaction P = new GeneralizedTransaction();
			for (String i : line.split(" ")) {
				P.AddItem(new GeneralizedTransactionItem(i));
			}
			Ps.AddRecord(P);
		}
	}

	private boolean CheckAllPrivacyContraints() {
		for (GeneralizedTransaction P : Ps.GetData()) {
			if (!CheckPrivacyContraint(P.GetData())) {
				return false;
			}
		}
		return true;
	}

	private boolean CheckPrivacyContraint(Vector<GeneralizedTransactionItem> P) {
		int i = 0;
		int sup = 0;
		for (GeneralizedTransaction p : Ps.GetData()) {
			if (p.GetData().size() == P.size() && p.IsContainsAll(P)) {
				sup = pc_supports.get(i);
				break;
			}
			i++;
		}
		// if (datachanged == false)
		// return false;
		// boolean res = checklist.contains(P.toString());
		// if (res) {
		// return checklist.get(P.toString());
		// }
		// int sup = gD.GetSupport(P);

		if (sup < k && sup > 0) {
			// checklist.put(P.toString(), false);
			return false;
		}
		// checklist.put(P.toString(), true);
		return true;
	}

	private void CalculatePrivacySupport() {
		for (GeneralizedTransaction P : Ps.GetData()) {
			int sup = gD.GetSupport(P.GetData());
			pc_supports.add(sup);
		}
		SortPrivacySupport();
	}

	public void SortPrivacySupport() {
		for (int i = 0; i < Ps.GetData().size(); i++) {
			for (int j = i + 1; j < Ps.GetData().size(); j++) {
				if (pc_supports.get(j) > pc_supports.get(i)) {
					GeneralizedTransaction temp = Ps.GetData().get(i);
					Ps.GetData().set(i, Ps.GetData().get(j));
					Ps.GetData().set(j, temp);
					int it = pc_supports.get(i);
					pc_supports.set(i, pc_supports.get(j));
					pc_supports.set(j, it);
				}
			}
		}
	}

	public void SortPrivacySupportItem(int k, int sup) {

		for (int i = k; i > 0; i--) {
			int j = i - 1;
			if (pc_supports.get(j) >= sup) {
				return;
			}
			GeneralizedTransaction temp = Ps.GetData().get(i);
			Ps.GetData().set(i, Ps.GetData().get(j));
			Ps.GetData().set(j, temp);
			int it = pc_supports.get(i);
			pc_supports.set(i, pc_supports.get(j));
			pc_supports.set(j, it);
		}
	}

	private Vector<GeneralizedTransactionItem> GetUnsatisfiedMaxSupport() {
		// int currentSupport = -1;
		int i = 0;
		for (GeneralizedTransaction P : Ps.GetData()) {
			int sup = pc_supports.get(i);

			if (sup < k && sup > 0) {
				// int sup = 0;
				// if (pc_supports.contains(P.toString())) {
				// sup = pc_supports.get(P.toString());
				// } else {
				// GeneralizedTransaction tP = P;
				// tP.FilterDuplication();
				// tP.RemoveEmpty();
				// if (tP.GetData().size() == 0)
				// continue;
				// sup = gD.GetSupport(P.GetData());
				// }
				// if (currentSupport < sup) {
				// pc_supports.put(P.toString(), sup);
				// currentSupport = sup;
				System.out.println("Processing: " + i * 100 / Ps.size() + "%");
				return P.GetData();
				// }
			}
			i++;
		}
		return null;
	}

	private void CreateUtilityFile(String dataFolder) {
		String content = "";
		
			content = Utilities.readFileAsString(dataFolder + "groups_" + alpha
					+ ".txt");
		
		String[] lines = content.split("\n");
		String utilities = "";
		for (String line : lines) {
			String u = "";
			for (String i : line.split("#")) {
				if (!i.trim().equals("") && extracted_term.get(i) != null)
					u += extracted_term.get(i) + " ";
			}
			utilities += u.trim() + "\n";
		}
		Utilities.WriteFile(dataFolder + "utility_constraints.txt", utilities,
				false);
	}

	private void ForcingSuppressingItem() {
		Vector<String> items = new Vector<String>();
		for (GeneralizedTransaction P : Ps.GetData()) {
			for (GeneralizedTransactionItem i : P.GetData()) {
				for (String ii : i.get_rule_data()) {
					if (!items.contains(ii)) {
						if (gD.GetSupport(ii) < k)
							items.add(ii);
					}
				}
			}
		}

		Vector<GeneralizedTransactionItem> uss = Us.GetDomain();
		GeneralizedTransaction ggt = new GeneralizedTransaction();
		ggt.AddItemAll(uss);

		for (String item : items) {
			GeneralizedTransactionItem gi = new GeneralizedTransactionItem();
			gi.AddItem(new TransactionItem(item));
			if (!ggt.IsContains(gi)) {
				Us.SuppressItem(item);
				Ps.SuppressItem(item);
				gD.SuppressItem(item);
				System.out.println("Suppressing: " + item);
			}
		}
	}

	public void CopyData(TransactionDataset d, GeneralizedTransactionDataSet g) {
		ogD = D.ConvertToGeneralizedForm();
	}

	public void run(String dataFolder) {
		this.dataFolder = dataFolder;
		LoadData(dataFolder + "transactions.txt");
		PGen2();
		CreateUtilityFile(dataFolder);

		gD = D.ConvertToGeneralizedForm();

		CopyData(D, gD);

		System.out.println("Loading Us and Ps...");
		LoadUtilityContraints(dataFolder + "utility_constraints.txt");
		LoadPrivacyContraints(dataFolder + "privacy_constraints_" + k + ".txt");
		Us.FilterDuplication();

		LoatPaths(dataFolder);
		CalculatePrivacySupport();

		// ForcingSuppressingItem();
		long a, b, f;
		b = Utilities.CurrentUnixTimeMili();
		while (true) {
			// boolean isProcess = false;
			// System.out.println(gD.toString());
			System.out.println("=========================");
			int r = 0;
			Vector<GeneralizedTransactionItem> p = GetUnsatisfiedMaxSupport();
			GeneralizedTransactionItem im = null;
			Vector<GeneralizedTransactionItem> items = null;
			if (p == null)
				break;
			do {
				if (items == null) {
					items = rmin(p);
				}
				if (items.size() <= r)
					im = null;
				else {
					im = new GeneralizedTransactionItem();
					im = items.get(r);
				}
				if (im == null)
					break;
				GeneralizedTransactionDataSet Um = GetUtilityByItem(im);
				boolean isGeneralized = false;

				if (Um.size() > 0) {
					System.out.println("Generalizing: " + im);
					// isProcess = true;
					countGeneralizations++;
					Generalize(im, Um);
					isGeneralized = true;
					items = null;
				}
				if (isGeneralized == false) {
					if (gD.GetSupport(im) < k) {
						System.out.println("Suppressing: " + im);
						// isProcess = true;
						items = null;
						countSuppressions++;
						Suppress(im);
						// p = GetUnsatisfiedMaxSupport();
						// if (p == null) {
						// break;
						// }
					} else {
						r++;
						// System.out.println("Increase size: " + r);
					}
				}

			} while (!CheckPrivacyContraint(p)
					&& NumberOfUtilityContraint(p) < D.getDomain().GetData().size());

			im = null;
			items = null;
			if (p != null) {
				r = 0;
				if (!CheckPrivacyContraint(p)) {
					// && NumberOfUtilityContraint(p) == D.getDomain().size()) {
					// while (!CheckPrivacyContraint(p)) {
					if (items == null) {
						items = rmin(p);
					}
					if (items.size() <= r)
						im = null;
					else {
						im = new GeneralizedTransactionItem();
						im = items.get(r);
					}
					if (im != null) {
						System.out.println("Suppressing: " + im);
						// /isProcess = true;
						Suppress(im);
					}
					// }
				}
			}
		}
		a = Utilities.CurrentUnixTimeMili();
		f = a - b;
		timetorun = f;
		Utilities.WriteFile(dataFolder + "result_" + k + ".txt", gD.toString(),
				false);

	}

	private void PGen2() {
		if (Utilities.IsFileExist(dataFolder + "privacy_constraints_" + k
				+ ".txt")) {
			return;
		}
		String content = "";
		for (TransactionItem i : D.getDomain().GetData()) {
			if (D.GetSupport(i) < k) {
				content += i.getItem() + "\n";
			}
		}
		Utilities.WriteFile(dataFolder + "privacy_constraints_" + k + ".txt",
				content, false);
	}

	private void Suppress(GeneralizedTransactionItem im) {

		ss.add(new GeneralizedTransactionItem(im.get_rule().get(0).getItem()));

		double total = gD.NumberOfItems();
		double res_s = gD.SuppressItem(im.get_rule_data()) / total;
		Ps.SuppressItem(im.get_rule_data());
		Ps.RemoveEmpty();
		Us.SuppressItem(im.get_rule_data());
		Us.RemoveEmpty();
		if (res_s > s) {
			System.out.println("Error: U is violated");
		}
		for (int i = 0; i < Ps.GetData().size(); i++) {
			if (Ps.GetData().get(i).IsContains(im)) {
				int sup = gD.GetSupport(Ps.GetData().get(i).GetData());
				pc_supports.set(i, sup);
				SortPrivacySupportItem(i, sup);
			}
		}

	}

	private void Generalize(GeneralizedTransactionItem im,
			GeneralizedTransactionDataSet um) {

		for (int i = 0; i < um.GetData().size(); i++) {
			for (int j = 0; j < um.GetData().get(i).GetData().size(); j++) {
				if (um.GetData().get(i).GetData().get(j).IsContain(im)) {
					um.GetData().get(i).GetData().remove(j);
					j--;
				}
			}
		}
		GeneralizedTransactionItem is = MinUL(im, um);

		GeneralizedTransactionItem cur = new GeneralizedTransactionItem();
		Vector<String> pp = new Vector<String>();
		pp.addAll(is.get_rule_data());
		pp.addAll(im.get_rule_data());
		cur.AddItemData(pp);

		gD.Generalize(cur);// TODO: need to be optimized

		Ps.Generalize(cur);
		// Ps.FilterDuplication();
		Us.Generalize(cur);
		Us.FilterDuplication();

		for (int i = 0; i < Ps.GetData().size(); i++) {
			if (Ps.GetData().get(i).IsContains(im)) {
				int sup = gD.GetSupport(Ps.GetData().get(i).GetData());
				pc_supports.set(i, sup);
				SortPrivacySupportItem(i, sup);
			}
		}
	}

	private GeneralizedTransactionItem MinUL(GeneralizedTransactionItem im,
			GeneralizedTransactionDataSet um) {
		Vector<Double> uls = new Vector<Double>();
		Vector<GeneralizedTransactionItem> checkingitem = new Vector<GeneralizedTransactionItem>();
		for (GeneralizedTransaction j : um.GetData()) {
			for (GeneralizedTransactionItem i : j.GetData()) {
				if (im.IsThis(i))
					continue;
				boolean similar = false;
				for (GeneralizedTransactionItem cki : checkingitem) {
					if (cki.IsThis(im)) {
						similar = true;
					}
				}
				if (similar)
					continue;
				checkingitem.add(i);
				GeneralizedTransactionItem cur = new GeneralizedTransactionItem();
				Vector<String> pp = new Vector<String>();
				if (i.get_rule_data().containsAll(im.get_rule_data()))
					continue;
				pp.addAll(i.get_rule_data());
				pp.addAll(im.get_rule_data());
				cur.AddItemData(pp);
				uls.add(UtilityLossG(cur));
			}
		}

		while (true) {
			boolean isFinished = true;
			for (int i = 0; i < uls.size() - 1; i++) {
				if (uls.get(i) > uls.get(i + 1)) {
					Double temp1 = uls.get(i);
					uls.set(i, uls.get(i + 1));
					uls.set(i + 1, temp1);

					GeneralizedTransactionItem temp2 = checkingitem.get(i);
					checkingitem.set(i, checkingitem.get(i + 1));
					checkingitem.set(i + 1, temp2);
					isFinished = false;
					break;
				}
			}
			if (isFinished)
				break;
		}
		return checkingitem.get(0);
	}

	private double UtilityLossG(GeneralizedTransactionItem im) {
		double res = 0;
		Double d = 1D;

		Double local = 1D;
		if (im.get_rule().size() > 1) {
			d = GetDistance(im);
			local = Math.pow(2, im.get_rule().size()) / Math.pow(2, maxUSize);
		}
		im.CalculateAverageWeight();
		Double w = 1D / Math.exp(im.get_ruleWeight());
		Double support = 1D;
		// (double) ogD.GetSupport(im)
		// / (double) D.getRecords().size();
		res = local * w * d * support;
		return res;
	}

	private double UtilityLossS(GeneralizedTransactionItem im) {
		double res = 0;
		GeneralizedTransactionItem newI = new GeneralizedTransactionItem();
		for (GeneralizedTransaction t : oUs.GetData()) {
			if (t.IsContains(im)) {
				for (GeneralizedTransactionItem iii : t.GetData()) {
					newI.AddItemData(iii.get_rule_data());
				}
				break;
			}
		}
		if (newI.size() > 0)
			res = UtilityLossG(newI);
		else {
			for (TransactionItem iii : im.get_rule()) {
				res += iii.getItemWeight();
			}
		}
		return res;
	}

	private GeneralizedTransactionDataSet GetUtilityByItem(
			GeneralizedTransactionItem im) {
		GeneralizedTransactionDataSet res = new GeneralizedTransactionDataSet();
		for (GeneralizedTransaction U : Us.GetData()) {
			if (U.GetData().size() <= 1)
				continue;
			if (U.IsContains(im)) {
				res.AddRecord(U);
			}
		}
		return res;
	}

	private Vector<GeneralizedTransactionItem> rmin(
			Vector<GeneralizedTransactionItem> p) {
		Vector<Integer> supports = new Vector<Integer>();
		Vector<GeneralizedTransactionItem> items = new Vector<GeneralizedTransactionItem>();
		for (GeneralizedTransactionItem pi : p) {
			if (pi.get_rule().size() == 0)
				continue;
			supports.add(gD.GetSupport(pi));
			items.add(pi);
		}
		while (true) {
			boolean isFinished = true;
			for (int i = 0; i < items.size() - 1; i++) {
				if (supports.get(i) > supports.get(i + 1)) {
					int temp1 = supports.get(i);
					supports.set(i, supports.get(i + 1));
					supports.set(i + 1, temp1);

					GeneralizedTransactionItem temp2 = items.get(i);
					items.set(i, items.get(i + 1));
					items.set(i + 1, temp2);
					isFinished = false;
					break;
				}
			}
			if (isFinished)
				break;
		}
		return items;

	}

	private int NumberOfUtilityContraint(Vector<GeneralizedTransactionItem> p) {
		int res = 0;
		for (GeneralizedTransaction U : Us.GetData()) {
			for (GeneralizedTransactionItem Ui : U.GetData()) {
				boolean isContain = true;
				for (GeneralizedTransactionItem pi : p) {
					if (!Ui.IsContain(pi)) {
						isContain = false;
					}
				}
				if (isContain)
					res++;
			}
		}
		return res;
	}

	public static void CreateRandomData(String folder, int noRulesf,
			int minSize, int maxSize, String output) {
		String content = "";
		
			content = Utilities.readFileAsString(folder + "extracted.txt");
		
		String[] lines = content.split("\n");
		content = "";
		int noRules = noRulesf;// (int) (lines.length * noRulesf);
		Vector<String> selectedTerms = new Vector<String>();
		for (int i = 0; i < noRules; i++) {
			boolean isF = true;
			Random r = new Random();
			int size = r.nextInt(maxSize - minSize) + minSize;
			for (int j = 0; j <= size; j++) {

				String line = "";
				do {
					line = lines[r.nextInt(lines.length)];
					if (selectedTerms.contains(line)) {
						continue;
					} else {
						selectedTerms.add(line);
					}
				} while (line.trim().equals(""));

				if (line.trim().equals(""))
					continue;
				if (isF) {
					content += line.split("#")[0];
				} else {
					content += " " + line.split("#")[0];
				}
				isF = false;

			}
			content += "\n";
		}
		Utilities.WriteFile(folder + output, content, false);
	}

	Vector<String> ugen_nodes = new Vector<String>();

	public void UGenConstructor(String folder, Double groupTH) {
		String content = "";
		
			content = Utilities.readFileAsString(folder + "extracted.txt");
		
		String[] lines = content.split("\n");

		MeSHUtility mesh = new MeSHUtility();

		int counter = 0;
		for (int k = 0; k < lines.length; k++) {
			String line = lines[k];
			counter++;
			// if (counter == 100) {
			// break;
			// }
			System.out.println("Classifier: " + counter + "/" + lines.length
					+ "(" + line.split("#")[1] + ")");
			Vector<String> tns = mesh.GetNodes2(line.split("#")[1]);
			String checkingTerm = line.split("#")[1];
			boolean isChanged = false;
			while (tns.size() == 0 && checkingTerm.indexOf(" ") > 0) {
				isChanged = true;
				String term = checkingTerm;
				checkingTerm = "";
				boolean isF = true;
				for (String w : term.split(" ")) {
					if (isF == true) {
						isF = false;
						continue;
					}
					checkingTerm += w + " ";
				}
				checkingTerm = checkingTerm.trim();
				System.out.println("Classifier: " + counter + "/"
						+ lines.length + "(" + checkingTerm + ")");
				tns = mesh.GetNodes(checkingTerm);
			}
			if (tns.size() > 0 && isChanged) {
				lines[k] = line.split("#")[0] + "#" + checkingTerm + "#"
						+ line.split("#")[2];
			}
			// if (tns.size() == 0) {
			// Vector<String> gt = new Vector<String>();
			// gt.add(line.split("#")[1]);
			// groups.add(gt);
			// }
			for (String item : tns) {
				ugen_nodes = AddingNode3(ugen_nodes, item);
			}
		}

		Utilities.WriteFile(folder + "extracted.txt", "", false);
		for (String line : lines) {
			// System.out.println(t.Term + " - " + t.Cvalue);
			Utilities.WriteFile(folder + "extracted.txt", line + "\n", true);
		}
		UGenGenerator2(folder, groupTH);
	}

	private void UGenGenerator2(String folder, Double groupTH) {
		Vector<Vector<String>> ugen_groups = new Vector<Vector<String>>();
		String gfile = folder + "groups_" + groupTH + ".txt";
		String pfile = folder + "path_" + groupTH + ".txt";
		Vector<String> g = new Vector<String>();

		while (ugen_nodes.size() > 0) {
			Vector<String> checkingList = new Vector<String>();
			int listSize = 0;
			if (ugen_nodes.size() >= 10) {
				listSize = 10;
			} else {
				listSize = ugen_nodes.size();
			}
			for (int i = 0; i < listSize; i++) {
				checkingList.add(ugen_nodes.get(i));
			}
			for (int i = 0; i < listSize; i++) {

				// if (i == listSize - 1) {
				// g.add(ugen_nodes.get(0));
				// ugen_nodes.remove(0);
				// ugen_groups.add(g);
				// g = new Vector<String>();
				// }
				// continue;
				// }
				Double dis = 0D;
				if (checkingList.get(0).split("#")[0].equals(checkingList.get(
						checkingList.size() - 1 - i).split("#")[0])) {
					dis = 0D;
				} else {
					dis = CalculateSemanticDistance(
							checkingList.get(0).split("#")[0], checkingList
									.get(checkingList.size() - 1 - i)
									.split("#")[0]);
				}
				if (dis > groupTH) {
					if (i == listSize - 1) {
						g.add(ugen_nodes.get(0));
						ugen_nodes.remove(0);
						ugen_groups.add(g);
						g = new Vector<String>();
						break;
					}
					continue;
				} else {
					for (int k = 0; k <= i; k++) {
						if (ugen_nodes.size() > 0) {
							g.add(ugen_nodes.get(0));
							ugen_nodes.remove(0);
						}
					}
					ugen_groups.add(g);
					g = new Vector<String>();
				}
			}
		}

		String fc = "";
		String path_str = "";
		for (Vector<String> gi : ugen_groups) {
			String wline = "#";
			if (gi.size() > 1) {
				for (String ii : gi) {
					if (wline.indexOf("#" + ii.split("#")[1] + "#") == -1)
						wline += ii.split("#")[1] + "#";
					path_str += ii + "\n";
				}
				fc += wline.trim() + "\n";
			}
		}
		Utilities.WriteFile(gfile, fc, false);
		Utilities.WriteFile(pfile, path_str, false);
	}

	// private void UGenGenerator(String folder, Double groupTH) {
	// String gfile = folder + "groups_" + groupTH + ".txt";
	// String pfile = folder + "path_" + groupTH + ".txt";
	// Vector<String> g = new Vector<String>();
	// double totalSize = 0;
	// Vector<String> temp_ugen_nodes = (Vector<String>) ugen_nodes.clone();
	// while (ugen_nodes.size() > 0) {
	// if (ugen_nodes.get(0).split("#")[1].equals(ugen_nodes.get(1).split(
	// "#")[1])) {
	// ugen_nodes.remove(0);
	// if (ugen_nodes.size() == 1) {
	// if (g.size() == 1) {
	// g = mesh.GetsliblingList(g.get(0));
	// }
	// ugen_groups.add(g);
	// break;
	// }
	// continue;
	// }
	// Double diff = mesh.GetDisNode(ugen_nodes.get(0), ugen_nodes.get(1));
	// totalSize += diff;
	// if (diff > groupTH) {// || totalSize > groupTH * maxSize) {
	// String addingT = ugen_nodes.get(0);
	// g.add(addingT);
	// for (int i = 0; i < ugen_nodes.size(); i++) {
	// if (ugen_nodes.get(i).split("#")[1].equals(addingT
	// .split("#")[1])) {
	// ugen_nodes.remove(i);
	// i--;
	// }
	// }
	// // if (g.size() == 1) {
	// // g = mesh.GetsliblingList(g.get(0));
	// // }
	// ugen_groups.add(g);
	// g = new Vector<String>();
	// totalSize = 0;
	// if (ugen_nodes.size() == 1) {
	// g.add(ugen_nodes.get(0));
	// if (g.size() == 1) {
	// g = mesh.GetsliblingList(g.get(0));
	// }
	// ugen_groups.add(g);
	// break;
	// }
	// } else {
	// String addingT = ugen_nodes.get(0);
	// g.add(addingT);
	// for (int i = 0; i < ugen_nodes.size(); i++) {
	// if (ugen_nodes.get(i).split("#")[1].equals(addingT
	// .split("#")[1])) {
	// ugen_nodes.remove(i);
	// i--;
	// }
	// }
	// if (ugen_nodes.size() == 1) {
	// g.add(ugen_nodes.get(0));
	// if (g.size() == 1) {
	// g = mesh.GetsliblingList(g.get(0));
	// }
	// ugen_groups.add(g);
	// break;
	// }
	// if (ugen_nodes.size() == 0) {
	// if (g.size() == 1) {
	// g = mesh.GetsliblingList(g.get(0));
	// }
	// ugen_groups.add(g);
	// break;
	// }
	// }
	// }
	// String fc = "";
	// String path_str = "";
	// for (Vector<String> gi : ugen_groups) {
	// String wline = "";
	// if (gi.size() > 1) {
	// for (String ii : gi) {
	// wline += ii.split("#")[1] + "#";
	// path_str += ii + "\n";
	// }
	// fc += wline.trim() + "\n";
	// }
	// }
	// Utilities.WriteFile(gfile, fc, false);
	// Utilities.WriteFile(pfile, path_str, false);
	// ugen_nodes = temp_ugen_nodes;
	// }

	private static Vector<String> AddingNode2(Vector<String> nodes, String n) {
		String[] ps = n.split("#")[0].split("\\.");// split adding node into
													// parts for comparing
		String AddingString = ps[0];// we compare from the root part to the
									// branch
		int lastState = -1;// if in comparing phase, last step =1 and the
							// comparing is not matched, we insert to that
							// position
		int currentComparing = 0;// current part of path we are comparing
		for (int i = 0; i < nodes.size(); i++) {
			String comparingPart = "";// we should construct current part of
										// comparing into this variable
			for (int j = 0; j < nodes.get(i).split("#")[0].split("\\.").length; j++) {
				if (j == 0) {
					comparingPart += nodes.get(i).split("#")[0].split("\\.")[j];
				} else {
					comparingPart += "."
							+ nodes.get(i).split("#")[0].split("\\.")[j];
				}
			}

			if (comparingPart.equals(AddingString))// if same we move to the
													// next smaller branch
			{
				lastState = 1;
				currentComparing++;
				AddingString += "." + ps[currentComparing];
				i--;
				continue;
			} else// if not the same, we should check if last step = 1 or not.
					// it will help us have a decision to add the node
			{
				if (lastState == 1)// we add it and return
				{
				} else// reset and do it again from next item in nodelist
				{
				}
			}
		}
		nodes.add(n);
		return nodes;
	}

	private static Double CalculateSemanticDistance(String node1, String node2) {
		return Math.log((CalculatePath(node1, node2) - 1)
				* (maxDepth - DepthLCS(node1, node2)) + 2);
	}

	private static int DepthLCS(String node1, String node2) {
		String[] p1 = node1.split("\\.");
		String[] p2 = node2.split("\\.");
		int res = 0;
		for (int i = 0; i < Math.min(p1.length, p2.length); i++) {
			if (!p1[i].equals(p2[i])) {
				return res;
			} else {
				res++;
			}
		}
		return res;
	}

	private static int CalculatePath(String node1, String node2) {
		String[] p1 = node1.split("\\.");
		String[] p2 = node2.split("\\.");
		int res = 0;
		int diff = Math.max(p1.length, p2.length)
				- Math.min(p1.length, p2.length);
		for (int i = 0; i < Math.min(p1.length, p2.length); i++) {
			if (!p1[i].equals(p2[i])) {
				res++;
			}
		}

		return res + diff;
	}

	static int maxDepth = 12;

	private static Vector<String> AddingNode3(Vector<String> nodes, String n) {
		// if (n.split("#")[0].split("\\.").length > maxDepth) {
		// maxDepth = n.split("#")[0].split("\\.").length;
		// }
		Double curN = CounvertNodeToNumber(n.split("#")[0]);
		Vector<String> newnodes = new Vector<String>();
		for (int i = 0; i < nodes.size(); i++) {
			Double nextN = CounvertNodeToNumber(nodes.get(i).split("#")[0]);
			if (curN < nextN) {
				newnodes.add(n);
				for (int j = i; j < nodes.size(); j++) {
					newnodes.add(nodes.get(j));
				}
				return newnodes;
			}
			newnodes.add(nodes.get(i));
		}
		nodes.add(n);
		return nodes;
	}

	public static Double CounvertNodeToNumber(String n) {
		String[] lettersCategory = new String[] { "Q", "W", "E", "R", "T", "Y",
				"U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K",
				"L", "Z", "X", "C", "V", "B", "N", "M" };
		String[] numsCategory = new String[] { "100", "200", "300", "400",
				"500", "600", "700", "800", "900", "1000", "1100", "1200",
				"1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000",
				"2100", "2200", "2300", "2400", "2500", "2600" };
		String m = n;
		for (int i = 0; i < lettersCategory.length; i++) {
			m = m.replace(lettersCategory[i], numsCategory[i]);
		}

		Double AddingStringToNumber = 0D;
		for (int i = 0; i < m.split("\\.").length; i++) {
			int k = -3 * i;
			AddingStringToNumber += Double.parseDouble(m.split("\\.")[i])
					* Math.pow(10, k);
		}
		return AddingStringToNumber;
	}

	private static Vector<String> AddingNode(Vector<String> nodes, String n) {
		String[] ps = n.split("#")[0].split("\\.");

		int comparingPart = 0;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).split("#")[0].split("\\.").length <= comparingPart
					|| ps.length <= comparingPart) {
				nodes.add(n);
				for (int j = nodes.size() - 1; j > i; j--) {
					nodes.set(j, nodes.get(j - 1));
				}
				nodes.set(i, n);
				return nodes;
			}
			String curPart = nodes.get(i).split("#")[0].split("\\.")[comparingPart];
			int curPathLength = Integer.parseInt(Utilities.SimpleRegexSingle(
					"(\\d+)", curPart, 1));
			int nLength = Integer.parseInt(Utilities.SimpleRegexSingle(
					"(\\d+)", ps[comparingPart], 1));
			if (comparingPart == 0) {
				String let1 = curPart.substring(0, 1);
				String let2 = ps[comparingPart].substring(0, 1);
				if (!let1.equals(let2)) {
					continue;
				}
			}
			if (nLength > curPathLength) {
				continue;
			} else if (nLength == curPathLength) {
				i--;
				comparingPart++;
			} else {
				nodes.add(n);
				for (int j = nodes.size() - 1; j > i; j--) {
					nodes.set(j, nodes.get(j - 1));
				}
				nodes.set(i, n);
				return nodes;
			}
			// else if (i == nodes.size() - 1)
			// {
			// nodes.add(nodes.get(i));
			// nodes.set(i, n);
			// return nodes;
			// }
		}
		nodes.add(n);
		return nodes;
	}

	public void PGen(String folder, TransactionDataset tD) {
		TransactionDataset res = D;
		// for (int i = 0; i < res.getRecords().size(); i++) {
		// for (int j = i + 1; j < res.getRecords().size(); j++) {
		// if (res.getRecords().get(i).GetData().size() < res.getRecords()
		// .get(j).GetData().size()) {
		// Transaction t = res.getRecords().get(i);
		// res.getRecords().set(i, res.getRecords().get(j));
		// res.getRecords().set(j, t);
		// }
		// }
		// }

		// for (int i = 0; i < res.getRecords().size(); i++) {
		// for (int j = i + 1; j < res.getRecords().size(); j++) {
		// if (res.getRecords().get(j)
		// .IsContainsAll(res.getRecords().get(i).GetData())) {
		// res.getRecords().remove(j);
		// j--;
		// }
		// }
		// if (res.GetSupport(res.getRecords().get(i).GetData()) > k) {
		// res.getRecords().remove(i);
		// i--;
		// }
		// }

		String f = "";
		for (Transaction t : res.getRecords()) {
			String line = "";
			for (TransactionItem i : t.GetData()) {
				f += i.getItem() + " ";
			}
			f += line.trim() + "\n";
		}
		Utilities.WriteFile(folder + "..\\privacy_constraints_" + k + ".txt",
				f, false);
	}

	public static void ClassifyTerms(String folder, Double groupTH) {
		String content = "";
		
			content = Utilities.readFileAsString(folder + "extracted.txt");
		
		String gfile = folder + "groups_" + groupTH + ".txt";
		String[] lines = content.split("\n");
		Vector<Vector<String>> groups = new Vector<Vector<String>>();

		MeSHUtility mesh = new MeSHUtility();
		// mesh.LoadIgnoreWordDistance(folder);

			content = Utilities.readFileAsString(gfile);
			String[] glines = content.split("\n");
			for (String line : glines) {
				if (line.trim().equals(""))
					continue;
				Vector<String> g = new Vector<String>();
				for (String i : line.split(" ")) {
					g.add(i);
				}
				groups.add(g);
			}
		

		int counter = 0;
		for (String line : lines) {
			counter++;
			System.out.println("Classifier: " + counter + "/" + lines.length);
			if (line.trim().equals(""))
				continue;
			if (groups.size() == 0) {
				Vector<String> g = new Vector<String>();
				g.add(line.split("#")[1]);
				groups.add(g);
				continue;
			}

			boolean isAdded = false;

			for (Vector<String> i : groups) {
				for (String j : i) {
					if (line.split("#")[1].equals(j)) {
						isAdded = true;
						break;
					}
				}
				if (isAdded) {
					break;
				}
			}
			if (isAdded) {
				continue;
			}

			for (int i = 0; i < groups.size(); i++) {
				Double dif = mesh.GetMinDistance(line.split("#")[1], groups
						.get(i).get(0));
				if (dif == -1D) {
					break;
				}
				if (dif < groupTH) {
					groups.get(i).add(line.split("#")[1]);

					// String fc = "";
					// for (Vector<String> gi : groups) {
					// String wline = "";
					// for (String ii : gi) {
					// wline += ii + " ";
					// }
					// fc += wline.trim() + "\n";
					// }
					// Utilities.WriteFile(gfile, fc, false);

					break;
				}

				if (i == groups.size() - 1) {
					Vector<String> g = new Vector<String>();
					g.add(line.split("#")[1]);
					groups.add(g);

					// String fc = "";
					// for (Vector<String> gi : groups) {
					// String wline = "";
					// for (String ii : gi) {
					// wline += ii + " ";
					// }
					// fc += wline.trim() + "\n";
					// }
					// Utilities.WriteFile(gfile, fc, false);

					break;
				}
			}
		}
	}

	public Double GetInfoLoss() {
		System.out.println("Calculate information loss");
		Vector<GeneralizedTransactionItem> gs = new Vector<GeneralizedTransactionItem>();
		for (GeneralizedTransaction tr : gD.GetData()) {
			for (GeneralizedTransactionItem i : tr.GetData()) {
				if (i.get_rule_data().size() < 2)
					continue;
				boolean found = false;
				for (GeneralizedTransactionItem g : gs) {
					if (g.IsContainAll(i)) {
						found = true;
						break;
					}
				}
				if (found == false) {
					gs.add(i);
				}
			}
		}

		countSuppressions = 0;
		Double ul = 0D;
		String result2 = "";
		for (GeneralizedTransactionItem i : ss) {
			for (String ii : i.get_rule_data()) {
				if (extracted_term2.get(ii) == null)
					continue;
				result2 += "Suppress: " + extracted_term2.get(ii) + "\n";
				countSuppressions++;
			}
			ul += UtilityLossS(i);
		}
		countGeneralizations = 0;
		for (GeneralizedTransactionItem i : gs) {
			result2 += "Generalize: ";
			for (String ii : i.get_rule_data()) {
				result2 += extracted_term2.get(ii) + ",";
				countGeneralizations++;
			}
			result2 += "\n";
			ul += UtilityLossG(i);
		}

			String content = Utilities.readFileAsString(dataFolder
					+ "transactions.txt");
			String[] lines = content.split("\n");
			for (String line : lines) {
				System.out.print(".");
				if (line.trim().equals(""))
					continue;
				String docFile = line.split(" ")[0];
				String docCon = Utilities
						.readFileAsString(dataFolder + docFile);
				docCon = docCon.toUpperCase();
				for (GeneralizedTransactionItem i : ss) {
					for (String ii : i.get_rule_data()) {
						if (extracted_term2.get(ii) == null)
							continue;
						docCon = docCon.replaceAll("[,\\.\\s:;]"
								+ extracted_term2.get(ii).toUpperCase()
										.replace(".", "\\.") + "[,\\.\\s:;]",
								"_____");
					}
				}

				for (GeneralizedTransactionItem i : gs) {
					String rule = "";
					String gen = "[";
					for (String ii : i.get_rule_data()) {
						if (extracted_term2.get(ii) == null)
							continue;
						if (rule.equals("")) {
							rule += "[,\\.\\s:;]"
									+ extracted_term2.get(ii).toUpperCase()
											.replace(".", "\\.")
									+ "[,\\.\\s:;]";
							gen += extracted_term2.get(ii).toUpperCase();
						} else {
							rule += "|"
									+ "[,\\.\\s:;]"
									+ extracted_term2.get(ii).toUpperCase()
											.replace(".", "\\.")
									+ "[,\\.\\s:;]";
							gen += "," + extracted_term2.get(ii).toUpperCase();
						}
					}
					gen += "]";
					docCon = docCon.replaceAll(rule, gen);
				}
				Utilities.WriteFile(dataFolder + docFile + "_ano_" + k, docCon,
						false);
			}
		

		Utilities.WriteFile(dataFolder + "result2_" + k + ".txt", result2,
				false);
		return ul / (double) D.getDomain().GetData().size();
	}

	public static void ExtractPrivacy(String dataFolder2, int i) {
		// TODO Auto-generated method stub

	}
}
