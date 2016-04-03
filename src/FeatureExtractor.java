import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Generates an arff file with a select set of features
 *
 */
public class FeatureExtractor {
	public static HashSet<String> goodPronouns = new HashSet<String>();
	
	public static void main(String[] args) {
		FastVector atts = new FastVector();
		
		// length data
		atts.addElement(new Attribute("avg_sentence_length"));
		atts.addElement(new Attribute("review_length"));
		// meta-data
		atts.addElement(new Attribute("coolness"));
		atts.addElement(new Attribute("funniness"));
		atts.addElement(new Attribute("stars"));

		// readability
		atts.addElement(new Attribute("readability"));
		
		//word types 
		atts.addElement(new Attribute("pronounRatio"));
		atts.addElement(new Attribute("adjectiveRatio"));
		atts.addElement(new Attribute("verbRatio"));
		atts.addElement(new Attribute("superlativeRatio"));
		atts.addElement(new Attribute("comparativeRatio"));
		
		// the final attribute is the score (usefulness)
		atts.addElement(new Attribute("usefulness"));

		//initializes list of personal pronouns
		setUpPronounList();
		
		//creates tagger object
		MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
		
		// create Instance object
		Instances data = new Instances("yelp_dataset", atts, 0);
		
		// read in the restaurant reviews
		try {
			BufferedReader br = new BufferedReader(new FileReader("yelp_academic_dataset_review_restaurants.json"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("feature_scores.arff"));
			JsonParser parser = new JsonParser();
			
			bw.write(data.toString());
			
			String line = null;
			while((line = br.readLine()) != null) {
				double[] vals = new double[data.numAttributes()];
				JsonElement element = parser.parse(line);
				String review = element.getAsJsonObject().get("text").getAsString();
				String tag = tagger.tagString(review); //tagged string
				
				// calculate average sentence length
				vals[0] = calculateAvgSentenceLength(review);
				// word count of entire review
				vals[1] = wordCount(review);
				
				// votes - useful, cool, funny, stars
				JsonObject votes = (JsonObject) element.getAsJsonObject().get("votes");
				// coolness
				vals[2] = votes.get("cool").getAsDouble();
				// funniness
				vals[3] = votes.get("funny").getAsDouble();
				
				// stars
				vals[4] = element.getAsJsonObject().get("stars").getAsDouble();

				// Christian's section -- compute readability
				vals[5] = readability(review);
				
				//spencer's section below
				double types[] = wordTypes(tag);
				vals[6] = types[0] / vals[1];
				
				vals[7] = types[1] / vals[1];
				
				vals[8] = types[2] / vals[1];
				
				vals[9] = types[3] / vals[1];
				
				vals[10] = types[4] / vals[1];
				//usefulness
				vals[vals.length-1] = votes.get("useful").getAsDouble();
				
				// output instance instance to data
				bw.write((new Instance(1.0, vals).toString()) + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double calculateAvgSentenceLength(String text) {
		StringReader sr = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(sr);
		double numSentences = 0;
		double numWords = 0;
		for (List<HasWord> sentence : dp) {
			ArrayList<String> tokenized = new ArrayList<String>(); 
			for(int i = 0; i < sentence.size() - 1; i++) {
				String word = sentence.get(i).word();
				String nextWord = sentence.get(i + 1).word();
				// re-concatenate contractions
	    		if(nextWord.equals("'m") || nextWord.equals("'d") || 
	    				nextWord.equals("'ve") || nextWord.equals("'re") || 
	    				nextWord.equals("'s") || nextWord.equals("'ll") || nextWord.equals("n't") ) {
	    			if(nextWord.equals("n't")) {
	    				word = word + "n't";
	    			}
	    			else {
	    				word = word + "'" + nextWord;
	    			}
	    			i++;
	    		}
	    		tokenized.add(word);
			}
			numSentences++;
			numWords += tokenized.size();
		}
		sr.close();
		return numWords/numSentences;
	}
	
	public static double wordCount(String text) {
		StringReader sr = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(sr);
		double numWords = 0;
		for (List<HasWord> sentence : dp) {
			ArrayList<String> tokenized = new ArrayList<String>(); 
			for(int i = 0; i < sentence.size() - 1; i++) {
				String word = sentence.get(i).word();
				String nextWord = sentence.get(i + 1).word();
				// re-concatenate contractions
	    		if(nextWord.equals("'m") || nextWord.equals("'d") || 
	    				nextWord.equals("'ve") || nextWord.equals("'re") || 
	    				nextWord.equals("'s") || nextWord.equals("'ll") || nextWord.equals("n't") ) {
	    			if(nextWord.equals("n't")) {
	    				word = word + "n't";
	    			}
	    			else {
	    				word = word + "'" + nextWord;
	    			}
	    			i++;
	    		}
	    		tokenized.add(word);
			}
			numWords += tokenized.size();
		}
		sr.close();
		return numWords;
	}
	
	
	// Uses classes from syllable.jar - include in classpath
	public static float readability(String review)
	{
		StringReader sr = new StringReader(review);
		DocumentPreprocessor dp = new DocumentPreprocessor(sr);
		EnglishSyllableCounter sc = new EnglishSyllableCounter();
		
		int totalSents = 0;
		int totalWords = 0;
		int totalSyllables = 0;
		
		for (List<HasWord> sentence : dp)
		{
			totalSents++;
			for (HasWord w : sentence)
			{
				String s = w.word();
				// Throw out any word that isn't just letters
				if (s.matches("[a-zA-z]+"))
				{
					totalWords++;
					int sylCount = sc.countSyllables(s);
					totalSyllables += sylCount;	
				}	
			}
		}
		return (float)(206.835 - 1.015*totalWords/totalSents - 84.6*totalSyllables/totalWords);
	}

	public static double[] wordTypes(String tag)
	{
		double types[] = new double[5];
		String[] words = tag.split(" ");
		int adjective = 0; 
		int pronoun = 0;
		int verb = 0;
		int superlative = 0;
		int comparative = 0;
		for(int i = 0; i < words.length; i++)
		{
			String[] splitter = words[i].split("_");
			if(splitter.length != 2)
			{
				continue;
			}
				
			//condition this so punctuation does not count
			if(splitter[1].equals("PRP") || splitter[1].equals("PRP$"))
			{
				if(pronounChecker(splitter[0]))
				{
					pronoun++;
				}
			}
			else if(splitter[1].equals("JJR"))
			{
				adjective++;
				comparative++;
			}
			else if(splitter[1].equals("JJS"))
			{
				adjective++;
				superlative++;
			}
			else if(splitter[1].substring(0,1).equals("V"))
			{
				verb++;
			}
		}
			
		types[0] = (double)pronoun;
		types[1] = (double)adjective;
		types[2] = (double)verb;
		types[3] = (double)superlative;
		types[4] = (double)comparative;
			
		return types;
		
	}
		
	public static boolean pronounChecker(String pronoun)
	{
		if(goodPronouns.contains(pronoun.toLowerCase()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static void setUpPronounList()
	{
		String list = "i me my our us ours ourselves we";
		String[] words = list.split(" ");
		for(int i = 0; i < words.length; i++)
			goodPronouns.add(words[i]);
		
	}
	
	
	
	

	
}
