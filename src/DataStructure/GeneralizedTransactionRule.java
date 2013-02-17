package DataStructure;

import java.util.Vector;

public class GeneralizedTransactionRule {
	private Vector<GeneralizedTransactionItem> ante = new Vector<GeneralizedTransactionItem>();
	private Vector<GeneralizedTransactionItem> cons = new Vector<GeneralizedTransactionItem>();
	public void setAnte(Vector<GeneralizedTransactionItem> ante) {
		this.ante = ante;
	}
	public Vector<GeneralizedTransactionItem> getAnte() {
		return ante;
	}
	public void setCons(Vector<GeneralizedTransactionItem> cons) {
		this.cons = cons;
	}
	public Vector<GeneralizedTransactionItem> getCons() {
		return cons;
	}
	
	@Override
	public String toString() {
		String aStr = "";
		boolean isf = true;
		for (GeneralizedTransactionItem a : ante) {
			if (isf) {
				aStr += a.toString();
				isf = false;
			} else
				aStr += ", " + a.toString();
		}

		String cStr = "";
		isf = true;
		for (GeneralizedTransactionItem a : cons) {
			if (isf) {
				cStr += a.toString();
				isf = false;
			} else
				cStr += ", " + a.toString();
		}

		if (aStr.trim() == "" || cStr.trim() == "")
			return "";
		return aStr + " -> " + cStr;
	}
}
