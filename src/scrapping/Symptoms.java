package scrapping;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.SysexMessage;

import com.google.protobuf.ByteString.Output;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import scrapping.Scrapping;
public class Symptoms {
	// such_as + <UL>
	public static final String[] urls = new String[] {
		"http://www.nhs.uk/Conditions/Heart-block/Pages/Symptoms.aspx",
		"http://www.nhs.uk/conditions/frozen-shoulder/Pages/Symptoms.aspx",
		"http://www.nhs.uk/conditions/coronary-heart-disease/Pages/Symptoms.aspx",
		"http://www.nhs.uk/conditions/bronchitis/Pages/Symptoms.aspx",
		"http://www.nhs.uk/conditions/warts/Pages/Introduction.aspx",
		"http://www.nhs.uk/conditions/Sleep-paralysis/Pages/Introduction.aspx",
		"http://www.nhs.uk/Conditions/Glue-ear/Pages/Symptoms.aspx",
		"http://www.nhs.uk/Conditions/Depression/Pages/Symptoms.aspx",
		"http://www.nhs.uk/Conditions/Turners-syndrome/Pages/Symptoms.aspx",
		"http://www.nhs.uk/Conditions/Obsessive-compulsive-disorder/Pages/Symptoms.aspx"
	};

	public List<String[]> search_symptoms(LinkedList<String[]> doc) {
		List<String[]> results = new LinkedList<String[]>();
		Iterator<String[]> it = doc.iterator();
		boolean step = true;
		String[] units = null;
		while (it.hasNext()) {
			if (step) 
				units = it.next();
			step = true;
			
			String text = units[1];
			
			Parsing p = new Parsing();
			
			List<Pair<String, SemanticGraph>> sentenceGraphs = p.getDependencyGraph(text);
			for (Pair<String, SemanticGraph> elem : sentenceGraphs) {
				String sentence = elem.getObj1();
				SemanticGraph dependencies = elem.getObj2();
				
				if (sentence.endsWith("symptoms such as:")) {
					if (it.hasNext()) {
						units = it.next();
						if (units[0].equals("ul")) {
							results.add(new String[] {sentence, units[1]});
							step = true;
						}
						else
							step = false;
					}
				}
				
				if (sentence.endsWith("include:") && p.hasSymptomsInclude(dependencies)) {
					if (it.hasNext()) { 
						units = it.next();
						if (units[0].equals("ul")) {
							results.add(new String[] {sentence, units[1]});
							step = true;
						}
						else
							step = false;
					}
				}
				
				if (sentence.endsWith("symptoms:") && p.hasExperienceSymptoms(dependencies)) {
					if (it.hasNext()) { 
						units = it.next();
						if (units[0].equals("ul")) {
							results.add(new String[] {sentence, units[1]});
							step = true;
						}
						else
							step = false;
					}
				}
				
				String symptomsInPassive = p.hasAreSymptoms(dependencies);
				if (symptomsInPassive != null) {
					results.add(new String[] {sentence, symptomsInPassive});
				}
				
				String hasSymptomsInActive = p.hasSymptomsAre(dependencies);
				if (hasSymptomsInActive != null && hasSymptomsInActive.length()>0) {
					results.add(new String[] {sentence, hasSymptomsInActive});
				}
				
				String hasSymptomsSuchAs = p.hasSymptomsSuchAs(dependencies);
				if (hasSymptomsSuchAs != null) {
					results.add(new String[] {sentence, hasSymptomsSuchAs});
				}
				
				String hasPeopleExperience = p.hasPeopleExperience(dependencies);
				if (hasPeopleExperience != null) {
					results.add(new String[] {sentence, hasPeopleExperience});
				}
			}
		}
		
		return results;
	}
	
		
	public static void main(String[] args){
		RedwoodConfiguration.empty().apply();
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter("output.txt"));
			for (String url : urls){
				output.write(url);
				output.write("\n");
				LinkedList<String[]> doc = Scrapping.downloadPage(url);
				Symptoms stms = new Symptoms();
				List<String[]> results = stms.search_symptoms(doc);
				for (String[] line :results) {
					
					output.write("Hint: " + line[0]);
					output.write("\n");
					output.write("Symptom(s): " + line[1]);
					output.write("\n");
					output.write("----------------------------------------------\n");
					
				}
			}
			
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
