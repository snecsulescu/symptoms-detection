package scrapping;


import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;

public class Parsing {
	StanfordCoreNLP pipeline = null;
	public Parsing() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}
	
	private String getOriginalText(Set<IndexedWord> list) {
		StringBuilder str = new StringBuilder();
		TreeMap<Integer, String> phrase = new TreeMap<Integer, String>();
 		for (IndexedWord word : list) {
 			phrase.put(word.index(), word.originalText());
 		}
 		for (String word : phrase.values()) {
 			str.append(word).append(" ");
 		}
 		
 		return str.toString().trim();
	}
	
	public List<Pair<String, SemanticGraph>> getDependencyGraph(String text){
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List < CoreMap > sentences = document.get(SentencesAnnotation.class);
		List<Pair<String,SemanticGraph>> graphs = new LinkedList<Pair<String,SemanticGraph>>();
		
		for (CoreMap sentence : sentences) {
			SemanticGraph dependencies = sentence.get(EnhancedPlusPlusDependenciesAnnotation.class);
			graphs.add(new Pair<String, SemanticGraph>(sentence.toString(), dependencies));
		}
		return graphs;
	}
	
	public boolean hasSymptomsInclude(SemanticGraph dependencies) {
	 	SemgrexPattern semgrex = SemgrexPattern.compile("{lemma:/symptom/}=A <nsubj=reln {lemma:/include/}=B");
	 	SemgrexMatcher matcher = semgrex.matcher(dependencies);
	 	return matcher.find();
	}	
	
	public boolean hasExperienceSymptoms(SemanticGraph dependencies) {
	 	SemgrexPattern semgrex = SemgrexPattern.compile("{lemma:/symptom/}=A <dobj=reln {lemma:/experience/}=B");
	 	SemgrexMatcher matcher = semgrex.matcher(dependencies);
	 	return matcher.find();
	}	
	
	public String hasAreSymptoms(SemanticGraph dependencies) {
		SemgrexPattern semgrex = SemgrexPattern.compile("{tag:/NN.*/}=A <nsubj=reln {lemma:/symptom/}=B");
	 	SemgrexMatcher matcher = semgrex.matcher(dependencies);
	 	while (matcher.find()) {
	 		Set<IndexedWord> words = dependencies.getSubgraphVertices(matcher.getNode("A"));
	 		String orgText = getOriginalText(words);
	 		return orgText.trim();
	 	}
	 	return null;
	}
	
	public String hasPeopleExperience(SemanticGraph dependencies) {
		
		SemgrexPattern semgrex = SemgrexPattern.compile("{lemma:/experience/}=A  >nsubj {lemma:/people/}=B  >dobj {}=C");
	 	SemgrexMatcher matcher = semgrex.matcher(dependencies);
	 	while (matcher.find()) {
	 		Set<IndexedWord> words = dependencies.getSubgraphVertices(matcher.getNode("C"));
	 		String orgText = getOriginalText(words);
	 		return orgText.trim();
	 	}
	 	return null;
	}
	
	public String hasSymptomsAre(SemanticGraph dependencies) {
		SemgrexPattern semgrex = SemgrexPattern.compile("{lemma:/symptom/}=A <nsubj=reln {tag:/NN.*/}=B");
	 	SemgrexMatcher matcher = semgrex.matcher(dependencies);
	 	StringBuilder str = new StringBuilder();
	 	while (matcher.find()) {
	 		 
	 		for (IndexedWord w : dependencies.getChildrenWithReln(matcher.getNode("B"), UniversalEnglishGrammaticalRelations.COMPOUND_MODIFIER)) {
	 			str.append(getOriginalText(dependencies.getSubgraphVertices(w)) + " " + matcher.getNode("B").originalText()).append("\n");
	 		}
	 	    
	 		for (IndexedWord w : dependencies.getChildrenWithReln(matcher.getNode("B"), UniversalEnglishGrammaticalRelations.APPOSITIONAL_MODIFIER)) {
	 			str.append(getOriginalText(dependencies.getSubgraphVertices(w))).append("\n");
	 		}
	 		
	 		for (IndexedWord w : dependencies.getChildrenWithRelns(matcher.getNode("B"), UniversalEnglishGrammaticalRelations.getConjs())) {
	 			str.append(getOriginalText(dependencies.getSubgraphVertices(w))).append("\n");
	 		}
	 		
	 		for (IndexedWord w : dependencies.getChildrenWithRelns(matcher.getNode("B"), UniversalEnglishGrammaticalRelations.getNmods())) {
	 			str.append(getOriginalText(dependencies.getSubgraphVertices(w))).append("\n");
	 		}
	 		return str.toString();
	 	}
	 	return null;
	}	
	
	public String hasSymptomsSuchAs(SemanticGraph dependencies) {
		StringBuilder str = new StringBuilder();
		
		IndexedWord node = dependencies.getNodeByWordPattern("symptoms");
		if (node == null)
			return null;
		
		Set<IndexedWord> incidentNodes = dependencies.getChildrenWithReln(node, UniversalEnglishGrammaticalRelations.getNmod("such_as"));
		if (incidentNodes.size() == 0)
			return null;
		try {
			List<IndexedWord> topologicalNodes = dependencies.topologicalSort();
			
	 		for (IndexedWord w : topologicalNodes)
	 			if (incidentNodes.contains(w)) {
	 				str.append(getOriginalText(dependencies.getSubgraphVertices(w)));
	 				return str.toString();
	 			}
		}
		catch (IllegalStateException e) {
			/* If the graph has cycle, the topological sort is not possible. 
			 * Therefore, we will generate all the phrases that are connected to symptoms by an edge nmod:such_as */
			
			for (IndexedWord w : incidentNodes)
	 			str.append(getOriginalText(dependencies.getSubgraphVertices(w)));
	 		return str.toString();
	 	}
 	    return null;
 	}	
}
