package experiments;

import java.util.Vector;

import utility.WordNetLib;
import AppParameters.AppConst;

import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.captcha.CaptchaForm;
import com.ngochoang.captcha.ICaptchForm;

public class MeasureRelatedBetweenItemInOneDocumentByGoogle implements
		ICaptchForm {

	WebClientX client = new WebClientX();
	private String captcha_id = "";
	private String test_query = "http://www.google.com/search?q=%22female%22";

	public void SolveCaptcha(String content) {
		CaptchaForm form = new CaptchaForm(this);
		this.captcha_id = Utilities.SimpleRegexSingle(
				"name=\"id\" value=\"([^\"]+)\"", content, 1);
		String link = "http://www.google.com"
				+ Utilities
						.SimpleRegexSingle("<img src=\"([^\"]+)", content, 1);
		client.DownloadFile(link, "google_captcha.jpg");
		form.GetInput("google_captcha.jpg", 1);
	}

	public static void main(String[] args) {
		(new MeasureRelatedBetweenItemInOneDocumentByGoogle()).run();
	}

	public void run() {
		String testContent = client.GetMethod(this.test_query);
		if (testContent.indexOf("('captcha')") > 0) {
			SolveCaptcha(testContent);
			return;
		}

		Measure();
	}

	public void Measure() {
		GoogleSimilarityDistance g = new GoogleSimilarityDistance();
		g.client = this.client;

		String datasetName = "data/testdata";
		String anonymizedResultFile = datasetName + "/results/result.txt";
		String dcontent = Utilities.readFileAsString(anonymizedResultFile);
		String[] lines = dcontent.split("\n");
		for (String line : lines) {
			String terms = line;//"14[record], [tract], [infection], [regurgitation], [diabete], [hypertension], [heart], [failure], [prolapse], [hyperlipidemium], [hernium], [breast], [nodule], [history], [patient], [ejection], [fraction], [discomfort], [week], [admission], [nausea], [appetite], [weakness], [fatigue], [pain], [vomiting], [diarrhea], [cough], [dysurium], [presentation], [shock], [bacteremium], [pressure], [support], [rate], [blood], [exam], [rhythm, cycle, interim], [crescendo], [murmur], [decrescendo], [border], [apex], [axilla], [heave], [chest], [lung], [leg], [basilar], [clubbing], [cyanosis], [edema], [culture], [levofloxacin], [urine], [pneumoniae], [ampicillin], [resistant], [gentamicin], [tee], [posterior], [leaflet], [density], [hospital], [course], [impression], [female], [recurrent], [hypotension], [stay], [endocarditi], [disease], [treatment], [vancomycin], [continuation], [penicillin], [aspirin], [transaminiti], [liver], [time], [episode], [flash], [setting], [bolus], [unit], [balance], [dependence], [overload], [attempt], [cardiologist], [outpatient], [regiman], [afterload], [reduction], [medication], [telemetry], [sinus], [tachycardium], [evidence], [phosphatase], [sludge], [ultrasound], [colon], [cancer], [concern], [anemium], [colonoscopy], [repeat], [hematocrit], [iron], [ferritin], [deficiency], [cortisol], [level], [diagnosis], [protocol], [insulin], [scale], [coverage], [sulfate], [potassium], [lisinopril], [chloride], [rehabilitation], [order], [therapy], [condition], [stress], [test], [cardiology], [success], [ace], [lab], [pager], [number], [code], [family], [situation], [document], [report]";
			String generalized_item_string = "cycle, interim, rhythm";
			Vector<String> generalized_item = new Vector<String>();
			for (String i : generalized_item_string.split(",")) {
				generalized_item.add(i.trim());
			}
			terms = terms.replaceAll("\\[|\\]|^\\d+", "");

			Vector<String> items = new Vector<String>();

			for (String i : terms.split(",")) {
				if (i.trim().isEmpty() == false) {
					items.add(i.trim());
				}
			}

			Float[][] table = new Float[items.size()][items.size()];

			Vector<Integer> nonMeasure = new Vector<Integer>();
			int size = 3;
			for (int i = 0; i < items.size(); i++) {
				if (generalized_item.contains(items.get(i))) {
					for (int k = 0; k < size; k++)
						nonMeasure.add(i + k);
				}
				if (nonMeasure.size() == size)
					break;
			}
			if (nonMeasure.size() == 0)
				continue;
			int f = nonMeasure.get(0) - 3;
			int t = nonMeasure.get(nonMeasure.size() - 1) + 3;

			for (int i = 0; i < items.size(); i++) {
				for (int j = 0; j < items.size(); j++) {
					table[i][j] = 0F;
					table[j][j] = 0F;
				}
			}

			for (int i : nonMeasure) {
				for (int j = f; j <= t; j++) {
					if (nonMeasure.contains(j))
						continue;

					Float dis = -1F;

					if (i < j)
						dis = (float) g.Similarity(items.get(i), items.get(j));
					else
						dis = (float) g.Similarity(items.get(j), items.get(i));
					if (dis == -1) {
						SolveCaptcha(g.newContent);
						return;
					}
					table[i][j] = dis;
					table[j][i] = dis;
				}
			}
			System.out.println();

			for (int calculatingIndex : nonMeasure) {
				float temp1 = 0;
				for (int j = 0; j < table[calculatingIndex].length; j++) {
					temp1 += table[calculatingIndex][j];
				}

				System.out.println("Average Distance "
						+ items.get(calculatingIndex) + ": " + temp1 / (t - f));
			}

			String content = "";
			content += "";
			for (int i = 0; i < items.size(); i++) {
				content += "," + items.get(i);
			}
			content += "\n";

			for (int i = 0; i < items.size(); i++) {
				content += items.get(i);
				for (int j = 0; j < items.size(); j++) {
					if (table[i][j] == 0 || table[i][j] == 1)
						content += ", ";
					else
						content += "," + table[i][j];
				}
				content += "\n";
			}
			// Utilities.WriteFile("output_google.csv", content, false);
			System.out.println("==================");
		}
	}

	@Override
	public void ReturnForm(String captcha, int code) {
		client.GetMethod("http://www.google.com/sorry/Captcha?continue="
				+ Utilities.EncodeQuery(this.test_query) + "&id="
				+ this.captcha_id + "&captcha=" + captcha + "&submit=Submit");
		Measure();
	}
}
