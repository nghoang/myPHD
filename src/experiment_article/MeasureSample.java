package experiment_article;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utility.WordNetLib;

import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

public class MeasureSample implements IWebClientX {

	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	GoogleSimilarityDistance g = null;
	WebClientX client = null;
	
	public static void main(String[] args) {
		(new MeasureSample()).run();
		(new MeasureSample()).GenerateRandomNoise();
	}
	
	public void GenerateRandomNoise()
	{
		client = new WebClientX();
		WordNetLib wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/articles?"
			              + "user=root&password=root");
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM processed_data WHERE distances <> '' AND noised=''");
			while (resultSet.next())
			{
				String noised = wn.GenerateRandomNoun();
				preparedStatement = connect.prepareStatement("UPDATE processed_data SET noised=? WHERE article_id=?");
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
			          .getConnection("jdbc:mysql://localhost/articles?"
			              + "user=root&password=root");
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM processed_data WHERE distances=''");
			result = 2;
			while(resultSet.next())
			{
				result = 1;
				String term1 = resultSet.getString("original_item");
				String[] termList = resultSet.getString("sample_items").split(",");
				String distances = "";
				
				for (String term2 : termList)
				{
					double res = g.Similarity(term1, term2);
					System.out.println(term1+" and "+term2+": "+ res);
					
					if (res != -1)
					{
						res = Utilities.round(res, 2, BigDecimal.ROUND_HALF_UP);
						if (distances.equals(""))
							distances += res;
						else
							distances += ","+res;
					}
					else 
					{
						statement.close();
						result = 0;
						break;
					}
				}

				if (!distances.equals("") && result == 1)
				{
					preparedStatement = connect.prepareStatement("UPDATE processed_data SET distances=? WHERE article_id=?");
					preparedStatement.setInt(2, resultSet.getInt("article_id"));
					preparedStatement.setString(1, distances);
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
