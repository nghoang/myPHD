package experiments.wordnet_vs_google;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

import experiment_article.MeasureSample;

public class MeasureGoogleContentByGeneratedPairs implements IWebClientX {

	WebClientX client = null;
	GoogleSimilarityDistance g = null;
	
	//database
	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new MeasureGoogleContentByGeneratedPairs()).run();
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
		int result = 1;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION);
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM word_pairs WHERE distance_close_g_c=0 AND distance_far_g_c=0");
			result = 2;
			while(resultSet.next())
			{
				result = 1;
				String term = resultSet.getString("term");
				String term_close = resultSet.getString("term_close");
				String term_far = resultSet.getString("term_far");
				
				System.out.println("Measuring: " + term + ", " + term_close +", "+ term_far);
				
				Float distance_close = (float)g.Similarity(term, term_close);
				if (Float.isInfinite(distance_close))
					distance_close = 10F;
				Float distance_far = 0F;
				if (distance_close == -1)
				{
					statement.close();
					result = 0;
					break;
				}
				else
				{
					distance_far = (float)g.Similarity(term, term_far);
					if (Float.isInfinite(distance_far))
						distance_far = 10F;
					if (distance_far == -1)
					{
						statement.close();
						result = 0;
						break;
					}
				}
				
				if (result == 1)
				{
					preparedStatement = connect.prepareStatement("UPDATE word_pairs SET distance_close_g_c=? , distance_far_g_c=? WHERE `id`=?");
					preparedStatement.setFloat(1, distance_close);
					preparedStatement.setFloat(2, distance_far);
					preparedStatement.setInt(3, resultSet.getInt("id"));
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
