import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;


public class ReviewUsefulness {
	public static void main(String[] args) {
		
		
		// word tokenization
		String review = ""; // replace this with review to be tokenized
		StringReader sr = new StringReader(review);
		DocumentPreprocessor dp = new DocumentPreprocessor(sr);
		for (List<HasWord> sentence : dp) {
			for(int i = 0; i < sentence.size() - 1; i++) {
				String word = sentence.get(i).word();
				String nextWord = sentence.get(i + 1).word();
				// re-concatenate contractions
	    		if(nextWord.equals("m") || nextWord.equals("ll") || nextWord.equals("d") || 
	    				nextWord.equals("ve") || nextWord.equals("re") || 
	    				nextWord.equals("s") || nextWord.equals("ll") || nextWord.equals("nt") ) {
	    			if(nextWord.equals("nt")) {
	    				word = word + "n't";
	    			}
	    			else {
	    				word = word + "'" + nextWord;
	    			}
	    			i++;
	    		}
			}
		}
		sr.close();
	}
}
