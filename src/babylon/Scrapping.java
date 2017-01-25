package babylon;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class Scrapping {
	public static int getStatusConnectionCode(String url) {
		Response response = null;
		
	    try {
			response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
		    } catch (IOException ex) {
			System.out.println("Error with Status Code: " + ex.getMessage());
		    }
	    return response.statusCode();
	}
	
	public static Document getHtmlDocument(String url) {

	    Document doc = null;
		try {
		    doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
		    } catch (IOException ex) {
			System.out.println("Error for downloading the HTML page" + ex.getMessage());
		    }
	    return doc;
	}
	
    public static LinkedList<String[]> downloadPage(String url) {
    	LinkedList<String[]> d = new LinkedList<String[]>();
        if (getStatusConnectionCode(url) == 200) {
            Document document = getHtmlDocument(url);
            Elements entries = document.select("div[class=main-content healthaz-content clear]");
            for (Element elem : entries) {
            	StringBuilder builder = new StringBuilder();
            	stripTags(d, builder, elem.childNodes());
            }
            
        }else
            System.out.println("Status Code Eroor: "+getStatusConnectionCode(url));
        return d;
    }
    public static void stripTags (LinkedList<String[]> d, StringBuilder builder, List<Node> nodesList) {
    	
        for (Node node : nodesList) {
        	String nodeName  = node.nodeName();

            if (nodeName.equalsIgnoreCase("#text")) {
            	String text = StringEscapeUtils.unescapeHtml4(node.toString());
            	builder.append(text);
            	
            } else {
                stripTags(d, builder, node.childNodes());
                if (nodeName.toLowerCase().matches("p")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	d.add(new String[]{nodeName, builder.toString()});
                	builder = new StringBuilder();
                }
                if (nodeName.toLowerCase().matches("h\\d")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.append(".");
                	d.add(new String[]{nodeName, builder.toString()});
                	builder = new StringBuilder();
                }
                if (nodeName.toLowerCase().matches("li")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.append("\n");
                	
                }
                if (nodeName.toLowerCase().matches("ul")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	d.add(new String[]{nodeName, builder.toString()});
                	builder = new StringBuilder();
                }
                if (builder.toString().matches("[.!?]$")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.append(" ");
                }
            }
        }
    }
}
