/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataset.MeshLib;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import DataStructure.Node;
import DataStructure.Tree;
import utility.Utilities;

/**
 * 
 * @author c1038943
 */
public class MeSHUtility {

	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	String[] stopwords = null;

	public MeSHUtility() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager
					.getConnection("jdbc:mysql://localhost/mesh_db?user=root&password=");
			stmt = conn.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void ImportMeshData(String path) {
		MeSHParser mp = new MeSHParser(conn);
		mp.BeginParse(path);
	}

	// public String[] ClearStopWords(String[] input) {
	//
	// return input;
	// }
	// convert document to array of words
	public String[] ExtractWords(String input) {

		String curPath = new File(".").getAbsolutePath();
		curPath = curPath.substring(0, curPath.length() - 1);
		if (stopwords == null) {
				stopwords = Utilities.readFileAsString(
						curPath + "/stopwords.txt").split("\r\n");
		}
		for (String sw : stopwords) {
			input = input.replaceAll(
					"[\\.\\,\\;\\?\\)\\(\\[\\]\\{\\}\\:\\!\"\\s]+" + sw.trim()
							+ "[\\s\\.\\,\\;\\?\\)\\(\\[\\]\\{\\}\\:\\!\"]+",
					" ");
		}
		input = input
				.replaceAll(
						"[\\x2C\\x2F\\x2E\\x3F\\x3E\\x3C\\x23\\x27\\x3B\\x7E\\x40\\x3A\\x5D\\x5B\\x7D\\x7B\\x3D\\x2D\\x2B\\x5F\\x29\\x28\\x2A\\x26\\x5E\\x25\\x24\\xA3\\x21\\xAC\\x60\\x22\\x5C]",
						" ");
		input = input.replace("[\\n\\t\\r]", " ");
		input = input.toLowerCase();
		input = Utilities.RemoveSpace(input);
		String[] res = input.split(" ");
		return res;
	}

	// filter out words is not in MeSH database
	public String[] FilterString(String[] input, int min_char, int maxSize) {
		Vector<String> res = new Vector<String>();
		String word = "";
		for (int i = 0; i < input.length; i++) {
			word = "";
			boolean isInMeSH = false;
			for (int j = i; j < i + maxSize && j < input.length; j++) {
				String curWord = word;
				curWord += input[j] + " ";
				if (IsWordInMeSH(curWord.trim())) {
					isInMeSH = true;
					word = curWord;
					continue;
				} else {
					if (isInMeSH) {
						break;
					}
					isInMeSH = false;
					break;
				}
			}

			if (isInMeSH) {
				if (!res.contains(word.trim())) {
					if (word.trim().length() >= min_char) {
						res.add(word.trim());
					}
				}
			}
		}
		return (String[]) res.toArray(new String[res.size()]);
	}

