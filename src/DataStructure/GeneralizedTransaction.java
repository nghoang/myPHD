package DataStructure;

import java.util.Vector;

public class GeneralizedTransaction {
	Vector<GeneralizedTransactionItem> _data = new Vector<GeneralizedTransactionItem>();
	
	public GeneralizedTransaction() {
		// TODO Auto-generated constructor stub
	}

	public void SetData(Vector<GeneralizedTransactionItem> data) {
		_data = data;
	}

	@Override
	public String toString() {
		String aStr = "";
		boolean isf = true;
		for (GeneralizedTransactionItem a : _data) {
			if (isf) {
				aStr += a.toString();
				isf = false;
			} else
				aStr += ", " + a.toString();
		}

		return aStr;
	}

	public Vector<GeneralizedTransactionItem> GetData() {
		return _data;
	}

	public void AddItem(GeneralizedTransactionItem i) {
		_data.add(i);
	}

	public boolean IsContainsAll(Vector<GeneralizedTransactionItem> is) {
		for (GeneralizedTransactionItem i : is) {
			if (i.get_rule().size() == 0)
				continue;
			if (!IsContains(i))
				return false;
		}
		return true;
	}

	public boolean IsContains(GeneralizedTransactionItem i) {
		// boolean res = false;
		for (GeneralizedTransactionItem item : _data) {
			if (item.get_rule().size() == 0)
				continue;
			if (item.IsContain(i)) {
				return true;
			}
			//
			// for (TransactionItem item2 : item.get_rule()) {
			// for (TransactionItem item3 : i.get_rule()) {
			// if (item3.getItem().equals(item2.getItem())) {
			// res = true;
			// break;
			// }
			// if (res)
			// break;
			// }
			//
			// if (res)
			// break;
			// }
			//
			// if (res)
			// break;
		}
		return false;
	}

	public void FilterDuplication() {
		for (int i = 0; i < _data.size(); i++) {
			for (int j = i + 1; j < _data.size(); j++) {
				GeneralizedTransactionItem i1 = _data.get(i);
				GeneralizedTransactionItem i2 = _data.get(j);
				if (i1.IsContain(i2)) {
					_data.remove(j);
					i = 0;
					break;
				} else if (i2.IsContain(i1)) {
					_data.remove(i);
					i = 0;
					break;
				}
			}
		}
	}

	public void Generalize(GeneralizedTransactionItem item) {

		for (int i = 0; i < _data.size(); i++) {
			_data.get(i).Generalize(item);
		}
		//FilterDuplication();
		// Vector<GeneralizedTransactionItem> ndata = new
		// Vector<GeneralizedTransactionItem>();
		// for (GeneralizedTransactionItem i : _data) {
		// if (item.IsContain(item)) {
		// if (this.IsContains(item) == false) {
		// ndata.add(item);
		// }
		// }
		// }
		// _data = ndata;
	}

	public void RemoveEmpty()
	{
		for (int i = 0; i < _data.size(); i++) {
			if (_data.get(i).size() == 0)
			{
				_data.remove(i);
				i--;
			}
		}
	}
	
	public int SuppressItem(String im) {
		int total = 0;
		for (int i = 0; i < _data.size(); i++) {
			total +=_data.get(i).SuppressItem(im);
		}
		//RemoveEmpty();
		//FilterDuplication();
		return total;
	}

	public int NumberOfItems() {
		int total = 0;
		for (GeneralizedTransactionItem gt : _data) {
			total +=gt.size();
		}
		return total;
	}

	public void AddItemAll(Vector<GeneralizedTransactionItem> data) {
		for (GeneralizedTransactionItem i : data)
		{
			AddItem(i);
		}
	}
}
