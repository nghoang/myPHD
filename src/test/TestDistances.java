package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import rita.wordnet.RiWordnet;
import algorithms.GoogleSimilarityDistance;
import AppParameters.AppConst;
import utility.WordNetLib;

public class TestDistances {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			run();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void run() throws ClassNotFoundException, SQLException
	{
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		GoogleSimilarityDistance ngd = new GoogleSimilarityDistance();
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager
		          .getConnection(AppConst.DB_CONNECTION_DISTANCE_TEST);
		statement = connect.createStatement();
		resultSet = statement
		          .executeQuery("SELECT * FROM cpbd_1 ORDER BY human");
		while (resultSet.next())
		{
			String t1 = resultSet.getString("term1");
			String t2 = resultSet.getString("term2");
			int counter = 0;
			String[] parents = null;
			
			try
			{
				parents = wn.riwn.getCommonParents(t1, t2, RiWordnet.NOUN);
			} 
			catch (Exception ex)
			{
				System.out.println(t1 + "," + t2);
				continue;
			}
			String content = "";
			if (parents != null)
			{
				for (String t : parents)
				{
					//counter++;
					System.out.print(t + " - ");
					
					//float dis = (float)ngd.Similarity(t1, t2, "", "","AND "+ t);
					//if (dis == -1)
					//	System.exit(0);
					//content += "," + dis;
				}
			}
			System.out.println(t1 + "," + t2);
		}
		
		connect.close();
		resultSet.close();
	}
}
