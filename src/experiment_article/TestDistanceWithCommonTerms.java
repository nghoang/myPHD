package experiment_article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

public class TestDistanceWithCommonTerms implements IWebClientX{

	static GoogleSimilarityDistance g;
	static WebClientX client = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new TestDistanceWithCommonTerms()).run();
	}
	
	public void run()
	{
		client = new WebClientX();
		g = new GoogleSimilarityDistance();
		g.client = client;
		client.callback = this;
		if (g.client.CheckGoogleBlock("allintitle: jobs AND analysts"))
			Measure();
	}

	public void Measure()
	{
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION_DISTANCE_TEST);
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM dataset3");
			while (resultSet.next())
			{
				String term1 = resultSet.getString("term1");
				String term2 = resultSet.getString("term2");
				String common = resultSet.getString("common");
				float distance = (float)g.SimilarityFlex(term1 + " intitle:" + common, 
						term1 + " intitle: " + common, 
						term1 + " " + term2 + " intitle:" + common);
				System.out.println(term1 + "," + term2 + "," + common + "," + distance);
				if (distance == -1)
					System.exit(0);
				
				preparedStatement = connect.prepareStatement("UPDATE dataset3 SET google=? WHERE term1=? AND term2=? AND common=?");
				preparedStatement.setFloat(1, distance);
				preparedStatement.setString(2, term1);
				preparedStatement.setString(3, term2);
				preparedStatement.setString(4, common);
				preparedStatement.executeUpdate();
				preparedStatement.close();
				
			}
			statement.close();
			connect.close();
			resultSet.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
		Measure();
	}
}
