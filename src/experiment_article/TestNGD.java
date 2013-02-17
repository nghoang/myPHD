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

public class TestNGD implements IWebClientX {

	Connection connect = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	GoogleSimilarityDistance g = null;
	WebClientX client = null;
	
	public static void main(String[] args) {
		(new TestNGD()).run();
	}
	
	public void run()
	{
		client = new WebClientX();
		g = new GoogleSimilarityDistance();
		client.callback = this;
		g.client = client;
		if (g.client.CheckGoogleBlock("jobs AND analysts") == true &&
				g.client.CheckGoogleBlock("jobs AND analysts inurl:abb") == true)
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
			          .executeQuery("SELECT * FROM testing_ngd WHERE ngd_result=-1");
			while(resultSet.next())
			{
				double res = Math.abs(g.Similarity(resultSet.getString("term1"), 
						resultSet.getString("term2"),
						resultSet.getString("context")));
				System.out.println(resultSet.getString("term1")+" and "+resultSet.getString("term2")+" in "+resultSet.getString("context")+": "+ res);
				res = Utilities.round(res, 2, BigDecimal.ROUND_HALF_UP);
				if (res != 1)
				{
					Float resf = (float)res;
					preparedStatement = connect.prepareStatement("UPDATE testing_ngd SET ngd_result=? WHERE test_id=?");
					preparedStatement.setInt(2, resultSet.getInt("test_id"));
					preparedStatement.setFloat(1, resf);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
				else 
				{
					statement.close();
					result = false;
					break;
				}
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
			if (g.client.CheckGoogleBlock("analysts") == true &&
					g.client.CheckGoogleBlock("jobs AND analysts") == true)
				Measure();
		}
	}

}
