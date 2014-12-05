import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class NLPParser
{

	public static void main(String[] args)
	{
		String text = null;
		Map<String, Integer> wordScore = new HashMap<String, Integer>();
		try
		{
			text = new String(Files.readAllBytes(Paths
					.get("src/main/resources/ReviewAggregate.txt")));
			System.out.println("Review text : " + text);

			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			props.put("annotators",
					"tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			// read some text in the text variable

			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);

			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys
			// and has values with custom types
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			// For each sentence, do this
			for (CoreMap sentence : sentences)
			{
				System.out.println("Sentence: " + sentence);
				// Sentiment analysis
				String sentiment = sentence.get(SentimentCoreAnnotations.ClassName.class);
				System.out.println("Sentiment: " + sentiment);
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific
				// methods
				for (CoreLabel token : sentence.get(TokensAnnotation.class))
				{
					// this is the text of the token
					String word = token.get(TextAnnotation.class);
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);

					if (pos.equals("JJ"))
					{
						if (!wordScore.containsKey(word))
							wordScore.put(word, 1);
						else
							wordScore.put(word, wordScore.get(word) + 1);
					}
					// this is the NER label of the token
					// String ne = token.get(NamedEntityTagAnnotation.class);
				}

				/*
				 * System.out.println("Dependency parsing"); SemanticGraph
				 * dependencies = sentence
				 * .get(CollapsedCCProcessedDependenciesAnnotation.class); for(
				 * TypedDependency dep : dependencies.typedDependencies()) {
				 * GrammaticalRelation reln = dep.reln(); IndexedWord gov =
				 * dep.gov(); IndexedWord depWord = dep.dep();
				 * System.out.println(reln + " " + gov + " " + depWord); }
				 */
			}

			System.out.println("***");
			// This is the coreference link graph
			// Each chain stores a set of mentions that link to each other,
			// along with a method for getting the most representative mention
			// Both sentence and token offsets start at 1!
			Map<Integer, CorefChain> graph = document
					.get(CorefChainAnnotation.class);
			for (Entry<Integer, CorefChain> entry : graph.entrySet())
				System.out.println(entry.getKey() + " " + entry.getValue());

			System.out.println("Word score");
			for (Entry<String, Integer> entry : wordScore.entrySet())
				System.out.println(entry.getKey() + " " + entry.getValue());
			System.out.println("WordSim");
			WordSim ws = new WordSim();
			ArrayList<String> finalList = ws.formList(wordScore);
			System.out.println("Final list");
			for (String fWord : finalList)
				System.out.println(fWord);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
