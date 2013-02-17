package experiments;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.captcha.CaptchaForm;
import com.ngochoang.captcha.ICaptchForm;

public class AllExperimentsForm extends JFrame implements ICaptchForm {

	private JPanel contentPane;
	private JTextField textFieldSDContext;
	private JTextField textFieldSDTerm1;
	private JTextField textFieldSDTerm2;
	GoogleSimilarityDistance gd = new GoogleSimilarityDistance();
	DefaultListModel listSDResultModel = new DefaultListModel();
	private JList listSDResult;
	private JTextField txtCOATBasedFolder;
	private JTextField txtCOATOriginalFolder;
	private JTextField txtCOATNerred;
	private JTextField txtCOATAnoFolder;
	private JTextField txtCOATResultFolder;
	WebClientX client = new WebClientX();
	String captcha_id = "";
	private JTextField textFieldContextQuery;
	private JTextField textFieldLDTextFile;
	private JTextField textFieldLDContext;
	StandardAnalyzer analyzer;
	Directory index;
	private JTextField textFieldCCSStart;
	private JTextField textFieldSSDGeneralizedItem;
	private JTextField textFieldSSDDataFile;
	private JTextField textFieldCOATK;
	private JTextField textFieldSSDSize;
	private JTextField textFieldSSDEliCount;
	private JTextField textFieldCOATPrivacyConstraint;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AllExperimentsForm frame = new AllExperimentsForm();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AllExperimentsForm() {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		
		setTitle("Hoang Ong Research's Experiments");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 889, 455);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Semantic Context Distance", null, panel_1, null);
		panel_1.setLayout(null);

		JLabel lblContext = new JLabel("Context");
		lblContext.setBounds(6, 12, 61, 16);
		panel_1.add(lblContext);

		JLabel lblTerms = new JLabel("Term");
		lblTerms.setBounds(6, 52, 61, 16);
		panel_1.add(lblTerms);

		textFieldSDContext = new JTextField();
		textFieldSDContext.setBounds(75, 6, 134, 28);
		panel_1.add(textFieldSDContext);
		textFieldSDContext.setColumns(10);

		textFieldSDTerm1 = new JTextField();
		textFieldSDTerm1.setColumns(10);
		textFieldSDTerm1.setBounds(75, 46, 134, 28);
		panel_1.add(textFieldSDTerm1);

