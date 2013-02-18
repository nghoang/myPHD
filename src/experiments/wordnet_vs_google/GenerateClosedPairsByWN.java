package experiments.wordnet_vs_google;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import utility.WordNetLib;

import AppParameters.AppConst;
public class GenerateClosedPairsByWN {

	
	public static void main(String[] args) {
		Connection connect = null;
		PreparedStatement preparedStatement = null;
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION);
			for (int i=0;i<1000;i++)
			{
				String term = "";
				String term_close = "";
				String term_far1 = "";
				String term_far2 = "";
				String term_far3 = "";
				
				while (term_far.equals("") || term.equals("") || term_close.equals(""))
				{
					term = wn.GenerateRandomNoun();
					term_close = wn.GetASibling(term);
					term_far1 = wn.GetAFar1Level(term);
					term_far2 = wn.GetAFar2Level(term);
					term_far3 = wn.GetAFar3Level(term);
				}
				Float distance_close = wn.GetDistance(term, term_close);
				Float distance_far = wn.GetDistance(term, term_far);
				
				preparedStatement = connect.prepareStatement("INSERT IGNORE INTO word_pairs SET term=?, term_close=?, distance_close=?, distance_far = ?, term_far=?");
				preparedStatement.setString(1, term);
				preparedStatement.setString(2, term_close);
				preparedStatement.setFloat(3, distance_close);
				preparedStatement.setFloat(4, distance_far);
				preparedStatement.setString(5, term_far);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
