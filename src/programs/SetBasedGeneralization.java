package programs;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import utility.Inflector;
import utility.Utilities;
import utility.WordNetLib;
import DataStructure.Transaction;
import DataStructure.TransactionDataset;
import DataStructure.TransactionItem;
import algorithms.COAT2;
import algorithms.CValue;
import algorithms.POSTagging;

public class SetBasedGeneralization extends JFrame {

	private JPanel contentPane;
	String DataFolder = "C:\\diggdata\\";
	String AnonymizedDataFolder = "C:\\an_diggdata\\";
	String POSFolder = "C:\\stanford-postagger-2012-05-22\\models\\wsj-0-18-left3words.tagger";
	String WordNetFolder = "C:\\Program Files\\WordNet\\2.1\\dict\\";
	TransactionDataset dataset;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SetBasedGeneralization frame = new SetBasedGeneralization();
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
	public SetBasedGeneralization() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 244, 205);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnBrowseFolder = new JButton("Browse Data Folder");
		btnBrowseFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(SetBasedGeneralization.this) == JFileChooser.APPROVE_OPTION) {
					DataFolder = chooser.getSelectedFile().getName();
				}
			}
		});
		btnBrowseFolder.setBounds(12, 13, 202, 25);
		contentPane.add(btnBrowseFolder);

		JButton btnGeneralize = new JButton("Generalize");
		btnGeneralize.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String data_set_file = AnonymizedDataFolder + "dataset.txt";
				String reg = "(\\d+);(.*)";
				String separator = ";";
				String domain_file = AnonymizedDataFolder + "domain.txt";
				String privacy_constraint_file = AnonymizedDataFolder
						+ "privacy_constraint.txt";
				String utility_constraint_file = AnonymizedDataFolder
						+ "utility_constraint.txt";
				String result_file = AnonymizedDataFolder + "result.txt";

				// //////////////////////
				// begin generalization
				WordNetLib wn = new WordNetLib(WordNetFolder);
				int k = 2;
				int z = 50;

				if (Utilities.IsFileExist(data_set_file) == false)
					CreateTransaction(DataFolder, data_set_file);
				LoadTransaction(data_set_file, reg, separator);
				if (Utilities.IsFileExist(domain_file) == false)
					CreateDomainFile(domain_file);
				if (Utilities.IsFileExist(privacy_constraint_file) == false)
					PGenRandom(privacy_constraint_file, 300, 2, 10);
				if (Utilities.IsFileExist(utility_constraint_file) == false)
					UGen(wn, utility_constraint_file, domain_file, z);

				Anonymize(privacy_constraint_file, utility_constraint_file, k,
						result_file, DataFolder, AnonymizedDataFolder);
				// end generalization
			}
		});
		btnGeneralize.setBounds(12, 122, 202, 25);
		contentPane.add(btnGeneralize);

		JButton btnNewButton = new JButton("Browse POS Folder");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				if (chooser.showOpenDialog(SetBasedGeneralization.this) == JFileChooser.APPROVE_OPTION) {
					POSFolder = chooser.getSelectedFile().getName();
				}
			}
		});
		btnNewButton.setBounds(12, 51, 202, 25);
		contentPane.add(btnNewButton);

		JButton btnBrowseWordnetFolder = new JButton("Browse WordNet Folder");
		btnBrowseWordnetFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(SetBasedGeneralization.this) == JFileChooser.APPROVE_OPTION) {
					WordNetFolder = chooser.getSelectedFile().getName();
				}
			}
		});
		btnBrowseWordnetFolder.setBounds(12, 89, 202, 25);
		contentPane.add(btnBrowseWordnetFolder);
	}

	public void CreateTransaction(String data_folder, String data_set_file) {
		POSTagging cvalueAlgorithm = new POSTagging();
		cvalueAlgorithm.ExtractTextToTransaction(data_folder, POSFolder);
		Integer i = 0;
		Utilities.WriteFile(data_set_file, "", false);
		for (Transaction t : cvalueAlgorithm.documents.getRecords()) {
			String line = i.toString();
			for (TransactionItem it : t.GetData()) {
				line += ";" + it.getItem();
			}
			Utilities.WriteFile(data_set_file, line + "\n", true);
			i++;
		}
	}

	public void LoadTransaction(String data_set_file, String reg,
			String separator) {

		String content = Utilities.readFileAsString(data_set_file);
		dataset = new TransactionDataset();
		Vector<String> ids = Utilities.SimpleRegex(reg, content, 1,
				Pattern.CASE_INSENSITIVE);
		Vector<String> item_lists = Utilities.SimpleRegex(reg, content, 2,
				Pattern.CASE_INSENSITIVE);
		Inflector inf;
		inf = new Inflector();
		for (int i = 0; i < ids.size(); i++) {
			Transaction tran = new Transaction();
			tran.setId(ids.get(i));
			String[] items = item_lists.get(i).trim().split(separator);
			for (String j : items) {
				if (!Utilities.SimpleRegexSingle("([^0-9a-zA-Z])", j.trim(), 1)
						.equals(""))
					continue;
				tran.AddItem(new TransactionItem(inf.singularize(j)
						.toLowerCase()));
			}
			dataset.AddRecord(tran);
		}
		dataset.SortBySize();
	}

	public void CreateDomainFile(String domain_file) {
		File f;
		f = new File(domain_file);
		f.delete();
		Vector<TransactionItem> domain = dataset.getDomain().GetData();
		for (TransactionItem i : domain) {
			Utilities.WriteFile(domain_file, i + "\n", true);
		}
	}

	public void PGenRandom(String privacy_constraint_file, int noRulesf,
			int minSize, int maxSize) {
		int noRules = noRulesf;
		Vector<TransactionItem> domain = dataset.getDomain().GetData();
		Vector<String> selectedTerms = new Vector<String>();
		String content = "";
		for (int i = 0; i < noRules; i++) {
			Random r = new Random();
			int size = r.nextInt(maxSize - minSize) + minSize;
			for (int j = 0; j <= size; j++) {
				String item = "";
				do {
					item = domain.get(r.nextInt(domain.size())).getItem();
					if (selectedTerms.contains(item)) {
						continue;
					} else {
						selectedTerms.add(item);
					}
				} while (item.trim().equals(""));
				if (j == 0)
					content += item;
				else
					content += ", " + item;
			}
			content += "\n";
		}
		Utilities.WriteFile(privacy_constraint_file, content, false);
	}

	public void PGen(String privacy_constraint_file, int k) {
		Utilities.WriteFile(privacy_constraint_file, "", false);
		for (int i = 0; i < dataset.getRecords().size(); i++) {
			int _k = 1;
			for (int j = i + 1; j < dataset.getRecords().size(); j++) {
				if (dataset.getRecords().get(j)
						.IsContainsAll(dataset.getRecords().get(i).GetData())) {
					_k++;
					if (_k >= k) {
						break;
					}
				}
			}
			if (_k < k) {
				Utilities.WriteFile(privacy_constraint_file, dataset
						.getRecords().get(i).toString()
						+ "\n", true);
			}
		}
	}

	public void UGen(WordNetLib wn, String utility_constraint_file,
			String domain_file, int max_UGen_size) {
		Utilities.WriteFile(utility_constraint_file, "", false);
		String content = Utilities.readFileAsString(domain_file);
		Vector<String> domain = new Vector<String>();
		for (String line : content.split("\n")) {
			domain.add(line.trim());
		}

		Collections.sort(domain);

		Vector<String> group;
		while (domain.size() > 0) {
			group = new Vector<String>();
			String term = domain.get(0).trim();
			System.out.println(domain.size());
			group.add(term);
			domain.remove(0);
			if (term.equals(""))
				continue;
			Vector<String> slidings = wn.GetRelated3(term, max_UGen_size);
			for (String sl : slidings) {
				int index = Collections.binarySearch(domain, sl);
				if (index > -1) {
					if (group.indexOf(domain.get(index)) == -1)
						group.add(domain.get(index));
					domain.remove(index);
				}
			}
			String print_line = group.toString();
			if (group.size() > 1) {
				print_line = print_line.replace("]", "");
				print_line = print_line.replace("[", "");
				Utilities.WriteFile(utility_constraint_file, print_line + "\n",
						true);
			}
		}
	}

	public void Anonymize(String privacy_constraint_file,
			String utility_constraint_file, int k, String result_file,
			String original_data_folder, String anononymized_folder) {
		COAT2 coat = new COAT2();
		coat.D = dataset;
		coat.LoadPrivacyContraints(privacy_constraint_file);
		coat.LoadUtilityContraints(utility_constraint_file);
		coat.SetParameters(k, 1);
		coat.run(result_file);

		Vector<String> filenames = Utilities
				.GetFileInFolder(original_data_folder);
		Vector<String> filecontents = new Vector<String>();
		for (String fn : filenames) {
			filecontents.add(Utilities.readFileAsString(original_data_folder
					+ fn));
		}

		String suppressed_by = " _____ ";
		// Inflector inf = new Inflector();

		for (int i = 0; i < filecontents.size(); i++) {
			String newData = filecontents.get(i);
			for (String suppressed_term : coat.GetSuppressedItems()) {
				newData = newData.replaceAll("(?i)[\\s\\.,;]+"
						+ suppressed_term + "[\\s\\.,;]+", suppressed_by);
			}

			filecontents.set(i, newData);
		}

		for (Vector<String> generalized_term : coat.GetGeneralizeditems()) {
			for (int i = 0; i < filecontents.size(); i++) {
				String newData = filecontents.get(i);
				for (String sub : generalized_term) {
					newData = newData.replaceAll("(?i)[\\s\\.,;]+" + sub
							+ "[\\s\\.,;]+", generalized_term.toString());
					if (!filecontents.get(i).equals(newData))
						break;
				}
				filecontents.set(i, newData);
			}
		}

		for (int i = 0; i < filecontents.size(); i++) {
			Utilities.WriteFile(anononymized_folder + filenames.get(i),
					filecontents.get(i), false);
		}
	}
}
