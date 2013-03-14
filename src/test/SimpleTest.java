package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.crawlerinterface.IWebClientX;

import AppParameters.AppConst;
import algorithms.GoogleSimilarityDistance;

public class SimpleTest implements IWebClientX{

	static GoogleSimilarityDistance g;
	static WebClientX client = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new SimpleTest()).run();
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
		System.out.println(g.SimilarityFlex("cord prolapse", 
				"smile prolapse", "cord smile prolapse"));
		System.out.println(g.SimilarityFlex("cord bank", 
				"smile bank", "cord smile bank"));
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
