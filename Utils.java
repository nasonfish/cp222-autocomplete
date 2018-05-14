import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class which provides generic functions which may be re-used. All methods included are static.
 * This is so I don't have to implement reading information from the Internet every single day.
 * 
 * @author Daniel Barnes '21
 */
public class Utils {

	/**
	 * Call this with a URL;
	 * then do something like:
	 * NodeList rows = data.getElementsByTagName("tr");
	 *
	 */
	public static Document pullDocument(URL url) {
		try {
			InputStream internetStream = url.openStream();
			// https://docs.oracle.com/javase/7/docs/api/javax/xml/parsers/DocumentBuilder.html
			Document data = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(internetStream);
			internetStream.close(); // Close stream, which has been read already, to prevent resource leak
			// https://docs.oracle.com/javase/7/docs/api/org/w3c/dom/Document.html#getElementsByTagName(java.lang.String)
			return data;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.out.println("An unexpected error occurred pulling the document from the Internet.");
			System.exit(1);
			return null;
		}
	}
	
	/**
	 * Read from the Internet, into a scanner.
	 * Remember to close the scanner after use!
	 */
	public static Scanner pullText(URL url) {
		try {
			InputStream internetStream = url.openStream();
			// I tried using a BufferedReader first but then
			// found this simpler solution from StackOverflow.
			// https://stackoverflow.com/questions/6259339/how-to-read-a-text-file-directly-from-internet-using-java
			Scanner s = new Scanner(internetStream);
			return s;
		} catch (IOException e) {
			System.out.println("An unexpected error occurred pulling the document from the Internet.");
			System.exit(1);
			return null;
		}	
	}
}
