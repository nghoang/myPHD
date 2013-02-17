package DataStructure;

import java.util.Vector;

public class TransactionRule {
	private Vector<TransactionItem> ante = new Vector<TransactionItem>();
	private Vector<TransactionItem> cons = new Vector<TransactionItem>();

	public void setAnte(Vector<TransactionItem> ante) {
		this.ante = ante;
	}

	public Vector<TransactionItem> getAnte() {
		return ante;
	}

	public void setCons(Vector<TransactionItem> cons) {
		this.cons = cons;
	}

	public Vector<TransactionItem> getCons() {
		return cons;
	}

	@Override
	public String toString() {
		String aStr = "";
		boolean isf = true;
		for (TransactionItem a : ante) {
			if (isf) {
				aStr += a.getItem();
				isf = false;
			} else
				aStr += ", " + a.getItem();
		}

		String cStr = "";
		isf = true;
		for (TransactionItem a : cons) {
			if (isf) {
				cStr += a.getItem();
				isf = false;
			} else
				cStr += ", " + a.getItem();
		}

		if (aStr.trim() == "" || cStr.trim() == "")
			return "";
		return aStr + " -> " + cStr;
	}
}
