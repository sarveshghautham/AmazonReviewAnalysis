import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordSim
{
	private URL url = null;
	private static IDictionary dict = null;

	public WordSim()
	{
		try
		{
			url = new URL("file", null, Constants.DICTPATH);
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

	public ArrayList<String> reduceList(ArrayList<Word> wordList)
	{
		ArrayList<String> finalList = new ArrayList<String>();
		ArrayList<String> glList = new ArrayList<String>();
		for (Word wordInst : wordList)
		{
			String word = wordInst.getWord();
			String posVal = posConverter(wordInst.getPartOfSpeech());
			if(null == posVal)
				continue;
			IIndexWord idxWord = dict.getIndexWord(word, POS.valueOf(posVal));
			if (null != idxWord)
			{
				IWordID wordID = idxWord.getWordIDs().get(0);
				IWord dictWord = dict.getWord(wordID);
				ArrayList<String> tempList = new ArrayList<String>();
				boolean found = false;
				for (IWordID dWord : dictWord.getRelatedWords())
				{
					String relWord = dWord.getLemma();
					if (finalList.contains(relWord))
					{
						found = true;
						tempList.add(relWord);
					}
				}
				if(!found)
					tempList.add(word);
				glList.addAll(tempList);
			}
			else
			{
				System.out.println(word + " not found in Dictionary!");
			}
		}
		finalList.addAll(glList);
		return finalList;
	}

	private String posConverter(String pos)
	{
		if(pos.equalsIgnoreCase(Constants.ADJECTIVE))
				return "adjective".toUpperCase();
		if(pos.equalsIgnoreCase(Constants.ADVERB))
			return "adverb".toUpperCase();
		if(pos.equalsIgnoreCase(Constants.NOUN_PLURAL) || pos.equalsIgnoreCase(Constants.NOUN_SINGULAR_OR_MASS))
			return "noun".toUpperCase();
		if(pos.equalsIgnoreCase(Constants.VERB_BASE_FORM) || pos.equalsIgnoreCase(Constants.VERB_3RD_PERSON_SINGULAR_PRESENT)
				|| pos.equalsIgnoreCase(Constants.VERB_GERUND_OR_PRESENT_PARTICIPLE) || pos.equalsIgnoreCase(Constants.VERB_NON3RD_PERSON_SINGULAR_PRESENT)
				|| pos.equalsIgnoreCase(Constants.VERB_PAST_PARTICIPLE) || pos.equalsIgnoreCase(Constants.VERB_PAST_TENSE))
			return "verb".toUpperCase();
		return null;
	}


}
