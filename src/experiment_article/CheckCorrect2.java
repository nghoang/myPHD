package experiment_article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import AppParameters.AppConst;

public class CheckCorrect2 {

	public static void main(String[] args) {
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION);
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM processed_data WHERE distances <> '' AND noised_distances2 <> ''");
			int total = 1;
			int failed = 0;
			int failed_noised = 0;
			int k=2;
			double md=0.7;
			while (resultSet.next())
			{
				total++;
				String ori_distances = resultSet.getString("distances");
				String noised_distances = resultSet.getString("noised_distances2");
				Double[] ds1 = new Double[ori_distances.split(",").length];
				Double[] ds2 = new Double[ori_distances.split(",").length];
				int i=0;
				for (String dd : ori_distances.split(","))
				{
					ds1[i] = Double.parseDouble(dd);
					i++;
				}
				i=0;
				for (String dd : noised_distances.split(","))
				{
					ds2[i] = Double.parseDouble(dd);
					i++;
				}
				
				int satis = 0;
				for (double h : ds1)
				{
					if (h <= md)
						satis++;
				}
				if (satis <  k)
					failed++;
				
				satis = 0;
				for (double h : ds2)
				{
					if (h <= md)
						satis++;
				}
				if (satis <  k)
					failed_noised++;
			}

			System.out.println("No related in original data: " + failed + "("+(failed*100/total)+")");
			System.out.println("No related in noised data2: " + failed_noised + "("+(failed_noised*100/total)+")");
			System.out.println("Total testing data:" + total);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
