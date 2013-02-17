package experiment_article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class CreateTestingDataNGDTesting2 {

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
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/articles?"
			              + "user=root&password=root");
			int test_case = 500;
			Random ran = new Random();
			
			for (int c=0;c<test_case;c++)
			{
				statement = connect.createStatement();
				resultSet = statement
				          .executeQuery("SELECT * FROM processed_data ORDER BY RAND() LIMIT 1");
				resultSet.next();
				String[] items = resultSet.getString("transaction").split(",");
				int index = ran.nextInt(items.length - 2);
				String term1 = items[index].toLowerCase().trim();
				String term2 = items[index + 1].toLowerCase().trim();
				while (term1.equals("") || term2.equals(""))
				{
					 index = ran.nextInt(items.length - 2);
					 term1 = items[index].toLowerCase().trim();
					 term2 = items[index + 1].toLowerCase().trim();
				}
				preparedStatement = connect.prepareStatement("INSERT IGNORE testing_ngd2 SET term1=?, term2=?");
				preparedStatement.setString(1, term1);
				preparedStatement.setString(2, term2);
				preparedStatement.executeUpdate();
				preparedStatement.close();
				
				resultSet.close();
				statement.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
