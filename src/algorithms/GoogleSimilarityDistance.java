package algorithms;

import java.util.Vector;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;

/*
class MeasurePair {
	public String k1 = "";
	public String k2 = "";
	public float score = 0;

	public MeasurePair(String _k1, String _k2, float _score) {
		this.k1 = _k1;
		this.k2 = _k2;
		this.score = _score;
	}

	public static MeasurePair IsScored(String _k1, String _k2,
			Vector<MeasurePair> pairs) {
		for (MeasurePair p : pairs) {
			if (p.k1.equals(_k1) && p.k2.equals(_k2))
				return p;
		}
		return null;
	}
}*/

public class GoogleSimilarityDistance {
	//static Vector<MeasurePair> pairs = new Vector<MeasurePair>();
	public WebClientX client;
	public String newContent = "";
	public String last_query = "";

	public double Similarity(String w1, String w2) {
		return Similarity(w1, w2, "");
	}

	public double Similarity(String w1, String w2, String context)
	{
		return Similarity(w1,w2,context," AND ","");
	}

	public double Similarity(String w1, String w2, String context, String ex)
	{
		return Similarity(w1,w2,context," AND ", ex);
	}
	
	public double ReadCache(String id)
	{
		String content = Utilities.readFileAsString("google-distance-cache.txt");
		for (String line : content.split("\n"))
		{
			if (line.startsWith(id))
			{
				return Double.parseDouble(line.split(":")[1].trim());
			}
		}
		return -1;
	}
	
	public void AppendCache(String wid, Double result)
	{
		Utilities.WriteFile("google-distance-cache.txt", wid + ":" + result + "\n",true);
	}
	
	public double Similarity(String w1, String w2, String context, String middle, String ex) {
		try {
			/*MeasurePair cache = MeasurePair.IsScored(w1, w2, pairs);
			if (cache != null)
				return cache.score;*/

			//w1 = "\"" + w1 + "\"";
			//w2 = "\"" + w2 + "\"";
			String wid = context + "-" + w1 + "-" + middle + "-"+ w2+"-"+ex;
			wid = wid.replace(":", "_").trim();
			Double cacheFile = ReadCache(wid);
			if (cacheFile != -1)
			{
				return cacheFile;
			}
				
			context = context.trim();
			if (context.trim().equals("") == false)
				context = "inurl:" + context.trim() + " ";
			//System.out.print(".");
			// System.out.println("Measure " + w1 + " " + w2);
			// WebClientX client = new WebClientX();
			// client.DebugFiddler();
			String resultParser = "<div id=resultStats>About ([0-9\\,]+) results<nobr>";
			if (resultParser.equals("")) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return -1;
			}
			if (client == null)
				client = new WebClientX();
			last_query = "http://www.google.com/search?q="
					+ Utilities.EncodeQuery((w1 + " "+ex+" " + context).replace("  ", " "));
			String content = client.GetMethod(last_query);
			if (content.indexOf("did not match any documents.") > 0)
				return 1;
			// try {
			// Thread.sleep(600);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			if (content.indexOf("('captcha')") > 0) {
				this.newContent = content;
				return -1;
			}
			String temp = Utilities.SimpleRegexSingle(
					resultParser, content, 1).replace(",", "");
			if (temp.equals(""))
				return 1;
			double countw1 = Double.parseDouble(temp);

			last_query = "http://www.google.com/search?q="
					+ Utilities.EncodeQuery((w2 + " "+ex+" " + context).replace("  ", " "));
			content = client.GetMethod(last_query);
			if (content.indexOf("did not match any documents.") > 0)
				return 1;

			if (content.indexOf("('captcha')") > 0) {
				this.newContent = content;
				return -1;
			}
			// try {
			// Thread.sleep(600);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			temp = Utilities.SimpleRegexSingle(
					resultParser, content, 1).replace(",", "");
			if (temp.equals(""))
				return 1;
			double countw2 = Double.parseDouble(temp);
			last_query = "http://www.google.com/search?q="
					+ Utilities.EncodeQuery((context + "" + w1 + " "+ middle +" " + w2 + " " + ex).replace("  ", " ").trim());
			content = client.GetMethod(last_query);
			if (content.indexOf("did not match any documents.") > 0)
				return 1;

			if (content.indexOf("('captcha')") > 0) {
				this.newContent = content;
				return -1;
			}
			// try {
			// Thread.sleep(600);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			String t = Utilities.SimpleRegexSingle(resultParser, content, 1)
					.replace(",", "");
			if (t.equals("")) {
				// there not so many results
				t = Utilities.SimpleRegexSingle(
						"<div id=resultStats>([0-9\\,]+) result", content, 1)
						.replace(",", "");
			}
			if (t.equals(""))
			{
				t = "1";
			}
			double countw12 = Double.parseDouble(t);
			double logw1 = Math.log10(countw1);
			double logw2 = Math.log10(countw2);
			double logw12 = Math.log10(countw12);

			double result = (Math.max(logw1, logw2) - logw12)
					/ (Math.log10(35000000000D) - Math.min(logw1, logw2));

			AppendCache(wid,result);
			//pairs.add(new MeasurePair(w1, w2, (float) result));

			return result;
		} catch (Exception exp) {
			exp.printStackTrace();
			return -1;
		}
	}

	public double Similarity2(String w1, String w2) {
		while (true) {
			w1 = "\"" + w1 + "\"";
			w2 = "\"" + w2 + "\"";

			System.out.println("Measure " + w1 + " -> " + w2);
			// WebClientX client = new WebClientX();
			// client.DebugFiddler();
			String resultParser = "<div id=resultStats>About ([0-9\\,]+) results<nobr>";
			if (resultParser.equals("")) {
				continue;
			}
			if (client == null)
				client = new WebClientX();
			String content = client.GetMethod("http://www.google.com/search?q="
					+ Utilities.EncodeQuery(w1));
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (content.indexOf("('captcha')") > 0) {
				this.newContent = content;
				return -1;
			}
			double countw1 = Double.parseDouble(Utilities.SimpleRegexSingle(
					resultParser, content, 1).replace(",", ""));
			content = client.GetMethod("http://www.google.com/search?q="
					+ Utilities.EncodeQuery(w2));
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double countw2 = Double.parseDouble(Utilities.SimpleRegexSingle(
					resultParser, content, 1).replace(",", ""));
			content = client.GetMethod("http://www.google.com/search?q="
					+ Utilities.EncodeQuery(w1 + " * " + w2));
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double countw12 = Double.parseDouble(Utilities.SimpleRegexSingle(
					resultParser, content, 1).replace(",", ""));
			double logw1 = Math.log10(countw1);
			double logw2 = Math.log10(countw2);
			double logw12 = Math.log10(countw12);

			double result = (Math.max(logw1, logw2) - logw12)
					/ (Math.log10(37000000000D) - Math.min(logw1, logw2));
			return result;
		}
	}
}
