import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.ClassName;
import edu.stanford.nlp.util.CoreMap;

public class NLPParser
{

	Properties props = null;
	static StanfordCoreNLP pipeline = null;
	static Annotation document = null;
	WordSim ws = null;
	Map<String, Boolean> stopWords = new HashMap<String, Boolean>();
	private static NLPParser nlpObj = null;
	
	private NLPParser()
	{
		// creates a StanfordCoreNLP object, with POS tagging,
		// lemmatization, NER, parsing, and coreference resolution
		props = new Properties();
		props.put("annotators",
				"tokenize, ssplit, pos, lemma, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
		ws = new WordSim();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Stopwords.txt"));
			String line = null;
			while ((line = br.readLine()) != null)
				stopWords.put(line, true);
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public static NLPParser getInstance () {
		if (nlpObj == null) {
			nlpObj = new NLPParser();			
		}	
		
		return nlpObj;
	}

	public NlpResult parseText(String text)
	{
	
		ArrayList<Word> wordList = new ArrayList<Word>();
		Map<String, Integer> sentimentList = new HashMap<String, Integer>();
		Class<ClassName> className = ClassName.class;
		sentimentList.put(Constants.POSITIVE, 0);
		sentimentList.put(Constants.NEGATIVE, 0);

		// Initialize document
		document = new Annotation(text);
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		int positive = 0, negative = 0;

		// For each sentence, do this
		for (CoreMap sentence : sentences)
		{
			// Sentiment analysis
			String sentiment = sentence.get(className);
			System.out.println("Sentence: " + sentence + " -- " + sentiment);
			if (sentiment.equals(Constants.POSITIVE))
				positive++;
			if (sentiment.equals(Constants.NEGATIVE))
				negative++;
			if (sentiment.equals(Constants.VERY_POSITIVE))
				positive += 2;
			if (sentiment.equals(Constants.VERY_NEGATIVE))
				negative += 2;
			if (sentence.toString().contains(Constants.REVIEWDELIM))
			{
				if ((positive - negative) >= 0)
					sentimentList.put(Constants.POSITIVE,
							sentimentList.get(Constants.POSITIVE) + 1);
				else
					sentimentList.put(Constants.NEGATIVE,
							sentimentList.get(Constants.NEGATIVE) + 1);
				System.out.println("Resetting count");
				positive = 0;
				negative = 0;
			}
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific

			for (CoreLabel token : sentence.get(TokensAnnotation.class))
			{
				// this is the text of the token
				String lemma = token.get(LemmaAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);

				if (pos.equals(Constants.ADJECTIVE) && null != lemma)
				{
					if (!lemma.equalsIgnoreCase(Constants.REVIEWDELIM))
						wordList.add(new Word(lemma, pos));
				}
			}

//			SemanticGraph dependencies = sentence
//					.get(CollapsedCCProcessedDependenciesAnnotation.class);
//			Set<Word> wordSet = new HashSet<Word>();
//			for (TypedDependency dep : dependencies.typedDependencies())
//			{
//				String reln = dep.reln().toString();
//				String gov = dep.gov().lemma();
//				String p_gov = dep.gov().tag();
//				String depWord = dep.dep().lemma();
//				String p_depWord = dep.dep().tag();
//				if (reln.equals(Constants.ADVCL) || reln.equals(Constants.ADVCOMP)
//						|| reln.equals(Constants.ADVMOD))
//				{
//					if(!stopWords.containsKey(gov))
//						wordSet.add(new Word(gov, p_gov));
//					if(!stopWords.containsKey(depWord))
//						wordSet.add(new Word(depWord, p_depWord));
//				}
//			}
//			wordList.addAll(wordSet);
		}

		System.out.println("Final list");
		// Reduce the list
		for (String fWord : ws.reduceList(wordList))
			System.out.println(fWord);

		int pos = sentimentList.get(Constants.POSITIVE);
		int neg = sentimentList.get(Constants.NEGATIVE);
		double posPercentage = ((double) pos / (pos + neg)) * 100;
		System.out.println("Positive %: " + posPercentage);

		return new NlpResult(ws.reduceList(wordList), posPercentage);
	}

	public static void main(String[] args)
	{
		NLPParser parser = new NLPParser();
		String text = null;
		try
		{
			// create an empty Annotation just with the given text
			text = new String(Files.readAllBytes(Paths
					.get("src/main/resources/ReviewAggregate.txt")));
			System.out.println("Review text : " + text);
			parser.parseText(text);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
