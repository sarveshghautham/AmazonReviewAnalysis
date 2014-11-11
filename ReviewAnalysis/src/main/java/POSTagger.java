import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class POSTagger
{
	public static void main(String args[])
	{
		try
		{
			String reviews = new String(Files.readAllBytes(Paths
					.get("src//main//resources//ReviewAggregate.txt")));
			System.out.println("Reviews : " + reviews);
			ArrayList<String> tokenList = new ArrayList<String>();
			tokenList = Tokenize(reviews);
			ArrayList<String> tagList = POSTag(tokenList);
			ArrayList<String> wordList = new ArrayList<String>();
			for (int i = 0; i < tokenList.size(); i++)
			{
				// System.out.println(tokenList.get(i) + " " + tagList.get(i));
				if (tagList.get(i).equalsIgnoreCase(Tags.ADJECTIVE)
						|| tagList.get(i).equalsIgnoreCase(
								Tags.ADJECTIVE_COMPARATIVE)
						|| tagList.get(i).equalsIgnoreCase(
								Tags.ADJECTIVE_SUPERLATIVE)
						|| tagList.get(i).equalsIgnoreCase(Tags.ADVERB)
						|| tagList.get(i).equalsIgnoreCase(
								Tags.ADVERB_COMPARATIVE)
						|| tagList.get(i).equalsIgnoreCase(
								Tags.ADVERB_SUPERLATIVE))
				{
					System.out.println(tokenList.get(i)
							+ " is an adverb or an adjective");
					wordList.add(tokenList.get(i));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String[] SentenceDetect(String paragraph)
			throws InvalidFormatException, IOException
	{
		// always start with a model, a model is learned from training data
		InputStream is = new FileInputStream(
				"src//main//resources//en-sent.bin");
		SentenceModel model = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String sentences[] = sdetector.sentDetect(paragraph);
		is.close();
		return sentences;
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
		POSModel model = new POSModelLoader().load(new File(
				"src//main//resources//en-pos-maxent.bin"));
		// PerformanceMonitor perfMon = new PerformanceMonitor(System.err,
		// "sent");
		POSTaggerME tagger = new POSTaggerME(model);
		// perfMon.start();
		// TODO Change crappier version
		int cnt = 0;
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
