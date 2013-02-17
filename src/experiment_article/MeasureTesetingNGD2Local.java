package experiment_article;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.ngochoang.CrawlerLib.Utilities;

public class MeasureTesetingNGD2Local {

	static IndexWriter indexWriter;
	static Analyzer analyzer;
	static IndexReader reader;
	static IndexSearcher searcher;
	static Directory indexer;

	public static void main(String[] args) {

		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			analyzer = new StandardAnalyzer(Version.LUCENE_35);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35,
					analyzer);
			indexer = new RAMDirectory();
			indexWriter = new IndexWriter(indexer, config);
			Document doc;

			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/articles?"
							+ "user=root&password=root");
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM articles");
			int i = 1;
			while (resultSet.next()) {
				doc = new Document();
				doc.add(new Field("id", resultSet.getString("id"),
						Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("content", resultSet.getString("articletext"), Field.Store.NO,
						Field.Index.ANALYZED));
				indexWriter.addDocument(doc);
				System.out.println("indexing: " + (double) i * 100/ (double) 22140);
				i++;
			}

			indexWriter.commit();

			reader = IndexReader.open(indexer);
			searcher = new IndexSearcher(reader);

			// search
			resultSet = statement
					.executeQuery("SELECT * FROM testing_ngd2");
			while (resultSet.next()) {
				String term1 = resultSet.getString("term1");
				String term2 = resultSet.getString("term2");

				double res = Similarity(term1, term2);
				if (Double.isInfinite(res) || Double.isNaN(res))
					res = 1;
				else
					res = Utilities.round(res, 2, BigDecimal.ROUND_HALF_UP);
				preparedStatement = connect
						.prepareStatement("UPDATE testing_ngd2 SET local_result=? WHERE test_id=?");
				preparedStatement.setInt(2, resultSet.getInt("test_id"));
				preparedStatement.setFloat(1, (float) res);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			statement.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static double Similarity(String term1, String term2) {
		int countw1 = SearchTerm(term1);
		int countw2 = SearchTerm(term2);
		int countw12 = SearchTerm(term1 + " AND " + term2);
		
		double logw1 = Math.log10(countw1);
		double logw2 = Math.log10(countw2);
		double logw12 = Math.log10(countw12);

		double result = (Math.max(logw1, logw2) - logw12)
				/ (Math.log10(50000) - Math.min(logw1, logw2));
		return result;
	}

	private static int SearchTerm(String term) {
		try {
			Query q = new QueryParser(Version.LUCENE_35, "content", analyzer)
					.parse(term);
			TopScoreDocCollector collector = TopScoreDocCollector.create(10000,
					true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			return hits.length;
		} catch (Exception ex) {
			return 0;
		}
	}
}
