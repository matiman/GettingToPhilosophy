package com.mathi.philosophy.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mathi.philosophy.DAO.PostgreSql;
import com.mathi.philosophy.interfaces.GettingToPhilosophyInterface;

public class GettingToPhilosophy implements GettingToPhilosophyInterface {
	private static final String PHILOSOPHY = "philosophy";
	private static final String baseEnglishUrl = "http://en.wikipedia.org/wiki/";
	// Wiki has an api that will give us its wiki text if we append
	// "?action=raw" on every url
	// e.g http://en.wikipedia.org/wiki/iCracked?action=raw
	private static final String wikiActionQuery = "?action=raw";
	private static List<String> linkPath = new ArrayList<String>();
	

	private void saveToDatabase(String title) throws SQLException,
			ClassNotFoundException {

		PostgreSql.insertPath(title, linkPath.toString());
	}

	/*
	 * // The first method to be called from outside API findNextLink method
	 * takes title of a wiki page and finds the nextLink if it is not Philosophy
	 */

	/* (non-Javadoc)
	 * @see com.mathi.philosophy.service.GettingToPhilosophyInterface#getPathFor(java.lang.String)
	 */
	@Override
	public List<String> getPathFor(String title) throws IOException,
			ClassNotFoundException, SQLException {

		List<String> titleExists = checkIfExistsInDB(title);

		if (!titleExists.isEmpty() && titleExists.size() > 1) {
			return titleExists;

		}
		if (!title.equalsIgnoreCase(PHILOSOPHY)) {

			linkPath.add(title);
			findAndProcessNextLink(title);
		}

		try {
			this.saveToDatabase(title);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return linkPath;
	}

	private  List<String> checkIfExistsInDB(String title)
			throws ClassNotFoundException, SQLException {

		List<String> list = new ArrayList<String>();
		String str = "";

		ResultSet rset = PostgreSql.fetchPath(title);
		while (rset.next()) {
			str = rset.getString("path");
		}

		list = Arrays.asList(str.replaceAll("\\[", "").replaceAll("\\]", "")
				.split("\\s*,\\s*"));
		return list;
	}

	/*
	 * Fetch the wiki content and we will go through the data to find the first
	 * link. // The first link is found in a double angle bracket like this
	 * [[iCracked]]. It shouldn't be nested in any other brackets or tags. If
	 * that is the case we skip it. *
	 */
	private void findAndProcessNextLink(String title) throws IOException {

		String content = fetchWikiText(title);
		if (content == null) {
			// System.err.println("Malformated URL ,most probably");
			throw new IOException("Malformated URL ,most probably");

		}

		else {
			char openBracket1 = '(';
			char openBracket2 = '{';
			char openBracket3 = '<';
			char openBracket4 = '[';

			char closedBracket1 = ')';
			char closedBracket2 = '}';
			char closedBracket3 = '>';
			char closedBracket4 = ']';

			// Citation links end with </ref> for e.g <ref>[[iCracked]]</ref>
			String citation = "</ref>";

			// once I know where the first link starts I put its begin and end
			// positions on beginEndex and endIndex respectively
			int beginIndex = -1;
			int endIndex = -1;

			int open1Counter = 0;
			int open2Counter = 0;
			int open3Counter = 0;
			int open4Counter = 0;
			char c;
			// Counting which brackets are opened and closed to make sure we are
			// either in a nested bracket or just outside
			for (int i = 0; i < content.length(); i++) {
				c = content.charAt(i);
				if (c == openBracket1)
					open1Counter++;
				if (c == openBracket2)
					open2Counter++;
				if (c == openBracket3)
					open3Counter++;
				if (c == openBracket4)
					open4Counter++;
				// decrement the bracket if it is closed
				if (c == closedBracket1)
					open1Counter--;
				if (c == closedBracket2)
					open2Counter--;
				if (c == closedBracket3)
					open3Counter--;
				if (c == closedBracket4)
					open4Counter--;

				// get the index of the first link for e.g if the content we
				// found is [[Europe]], we find the index of E
				if (open1Counter == 0 && open2Counter == 0 && open3Counter == 0
						&& open4Counter == 2 && c == openBracket4) {
					beginIndex = i + 1;
				}

				String next;
				// This the point that we may have reached to the final char of
				// the link we need
				if (open1Counter == 0 && open2Counter == 0 && open3Counter == 0
						&& open4Counter == 0 && c == closedBracket4
						&& beginIndex != -1) {
					boolean isCitation = false;
					// for (int j = 0; j < 6; j++) {
					// if (!(citation.charAt(j) == content.charAt(i + j + 1)))
					// isCitation = false;
					// }
					// If the title(url) we found is not a citation
					if (!isCitation) {
						endIndex = i - 2;
						next = content.substring(beginIndex, endIndex + 1);
						if (!next.contains(":")) {
							int separator = next.indexOf("|");
							int separator2 = next.indexOf("#");
							/*
							 * After we fetch the title of the content it may
							 * look like this1) iCracked#IOS or iCracked|IOS In
							 * this case we don't need the characters after "#"
							 * or "|"Any character after that is irrelevant.
							 * Wikipidea only takes the first characters before
							 * the above signsIf there are strings separated by
							 * space we have to replace it by "_"This process is
							 * very essential since we need the exact title to
							 * fetch the other links it containsAnd do the
							 * process again and again until we reach
							 * Philosophy.
							 */

							if (separator2 != -1 && separator2 < separator)
								separator = separator2;

							next = next.replace(" ", "_");
							if (separator != -1)
								next = next.substring(0, separator);

							// System.out.println("The word is>>" + next);
							// Here we check if the link is already in our list
							// which is going to be a loop and we exit
							if (!(next.equalsIgnoreCase(PHILOSOPHY))
									&& linkPath.contains(next)) {

								throw new IOException("LOOP FOUND... EXITING");
							}
							// HERE we found the title to next link and we put
							// it in to the list
							linkPath.add(next);
							if (isPhilosophy(next))
								break;
							// YEYY.. Done!!
							else {

								/*
								 * Go to the next link and repeat recursively
								 * until we reach Philosophy
								 */
								findAndProcessNextLink(next);
								// This break is not to go through the text
								// anymore since the link we need is found on
								// the current page
								break;
							}
						} else {
							// resetting starting and ending indices of the next
							// title we are looking for
							endIndex = beginIndex = -1;
						}
					}
				}
			}
		}

	}

	/*
	 * There is a Web service (WikiMedia Api) which can give us somehow
	 * formatted text of a given page.
	 * http://en.wikipedia.org/wiki/ICracked?action=raw will return back a
	 * formatted source code. In this case the title is iCracked. Try copy
	 * pasting the above link and you will see the formatted code.
	 */
	private String fetchWikiText(String title) {
		try {
			URL url = new URL(baseEnglishUrl + title + wikiActionQuery);
			InputStream in = url.openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			reader.close();
			in.close();
			return out.toString();
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
	}

	private boolean isPhilosophy(String title) {
		return title.equalsIgnoreCase(PHILOSOPHY);
	}
}