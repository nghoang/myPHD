package programs;

import java.util.Vector;

import com.ngochoang.CrawlerLib.ParserLib;
import com.ngochoang.CrawlerLib.Utilities;
import com.ngochoang.CrawlerLib.WebClientX;
import com.ngochoang.ParseObject.GeneralObject;

public class DiggCommentCollector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String keyword = "health";
		int maxPage = 10;
		WebClientX client = new WebClientX();
		//client.DebugFiddler();
		String content = "";
		String linkPs = "story-item-comments\"><a href=\"([^\"]*)\".*?(?=storylist-item-comment-count)storylist-item-comment-count\">(\\d*)";
		String commentPs = "<p class=\"comment-body\">(.*?)(?=</p>)";
		String authorPs = "data-digg-username=\"([^\"]+)";
		ParserLib parser = new ParserLib();
		Vector<GeneralObject> groups;
		String commentBody = "";
		String author = "";
		int itemCount = 1;
		for (int page = 1; page <= maxPage; page++) {
			content = client.GetMethod("http://digg.com/search?q="
					+ Utilities.EncodeQuery(keyword)
					+ "&sort=relevance&ajax=off&page=" + page
					+ "&digg_count=50");
			Vector<String> links = Utilities.SimpleRegex(linkPs, content, 1);
			Vector<String> counts = Utilities.SimpleRegex(linkPs, content, 2);
			for (int i = 0; i < counts.size(); i++) {
				String count = counts.get(i);
				if (Integer.parseInt(count) > 0) {
					content = client
							.GetMethod("http://digg.com" + links.get(i));
					groups = parser.GetBlock(content, "div", "", "",
							"comment group ", "", "");
					for (GeneralObject group : groups) {
						commentBody = Utilities.SimpleRegexSingle(commentPs,
								group.getInnerText(), 1);
						commentBody = Utilities.HTMLDecode(commentBody);
						commentBody = Utilities.GetPlainText(commentBody).trim();
						author = Utilities.SimpleRegexSingle(authorPs,
								group.getInnerText(), 1);
						if (commentBody.equals("") == false
								&& author.equals("") == false) {
							Utilities
									.WriteFile("C:\\diggdata\\" + itemCount + ".txt",
											author + " wrote:\n\n"
													+ commentBody, false);
							itemCount++;
						}
					}
				}
			}
		}
	}

}
