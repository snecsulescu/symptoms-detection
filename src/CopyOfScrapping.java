import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class CopyOfScrapping {
	public static int getStatusConnectionCode(String url) {
		
	    Response response = null;
		
	    try {
			response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
		    } catch (IOException ex) {
			System.out.println("Excepción al obtener el Status Code: " + ex.getMessage());
		    }
	    return response.statusCode();
	}
	
	public static Document getHtmlDocument(String url) {

	    Document doc = null;
		try {
		    doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
		    } catch (IOException ex) {
			System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
		    }
	    return doc;
	}
	public static final String url = "http://www.nhs.uk/Conditions/Heart-block/Pages/Symptoms.aspx";
	
    public static void downloadPage(String url) {
		
        // Compruebo si me da un 200 al hacer la petición
        if (getStatusConnectionCode(url) == 200) {
			
            // Obtengo el HTML de la web en un objeto Document
            Document document = getHtmlDocument(url);
			
            // Busco todas las entradas que estan dentro de: 
            Elements entradas = document.select("div[class=main-content healthaz-content clear]");
            
            // Paseo cada una de las entradas
            for (Element elem : entradas) {
        		StringBuilder str = new StringBuilder();
        		stripTags(str, elem.childNodes());
            	System.out.println(str.toString()); 
            }
        }else
            System.out.println("El Status Code no es OK es: "+getStatusConnectionCode(url));
    }
    public static void stripTags (StringBuilder builder, List<Node> nodesList) {

        for (Node node : nodesList) {
            String nodeName  = node.nodeName();

            if (nodeName.equalsIgnoreCase("#text")) {
            	String text = StringEscapeUtils.unescapeHtml4(node.toString());
            	builder.append(text);
            	
            } else {
                // recurse
                stripTags(builder, node.childNodes());
                
                if (nodeName.toLowerCase().matches("h\\d")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.append(".");
                }
                if (nodeName.toLowerCase().matches("li")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.append(",");
                }
                if (nodeName.toLowerCase().matches("ul")) {
                	while (builder.toString().matches(".*\\h$")) { 
                		builder.deleteCharAt(builder.length()-1);
                	}
                	builder.deleteCharAt(builder.length()-1).append(".");
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
