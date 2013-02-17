package DataStructure;

import java.util.Collections;
import java.util.Vector;

public class GeneralizedTransactionDataSet {
	// Vector<Hashtable<String, Boolean>> itemMatrix = new
	// Vector<Hashtable<String, Boolean>>();
	Vector<GeneralizedTransaction> records = new Vector<GeneralizedTransaction>();
	Vector<TransactionItem> suppressedItem = new Vector<TransactionItem>();

	public Vector<GeneralizedTransaction> GetData() {
		return records;
	}

	public GeneralizedTransaction get(int i) {
		return records.get(i);
	}

	public int size() {
		return records.size();
	}

	@Override
	public String toString() {
		String aStr = "";
		for (GeneralizedTransaction a : records) {
			aStr += a.toString() + "\n";
		}
		return aStr;
	}

	public void AddRecord(GeneralizedTransaction item) {
		records.add(item);
		// Hashtable<String, Boolean> m = new Hashtable<String, Boolean>();
		// for (GeneralizedTransactionItem i : item.GetData()) {
		// for (String ii : i.get_rule_data()) {
		// m.put(ii, true);
		// }
		// }
		// itemMatrix.add(m);
	}

	public int GetSupport(String p) {
		GeneralizedTransactionItem setItem = new GeneralizedTransactionItem();
		Vector<String> ps = new Vector<String>();
		ps.add(p);
		setItem.AddItemData(ps);
		return GetSupport(setItem);
	}

	public int GetSupport(GeneralizedTransactionItem setItem) {
		// int res = 0;
		// for (GeneralizedTransaction record : records) {
		// if (record.IsContains(setItem))
		// res++;
		// }
		// return res;
		// for (Hashtable<String, Boolean> i : itemMatrix)
		// {
		// boolean HasAll = true;
		// for (String d : setItem.get_rule_data())
		// {
		// if (!i.containsKey(d))
		// {
		// HasAll = false;
		// break;
		// }
		// }
		// if (HasAll)
		// {
		// res++;
		// }
		// }
		// return res;
		Vector<GeneralizedTransactionItem> setItems = new Vector<GeneralizedTransactionItem>();
		setItems.add(setItem);
		return GetSupport(setItems);
	}

	public int GetSupport(Vector<GeneralizedTransactionItem> setItems) {
		// int res = 0;
		// GeneralizedTransactionItem ite = new GeneralizedTransactionItem();
		// Vector<String> data = new Vector<String>();

		Vector<GeneralizedTransaction> temp = new Vector<GeneralizedTransaction>(
				records);
		Collections.copy(temp, records);
		int pos = 0;
		for (GeneralizedTransactionItem setitem : setItems) {
			pos = 0;
			if (setitem.size() == 0)
				continue;
			int size = temp.size();
			for (int j = 0; j < size; j++) {
				if (temp.get(pos).IsContains(setitem))
					pos++;
				else
					temp.remove(pos);
			}
		}

		return temp.size();
		// Vector<Integer> ignore = new Vector<Integer>();
		// for (GeneralizedTransactionItem setitem : setItems) {
		// for (int i = 0; i < records.size(); i++) {
		// if (ignore.contains(i))
		// continue;
		// if (!records.get(i).IsContains(setitem)) {
		// ignore.add(i);
		// }
		// }
		// }

		// for (GeneralizedTransaction record : records) {
		// for (GeneralizedTransactionItem ii : setItems) {
		// for (String iii : ii.get_rule_data()) {
		// if (!data.contains(iii)) {
		// data.add(iii);
		// }
		// }
		// }
		// if (record.IsContainsAll(setItems))
		// res++;

		// boolean isFound = false;
		// for (GeneralizedTransactionItem tarItem : setItems) {
		// for (GeneralizedTransactionItem comparingItem : record
		// .GetData()) {
		// for (TransactionItem i : tarItem.get_rule()) {
		// for (TransactionItem j : comparingItem.get_rule()) {
		// if (i.getItem().equals(j.getItem())) {
		// isFound = true;
		// break;
		// }
		// }
		// if (isFound)
		// break;
		// }
		// }
		// if (isFouFnd)
		// res++;
		// }
		// }
		// ite.AddItemData(data);
		// return GetSupport(ite);
		// res = records.size() - ignore.size();
		// return res;// / records.size();
	}

