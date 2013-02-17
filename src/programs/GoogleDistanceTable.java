package programs;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import utility.WordNetLib;
import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;

public class GoogleDistanceTable extends JFrame {

	private JPanel contentPane;
	private JTextField txtTargets;


	WebClientX client = new WebClientX();
	private JTextField txtItems;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GoogleDistanceTable frame = new GoogleDistanceTable();
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
	public GoogleDistanceTable() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 448, 123);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblTransaction = new JLabel("Targets");
		lblTransaction.setBounds(12, 13, 81, 16);
		contentPane.add(lblTransaction);

		txtTargets = new JTextField();
		txtTargets.setText("Kardashian, Galaxy, industry,obsession");
		txtTargets.setBounds(105, 10, 315, 22);
		contentPane.add(txtTargets);
		txtTargets.setColumns(10);

		JButton btnGenerateExcelFile = new JButton("Generate Excel File");
		btnGenerateExcelFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//client.SetProxyList("proxy-list.txt");
				GoogleSimilarityDistance g = new GoogleSimilarityDistance();
				g.client = client;
				WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
				Vector<String> targets = new Vector<String>();
				Vector<String> items = new Vector<String>();

				for (String i : txtTargets.getText().split(",")) {
					if (i.trim().isEmpty() == false) {
						targets.add(i.trim());
					}
				}
				for (String i : txtItems.getText().split(",")) {
					if (i.trim().isEmpty() == false) {
						items.add(i.trim());
					}
				}

				Float[][] table = new Float[items.size()][targets.size()];

				for (int i = 0; i < targets.size(); i++) {
					for (int j = 0; j < items.size(); j++) {
						
						Float dis = (float)g.Similarity(targets.get(i), items.get(j));
						//Float dis = wn.GetDistance(items.get(i), items.get(j));
						
						System.out.println("Distance " + items.get(j) + ", "
								+ targets.get(i) + ": " + dis);
						table[j][i] = dis;

					}
				}

				String content = "";
				content += "";
				for (int i = 0; i < targets.size(); i++) {
					content += "," + targets.get(i);
				}
				content += "\n";

				for (int i = 0; i < items.size(); i++) {
					content += items.get(i);
					for (int j = 0; j < targets.size(); j++) {
						content += "," + table[i][j];
					}
					content += "\n";
				}
				Utilities.WriteFile("output.csv", content, false);
			}
		});
		btnGenerateExcelFile.setBounds(12, 70, 209, 25);
		contentPane.add(btnGenerateExcelFile);
		
		txtItems = new JTextField();
		txtItems.setText("icons,style,exemplified,ego,world,preoccupied,        Mouret,became,dress,tailored,sheath,spring,           fashion,table,topic,criticized,seeming,promote,                turned,interest,blankets,came,surprise,did");
		txtItems.setColumns(10);
		txtItems.setBounds(105, 36, 315, 22);
		contentPane.add(txtItems);
		
		JLabel lblItems = new JLabel("Items");
		lblItems.setBounds(12, 39, 81, 16);
		contentPane.add(lblItems);
		

		client.CheckGoogleBlock("analysts");
		client.CheckGoogleBlock("jobs AND analysts");
	}
}
