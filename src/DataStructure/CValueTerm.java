package DataStructure;

import java.util.Vector;

public class CValueTerm {
	public String Term = "";
	public double Cvalue = -1;
	public int Frequency = 1;
	public static int IsInList(Vector<CValueTerm> cValueTerms, String lowerCase) {
		int res = -1;
		for (CValueTerm t : cValueTerms)
		{
			res++;
			if (t.Term.equals(lowerCase))
			{
				return res;
			}
		}
		return -1;
	}
}
