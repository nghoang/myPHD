package programs;

import java.io.IOException;
import java.util.Random;
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
import com.ngochoang.CrawlerLib.WebClientX;

public class TestingLucene {

	String DIRECTORY = "C:\\data\\documents\\txt_600\\anonymized";// "C:\\projects\\ngochoangprojects\\Java\\phdproject\\test_data";
	String PRIVACY_CONSTRAINT = "C:\\data\\documents\\txt_600\\results\\privacy_constraint.txt";
	private IndexWriter indexWriter;
	private Analyzer analyzer;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Directory indexer;
	// final int test_time = 30;
	int failed_count = 0;

	public static void main(String[] args) throws Exception {
		TestingLucene test = new TestingLucene();
		test.CreateIndex();
		test.TestPrivacyLevel();
		// test.SearchTerm("\"Comparison of Internet Relay Chat daemons\"");
	}

	private void TestPrivacyLevel() throws ParseException, IOException {
		// Integer DataSet = 200;
		// Integer k = 3;
		// Integer knowledge = 10;
		// Double failed = 0D;

		Utilities.WriteFile("result.csv",
				"DataSet,k,knowledge,Failed,Test Time" + "\n", false);
		for (Integer test_time = 20; test_time <= 60; test_time += 20) {
			for (Integer DataSet = 200; DataSet <= 600; DataSet += 200) {
				for (Integer k = 3; k <= 3; k += 3) {
					for (Integer knowledge = 10; knowledge <= 30; knowledge += 10) {
						failed_count = 0;
						DIRECTORY = "C:\\data\\documents\\txt_" + DataSet
								+ "\\anonymized";
						PRIVACY_CONSTRAINT = "C:\\data\\documents\\txt_"
								+ DataSet + "\\results\\privacy_constraint.txt";

						String content = Utilities
								.readFileAsString(PRIVACY_CONSTRAINT);
						Random ran = new Random();
						String[] lines = content.split("\n");
						for (int i = 0; i < test_time; i++) {
							String line = lines[ran.nextInt(lines.length - 1)];
							if (line.split(",").length >= 2) {
								String w1 = line.split(",")[ran.nextInt(line
										.split(",").length)];
								String w2 = w1;
								while (w2.equals(w1)) {
									w2 = line.split(",")[ran.nextInt(line
											.split(",").length)];
								}
								Search(w1.trim() + " AND " + w2.trim(),
										knowledge, k);
							} else {
								i--;
							}
						}
						System.out.println("Failed Rate: " + failed_count * 100
								/ test_time);
						Utilities.WriteFile("result.csv", DataSet + "," + k
								+ "," + knowledge + ","
								+ (failed_count * 100 / test_time) + ","
								+ test_time + "\n", true);
					}
				}
			}
		}
	}

	private boolean SearchTerm(String term, Integer k) throws ParseException,
			IOException {
		Query q = new QueryParser(Version.LUCENE_35, "content", analyzer)
				.parse(term);
		TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// System.out.print("Found " + hits.length + " hits for " + term);
		// for (int i = 0; i < hits.length; ++i) {
		// int docId = hits[i].doc;
		// Document d = searcher.doc(docId);
		// System.out.print((i + 1) + ". " + d.get("title") + "; ");
		// }
		// System.out.println();

		if (hits.length < k && hits.length != 0) {
			System.out.print("=>  \"" + term + "\": ");
			System.out.println("Failed");
			failed_count++;
			return false;
		}
		return true;
	}

	private void Search(String query, Integer Knowledge, Integer k)
			throws ParseException, IOException {

		System.out.print("Search \"" + query + "\" ");
		Vector<String> relatedTerms = GetRelatedFromWikipedia(query, Knowledge);

		for (String subq : relatedTerms) {
			if (SearchTerm("\"" + subq + "\"", k) == false)
				return;
			// System.out.print("Found " + hits.length + " hits.");
			// for (int i = 0; i < hits.length; ++i) {
			// int docId = hits[i].doc;
			// Document d = searcher.doc(docId);
			// System.out.print((i + 1) + ". " + d.get("title") + "; ");
			// }
			// System.out.println();
		}
		System.out.println("Protected");
	}

	public void CreateIndex() throws CorruptIndexException,
			LockObtainFailedException, IOException {

		analyzer = new StandardAnalyzer(Version.LUCENE_35);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35,
				analyzer);
		indexer = new RAMDirectory();
		indexWriter = new IndexWriter(indexer, config);
		Vector<String> documents = Utilities.GetFileInFolder(DIRECTORY);
		Document doc;
		for (String d : documents) {
			doc = new Document();
			String content = Utilities.readFileAsString(DIRECTORY + "\\" + d);
			doc.add(new Field("title", d, Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			doc.add(new Field("content", content, Field.Store.NO,
					Field.Index.ANALYZED));
			indexWriter.addDocument(doc);
		}
		indexWriter.commit();

		reader = IndexReader.open(indexer);
		searcher = new IndexSearcher(reader);
	}

	private Vector<String> GetRelatedFromWikipedia(String query, int limit) {
		Vector<String> res = new Vector<String>();
		WebClientX client = new WebClientX();
		String content = "";

		content = client
				.GetMethod("http://en.wikipedia.org/w/index.php?title=Special:Search&redirs=1&profile=default&search="
						+ Utilities.EncodeQuery(query)
						+ "&limit="
						+ limit
						+ "&offset=0");
		res = Utilities
				.SimpleRegex(
						"<div class='mw-search-result-heading'><a href=\"[^\"]*\" title=\"([^\"]*)\">",
						content, 1);
		return res;
	}
}
