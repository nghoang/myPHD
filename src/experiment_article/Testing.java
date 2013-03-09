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

public class Testing implements IWebClientX{

	static GoogleSimilarityDistance g;
	static WebClientX client = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new Testing()).run();
	}
	
	public void run()
	{
		client = new WebClientX();
		g = new GoogleSimilarityDistance();
		g.client = client;
		client.callback = this;
		if (g.client.CheckGoogleBlock("jobs AND analysts"))
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
			resultSet = statement.executeQuery("SELECT * FROM cpbd_2 WHERE google=0 OR google=-1 ORDER BY human");
			while (resultSet.next())
			{
				String term1 = resultSet.getString("term1");
				String term2 = resultSet.getString("term2");
				float distance = (float)g.Similarity(term1, term2);
				System.out.println(term1 + " " + term2 + " " + distance);
				if (distance == -1)
					System.exit(0);
				
				preparedStatement = connect.prepareStatement("UPDATE cpbd_2 SET google=? WHERE term1=? AND term2=?");
				preparedStatement.setFloat(1, distance);
				preparedStatement.setString(2, term1);
				preparedStatement.setString(3, term2);
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
