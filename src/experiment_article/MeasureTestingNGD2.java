package experiment_article;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import algorithms.GoogleSimilarityDistance;

import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

public class MeasureTestingNGD2 implements IWebClientX {

	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	GoogleSimilarityDistance g = null;
	WebClientX client = null;
	
	public static void main(String[] args) {
		(new MeasureTestingNGD2()).run();
	}
	
	public void run()
	{
		client = new WebClientX();
		g = new GoogleSimilarityDistance();
		client.callback = this;
		g.client = client;
		while (g.client.CheckGoogleBlock("jobs AND analysts") == true)
			Measure();
	}
	
	public boolean Measure()
	{
		boolean result = true;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/articles?"
			              + "user=root&password=root");
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM testing_ngd2 WHERE ngd_result = 0");
			while(resultSet.next())
			{
				String term1 = resultSet.getString("term1");
				String term2 = resultSet.getString("term2");
				
				double res = g.Similarity(term1, term2);
				System.out.println(term1+" and "+term2+": "+ res);
				
				if (res != -1)
				{
					res = Utilities.round(res, 2, BigDecimal.ROUND_HALF_UP);
				}
				else 
				{
					statement.close();
					result = false;
					break;
				}

				if (result == true)
				{
					preparedStatement = connect.prepareStatement("UPDATE testing_ngd2 SET ngd_result=? WHERE test_id=?");
					preparedStatement.setInt(2, resultSet.getInt("test_id"));
					preparedStatement.setFloat(1, (float)res);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
				
				if (result == false)
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
		if (Measure() == false)
		{
			while (g.client.CheckGoogleBlock("analysts AND tree") == true)
				Measure();
		}
	}

}
