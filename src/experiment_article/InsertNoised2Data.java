package experiment_article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import utility.WordNetLib;
import AppParameters.AppConst;

public class InsertNoised2Data {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try
		{
			WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION);
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM processed_data WHERE noised2 = ''");
			while (resultSet.next())
			{
				Vector<String> items = wn.GetRelated3(resultSet.getString("original_item"), 1);
				Vector<String> newItems = new Vector<String>();
				for (String i : items)
				{
					if (i.equals(resultSet.getString("original_item")))
						continue;
					newItems.add(i);
				}
				String noised = "";
				if (newItems.size() == 0)
					noised = wn.GenerateRandomNoun();
				else
					noised = newItems.get(0);
				if (noised.equals(""))
					continue;
				preparedStatement = connect.prepareStatement("UPDATE processed_data SET noised2=? WHERE article_id=?");
				preparedStatement.setInt(2, resultSet.getInt("article_id"));
				preparedStatement.setString(1, noised);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			resultSet.close();
			statement.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
