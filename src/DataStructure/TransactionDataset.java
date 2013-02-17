package DataStructure;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


public class TransactionDataset {
	Transaction domain = null;
	Vector<Transaction> records = new Vector<Transaction>();

	public Transaction getDomain() {
		if (domain == null)
		{
			domain = new Transaction();
			for (Transaction tran : getRecords()) {
				for (TransactionItem item : tran.GetData())
					if (!domain.IsContains(item))
						domain.AddItem(item);
			}
		}
		return domain;
	}
	

	public void SortBySize()
	{
		Collections.sort(records, new Comparator<Transaction>() {

			@Override
			public int compare(Transaction o1, Transaction o2) {
				if (o1.GetData().size() > o2.GetData().size())
					return 1;
				else if (o1.GetData().size() <  o2.GetData().size())
					return -1;
				else
					return 0;
			}
		});
	}

	public Vector<Transaction> getRecords() {
		return records;
	}

	public void setRecords(Vector<Transaction> records) {
		domain = null;
		this.records = records;
	}

	@Override
	public String toString() {
		String aStr = "";
		for (Transaction a : records) {
			aStr += a.toString() + "\n";
		}
		return aStr;
	}

	public void AddRecord(Transaction item) {
//		for (TransactionItem i : item.GetData()) {
//			if (!domain.contains(i))
//				domain.add(i);
//		}
		domain = null;
		records.add(item);
	}

	public int GetSupport(Vector<TransactionItem> setItems) {
		//int res = 0;

		Vector<Integer> ignore = new Vector<Integer>();
		for (TransactionItem setitem : setItems) {
			for (int i = 0; i < records.size(); i++) {
				if (ignore.contains(i))
					continue;
				if (!records.get(i).IsContains(setitem)) {
					ignore.add(i);
				}
			}
		}

		// for (Transaction record : records) {
		// boolean isFound = false;
		// for (TransactionItem tarItem : setItems) {
		// for (TransactionItem comparingItem : record.GetData()) {
		// if (comparingItem.getItem().equals(tarItem)) {
		// isFound = true;
		// break;
		// }
		// }
		// if (isFound)
		// res++;
		// }
		// }

		return records.size() - ignore.size();// res / records.size();
	}

	public int GetSupport(TransactionItem setItems) {
		Vector<TransactionItem> items = new Vector<TransactionItem>();
		items.add(setItems);
		return GetSupport(items);
	}

	public float GetConfidence(Vector<TransactionItem> anteItems,
			Vector<TransactionItem> conItems) {
		float support1 = GetSupport(anteItems);
		anteItems.addAll(conItems);
		float support2 = GetSupport(anteItems);
		return support2 / support1;
	}

	public GeneralizedTransactionDataSet ConvertToGeneralizedForm() {
		GeneralizedTransactionDataSet res = new GeneralizedTransactionDataSet();
		for (Transaction tr : records) {
			res.AddRecord(tr.ConvertToGeneralizedForm());
		}
		return res;
	}


	public void Suppress(String term) {
		for (int i=0;i<records.size();i++)
		{
			records.get(i).Suppress(term);
		}
	}
}
