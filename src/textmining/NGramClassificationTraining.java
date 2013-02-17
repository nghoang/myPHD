package textmining;

import java.util.Vector;

public class NGramClassificationTraining {
	Vector<String> texts = new Vector<String>();
	Vector<String> tableKey = new Vector<String>();
	Vector<Integer> tableValue = new Vector<Integer>();
	Integer gram = 2;
	
	public void AddText(String t)
	{
		texts.add(t);
	}
	
	void CreateInverseText(String text)
	{
		Vector<String> terms = new Vector<String>();
		String[] paragraphs = text.split("\n");
		for (String p : paragraphs)
		{
			String[] sentences = p.split(",|\\.|/|;|'|\\|\\[|\\]|-|=|\\(|\\)|_|+|\\{|\\}|\"|<|>|\\?");
			for (String s : sentences)
			{
				String ss = s.replaceAll("\\s+", "\\s");
				Vector<String> ngrams = new Vector<String>();
		        String[] words = ss.split(" ");
		        for (int i = 0; i < words.length - gram + 1; i++)
		            ngrams.add(concat(words, i, i+gram));
		        
		        for (String g : ngrams)
		        {
		        	if (terms.contains(g) == false)
		        	{
		        		terms.add(g);
		        	}
		        }
			}
		}
		
		for (String g : terms)
        {
        	if (!tableKey.contains(g))
        	{
        		tableKey.add(g);
        		tableValue.add(1);
        	}
        	else
        	{
        		int i = tableKey.indexOf(g);
        		tableValue.set(i, tableValue.get(i) + 1);
        	}
        }
	}
	
	String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }
	
	public void CreateInverseTable()
	{
		for (String text : texts)
		{
			CreateInverseText(text);
		}
		
		for (int i=0;i<tableKey.size();i++)
		{
			for (int j=i+1;j<tableKey.size();j++)
			{
				if (tableValue.get(i) < tableValue.get(j))
				{
					int oldValue = tableValue.get(i);
					String oldKey = tableKey.get(i);
					
					tableValue.set(i, tableValue.get(j));
					tableKey.set(i, tableKey.get(j));
					
					tableValue.set(j, oldValue);
					tableKey.set(j, oldKey);
				}
			}
		}
	}
}
