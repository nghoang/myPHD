package DataStructure;

import java.util.UUID;
import java.util.Vector;
public class Transaction {
	String id = "";
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	Vector<TransactionItem> _data = new Vector<TransactionItem>();
	double weight = 1;
	
	public Transaction()
	{
		id = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		String aStr = "";
		boolean isf = true;
		for (TransactionItem a : _data) {
			if (isf) {
				aStr += a.getItem();
				isf = false;
			} else
				aStr += ", " + a.getItem();
		}

		return aStr;
	}

	public double GetWeight() {
		return weight;
	}

	public void SetData(Vector<TransactionItem> data) {
		_data = data;
	}

	public Vector<TransactionItem> GetData() {
		return _data;
	}

	public void AddItem(TransactionItem i) {
		_data.add(i);
	}

	public boolean IsContains(TransactionItem i) {
		boolean res = false;
		for (TransactionItem item : _data) {
			if (item.getItem().equals(i.getItem())) {
				res = true;
				break;
			}
			if (res)
				break;
		}
		return res;
	}

	public GeneralizedTransaction ConvertToGeneralizedForm() {
		GeneralizedTransaction res = new GeneralizedTransaction();
		for (TransactionItem i : _data)
		{
			GeneralizedTransactionItem ni = new GeneralizedTransactionItem();
			Vector<TransactionItem> r = new Vector<TransactionItem>();
			r.add(i);
			ni.set_rule(r);
			res.AddItem(ni);
		}
		return res;
	}

	public <T> boolean IsContainsAll(Vector<TransactionItem> is) {
		for (TransactionItem i : is)
		{
			if (!IsContains(i))
			{
				return false;
			}
		}
		return true;
	}

	public void Suppress(String term) {
		for (int i=0;i>_data.size();i++)
		{
			if (_data.get(i).getItem().toLowerCase().equals(term))
			{
				_data.removeElementAt(i);
				i--;
			}
		}
	}
}
