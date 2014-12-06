public class Word
{
	String word;
	String partOfSpeech;

	public Word(String word, String pos)
	{
		this.word = word;
		this.partOfSpeech = pos;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Word))
		{
			return false;
		}
		Word other = (Word) obj;
		if (word == null)
		{
			if (other.word != null)
			{
				return false;
			}
		}
		else if (!word.equals(other.word))
		{
			return false;
		}
		return true;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	public String getPartOfSpeech()
	{
		return partOfSpeech;
	}

	public void setPartOfSpeech(String partOfSpeech)
	{
		this.partOfSpeech = partOfSpeech;
	}
	
	public String toString(){
		return this.word;
	}

}
