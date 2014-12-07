import java.util.ArrayList;

public class NlpResult {

	public ArrayList<String> adjectives = new ArrayList<String>();
	public double posPercent;
	
	public NlpResult(ArrayList<String> words, double percent) {
		this.adjectives = words;
		this.posPercent = percent;
	}
	
}
