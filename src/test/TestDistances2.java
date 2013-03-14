package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import rita.wordnet.RiWordnet;
import algorithms.GoogleSimilarityDistance;
import AppParameters.AppConst;
import utility.WordNetLib;

public class TestDistances2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			run();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void run() throws ClassNotFoundException, SQLException
	{
		GoogleSimilarityDistance ngd = new GoogleSimilarityDistance();
		System.out.println(ngd.SimilarityFlex("allintitle:cord vtech","smile vtech","core"));
	}
}
