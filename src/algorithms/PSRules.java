package algorithms;

import java.util.Vector;

import DataStructure.GeneralizedTransactionDataSet;
import DataStructure.GeneralizedTransactionItem;
import DataStructure.GeneralizedTransactionRule;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import DataStructure.TransactionRule;
import DataStructure.Tree;

public class PSRules {
	public int k;
	public float c;
	public Vector<Vector<String>> records;
	public Vector<Vector<String>> result_records;
	public Vector<String> ItemSet;
	public Vector<TransactionRule> rules;
	public Tree<String> ontology;

	public GeneralizedTransactionDataSet TreeBase(GeneralizedTransactionItem gi,
			GeneralizedTransactionDataSet gD, TransactionDataset D,
			Vector<GeneralizedTransactionRule> R, int k, double c) {
		if (gi.size() == 1)
			return gD;
		GeneralizedTransactionItem[] split = BSplit(gi, D, gD);
		GeneralizedTransactionItem left = split[0];
		GeneralizedTransactionItem right = split[1];
		GeneralizedTransactionDataSet tD = Update(left,right,D);
		int u = Check(left, tD, R, k, c) * Check(right, tD, R, k, c);
		if (u == -1)
			return gD;
		else if (u == 1)
		{
			GeneralizedTransactionDataSet nextD = new GeneralizedTransactionDataSet();
			GeneralizedTransactionDataSet Dl = TreeBase(left, tD, D, R, k, c);
			GeneralizedTransactionDataSet Dr = TreeBase(right, tD, D, R, k, c);
			Dl.AddSet(Dr);
			return Dl;
		}
		else 
		{}
		return gD;
	}

	private Vector<GeneralizedTransactionRule> FindRules(
			Vector<GeneralizedTransactionRule> R, GeneralizedTransactionItem gi) {
		Vector<GeneralizedTransactionRule> nR = new Vector<GeneralizedTransactionRule>();
		for (GeneralizedTransactionRule rule : R)
		{
			for (GeneralizedTransactionItem i : rule.getAnte())
			{
				if (i.IsContain(gi))
				{
					nR.add(rule);
					break;
				}
			}
		}
		return nR;
	}

	public static GeneralizedTransactionItem[] BSplit(GeneralizedTransactionItem i, TransactionDataset D,
			GeneralizedTransactionDataSet Dg) {
		Transaction left = new Transaction();
		Transaction right = new Transaction();
		int selectedIndexI1 = -1;
		int selectedIndexI2 = -1;
		double maxUL = -1;
		Vector<TransactionItem> publicSet = TransactionItem.GetPublicItems(D);
		Vector<TransactionItem> nonpublicSet = TransactionItem
				.GetNonpublicItems(D);
		int ci = 0;
		int cj = 0;
		for (TransactionItem i1 : i.get_rule()) {
			for (TransactionItem i2 : i.get_rule()) {
				if (i1.getItem().equals(i2.getItem()))
					continue;
				GeneralizedTransactionItem gen = new GeneralizedTransactionItem();
				Vector<TransactionItem> rule = new Vector<TransactionItem>();
				rule.add(i1);
				rule.add(i2);
				gen.set_rule(rule);
				double curUL = UtilityLossTransaction(gen, Dg, publicSet,
						nonpublicSet);
				if (curUL > maxUL) {
					maxUL = curUL;
					selectedIndexI1 = ci;
					selectedIndexI2 = cj;
				}

				cj++;
			}
			ci++;
		}

		left.AddItem(i.get_rule().get(selectedIndexI1));
		right.AddItem(i.get_rule().get(selectedIndexI2));
		i.get_rule().remove(selectedIndexI1);
		i.get_rule().remove(selectedIndexI2);

		for (TransactionItem tranItem : i.get_rule()) {
			Transaction temp1 = left;
			temp1.AddItem(tranItem);
			Transaction temp2 = right;
			temp2.AddItem(tranItem);
			double sup1 = 0;
			for (TransactionItem ii : temp1.GetData()) {
				sup1 += D.GetSupport(ii);
			}
			double sup2 = 0;
			for (TransactionItem ii : temp2.GetData()) {
				sup2 += D.GetSupport(ii);
			}
			if (left.GetData().size() * temp1.GetWeight() * sup1 <= right
					.GetData().size() * temp2.GetWeight() * sup2) {
				left.AddItem(tranItem);
			} else {
				right.AddItem(tranItem);
			}
		}
		GeneralizedTransactionItem[] res = new GeneralizedTransactionItem[2];
		res[0].set_rule(left.GetData());
		res[1].set_rule(right.GetData());
		return res;
	}
	
	private GeneralizedTransactionDataSet Update(GeneralizedTransactionItem left,
			GeneralizedTransactionItem right,
			TransactionDataset D)
	{
		GeneralizedTransactionDataSet gD = D.ConvertToGeneralizedForm();
		gD.Generalize(left);
		gD.Generalize(right);
		return gD;
	}

	public int Check(GeneralizedTransactionItem i,
			GeneralizedTransactionDataSet D,
			Vector<GeneralizedTransactionRule> rules, int k, double c) {
		boolean isExceed = false;
		rules = FindRules(rules, i);
		for (GeneralizedTransactionRule rule : rules) {
			if (D.GetSupport(rule.getAnte()) < k)
				return -1;
			if (D.GetConfidenceOr(rule.getAnte(), rule.getAnte()) > c)
				isExceed = true;
		}
		if (isExceed)
			return 0;
		else
			return 1;
	}

	public static double UtilityLossTransaction(
			GeneralizedTransactionItem rule, GeneralizedTransactionDataSet Dg,
			Vector<TransactionItem> publicSet,
			Vector<TransactionItem> nonpublicSet) {
		double res = 0;
		int numbergenItems = rule.get_rule().size();
		int numberPublicItem = publicSet.size();
		int numbernonpubItem = nonpublicSet.size();
		Vector<GeneralizedTransactionItem> rules = new Vector<GeneralizedTransactionItem>();
		rules.add(rule);
		double support = Dg.GetSupport(rules);
		double w = rule.get_ruleWeight();
		res = (Math.pow(2, numbergenItems) - 1)
				/ (Math.pow(2, numberPublicItem) - 1) * w * support
				/ numbernonpubItem;
		return res;
	}

	public static double UtilityLossDataSet(
			Vector<GeneralizedTransactionItem> rules,
			GeneralizedTransactionDataSet D, Vector<TransactionItem> publicSet,
			Vector<TransactionItem> nonpublicSet) {
		double total = 0;
		for (GeneralizedTransactionItem r : rules) {
			total += UtilityLossTransaction(r, D, publicSet, nonpublicSet);
		}
		return total;
	}
}
