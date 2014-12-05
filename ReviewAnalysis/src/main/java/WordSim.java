import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordSim
{
	private String path = "src/main/resources/dict";
	private URL url = null;
	private static IDictionary dict = null;

	public WordSim()
	{
		try
		{
			url = new URL("file", null, path);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		if (url == null)
			return;

		// construct the dictionary object and open it
		dict = new Dictionary(url);
		try
		{
			dict.open();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public ArrayList<String> formList(Map<String, Integer> wordList)
	{
		// look up first sense of the word "dog"
		ArrayList<String> finalList = new ArrayList<String>();
		for (String word : wordList.keySet())
		{
			IIndexWord idxWord = dict.getIndexWord(word, POS.ADJECTIVE);
			IWordID wordID = idxWord.getWordIDs().get(0);
			IWord dictWord = dict.getWord(wordID);
			List<IWordID> relatedWords = dictWord.getRelatedWords();
			for (Object dWord : relatedWords)
			{
				String relWord = dWord.toString();
				if(finalList.contains(relWord))
					finalList.add(relWord);
				else
					finalList.add(word);
			}
			// System.out.println("Lemma = " + dictWord.getLemma());
			// System.out.println("Gloss = " + dictWord.getSynset().getGloss());
		}

		return finalList;
	}

}
