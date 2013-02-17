package DataStructure;

import java.util.Collections;
import java.util.Vector;
public class GeneralizedTransactionItem {
	Vector<TransactionItem> _rule = new Vector<TransactionItem>();
	double _ruleWeight = 1;// default weight

	public GeneralizedTransactionItem() {
	}

	public GeneralizedTransactionItem(String is) {
		TransactionItem i = new TransactionItem(is);
		_rule.add(i);
	}

	@Override
	public String toString() {
		String aStr = "";
		boolean isf = true;
		for (TransactionItem a : _rule) {
			if (isf) {
				aStr += a.getItem();
				isf = false;
			} else
				aStr += ", " + a.getItem();
		}

		return "[" + aStr + "]";
	}

	public int size() {
		return _rule.size();
	}

	public Vector<TransactionItem> get_rule() {
		return _rule;
	}

	public void set_rule(Vector<TransactionItem> rule) {
		this._rule = new Vector<TransactionItem>(rule);
		Collections.copy(this._rule, rule);
	}

	public double get_ruleWeight() {
		return _ruleWeight;
	}

	public void set_ruleWeight(double _ruleWeight) {
		this._ruleWeight = _ruleWeight;
	}

	public boolean IsContain(GeneralizedTransactionItem targetItem) {
		for (TransactionItem i : _rule) {
			for (TransactionItem i2 : targetItem.get_rule()) {
				if (i.getItem().equals(i2.getItem()))
					return true;
			}
		}
		return false;
	}

	public boolean IsContainAll(GeneralizedTransactionItem targetItem) {
		return get_rule_data().containsAll(targetItem.get_rule_data());
	}

	public boolean IsThis(GeneralizedTransactionItem tarItem) {
		if (_rule.size() != tarItem.get_rule().size())
			return false;
		if (!IsContain(tarItem)) {
			return false;
		}
		return true;
	}

	public void AddItemData(Vector<String> p) {
		for (String i : p) {
			TransactionItem ti = new TransactionItem();
			ti.setItem(i);
			_rule.add(ti);
		}
	}

	public void CalculateAverageWeight() {
		double res = 0;
		int count = 0;
		for (TransactionItem i : _rule) {
			count++;
			res += i.getItemWeight();
		}
		_ruleWeight = res / count;
	}

	public int SuppressItem(String im) {
		int total = 0;
		for (int c = 0; c < _rule.size(); c++) {
			if (_rule.get(c).getItem().equals(im)) {
				_rule.remove(c);
				c--;
				total++;
			}
		}
		return total;
	}

	public void AddItem(TransactionItem ii) {
		for (TransactionItem i : _rule) {
			if (i.getItem().equals(ii.getItem())) {
				return;
			}
		}
		_rule.add(ii);
	}

	public Vector<String> get_rule_data() {
		Vector<String> res = new Vector<String>();
		for (TransactionItem i : _rule) {
			res.add(i.getItem());
		}
		return res;
	}

	public void Generalize(GeneralizedTransactionItem item) {
		if (IsContain(item)) {
			for (TransactionItem ii : item._rule) {
				AddItem(ii);
			}
		}
	}
}
