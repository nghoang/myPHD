package experiments.wordnet_vs_google;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import rita.wordnet.RiWordnet;
import utility.WordNetLib;
import AppParameters.AppConst;

public class ExpWordPair3 {
	Connection connect = null;
	PreparedStatement preparedStatement = null;
	WordNetLib wn;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExpWordPair3 program = new ExpWordPair3();
		program.Init();
		program.Generate(7, 30, 2,"6");
		program.Generate(7, 30, 3,"6");
		program.Generate(7, 30, 4,"6");
		program.Generate(7, 30, 5,"6");
		program.Generate(7, 30, 6,"6");
		program.Generate(7, 30, 7,"6");
		program.Close();
	}
	
	public void Close()
	{
		try {
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void Init()
	{
		wn = new WordNetLib(AppConst.WORDNET_DICT_MAC);
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection(AppConst.DB_CONNECTION_DISTANCE_TEST);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String[] RandomizeArray(String[] arr)
	{
		String[] res = new String[arr.length];
		Vector<String> temp = new Vector<String>();
		for(int i=0;i<arr.length;i++)
			temp.add(arr[i]);
		Collections.shuffle(temp);
		for (int i=0;i<temp.size();i++)
			res[i] = temp.get(i);
		return res;
	}
	
	public String GetOneRandomly(String[] arr)
	{
		if (arr == null || arr.length == 0)
			return "";
		Random ran = new Random();
		return arr[ran.nextInt(arr.length)];
	}
	
	public void Generate(int minheight, int nmr, int level, String table)
	{
		System.out.println("Lvl:" + level);
		int generated_term = 0;
		while (generated_term < nmr)
		{
			String term = wn.GenerateRandomNoun();
			Vector<String> up_path = new Vector<String>();
			Vector<String> down_path = new Vector<String>();
			String curr = term;
			up_path.add(curr);
			boolean isFailed = false;
			for (int l=0;l<level;l++)
			{
				curr = GetOneRandomly(wn.GetHypernyms(curr));
				if (curr.equals(""))
				{
					isFailed = true;
					break;
				}
				up_path.add(curr);
			}
			if (isFailed)
				continue;

			String parent = curr;
			String last_node = parent;
			int temp_height = 0;
			while (true)
			{
				String[] temp = wn.GetHypernyms(last_node);
				if (temp == null || temp.length == 0)
					break;
				last_node = temp[0];
				temp_height++;
				if (temp_height > 100)
					break;
			}
			//it may go into a loop
			if (temp_height > 100)
				continue;
			int term_height = temp_height + level;
			if (term_height < minheight )
				continue;
			down_path.add(curr);
			for (int l=0;l<level;l++)
			{
				curr = GetOneRandomly(wn.riwn.getHyponyms(curr, RiWordnet.NOUN));
				if (up_path.contains(curr) || curr.equals(""))
				{
					isFailed = true;
					break;
				}
				down_path.add(curr);
			}
			if (isFailed)
				continue;
			
			float distance = wn.GetDistance(term, curr);
			
			InsertTermIntoDB(term, curr, parent, term_height, level, up_path.toString(), down_path.toString(), distance, table);

			System.out.print(".");
			generated_term++;
		}
	}

	public void InsertTermIntoDB(String term, String related_term, 
			String parent, int height, int level, String up_path, String down_path, float distance, String table)
	{
		try {
			preparedStatement = connect.prepareStatement("INSERT INTO word_pairs"+table+" SET " +
					"term=?, " +
					"related_term=?, " +
					"parent=?, " +
					"height=?, " +
					"distance_level=?, " +
					"up_path=?, " +
					"down_path=?, " +
					"wordnet_distance=?");
			preparedStatement.setString(1, term);
			preparedStatement.setString(2, related_term);
			preparedStatement.setString(3, parent);
			preparedStatement.setInt(4, height);
			preparedStatement.setInt(5, level);
			preparedStatement.setString(6, up_path);
			preparedStatement.setString(7, down_path);
			preparedStatement.setFloat(8, distance);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
