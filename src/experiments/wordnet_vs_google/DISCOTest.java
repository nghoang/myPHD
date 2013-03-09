package experiments.wordnet_vs_google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import AppParameters.AppConst;

public class DISCOTest {

	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;

	public static void main(String[] args) {
		(new DISCOTest()).run();
	}

	public void run() {
		try {
			String dataset = "_1_10";
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
					.getConnection(AppConst.DB_CONNECTION_DISTANCE_TEST);
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM word_pairs"+dataset);
			// Runtime rt = Runtime.getRuntime();
			while (resultSet.next()) {
				String term = resultSet.getString("term");
				String related_term = resultSet.getString("related_term");
				String google_distance = resultSet.getString("google_distance");
				String wordnet_distance = resultSet.getString("wordnet_distance");
				String command = "java -jar /Users/hoangong/github/myPHD/disco.jar /Users/hoangong/Desktop/disco/eng -s \""
						+ term
						+ "\" \""
						+ related_term
						+ "\"";
				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				String line = "";
				while ((line = in.readLine()) != null) {
					if (line.indexOf("Error") == -1)
					{
						System.out.println("Similarity " + term + " " + related_term + "("+wordnet_distance+", "+google_distance+"): " + line);
						preparedStatement = connect.prepareStatement("REPLACE INTO disco_test"+dataset+" SET term=?, related_term=?, path_count=?, disco_distance = ?");
						preparedStatement.setString(1, term);
						preparedStatement.setString(2, related_term);
						preparedStatement.setInt(3, resultSet.getInt("distance_level"));
						preparedStatement.setFloat(4, Float.parseFloat(line));
						preparedStatement.executeUpdate();
						preparedStatement.close();
					}
				}
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