	private boolean IsWordInMeSH(String input) {
		try {
			String query = "select count(*) as counter from terms where term_string = '"
					+ input + "'";
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				if (rs.getInt("counter") > 0) {
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return false;
	}

	public Tree GetChildrenNodes(String parent_node) {
		Tree<String> res = new Tree<String>();
		Node<String> root = new Node<String>("Root");
		try {
			String query = "select c.node_string from concept as b, record_node as c where b.record_id=c.record_id and b.concept_string='"
					+ parent_node + "'";
			// String query =
			// "select c.node_string from concept as b, record_node as c, terms as d where d.concept_id=b.concept_id and b.record_id=c.record_id and (b.concept_string='"
			// + parent_node + "' or d.term_string='" + parent_node + "')";
			rs = stmt.executeQuery(query);
			rs.last();
			if (rs.getRow() == 0) {
				query = "select c.node_string from concept as b, record_node as c, terms as d where d.concept_id=b.concept_id and b.record_id=c.record_id and (b.concept_string='"
						+ parent_node
						+ "' or d.term_string='"
						+ parent_node
						+ "')";
				rs = stmt.executeQuery(query);
			}
			rs.beforeFirst();
			while (rs.next()) {
				Node<String> branch = new Node<String>(parent_node);
				query = "select b.concept_string,b.record_id from record_node as a, concept as b where a.record_id=b.record_id and a.node_string like '"
						+ rs.getString("node_string")
						+ ".%' group by b.record_id";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(query);
				while (rs2.next()) {
					branch.addChild(new Node<String>(rs2
							.getString("concept_string")));
				}
				root.addChild(branch);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return res;
		}
		res.setRootElement(root);
		return res;
	}

	public Vector<String> GetParentNode(String child_node) {
		Vector<String> res = new Vector<String>();
		try {
			String query = "select c.node_string from concept as b, record_node as c where b.record_id=c.record_id and b.concept_string='"
					+ child_node + "'";
			// String query =
			// "select c.node_string from concept as b, record_node as c, terms as d where d.concept_id=b.concept_id and b.record_id=c.record_id and (b.concept_string='"
			// + child_node + "' or d.term_string='" + child_node + "')";
			rs = stmt.executeQuery(query);
			rs.last();
			if (rs.getRow() == 0) {
				query = "select c.node_string from concept as b, record_node as c, terms as d where d.concept_id=b.concept_id and b.record_id=c.record_id and (b.concept_string='"
						+ child_node
						+ "' or d.term_string='"
						+ child_node
						+ "')";
				rs = stmt.executeQuery(query);
			}
			rs.beforeFirst();
			while (rs.next()) {
				String tree = rs.getString("node_string");
				tree = tree.replaceAll("\\.[0-9a-zA-Z]+$", "");
				query = "select b.concept_string,b.record_id from record_node as a, concept as b where a.record_id=b.record_id and a.node_string = '"
						+ tree + "' group by b.record_id";
				// query =
				// "select b.record_string from record_node as a, record as b where a.record_id=b.id and a.node_string = '"
				// + tree + "'";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(query);
				if (rs2.next()) {
					if (!res.contains(rs2.getString("concept_string"))) {
						res.add(rs2.getString("concept_string"));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return res;
		}
		return res;
	}

	public Double GetMinDistance(String w1, String w2) {
		Vector<Double> d = GetDistances(w1, w2);
		if (d.size() == 0)
			return -1D;
		Double min = 9999999D;
		for (Double i : d) {
			if (min > i) {
				min = i;
			}
		}
		return min;
	}

	public Double GetMaxDistance(String w1, String w2) {
		Vector<Double> d = GetDistances(w1, w2);
		if (d.size() == 0)
			return -1D;
		Double max = -1D;
		for (Double i : d) {
			if (max < i) {
				max = i;
			}
		}
		return max;
	}

	// Vector<String> igwords = new Vector<String>();
	// String ignore_folder = "";
	//
	// public void LoadIgnoreWordDistance(String folder) {
	// ignore_folder = folder;
	// String igfile = folder + "../igwords.txt";
	// try {
	// String content = Utilities.readFileAsString(igfile);
	// String[] glines = content.split("\n");
	// for (String line : glines) {
	// if (line.trim().equals(""))
	// continue;
	// igwords.add(line);
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public Vector<Double> GetDistances(String w1, String w2) {

		Vector<Double> res = new Vector<Double>();

		// if (igwords.contains(w1) ||
		// igwords.contains(w2))
		// {
		// return res;
		// }

		Vector<String> nodes1 = new Vector<String>();
		Vector<String> nodes2 = new Vector<String>();

		try {
			String query = "SELECT * FROM node_cache WHERE term like '%" + w1
					+ "%'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				nodes1.add(rs.getString("node"));
			}

			if (nodes1.size() == 0) {
				query = "SELECT rn.node_string FROM `record_node` as rn, "
						+ "concept as c, record as r, terms as t WHERE rn.record_id=r.id "
						+ "AND c.record_id=r.id AND t.concept_id=c.concept_id AND "
						+ "(c.concept_string like '%" + w1
						+ "%' OR t.term_string like '%" + w1 + "%')";
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					nodes1.add(rs.getString("node_string"));
					Statement stmt2 = conn.createStatement();
					stmt2.execute("INSERT IGNORE INTO node_cache SET node='"
							+ rs.getString("node_string") + "', term='" + w1
							+ "'");
					stmt2.close();
				}
			}
			if (nodes1.size() == 0) {
				// if (!igwords.contains(w1)) {
				// igwords.add(w1);
				// WriteIgnores();
				// }
				return res;
			}
			query = "SELECT * FROM node_cache WHERE term like '%" + w2 + "%'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				nodes2.add(rs.getString("node"));
			}

			if (nodes1.size() > 0) {
				query = "SELECT rn.node_string FROM `record_node` as rn, "
						+ "concept as c, record as r, terms as t WHERE rn.record_id=r.id "
						+ "AND c.record_id=r.id AND t.concept_id=c.concept_id AND "
						+ "(c.concept_string like '%" + w2
						+ "%' OR t.term_string like '%" + w2 + "%')";
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					nodes2.add(rs.getString("node_string"));
					Statement stmt2 = conn.createStatement();
					stmt2.execute("INSERT IGNORE INTO node_cache SET node='"
							+ rs.getString("node_string") + "', term='" + w2
							+ "'");
					stmt2.close();
				}
				if (nodes2.size() == 0) {
					// if (!igwords.contains(w2)) {
					// igwords.add(w2);
					// WriteIgnores();
					// }
					return res;
				}
			}

			for (String node1 : nodes1) {
				for (String node2 : nodes2) {
					double diff = GetDisNode(node1, node2);
					res.add(diff);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	public Double GetDisNode(String n1, String n2) {
		try {
			double diff = 0;
			String[] np1 = n1.split("\\.");
			String[] np2 = n2.split("\\.");
			for (int i = 0; i < Math.min(np1.length, np2.length); i++) {
				int np1n = Integer.parseInt(Utilities.SimpleRegexSingle(
						"(\\d+)", np1[i], 1));
				int np2n = Integer.parseInt(Utilities.SimpleRegexSingle(
						"(\\d+)", np2[i], 1));
				diff += (double) Math.abs(np1n - np2n)
						/ (double) (Math.pow(10, i + 1));
			}
			return diff;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 999D;
	}

	// private void WriteIgnores() {
	// String content = "";
	// for (String i : igwords) {
	// content += i + "\n";
	// }
	// Utilities.WriteFile(ignore_folder + "../igwords.txt", content, false);
	// }

	public Vector<String> GetsliblingList(String child_node) {
		child_node = child_node.replace("'", "''");
		Vector<String> data = new Vector<String>();
		try {
			rs = stmt
					.executeQuery("select * from replacable where target_concept_id='"
							+ child_node + "'");
			while (rs.next()) {
				data.add(rs.getString("replace_concept_id"));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		if (data.size() > 0) {
			return data;
		}

		Vector<Tree> ress = GetSliblingNodes(child_node);
		for (Tree res : ress) {
			Node<String> root = res.getRootElement();
			for (Node<String> r : root.getChildren()) {
				if (r.getChildren().size() > 0) {
					for (Node<String> sr : r.getChildren()) {
						if (!data.contains(sr.data)) {
							data.add(sr.data);

						}
					}
				}
			}
		}
		for (String d : data) {
			String query = "insert ignore into replacable (target_concept_id,replace_concept_id) values('"
					+ child_node + "','" + d.replace("'", "''") + "')";
			try {
				stmt.execute(query);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return data;
	}

	public Vector<Tree> GetSliblingNodes(String child_node) {
		Vector<String> parNodes = GetParentNode(child_node);
		Vector<Tree> tree = new Vector<Tree>();
		for (String p : parNodes) {
			Tree t = GetChildrenNodes(p);
			if (t.getRootElement().getChildren().size() > 0) {
				tree.add(t);
			}
		}
		return tree;
	}

	public Vector<String> GetNodes(String t) {
		t = t.replaceAll("[^0-9a-zA-Z\\s\\.]", "").trim();
		Vector<String> res = new Vector<String>();
		try {
			String query = "SELECT DISTINCT rn.node_string FROM `record_node` as rn, "
					+ "concept as c, record as r, terms as t WHERE rn.record_id=r.id "
					+ "AND c.record_id=r.id AND t.concept_id=c.concept_id AND "
					+ "(t.term_string like '"
					+ t
					+ "' OR t.term_string like '%, "
					+ t
					+ "' OR t.term_string like '%, "
					+ t
					+ ",%' OR t.term_string like '" + t + ", %') LIMIT 10";

			rs = stmt.executeQuery(query);

			while (rs.next()) {
				res.add(rs.getString("node_string") + "#" + t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public Vector<String> GetNodes2(String t) {
		t = t.replaceAll("[^0-9a-zA-Z\\s\\.]", "").trim();
		Vector<String> res = new Vector<String>();
		try {
			String query = "SELECT term_id FROM terms as t WHERE (t.term_string like '"
					+ t
					+ "' OR t.term_string like '%, "
					+ t
					+ "' OR t.term_string like '%, "
					+ t
					+ ",%' OR t.term_string like '" + t + ", %') LIMIT 10";

			rs = stmt.executeQuery(query);
			String termids = "";
			boolean isF = true;
			while (rs.next()) {
				if (isF == true) {
					isF = false;
					termids += "t.term_id= '" + rs.getString("term_id") + "'";
				} else {
					termids += " OR t.term_id= '" + rs.getString("term_id") + "'";
				}
			}
			if (!termids.equals("")) {
				query = "SELECT DISTINCT rn.node_string FROM `record_node` as rn, "
						+ "concept as c, record as r, terms as t WHERE rn.record_id=r.id "
						+ "AND c.record_id=r.id AND t.concept_id=c.concept_id AND "
						+ "(" + termids + ") LIMIT 10";
				rs = stmt.executeQuery(query);

				while (rs.next()) {
					res.add(rs.getString("node_string") + "#" + t);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
