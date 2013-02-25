package experiments.wordnet_vs_google;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

public class ExpWordPair3Google implements IWebClientX {

	WebClientX client = null;
	GoogleSimilarityDistance g = null;
	int counter = 1;
	
	//database
	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new ExpWordPair3Google()).run();
	}
	
	public void run()
	{
		client = new WebClientX();
		g = new GoogleSimilarityDistance();
		client.callback = this;
		g.client = client;
		while (g.client.CheckGoogleBlock("jobs AND analysts") == true)
			if (Measure() == 2)
			{
				break;
			}
	}
	
	public int Measure()
	{
		String table = "_3_10";
		
		int result = 1;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION_DISTANCE_TEST);
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM word_pairs"+table+" WHERE google_distance=0");
			result = 2;
			while(resultSet.next())
			{
				result = 1;
				String term = resultSet.getString("term");
				String term_close = resultSet.getString("related_term");
				String context = resultSet.getString("parent");
				
				System.out.print("Measuring: " + term + ", " + term_close);

				Float distance = (float)g.Similarity(term, term_close);
				//Float distance = (float)g.Similarity(term, term_close,context);
				System.out.print(": " + distance);
				if (Float.isInfinite(distance))
					distance = 10F;
				if (distance == -1)
				{
					statement.close();
					result = 0;
					System.exit(0);
					break;
				}
				System.out.println();
				
				if (result == 1)
				{
					System.out.print(counter + ">");
					counter++;
					preparedStatement = connect.prepareStatement("UPDATE word_pairs"+table+" SET google_distance=? WHERE `pair_id`=?");
					preparedStatement.setFloat(1, distance);
					preparedStatement.setInt(2, resultSet.getInt("pair_id"));
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
				
				if (result == 0)
					break;
			}
			statement.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (connect != null)
			{
				try {
					connect.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (preparedStatement != null)
			{
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	@Override
	public void ProxyFailed(String px) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void ProxySuccess(String px) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void DropConnection(String url) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void FinishedCaptcha() {
		if (Measure() == 0)
		{
			while (g.client.CheckGoogleBlock("analysts AND tree") == true)
				if (Measure() == 2)
					break;
		}
	}
}
