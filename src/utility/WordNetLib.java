package utility;

import java.util.Collections;
import java.util.Vector;

import rita.wordnet.RiWordnet;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WordNetLib {
	NounSynset nounSynset;
	NounSynset[] hyponyms;
	WordNetDatabase database;
	public RiWordnet riwn;
	
	public Float GetDistance(String t1, String t2)
	{
		return riwn.getDistance(t1, t2, RiWordnet.NOUN);
	}
	
	public String GenerateRandomNoun()
	{
		return riwn.getRandomWord(RiWordnet.NOUN);
	}
	
	

	public WordNetLib(String wordnet_path) {
		
		riwn = new RiWordnet(null, wordnet_path);
		System.setProperty("wordnet.database.dir", wordnet_path);
		database = WordNetDatabase.getFileInstance();
	}

	public Vector<String> GetSlidings(String term) {
		Vector<String> res = new Vector<String>();
		Synset[] synsets = database.getSynsets(term, SynsetType.NOUN);
		for (int i = 0; i < synsets.length; i++) {
			nounSynset = (NounSynset) (synsets[i]);
			for (int j = 0; j < nounSynset.getWordForms().length; j++) {
				res.add(nounSynset.getWordForms()[j]);
			}
		}
		return res;
	}
	
	public Vector<String> GetRelated3(String term, int max) {
		Vector<String> res = new Vector<String>();
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(term, SynsetType.NOUN); 
		for (Synset synset : synsets)
		{
			Synset[] parents = ((NounSynset)synset).getHypernyms();
			for (Synset parent : parents)
			{
				if (res.size() >= max)
					return res;
				Synset[] chidrends = ((NounSynset)parent).getHyponyms();
				for (Synset child : chidrends)
				{
					for (String word : child.getWordForms())
					{
						if (res.size() > max)
							return res;
						if (!res.contains(word))
							res.add(word);
					}
				}
				
				for (String word : parent.getWordForms())
				{
					Vector<String> temp = GetRelated3(word,max - res.size());
					for (String t : temp)
					{
						if (res.size() > max)
							return res;
						if (!res.contains(t))
							res.add(t);
					}
				}
			}
		}
		return res;
	}
	
	public String GetASibling(String term)
	{
		String[] parents = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (parents == null)
			return "";
		for (int i=0;i<parents.length;i++)
		{
			String[] curs = riwn.getHyponyms(parents[i], RiWordnet.NOUN);
			Vector<String> col = new Vector<String>();
			if (curs == null)
				continue;
			for (int j=0;j<curs.length;j++)
			{
				col.add(curs[j]);
			}
			Collections.shuffle(col);
			for (int j=0;j<curs.length;j++)
			{
				if (col.get(j).equals(term) == false)
					return col.get(j);
			}
		}
		return "";
	}

	public Vector<String> GetRelated2(String term, int max) {
		Vector<String> res = new Vector<String>();
		
		String[] hypers = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (res.size() > max)
			return res;
		for (String hyper : hypers) {
			if (res.size() > max)
				return res;
			if (!res.contains(hyper))
				res.add(hyper);
			String[] hypos = riwn.getHyponyms(hyper, RiWordnet.NOUN);
			for (String hypo : hypos) {
				if (res.size() > max)
					return res;
				if (hypo == null)
					continue;
				if (!res.contains(hypo))
					res.add(hypo);
			}

			Vector<String> res_temp = GetRelated(hyper, max - res.size());
			for (String temp : res_temp) {
				if (!res.contains(temp))
					res.add(temp);
			}

			String[] co_relateds = riwn.getCoordinates(hyper, RiWordnet.NOUN);

			for (String co_related : co_relateds) {
				res_temp = GetRelated(co_related, max - res.size());
				for (String temp : res_temp) {
					if (!res.contains(temp))
						res.add(temp);
				}
			}

		}

		return res;
	}

	public String[] GetHypernyms(String term)
	{
		String[] h1 = riwn.getHypernyms(term, RiWordnet.NOUN);
		String[] h2 = {};//riwn.getSynset(term, RiWordnet.NOUN);
		Vector<String> res = new Vector<String>();
		if (h1 != null)
		for (String i : h1)
		{
			res.add(i);
		}
		if (h2 != null)
		for (String i : h2)
		{
			if (!res.contains(i))
				res.add(i);
		}
		return res.toArray(new String[res.size()]);
	}
	
	public Vector<String> GetRelated(String term, int max) {
		Vector<String> res = new Vector<String>();
		String[] hypers = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (res.size() > max)
			return res;
		res.add(term);
		if (res.size() > max)
			return res;
		if (hypers == null) {
			hypers = riwn.getAllSynsets(term, RiWordnet.NOUN);
			for (String h : hypers) {
				if (res.size() > max)
					return res;
				res.add(h);
			}
		}
		for (String hyper : hypers) {
			if (res.size() > max)
				return res;
			res.add(hyper);
			String[] hypos = riwn.getHyponyms(hyper, RiWordnet.NOUN);
			if (res.size() > max)
				break;
			if (hypos == null)
				continue;
			for (String hypo : hypos) {
				if (res.size() > max)
					return res;
				res.add(hypo);
			}
			if (res.size() > max)
				break;
			Vector<String> res_temp = GetRelated(hyper, max - res.size());
			for (String temp : res_temp) {
				if (!res.contains(temp))
					res.add(temp);
			}
			if (res.size() > max)
				break;
		}
		Collections.shuffle(res);
		return res;
	}

	public String GetAFar2Level(String term) {
		String[] parent1 = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (parent1 == null)
			return "";
		Vector<String> parent_col_1 = new Vector<String>();
		for (String p1 : parent1)
			parent_col_1.add(p1);
		Collections.shuffle(parent_col_1);
		for (String p1 : parent_col_1)
		{
			String[] parent2 = riwn.getHypernyms(p1, RiWordnet.NOUN);
			if (parent2 == null)
				continue;
			Vector<String> parent_col_2 = new Vector<String>();
			for (String p2 : parent1)
				parent_col_2.add(p2);
			Collections.shuffle(parent_col_2);
			for (String p2 : parent_col_2)
			{
				String[] child1 = riwn.getHyponyms(p2, RiWordnet.NOUN);
				if (child1 == null)
					continue;
				Vector<String> child_col_1 = new Vector<String>();
				for (String c1 : child1)
					if (c1.equals(p1) == false)
						child_col_1.add(c1);
				Collections.shuffle(child_col_1);
				for (String c1 : child_col_1)
				{
					String[] child2 = riwn.getHyponyms(c1, RiWordnet.NOUN);
					if (child2 == null)
						continue;
					Vector<String> child_col_2 = new Vector<String>();
					for (String c2 : child2)
						child_col_2.add(c2);
					Collections.shuffle(child_col_2);
					for (String c2 : child_col_2)
					{
						if (c2.equals(term) == false)
							return c2;
					}
				}
			}
		}
		return "";
	}
	


	public String GetAFar3Level(String term) {
		String[] parent1 = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (parent1 == null)
			return "";
		Vector<String> parent_col_1 = new Vector<String>();
		for (String p1 : parent1)
			parent_col_1.add(p1);
		Collections.shuffle(parent_col_1);
		for (String p1 : parent_col_1)
		{
			String[] parent2 = riwn.getHypernyms(p1, RiWordnet.NOUN);
			if (parent2 == null)
				continue;
			Vector<String> parent_col_2 = new Vector<String>();
			for (String p2 : parent1)
				parent_col_2.add(p2);
			Collections.shuffle(parent_col_2);
			for (String p2 : parent_col_2)
			{
				String[] child1 = riwn.getHyponyms(p2, RiWordnet.NOUN);
				if (child1 == null)
					continue;
				Vector<String> child_col_1 = new Vector<String>();
				for (String c1 : child1)
					if (c1.equals(p1) == false)
						child_col_1.add(c1);
				Collections.shuffle(child_col_1);
				for (String c1 : child_col_1)
				{
					String[] child2 = riwn.getHyponyms(c1, RiWordnet.NOUN);
					if (child2 == null)
						continue;
					Vector<String> child_col_2 = new Vector<String>();
					for (String c2 : child2)
						child_col_2.add(c2);
					Collections.shuffle(child_col_2);
					for (String c2 : child_col_2)
					{
						if (c2.equals(term) == false)
							return c2;
					}
				}
			}
		}
		return "";
	}
	
	public String GetAFar1Level(String term) {
		String[] parent1 = riwn.getHypernyms(term, RiWordnet.NOUN);
		if (parent1 == null)
			return "";
		Vector<String> parent_col_1 = new Vector<String>();
		for (String p1 : parent1)
			parent_col_1.add(p1);
		Collections.shuffle(parent_col_1);
		for (String p1 : parent_col_1)
		{
			String[] child1 = riwn.getHyponyms(p1, RiWordnet.NOUN);
			if (child1 == null)
				continue;
			Vector<String> child_col_1 = new Vector<String>();
			for (String c1 : child1)
				if (c1.equals(p1) == false)
					child_col_1.add(c1);
			Collections.shuffle(child_col_1);
			for (String c1 : child_col_1)
			{
				if (c1.equals(term) == false)
					return c1;
			}
		}
		return "";
	}
}
