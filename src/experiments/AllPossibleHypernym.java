package experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import utility.WordNetLib;
import AppParameters.AppConst;

public class AllPossibleHypernym {

	/**
	 * @param args
	 */
	static WordNetLib wn;
	
	public static void main(String[] args) {

		wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		
		Vector<String> terms1 = GetHyper("auscultation");
		Vector<String> terms2 = GetHyper("bowel");
		Vector<String> terms3 = GetHyper("edema");
		Vector<String> terms4 = GetHyper("incision");
		Vector<String> terms5 = GetHyper("erythema");
		Vector<String> terms6 = GetHyper("drainage");

		Vector<String> allitems = new Vector<String>();
		allitems.addAll(terms1);
		allitems.addAll(terms2);
		allitems.addAll(terms3);
		allitems.addAll(terms4);
		allitems.addAll(terms5);
		allitems.addAll(terms6);
		
		Vector<String> checked = new Vector<String>();
		
		for (String i : allitems)
		{
			if (checked.contains(i))
				continue;
			
			checked.add(i);
			int count = 0;
			for (String j : allitems)
			{
				if (i.equals(j))
					count++;
			}
			if (count == 3)
				System.out.println(i + " " + count);
		}
		
//		System.out.println(terms1);
//		System.out.println(terms2);
//		System.out.println(terms3);
//		System.out.println(terms4);
//		System.out.println(terms5);
//		System.out.println(terms6);
	}
	
	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
	
	public static Vector<String> GetHyper(String target)
	{
		Vector<String> terms = new Vector<String>();
		String[] ts1 = {target};
		while (ts1 != null && ts1.length > 0)
		{
			Vector<String> temp = new Vector<String>();
			String[] ts = null;
			for (String t : ts1)
			{ 
				ts = wn.GetHypernyms(t);
				if (ts == null)
					break;
				for (String tt : ts)
				{
					if (!terms.contains(tt))
						temp.add(tt);
				}
			}
			//System.out.println(temp.toString());
			ts = temp.toArray(new String[temp.size()]);
			ts1 = ts;
			if (ts != null)
				for (String t : ts)
				{
					if (!terms.contains(t))
						terms.add(t);
				}
		}
		return terms;
	}

}
