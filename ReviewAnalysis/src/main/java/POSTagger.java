import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class POSTagger
{
	
	private static POSModel model = new POSModelLoader().load(new File(
			"src//main//resources//en-pos-maxent.bin"));
	
	public static ArrayList<String> POSTaggedWords (String reviews) throws IOException
	{
//			String reviews = new String(Files.readAllBytes(Paths
//					.get("src/main/resources/ReviewAggregate.txt")));
//			System.out.println("Reviews : " + reviews);
		ArrayList<String> tokenList = new ArrayList<String>();
		tokenList = Tokenize(reviews);
		ArrayList<String> tagList = POSTag(tokenList);
		ArrayList<String> wordList = new ArrayList<String>();
		for (int i = 0; i < tokenList.size(); i++)
		{
			// System.out.println(tokenList.get(i) + " " + tagList.get(i));
			if (tagList.get(i).equalsIgnoreCase(Constants.ADJECTIVE)
					|| tagList.get(i).equalsIgnoreCase(
							Constants.ADJECTIVE_COMPARATIVE)
					|| tagList.get(i).equalsIgnoreCase(
							Constants.ADJECTIVE_SUPERLATIVE)
					|| tagList.get(i).equalsIgnoreCase(Constants.ADVERB)
					|| tagList.get(i).equalsIgnoreCase(
							Constants.ADVERB_COMPARATIVE)
					|| tagList.get(i).equalsIgnoreCase(
							Constants.ADVERB_SUPERLATIVE))
			{
//					System.out.println(tokenList.get(i)
//							+ " is an adverb or an adjective");
				wordList.add(tokenList.get(i));
			}
		}
		
		return wordList;
	}

	public static ArrayList<String> Tokenize(String sentence)
			throws InvalidFormatException, IOException
	{
		InputStream tokenStream = new FileInputStream(
				"src//main//resources//en-token.bin");
		TokenizerModel model = new TokenizerModel(tokenStream);
		Tokenizer tokenizer = new TokenizerME(model);
		ArrayList<String> tokenList = new ArrayList<String>(
				Arrays.asList(tokenizer.tokenize(sentence)));
		tokenStream.close();
		return tokenList;
	}

	public static ArrayList<String> POSTag(ArrayList<String> tokenList)
			throws IOException
	{
		//POSModel model = new POSModelLoader().load(new File(
			//	"src//main//resources//en-pos-maxent.bin"));
		// PerformanceMonitor perfMon = new PerformanceMonitor(System.err,
		// "sent");
		POSTaggerME tagger = new POSTaggerME(model);
		// perfMon.start();
		// TODO Change crappier version
		
		ArrayList<String> tagList = new ArrayList<String>();
		for (String line : tokenList)
		{
			String whitespaceTokenizerLine[] = new String[1];
			whitespaceTokenizerLine[0] = line;
			String[] tags = tagger.tag(whitespaceTokenizerLine);
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			String[] tagArr = sample.getTags();
			String[] wordArr = sample.getSentence();
			for (int i = 0; i < wordArr.length; i++)
			{
				// System.out.println(wordArr[i] + " " + tagArr[i]);
				tagList.add(tagArr[i]);
			}
			// perfMon.incrementCounter();
		}
		// perfMon.stopAndPrintFinalResult();
		return tagList;
	}

}