		JButton btnBeginMeasure = new JButton("Begin Measure");
		btnBeginMeasure.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				gd.client = client;
				Double dis = gd.Similarity(textFieldSDTerm1.getText(),
						textFieldSDTerm2.getText(),
						textFieldSDContext.getText(), "AND");
				if (dis == -1)
					listSDResultModel.addElement("Requite to solve captcha");
				else
					listSDResultModel.addElement("\""
							+ textFieldSDTerm1.getText() + "\" " + "\""
							+ textFieldSDTerm2.getText() + "\" in \""
							+ textFieldSDContext.getText() + "\" = " + dis);
			}
		});
		btnBeginMeasure.setBounds(75, 86, 117, 29);
		panel_1.add(btnBeginMeasure);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 121, 409, 105);
		panel_1.add(scrollPane);

		listSDResult = new JList();
		scrollPane.setViewportView(listSDResult);
		listSDResult.setModel(listSDResultModel);

		textFieldSDTerm2 = new JTextField();
		textFieldSDTerm2.setColumns(10);
		textFieldSDTerm2.setBounds(221, 46, 134, 28);
		panel_1.add(textFieldSDTerm2);

		JButton btnSolveCaptcha = new JButton("Solve Captcha");
		btnSolveCaptcha.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				SolveCaptcha();
			}
		});
		btnSolveCaptcha.setBounds(221, 86, 117, 29);
		panel_1.add(btnSolveCaptcha);

		JPanel panelCOAT = new JPanel();
		tabbedPane.addTab("COAT Algorithm", null, panelCOAT, null);
		panelCOAT.setLayout(null);

		JLabel lblBasedFolder = new JLabel("Based Folder");
		lblBasedFolder.setBounds(6, 26, 116, 16);
		panelCOAT.add(lblBasedFolder);

		txtCOATBasedFolder = new JTextField();
		txtCOATBasedFolder.setText("data/testdata");
		txtCOATBasedFolder.setBounds(168, 20, 195, 28);
		panelCOAT.add(txtCOATBasedFolder);
		txtCOATBasedFolder.setColumns(10);

		txtCOATOriginalFolder = new JTextField();
		txtCOATOriginalFolder.setText("/original/");
		txtCOATOriginalFolder.setColumns(10);
		txtCOATOriginalFolder.setBounds(168, 54, 195, 28);
		panelCOAT.add(txtCOATOriginalFolder);

		JLabel lblOriginalDataFolder = new JLabel("Old Text Data Folder");
		lblOriginalDataFolder.setBounds(6, 60, 150, 16);
		panelCOAT.add(lblOriginalDataFolder);

		txtCOATNerred = new JTextField();
		txtCOATNerred.setText("/nerred/");
		txtCOATNerred.setColumns(10);
		txtCOATNerred.setBounds(168, 94, 195, 28);
		panelCOAT.add(txtCOATNerred);

		JLabel lblOptimalDataFolder = new JLabel("Optimal Data Folder");
		lblOptimalDataFolder.setBounds(6, 100, 150, 16);
		panelCOAT.add(lblOptimalDataFolder);

		txtCOATAnoFolder = new JTextField();
		txtCOATAnoFolder.setText("/anonymized/");
		txtCOATAnoFolder.setColumns(10);
		txtCOATAnoFolder.setBounds(168, 134, 195, 28);
		panelCOAT.add(txtCOATAnoFolder);

		JLabel lblAnonymizedDataFolder = new JLabel("Anonymized Data Folder");
		lblAnonymizedDataFolder.setBounds(6, 140, 154, 16);
		panelCOAT.add(lblAnonymizedDataFolder);

		JLabel lblDataFile = new JLabel("Result Folder");
		lblDataFile.setBounds(6, 180, 154, 16);
		panelCOAT.add(lblDataFile);

		txtCOATResultFolder = new JTextField();
		txtCOATResultFolder.setText("/results/");
		txtCOATResultFolder.setColumns(10);
		txtCOATResultFolder.setBounds(168, 174, 195, 28);
		panelCOAT.add(txtCOATResultFolder);

		JButton btnExtractToTransaction = new JButton("Extract to Transaction");
		btnExtractToTransaction.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				AnonymizeDocuments.AnonymizeDocs(
						txtCOATBasedFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATOriginalFolder.getText(),
						txtCOATBasedFolder.getText() + txtCOATNerred.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATAnoFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATResultFolder.getText(), 1, Integer.parseInt(textFieldCOATK.getText()),"");
			}
		});
		btnExtractToTransaction.setBounds(168, 214, 195, 29);
		panelCOAT.add(btnExtractToTransaction);

		JButton btnExtractUtilityConstraints = new JButton(
				"Extract Utility Constraints");
		btnExtractUtilityConstraints.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				AnonymizeDocuments.AnonymizeDocs(
						txtCOATBasedFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATOriginalFolder.getText(),
						txtCOATBasedFolder.getText() + txtCOATNerred.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATAnoFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATResultFolder.getText(), 3, Integer.parseInt(textFieldCOATK.getText()),"");
			}
		});
		btnExtractUtilityConstraints.setBounds(168, 242, 195, 29);
		panelCOAT.add(btnExtractUtilityConstraints);

		JButton btnExtractPrivacyConstraints = new JButton(
				"Extract Privacy Constraints");
		btnExtractPrivacyConstraints.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				AnonymizeDocuments.AnonymizeDocs(
						txtCOATBasedFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATOriginalFolder.getText(),
						txtCOATBasedFolder.getText() + txtCOATNerred.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATAnoFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATResultFolder.getText(), 2, Integer.parseInt(textFieldCOATK.getText()),"");
			}
		});
		btnExtractPrivacyConstraints.setBounds(168, 272, 195, 29);
		panelCOAT.add(btnExtractPrivacyConstraints);

		JButton btnAnonymize = new JButton("Anonymize");
		btnAnonymize.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				AnonymizeDocuments.AnonymizeDocs(
						txtCOATBasedFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATOriginalFolder.getText(),
						txtCOATBasedFolder.getText() + txtCOATNerred.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATAnoFolder.getText(),
						txtCOATBasedFolder.getText()
								+ txtCOATResultFolder.getText(), 4, Integer.parseInt(textFieldCOATK.getText()),textFieldCOATPrivacyConstraint.getText());
			}
		});
		btnAnonymize.setBounds(168, 300, 195, 29);
		panelCOAT.add(btnAnonymize);
		
		JLabel lblK = new JLabel("K");
		lblK.setBounds(375, 26, 34, 16);
		panelCOAT.add(lblK);
		
		textFieldCOATK = new JTextField();
		textFieldCOATK.setText("2");
		textFieldCOATK.setColumns(10);
		textFieldCOATK.setBounds(385, 20, 65, 28);
		panelCOAT.add(textFieldCOATK);
		
		textFieldCOATPrivacyConstraint = new JTextField();
		textFieldCOATPrivacyConstraint.setBounds(375, 271, 379, 28);
		panelCOAT.add(textFieldCOATPrivacyConstraint);
		textFieldCOATPrivacyConstraint.setColumns(10);

		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Collect Context Sample", null, panel_2, null);
		panel_2.setLayout(null);

		JLabel lblContext_1 = new JLabel("Context");
		lblContext_1.setBounds(10, 22, 77, 14);
		panel_2.add(lblContext_1);

		textFieldContextQuery = new JTextField();
		textFieldContextQuery.setBounds(68, 19, 190, 20);
		panel_2.add(textFieldContextQuery);
		textFieldContextQuery.setColumns(10);

		JButton btnBegin = new JButton("Begin");
		btnBegin.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				String context = textFieldContextQuery.getText();
				String nextPage = "";
				do {
					String content = "";
					if (nextPage.equals(""))
						content = client
								.GetMethod("http://www.google.com/search?q=inurl%3A"
										+ Utilities.EncodeQuery(context)
										+ "&start="
										+ textFieldCCSStart.getText());
					else
						content = client.GetMethod("http://www.google.com"
								+ nextPage.replace("&amp;", "&"));

					// Utilities.WriteFile("abc.txt", content, false);
					Vector<String> links = Utilities.SimpleRegex(
							"<h3 class=\"r\"><a href=\"(http[^\"]*)", content,
							1);
					Utilities.CreateFolderIfNotExist("contexts/" + context);
					for (String link : links) {
						if (link.startsWith("http") == false)
							continue;
						// System.out.print(".");
						String filePath = "contexts/" + context + "/"
								+ Utilities.md5(link) + ".html";
						if (Utilities.IsFileExist(filePath))
							continue;
						String subcontent = client.GetMethod(link);
						Utilities.WriteFile(filePath, subcontent, false);
					}
					System.out.println();
					nextPage = Utilities.SimpleRegexSingle(
							"<a href=\"([^\"]*)\" class=\"pn\" id=\"pnnext\"",
							content, 1);
					System.out.println("next page: " + nextPage);
					Utilities.WriteFile("last_page.html", content, false);
				} while (!nextPage.equals(""));
				System.out.println("Finished");
			}
		});
		btnBegin.setBounds(68, 85, 89, 23);
		panel_2.add(btnBegin);

		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(10, 51, 77, 14);
		panel_2.add(lblStart);

		textFieldCCSStart = new JTextField();
		textFieldCCSStart.setText("0");
		textFieldCCSStart.setColumns(10);
		textFieldCCSStart.setBounds(68, 48, 77, 20);
		panel_2.add(textFieldCCSStart);

		JPanel panel_Semantic_Attack = new JPanel();
		tabbedPane.addTab("Semantic Attack", null, panel_Semantic_Attack, null);
		panel_Semantic_Attack.setLayout(null);

		JButton btnIndex = new JButton("Measure");
		btnIndex.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				//SearchIndex(textFieldLDTextFile.getText());
				//SearchMultipleIndexes(
				for (String size : textFieldSSDSize.getText().split(","))
					for (String count : textFieldSSDEliCount.getText().split(","))
						SeachMultipleIndexes(textFieldLDTextFile.getText(),
								textFieldSSDGeneralizedItem.getText(),
								textFieldSSDDataFile.getText(),
								Integer.parseInt(textFieldCOATK.getText()),
								Integer.parseInt(size),
								Integer.parseInt(count));
			}
		});
		btnIndex.setBounds(189, 174, 117, 29);
		panel_Semantic_Attack.add(btnIndex);

		textFieldLDTextFile = new JTextField();
		textFieldLDTextFile.setText("/Users/hoangong/Desktop/ProjectMac/Java/phdproject/data/testdata/results/result.txt");
		textFieldLDTextFile.setEditable(false);
		textFieldLDTextFile.setColumns(10);
		textFieldLDTextFile.setBounds(79, 50, 263, 28);
		panel_Semantic_Attack.add(textFieldLDTextFile);

		textFieldLDContext = new JTextField();
		textFieldLDContext.setColumns(10);
		textFieldLDContext.setBounds(79, 11, 134, 28);
		panel_Semantic_Attack.add(textFieldLDContext);

		JLabel label = new JLabel("Context");
		label.setBounds(10, 17, 61, 16);
		panel_Semantic_Attack.add(label);

		JLabel lblItem = new JLabel("Result File");
		lblItem.setBounds(10, 57, 125, 16);
		panel_Semantic_Attack.add(lblItem);

		JButton btnPrepare = new JButton("Prepare");
		btnPrepare.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				CreateSearchIndex();
			}
		});
		btnPrepare.setBounds(79, 174, 117, 29);
		panel_Semantic_Attack.add(btnPrepare);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				 JFileChooser fc = new JFileChooser();
				 int returnVal = fc.showOpenDialog(AllExperimentsForm.this);
				 if (returnVal == JFileChooser.APPROVE_OPTION)
				 {
					 textFieldLDTextFile.setText(fc.getSelectedFile().getAbsolutePath());
				 }
			}
		});
		btnBrowse.setBounds(343, 51, 117, 29);
		panel_Semantic_Attack.add(btnBrowse);
		
		JLabel lblGItems = new JLabel("G Items");
		lblGItems.setBounds(10, 141, 61, 16);
		panel_Semantic_Attack.add(lblGItems);
		
		textFieldSSDGeneralizedItem = new JTextField();
		textFieldSSDGeneralizedItem.setColumns(10);
		textFieldSSDGeneralizedItem.setBounds(79, 134, 263, 28);
		panel_Semantic_Attack.add(textFieldSSDGeneralizedItem);
		
		JLabel lblDataFile_1 = new JLabel("Data File");
		lblDataFile_1.setBounds(10, 92, 125, 16);
		panel_Semantic_Attack.add(lblDataFile_1);
		
		textFieldSSDDataFile = new JTextField();
		textFieldSSDDataFile.setText("/Users/hoangong/Desktop/ProjectMac/Java/phdproject/data/testdata/results/dataset.txt");
		textFieldSSDDataFile.setEditable(false);
		textFieldSSDDataFile.setColumns(10);
		textFieldSSDDataFile.setBounds(79, 85, 263, 28);
		panel_Semantic_Attack.add(textFieldSSDDataFile);
		
		JButton button = new JButton("Browse");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				JFileChooser fc = new JFileChooser();
				 int returnVal = fc.showOpenDialog(AllExperimentsForm.this);
				 if (returnVal == JFileChooser.APPROVE_OPTION)
				 {
					 textFieldSSDDataFile.setText(fc.getSelectedFile().getAbsolutePath());
				 }
				
			}
		});
		button.setBounds(343, 86, 117, 29);
		panel_Semantic_Attack.add(button);
		
		textFieldSSDSize = new JTextField();
		textFieldSSDSize.setText("3");
		textFieldSSDSize.setColumns(10);
		textFieldSSDSize.setBounds(255, 11, 61, 28);
		panel_Semantic_Attack.add(textFieldSSDSize);
		
		JLabel lblSize = new JLabel("Size");
		lblSize.setBounds(225, 17, 61, 16);
		panel_Semantic_Attack.add(lblSize);
		
		textFieldSSDEliCount = new JTextField();
		textFieldSSDEliCount.setText("1,2");
		textFieldSSDEliCount.setColumns(10);
		textFieldSSDEliCount.setBounds(456, 11, 61, 28);
		panel_Semantic_Attack.add(textFieldSSDEliCount);
		
		JLabel lblEliminatedCount = new JLabel("Eliminated Count");
		lblEliminatedCount.setBounds(343, 17, 117, 16);
		panel_Semantic_Attack.add(lblEliminatedCount);
		
		JButton btnMeasure = new JButton("Measure 2");
		btnMeasure.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				for (String size : textFieldSSDSize.getText().split(","))
					for (String count : textFieldSSDEliCount.getText().split(","))
						SeachMultipleIndexesByConditionalProbability(textFieldLDTextFile.getText(),
								textFieldSSDGeneralizedItem.getText(),
								textFieldSSDDataFile.getText(),
								Integer.parseInt(textFieldCOATK.getText()),
								Integer.parseInt(size),
								Integer.parseInt(count));
			}
		});
		btnMeasure.setBounds(318, 174, 117, 29);
		panel_Semantic_Attack.add(btnMeasure);
	}

	public void SolveCaptcha() {
		String content = "";
		if (!gd.last_query.equals(""))
			content = client.GetMethod(gd.last_query);
		else
			content = client
					.GetMethod("http://www.google.com/search?q=inurl:health");

		CaptchaForm form = new CaptchaForm(this);
		this.captcha_id = Utilities.SimpleRegexSingle(
				"name=\"id\" value=\"([^\"]+)\"", content, 1);
		String link = "http://www.google.com"
				+ Utilities
						.SimpleRegexSingle("<img src=\"([^\"]+)", content, 1);
		client.DownloadFile(link, "google_captcha.jpg");
		form.GetInput("google_captcha.jpg", 1);
	}

	@Override
	public void ReturnForm(String captcha, int code) {
		client.GetMethod("http://www.google.com/sorry/Captcha?continue=abc&id="
				+ this.captcha_id + "&captcha=" + captcha + "&submit=Submit");
	}

	public void SeachMultipleIndexesByConditionalProbability(String file, String generalized_item_string, String dataSetFile, int k, int measuresize, int eliminatedCount)
	{
		String anonymizedResultFile = file;
		String dcontent = Utilities.readFileAsString(anonymizedResultFile);
		String[] lines = dcontent.split("\n");
		int correctResult = 0;
		int failedResult = 0;
		Vector<Vector<String>> final_result = new Vector<Vector<String>>();
		Vector<String> setOfItem = new Vector<String>();
		Vector<String> generalized_item = new Vector<String>();
		for (String i : generalized_item_string.split(",")) {
			generalized_item.add(i.trim());
		}
		
		Vector<Vector<String>> final_correct_result = new Vector<Vector<String>>();
		
		for (String line : lines) {
			String terms = line;
			String documentId = Utilities.SimpleRegexSingle("^(\\d+)", terms, 1);
			terms = terms.replaceAll("\\[|\\]|^\\d+", "");
			Vector<String> items = new Vector<String>();
			for (String i : terms.split(",")) {
				if (i.trim().isEmpty() == false) {
					items.add(i.trim());
				}
			}
			
			Vector<Integer> nonMeasure = new Vector<Integer>();
			int size = generalized_item.size();
			for (int i = 0; i < items.size(); i++) {
				if (generalized_item.contains(items.get(i))) {
					for (int jk = 0; jk < size; jk++)
						nonMeasure.add(i + jk);
				}
				if (nonMeasure.size() == size)
					break;
			}
			if (nonMeasure.size() == 0)
				continue;
			int f = nonMeasure.get(0) - measuresize;
			int t = nonMeasure.get(nonMeasure.size() - 1) + measuresize;
			if (f < 0)
				f = 0;
			if (t >= items.size())
				t = items.size() - 1;
			
			Vector<String> setOfItemInside = new Vector<String>();
			
			for (int i=f; i<= t;i++)
			{
				if (setOfItemInside.contains(items.get(i)) == false && generalized_item.contains(items.get(i)) == false)
					setOfItemInside.add(items.get(i));
			}
			
			Vector<Double> pros = new Vector<Double>();
			for (String target : generalized_item)
			{
				String text1 = setOfItemInside.toString().replace("]", "").replace("[", "").replace(", ", " AND ");
				String text2 = target + " AND " + text1;
				Double pro1 = CountAppear(text1)/1000D;
				Double pro2 = CountAppear(text2)/1000D;
				pros.add(pro1/pro2);
			}
			System.out.println(documentId + "<<<" + generalized_item_string + ": " + pros.toString());
		}
	}

	public void SeachMultipleIndexes(String file, String generalized_item_string, String dataSetFile, int k, int measuresize, int eliminatedCount)
	{
		String anonymizedResultFile = file;
		String dcontent = Utilities.readFileAsString(anonymizedResultFile);
		String[] lines = dcontent.split("\n");
		int correctResult = 0;
		int failedResult = 0;
		Vector<Vector<String>> final_result = new Vector<Vector<String>>();
		Vector<String> setOfItem = new Vector<String>();
		Vector<String> generalized_item = new Vector<String>();
		for (String i : generalized_item_string.split(",")) {
			generalized_item.add(i.trim());
		}
		Vector<Vector<String>> final_correct_result = new Vector<Vector<String>>();
		for (String line : lines) {
			String terms = line;//"14[record], [tract], [infection], [regurgitation], [diabete], [hypertension], [heart], [failure], [prolapse], [hyperlipidemium], [hernium], [breast], [nodule], [history], [patient], [ejection], [fraction], [discomfort], [week], [admission], [nausea], [appetite], [weakness], [fatigue], [pain], [vomiting], [diarrhea], [cough], [dysurium], [presentation], [shock], [bacteremium], [pressure], [support], [rate], [blood], [exam], [rhythm, cycle, interim], [crescendo], [murmur], [decrescendo], [border], [apex], [axilla], [heave], [chest], [lung], [leg], [basilar], [clubbing], [cyanosis], [edema], [culture], [levofloxacin], [urine], [pneumoniae], [ampicillin], [resistant], [gentamicin], [tee], [posterior], [leaflet], [density], [hospital], [course], [impression], [female], [recurrent], [hypotension], [stay], [endocarditi], [disease], [treatment], [vancomycin], [continuation], [penicillin], [aspirin], [transaminiti], [liver], [time], [episode], [flash], [setting], [bolus], [unit], [balance], [dependence], [overload], [attempt], [cardiologist], [outpatient], [regiman], [afterload], [reduction], [medication], [telemetry], [sinus], [tachycardium], [evidence], [phosphatase], [sludge], [ultrasound], [colon], [cancer], [concern], [anemium], [colonoscopy], [repeat], [hematocrit], [iron], [ferritin], [deficiency], [cortisol], [level], [diagnosis], [protocol], [insulin], [scale], [coverage], [sulfate], [potassium], [lisinopril], [chloride], [rehabilitation], [order], [therapy], [condition], [stress], [test], [cardiology], [success], [ace], [lab], [pager], [number], [code], [family], [situation], [document], [report]";
			//String generalized_item_string = "cycle, interim, rhythm";
			String documentId = Utilities.SimpleRegexSingle("^(\\d+)", terms, 1);
			terms = terms.replaceAll("\\[|\\]|^\\d+", "");

			Vector<String> items = new Vector<String>();

			for (String i : terms.split(",")) {
				if (i.trim().isEmpty() == false) {
					items.add(i.trim());
				}
			}

			Float[][] table = new Float[items.size()][items.size()];

			Vector<Integer> nonMeasure = new Vector<Integer>();
			int size = generalized_item.size();
			for (int i = 0; i < items.size(); i++) {
				if (generalized_item.contains(items.get(i))) {
					for (int jk = 0; jk < size; jk++)
						nonMeasure.add(i + jk);
				}
				if (nonMeasure.size() == size)
					break;
			}
			if (nonMeasure.size() == 0)
				continue;
			int f = nonMeasure.get(0) - measuresize;
			int t = nonMeasure.get(nonMeasure.size() - 1) + measuresize;
			if (f < 0)
				f = 0;
			if (t >= items.size())
				t = items.size() - 1;

			for (int i = 0; i < items.size(); i++) {
				for (int j = 0; j < items.size(); j++) {
					table[i][j] = 0F;
					table[j][j] = 0F;
				}
			}
			
			Vector<String> setOfItemInside = new Vector<String>();
			
			for (int i=f; i<= t;i++)
			{
				if (setOfItem.contains(items.get(i)) == false)
					setOfItem.add(items.get(i));
				
				if (setOfItemInside.contains(items.get(i)) == false && generalized_item.contains(items.get(i)) == false)
					setOfItemInside.add(items.get(i));
			}

			for (int i : nonMeasure) {
				for (int j = f; j <= t; j++) {
					if (nonMeasure.contains(j))
						continue;

					double dis = 0D;
					//System.out.println(i+","+j);
					dis = SampleGoogleDistanceValue(items.get(i), items.get(j));
					table[i][j] = (float)dis;
					table[j][i] = (float)dis;
				}
			}
			System.out.println();
			System.out.println("Document: " + documentId);

			int notCountedIn = 0;
			Vector<String> eliminatedItem = new Vector<String>();
			Vector<Double> eliminatedItemDistance = new Vector<Double>();
			Double maxDistance = 0D;
			for (int calculatingIndex : nonMeasure) {
				float temp1 = 0;
				for (int j = 0; j < table[calculatingIndex].length; j++) {
					if (table[calculatingIndex][j] > 0)
						temp1 += table[calculatingIndex][j];
					else if (table[calculatingIndex][j] == Double.NaN)
						notCountedIn++;
				}
				
				Double averageDistance = (double)temp1 / (t - f - notCountedIn);

				if (maxDistance < averageDistance)
				{
					maxDistance = averageDistance;
					if (eliminatedItemDistance.size() < eliminatedCount)
					{
						eliminatedItemDistance.add(maxDistance);
						eliminatedItem.add(items.get(calculatingIndex));
					}
					else
					{
						Double smallestDistance = 999D;
						int smallestIndex = -1;
						for (int i=0;i<eliminatedItem.size();i++)
						{
							if (eliminatedItemDistance.get(i) < smallestDistance)
							{
								smallestDistance = eliminatedItemDistance.get(i);
								smallestIndex = i;
							}
						}
						maxDistance = smallestDistance;
						eliminatedItemDistance.setElementAt(maxDistance,smallestIndex);
						eliminatedItem.setElementAt(items.get(calculatingIndex),smallestIndex);
					}
				}
				System.out.println("Average Distance "
						+ items.get(calculatingIndex) + ": " + averageDistance);
			}
			
			Vector<String> correctItem = new Vector<String>();
			String dsContent = Utilities.readFileAsString(dataSetFile);
			String[] dsLines = dsContent.split("\n");
			for (String dsLine : dsLines)
			{
				if (dsLine.trim().equals(""))
					continue;
				String[] parts = dsLine.split(";");
				String dsLineId = parts[0];
				if (dsLineId.equals(documentId) == false)
					continue;
				for (String gi : generalized_item)
				{
					if (Arrays.asList(parts).contains(gi))
						correctItem.add(gi);
				}
			}
			Vector<String> result = generalized_item;
			result.removeAll(eliminatedItem);
			final_result.add(result);
			System.out.println("Eliminated Item: " + eliminatedItem.toString());
			System.out.println("Correct Item: " + correctItem.toString());
			boolean isFailed = false;
			for (String ei : eliminatedItem)
			{
				if (correctItem.contains(ei))
				{
					failedResult++;
					isFailed = true;
					break;
				}
			}
			if (isFailed == false)
			{
				final_correct_result.add(result);
				correctResult++;
			}
		}
		
		System.out.println("success ratio: " + String.valueOf((double)correctResult/(failedResult + correctResult)));
		
		System.out.println("Set Item: " + setOfItem.toString());
		for (Vector<String> r : final_result)
			System.out.println(r.toString());
		
		int attackedCount = 0;
		for (Vector<String> r : final_correct_result)
		{
			int kk = 0;
			for (Vector<String> r2 : final_correct_result)
			{
				if (r2.containsAll(r))
				{
					kk++;
				}
			}
			if (kk < k)
				attackedCount++;
		}
		System.out.println("attacked ratio: " + String.valueOf((double)attackedCount/final_result.size()));
		String content = k+","+measuresize+","+eliminatedCount+"," + String.valueOf((double)correctResult/(failedResult + correctResult)) + ","+String.valueOf((double)attackedCount/final_result.size())+"\n";
		Utilities.WriteFile("attack_result.csv", content, true);
	}
	
	public void SearchIndex(String terms) {
		Vector<String> items = new Vector<String>();
		for (String i : terms.split(",")) {
			if (i.trim().isEmpty() == false) {
				items.add(i.trim());
			}
		}
		Float[][] table = new Float[items.size()][items.size()];

		for (int i = 0; i < items.size(); i++) {
			for (int j = i; j < items.size(); j++) {
				if (i == j) {
					table[i][j] = 0F;
					continue;
				}

				String text1 = items.get(i);
				String text2 = items.get(j);

				double dis = SampleGoogleDistanceValue(text1, text2);
				table[i][j] = (float)dis;
				table[j][i] = (float)dis;

			}
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
		Utilities.WriteFile("sample_distance_table.csv", content, false);
	}
	
	public int CountAppear(String text)
	{
		try {
			int hitsPerPage = 1000;
			Query q = new QueryParser(Version.LUCENE_35, "content",
					analyzer).parse(text);
			IndexReader reader = IndexReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector
					.create(hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			return hits.length;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public Double SampleGoogleDistanceValue(String text1, String text2)
	{
		try {
			int hitsPerPage = 1000;
			Query q = new QueryParser(Version.LUCENE_35, "content",
					analyzer).parse(text1);
			IndexReader reader = IndexReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector
					.create(hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			int countw1 = hits.length;

			q = new QueryParser(Version.LUCENE_35, "content", analyzer)
					.parse(text2);
			reader = IndexReader.open(index);
			searcher = new IndexSearcher(reader);
			collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(q, collector);
			hits = collector.topDocs().scoreDocs;
			int countw2 = hits.length;

			q = new QueryParser(Version.LUCENE_35, "content", analyzer)
					.parse(text1 + " AND " + text2);
			reader = IndexReader.open(index);
			searcher = new IndexSearcher(reader);
			collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(q, collector);
			hits = collector.topDocs().scoreDocs;
			int countw12 = hits.length;

			double logw1 = Math.log10(countw1);
			double logw2 = Math.log10(countw2);
			double logw12 = Math.log10(countw12);

			if (logw1 == 0 || logw2 == 0 || logw12 == 0)
			{
				searcher.close();
				return Double.NaN;
			}
			double dis = (Math.max(logw1, logw2) - logw12)
					/ (Math.log10(hitsPerPage) - Math.min(logw1, logw2));

			searcher.close();
			return dis;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void CreateSearchIndex() {
		try {
			String context = "/Users/hoangong/Desktop/ProjectMac/Java/phdproject/contexts/" + textFieldLDContext.getText() + "/";
			index = new RAMDirectory();
			analyzer = new StandardAnalyzer(Version.LUCENE_35);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35,
					analyzer);
			IndexWriter w = new IndexWriter(index, config);
			Vector<String> files = Utilities.GetFileInFolder(context);
			for (String file : files) {
				String content = Utilities.readFileAsString(context + file);
				content = content.replaceAll("<script>.*?(?=<script>)<script>", "");
				content = content.replaceAll("<.*?(?=>)>", "");
				addDoc(w, file, content);
				System.out.println("Create index " + file);
			}
			w.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addDoc(IndexWriter w, String title, String content)
			throws IOException {
		Document doc = new Document();
		doc.add(new Field("title", title, Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("content", content, Field.Store.YES,
				Field.Index.ANALYZED));
		w.addDocument(doc);
	}
}
