package algorithms;

import java.util.Vector;

public class TermSeparation {
	String document_file = "";
	Vector<String> extracted_terms;
	Vector<String> QIDs;
	Vector<String> SIs;
	Vector<String> sensitive_topics;
	
	
	public void Process()
	{
		CValue cv = new CValue();
		cv.Process("C:\\data\\testingSD", "C:\\data\\models\\left3words-wsj-0-18.tagger");
		System.out.println("QID List:" + QIDs.toString());
		System.out.println("==================");
		System.out.println("SI List:" + SIs.toString());
	}
}