	// public int GetSupportAnd(Vector<GeneralizedTransactionItem> setItems) {
	// int res = 0;
	// for (GeneralizedTransaction record : records) {
	// boolean isFound = false;
	// for (GeneralizedTransactionItem tarItem : setItems) {
	// for (GeneralizedTransactionItem comparingItem : record
	// .GetData()) {
	// if (comparingItem.IsThis(tarItem)) {
	// res++;
	// }
	// }
	// }
	// }
	// return res;// / records.size();
	// }

	public double GetConfidenceOr(Vector<GeneralizedTransactionItem> anteItems,
			Vector<GeneralizedTransactionItem> conItems) {
		float support1 = GetSupport(anteItems);
		anteItems.addAll(conItems);
		float support2 = GetSupport(anteItems);
		return support2 / support1;
	}

	// public double GetConfidenceAnd(
	// Vector<GeneralizedTransactionItem> anteItems,
	// Vector<GeneralizedTransactionItem> conItems) {
	// float support1 = GetSupportAnd(anteItems);
	// anteItems.addAll(conItems);
	// float support2 = GetSupportAnd(anteItems);
	// return support2 / support1;
	// }

	public static GeneralizedTransactionDataSet FilterDataSet(
			GeneralizedTransactionDataSet D, GeneralizedTransactionItem item) {
		GeneralizedTransactionDataSet nD = new GeneralizedTransactionDataSet();
		for (int i = 0; i < D.GetData().size(); i++) {
			if (D.GetData().get(i).IsContains(item)) {
				nD.AddRecord(D.GetData().get(i));
			}
		}
		return nD;
	}

	public void Generalize(GeneralizedTransactionItem item) {
		for (int i = 0; i < records.size(); i++) {
			records.get(i).Generalize(item);
		}
		// for (int i = 0; i < itemMatrix.size(); i++) {
		// boolean isCon = false;
		// for (String ii : item.get_rule_data()) {
		// if (itemMatrix.get(i).contains(ii)) {
		// isCon = true;
		// break;
		// }
		// }
		// if (isCon == true) {
		// for (String ii : item.get_rule_data()) {
		// itemMatrix.get(i).put(ii, true);
		// }
		// }
		//
		// }
	}

	public void AddSet(GeneralizedTransactionDataSet dr) {
		for (GeneralizedTransaction t : dr.GetData()) {
			AddRecord(t);
		}
	}

	public int NumberOfItems() {
		int total = 0;
		for (GeneralizedTransaction gt : records) {
			total += gt.NumberOfItems();
		}
		return total;
	}

	public int SuppressItem(String im) {
		TransactionItem i = new TransactionItem(im);
		for (TransactionItem ii : suppressedItem) {
			if (ii.getItem().equals(i.getItem())) {
				return 0;
			}
		}
		suppressedItem.add(i);
		int total = 0;
		int total_i = 0;
		for (int ic = 0; ic < records.size(); ic++) {
			total_i = records.get(ic).SuppressItem(im);
			if (total_i > 0) {
				if (!suppressed_items.contains(ic))
					suppressed_items.add(ic);
			}
			total += total_i;
		}
		// for (int j = 0; j < itemMatrix.size(); j++) {
		// itemMatrix.get(j).remove(im);
		// }
		return total;
	}

	public void RemoveEmpty() {
		for (int i = 0; i < GetData().size(); i++) {
			GetData().get(i).RemoveEmpty();
			if (GetData().get(i).GetData().size() == 0) {
				GetData().remove(i);
				i--;
			}
		}
	}

	public void FilterDuplication() {
		for (int i = 0; i < GetData().size() - 1; i++) {
			for (int j = i + 1; j < GetData().size(); j++) {
				if (GetData().get(i).IsContainsAll(GetData().get(j).GetData())) {
					GetData().remove(j);
					j--;
				}
			}
		}
	}

	Vector<Integer> suppressed_items;

	public Vector<Integer> GetSuppressedItems() {
		return suppressed_items;
	}

	public int SuppressItem(Vector<String> ims) {
		int total = 0;
		suppressed_items = new Vector<Integer>();
		for (String im : ims) {
			total += SuppressItem(im);
		}
		return total;
	}

	public Vector<GeneralizedTransactionItem> GetDomain() {
		Vector<GeneralizedTransactionItem> res = new Vector<GeneralizedTransactionItem>();
		for (GeneralizedTransaction tr : records) {
			res.addAll(tr.GetData());
		}
		return RemoveDumplication(res);
	}

	public static Vector<GeneralizedTransactionItem> RemoveDumplication(
			Vector<GeneralizedTransactionItem> data) {
		GeneralizedTransaction tr = new GeneralizedTransaction();
		tr.AddItemAll(data);
		tr.FilterDuplication();
		return tr.GetData();
	}
}
