package algorithms;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import DataStructure.GeneralizedTransaction;
import DataStructure.GeneralizedTransactionDataSet;
import DataStructure.GeneralizedTransactionItem;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import utility.Utilities;

import dataset.MeshLib.MeSHUtility;

public class COAT2 {

	public TransactionDataset D;
	int k = 5;
	double s = 1;
	GeneralizedTransactionDataSet gD;
	GeneralizedTransactionDataSet ogD;
	GeneralizedTransactionDataSet Ps;
	GeneralizedTransactionDataSet Us;
	GeneralizedTransactionDataSet oUs;
	Hashtable<String, String> extracted_term;
	Hashtable<String, String> extracted_term2;
	Vector<GeneralizedTransactionItem> ss = new Vector<GeneralizedTransactionItem>();
	private Vector<Integer> pc_supports = new Vector<Integer>();
	public int countGeneralizations = 0;
	public int countSuppressions = 0;
	Vector<String> suppressedList = new Vector<String>();
	Hashtable<String, Double> weights = null;
	public long timetorun = 0;
	int maxUSize = 0;

	public void SetParameters(int k, double s) {
		this.k = k;
		this.s = s;
	}

	public Vector<String> GetSuppressedItems() {
		return suppressedList;
	}

	public Vector<Vector<String>> GetGeneralizeditems() {
		Vector<Vector<String>> res = new Vector<Vector<String>>();
		for (GeneralizedTransaction gt : gD.GetData())
		{
			for (GeneralizedTransactionItem gti : gt.GetData())
			{
				if (gti.get_rule_data().size() > 0)
				{
					boolean add = true;
					for (int i=0;i<res.size();i++)
					{
						Vector<String> rule = res.get(i);
						if (rule.containsAll(gti.get_rule_data()))
						{
							add = false;
							break;
						}	
						else if (gti.get_rule_data().containsAll(rule))
						{
							res.set(i, gti.get_rule_data());
							add = false;
							break;
						}
					}
					if (add == true)
						res.add(gti.get_rule_data());
				}
			}
		}
		return res;
	}

	public void LoadUtilityContraints(String file) {
		String content = "";

		content = Utilities.readFileAsString(file);

		String[] lines = content.split("\n");
		Us = new GeneralizedTransactionDataSet();
		oUs = new GeneralizedTransactionDataSet();
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			GeneralizedTransaction U = new GeneralizedTransaction();
			for (String i : line.split(", ")) {
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

	public void LoadPrivacyContraints(String file) {
		String content = "";

		content = Utilities.readFileAsString(file);

		String[] lines = content.split("\n");
		Ps = new GeneralizedTransactionDataSet();
		for (String line : lines) {
			if (line.trim().equals(""))
				continue;
			GeneralizedTransaction P = new GeneralizedTransaction();
			for (String i : line.split(", ")) {
				P.AddItem(new GeneralizedTransactionItem(i));
			}
			Ps.AddRecord(P);
		}
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

		if (sup < k && sup >= 0) {
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

	public void SortPrivacySupportItem2() {
		for (int i = 0; i > pc_supports.size(); i++) {
			for (int j = i; j > pc_supports.size(); j++) {
				if (pc_supports.get(j) > pc_supports.get(i)) {
					Collections.swap(pc_supports, i, j);
					Collections.swap(Ps.GetData(), i, j);
				}
			}
		}
	}

	private Vector<GeneralizedTransactionItem> GetUnsatisfiedMaxSupport() {
		// int currentSupport = -1;
		int i = 0;
		for (GeneralizedTransaction P : Ps.GetData()) {
			int sup = pc_supports.get(i);

			if (sup < k && sup >= 0) {
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
				boolean is_continue = false;
				for (int l = 0; l < i; l++) {
					GeneralizedTransaction P2 = Ps.GetData().get(l);
					if (P2.IsContainsAll(P.GetData())
							&& P.IsContainsAll(P2.GetData())) {
						pc_supports.set(i, pc_supports.get(l));
						is_continue = true;
						break;
					}
				}
				if (is_continue)
					continue;
				System.out.println("Processing: " + i * 100 / Ps.size() + "%");
				return P.GetData();
				// }
			}
			i++;
		}
		return null;
	}

	public void CopyData(TransactionDataset d, GeneralizedTransactionDataSet g) {
		ogD = D.ConvertToGeneralizedForm();
	}

	private void suprpess_non_english_terms() {
		for (GeneralizedTransaction P : Ps.GetData()) {
			for (GeneralizedTransactionItem pp : P.GetData()) {
				for (TransactionItem ppp : pp.get_rule()) {
					if (!Utilities.SimpleRegexSingle("([^0-9a-zA-Z])",
							ppp.getItem(), 1).equals("")) {
						Suppress(pp);
						break;
					}
				}
			}
		}
	}

	public void run(String result_file) {
		gD = D.ConvertToGeneralizedForm();
		CopyData(D, gD);
		CalculatePrivacySupport();

		// ForcingSuppressingItem();
		long a, b, f;
		b = Utilities.CurrentUnixTimeMili();

		suprpess_non_english_terms();

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
					//System.out.println("Generalizing: " + im);
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
					&& NumberOfUtilityContraint(p) < D.getDomain().GetData()
							.size());

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
			Utilities.WriteFile(result_file, "Time to run: " + timetorun + "\n",
				false);
		for (int i = 0; i < D.getRecords().size(); i++) {
			Utilities.WriteFile(result_file, D.getRecords().get(i).getId()
					+ gD.GetData().get(i).toString() + "\n", true);
		}
	}

	private void Suppress(GeneralizedTransactionItem im) {

		for (TransactionItem si : im.get_rule()) {
			if (suppressedList.contains(si.getItem()) == false)
				suppressedList.add(si.getItem());
		}
		
		GeneralizedTransactionItem temp = new GeneralizedTransactionItem();
		temp.set_rule(im.get_rule());
		ss.add(new GeneralizedTransactionItem(im.get_rule().get(0).getItem()));

		double total = gD.NumberOfItems();
		double res_s = gD.SuppressItem(im.get_rule_data()) / total;
		Ps.SuppressItem(im.get_rule_data());
		Vector<Integer> suppressed_items = Ps.GetSuppressedItems();
		// Ps.RemoveEmpty();
		Us.SuppressItem(im.get_rule_data());
		Us.RemoveEmpty();
		if (res_s > s) {
			System.out.println("Error: U is violated");
		}
		for (int i : suppressed_items) {
			int sup = gD.GetSupport(Ps.GetData().get(i).GetData());
			pc_supports.set(i, sup);
			SortPrivacySupportItem2();
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

		System.out.println("Generalize: " + cur.toString());
		
		gD.Generalize(cur);// TODO: need to be optimized

		Ps.Generalize(cur);
		// Ps.FilterDuplication();
		Us.Generalize(cur);
		Us.FilterDuplication();

		for (int i = 0; i < Ps.GetData().size(); i++) {
			if (Ps.GetData().get(i).IsContains(im)) {
				int sup = gD.GetSupport(Ps.GetData().get(i).GetData());
				pc_supports.set(i, sup);
				SortPrivacySupportItem2();
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
}
