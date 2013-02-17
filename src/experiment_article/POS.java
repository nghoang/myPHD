package experiment_article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import algorithms.POSTagging;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;

public class POS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		POSTagging pos = new POSTagging();
		pos.modelFile = "data/left3words-wsj-0-18.tagger";
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/articles?"
			              + "user=root&password=root");
			statement = connect.createStatement();
			resultSet = statement
			          .executeQuery("SELECT * FROM articles LIMIT 200");
			while(resultSet.next())
			{
				String articletext = resultSet.getString("articletext");
				Integer id = resultSet.getInt("id");
				articletext = articletext.replaceAll("<[^>]*>", "");
				List<List<HasWord>> sentences = pos.TagSentenceCorpus(articletext);
				String transaction = "";
				Vector<String> all_items = new Vector<String>();
				for (List<HasWord> sentence : sentences) {
					ArrayList<TaggedWord> tSentence = pos.TaggingWords(sentence);
					for (int i = 0; i < tSentence.size(); i++) {
						TaggedWord word = tSentence.get(i);
						if (word.tag().equals("NN") && !word.tag().equals(",")
								&& !word.tag().equals(".")
								&& !word.tag().equals(";")) {
							if (word.value().toString().length() <= 1)
								continue;
							if (all_items.contains(word.value().toLowerCase()) == false)
								all_items.add(word.value().toLowerCase());
							if (transaction.equals(""))
								transaction += word.value();
							else
								transaction += ","+word.value();
						}
					}
				}
				String sample = "";
				java.util.Random ran = new Random();
				int current = ran.nextInt(all_items.size());
				//sample += all_items.get(current);
				int count = 0;
				for (int i=current-1;i>=0;i--)
				{
					if (sample.equals(""))
						sample += all_items.get(i);
					else
						sample += ","+all_items.get(i);
					count++;
					if (count >= 3)
						break;
				}
				count = 0;
				for (int i=current+1;i< all_items.size();i++)
				{
					sample += ","+all_items.get(i);
					count++;
					if (count >= 3)
						break;
				}
				preparedStatement = connect.prepareStatement("INSERT IGNORE processed_data SET article_id=?, transaction=?, sample_items=?,original_item=?");
				preparedStatement.setInt(1, id);
				preparedStatement.setString(2, transaction);
				preparedStatement.setString(3, sample);
				preparedStatement.setString(4, all_items.get(current));
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			statement.close();
			resultSet.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		} finally {
		      try {
				connect.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		/*POSTagging pos = new POSTagging();
		pos.ExtractTextToTransaction2("data/data2/", "data/left3words-wsj-0-18.tagger");
		for (Transaction t : pos.documents.getRecords())
		{
			System.out.println(t.toString());
		}*/
	}

}
