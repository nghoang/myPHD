package InformationRetrieval;

import java.util.List;
import java.util.Vector;

import utility.Utilities;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class NamedEntitiesRecognition {
	String serializedClassifier = "libs/stanford-ner-2012-07-09/classifiers/english.all.3class.distsim.crf.ser.gz";
	
	public void RemoveNamedEntities(String sourceFolder, String destinationFolder)
	{
		Vector<String> files = Utilities.GetFileInFolder(sourceFolder);
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		for (String file : files)
		{
			String fileContent = Utilities.readFileAsString(sourceFolder + file);
			List<List<CoreLabel>> out = classifier.classify(fileContent);
			fileContent = "";
			for (List<CoreLabel> sentence : out) {
				for (CoreLabel word : sentence) {
					if (!word.get(AnswerAnnotation.class).equals("PERSON") && 
							!word.get(AnswerAnnotation.class).equals("DATE") && 
							!word.get(AnswerAnnotation.class).equals("TIME") && 
							!word.get(AnswerAnnotation.class).equals("LOCATION") && 
							!word.get(AnswerAnnotation.class).equals("ORGANIZATION"))
						fileContent += word.word() + " ";
					else
						fileContent += "[SUPPRESSED] ";
//					System.out.print(word.word() + '/'
//							+ word.get(AnswerAnnotation.class) + ' ');
				}
				fileContent += "\n";
				//System.out.println();
			}
			Utilities.WriteFile(destinationFolder + file, fileContent, false);
		}
	}
}
