import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.functions.LinearRegression;
// This is what we can use to output information about
// the model's performance (root mean squared error, etc.)
import weka.classifiers.Evaluation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.bcel.internal.generic.NEW;

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
	
	public static void main(String[] args) throws IOException {
		FastVector atts = new FastVector();
		FastVector attsClass = new FastVector();
		FastVector classes = new FastVector();
		
		classes.addElement("bin0");
		classes.addElement("bin12");
		classes.addElement("bin3");

		//Linear Regression attributes
		// length data
		atts.addElement(new Attribute("avg_sentence_length"));
		atts.addElement(new Attribute("review_length"));
		// meta-data
		atts.addElement(new Attribute("coolness_attr"));
		atts.addElement(new Attribute("funniness_attr"));
		atts.addElement(new Attribute("star_count"));

		// readability
		atts.addElement(new Attribute("readability_attr"));
		
		//word types 
		atts.addElement(new Attribute("pronounRatio"));
		atts.addElement(new Attribute("adjectiveRatio"));
		atts.addElement(new Attribute("verbRatio"));
		atts.addElement(new Attribute("superlativeRatio"));
		atts.addElement(new Attribute("comparativeRatio"));
		
		// the final attribute is the score (usefulness)
		atts.addElement(new Attribute("usefulness_attr"));
		
		// Classifier attributes
		// length data
		attsClass.addElement(new Attribute("avg_sentence_length"));
		attsClass.addElement(new Attribute("review_length"));
		// meta-data
		attsClass.addElement(new Attribute("coolness_attr"));
		attsClass.addElement(new Attribute("funniness_attr"));
		attsClass.addElement(new Attribute("star_count"));

		// readability
		attsClass.addElement(new Attribute("readability_attr"));
		
		//word types 
		attsClass.addElement(new Attribute("pronounRatio"));
		attsClass.addElement(new Attribute("adjectiveRatio"));
		attsClass.addElement(new Attribute("verbRatio"));
		attsClass.addElement(new Attribute("superlativeRatio"));
		attsClass.addElement(new Attribute("comparativeRatio"));
		
		// the final attribute is the score (usefulness)
		attsClass.addElement(new Attribute("usefulness_attr", classes));		

		//initializes list of personal pronouns
		setUpPronounList();
		
		//creates tagger object
		MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
		
		// create Instance object
		Instances data = new Instances("yelp_dataset", atts, 0);
		Instances dataClass = new Instances("yelp_dataset_class", attsClass, 0);
		
		// read in the restaurant reviews
		try {
			BufferedReader br = new BufferedReader(new FileReader("yelp_academic_dataset_review_restaurants.json"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("feature_scores.arff"));
			BufferedWriter bwUnder = new BufferedWriter(new FileWriter("feature_scores_undersample.arff"));
			JsonParser parser = new JsonParser();
			
			bw.write(data.toString());
			bwUnder.write(data.toString());
			int counter = 0;
			String line = null;
			while((line = br.readLine()) != null && counter < 10000) {
				counter++;
				double[] vals = new double[data.numAttributes()];
				double[] valsClass = new double[dataClass.numAttributes()];
				JsonElement element = parser.parse(line);
				String review = element.getAsJsonObject().get("text").getAsString();
				String tag = tagger.tagString(review); //tagged string
				StringReader sr = new StringReader(review);
				DocumentPreprocessor dp = new DocumentPreprocessor(sr);
				EnglishSyllableCounter sc = new EnglishSyllableCounter();
				
				double numSentences = 0;
				double numWords = 0;
				int totalSyllables = 0;
				
				//TextProcessor tp = new TextProcessor();
				//review = tp.processString( review );
				//ru.add( review );
				
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
						totalSyllables += sc.countSyllables(word);
					}
					numSentences++;
					numWords += tokenized.size();
				}
				float syllableScore = (float)(206.835 - 1.015*numWords/numSentences - 84.6*totalSyllables/numWords);
				sr.close();
				
				// calculate average sentence length
				vals[0] = numWords/numSentences;
				// word count of entire review;
				vals[1] = numWords;
				
				// votes - useful, cool, funny, stars
				JsonObject votes = (JsonObject) element.getAsJsonObject().get("votes");
				// coolness
				vals[2] = votes.get("cool").getAsDouble();
				// funniness
				vals[3] = votes.get("funny").getAsDouble();
				
				// stars
				vals[4] = element.getAsJsonObject().get("stars").getAsDouble();

				// Christian's section -- compute readability
				vals[5] = syllableScore;
				
				//spencer's section below
				double types[] = wordTypes(tag);
				if(numWords == 0.0)
				{
					numWords = 1.0;
				}
				vals[6] = types[0] / numWords;
				
				vals[7] = types[1] / numWords;
				
				vals[8] = types[2] / numWords;
				
				vals[9] = types[3] / numWords;
				
				vals[10] = types[4] / numWords;
				
				//usefulness
				vals[vals.length-1] = votes.get("useful").getAsDouble();
				
				// calculate average sentence length
				valsClass[0] = numWords/numSentences;
				// word count of entire review;
				valsClass[1] = numWords;
				
				// votes - useful, cool, funny, stars
				// coolness
				valsClass[2] = votes.get("cool").getAsDouble();
				// funniness
				valsClass[3] = votes.get("funny").getAsDouble();
				
				// stars
				valsClass[4] = element.getAsJsonObject().get("stars").getAsDouble();

				// Christian's section -- compute readability
				valsClass[5] = syllableScore;
				
				//spencer's section below
				valsClass[6] = types[0] / numWords;
				
				valsClass[7] = types[1] / numWords;
				
				valsClass[8] = types[2] / numWords;
				
				valsClass[9] = types[3] / numWords;
				
				valsClass[10] = types[4] / numWords;
				
				//usefulness
				if(vals[vals.length-1] == 0) {
					valsClass[valsClass.length-1] = 0;
				}
				else if(vals[vals.length-1] < 3) {
					valsClass[valsClass.length-1] = 1;
				}
				else {
					valsClass[valsClass.length-1] = 2;
				}
				// output instance instance to data
				bw.write((new Instance(1.0, vals).toString()) + "\n");
				bwUnder.write((new Instance(1.0, valsClass).toString()) + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Use Weka on feature_scores.arff to create a language model using Linear Regression
		Instances featureData = new Instances( new BufferedReader( new FileReader("feature_scores.arff")));
		featureData.setClassIndex( featureData.numAttributes()-1); //set on useful votes
		LinearRegression model = new LinearRegression();
		String output= "";
		try{
			model.buildClassifier(featureData);
			output = model + "";
		}
		catch( Exception e){
			System.out.println("Exception while building classifier");
		}
		//Write the model out to a file
		BufferedWriter linRegOut = new BufferedWriter(new FileWriter("results.txt"));
		linRegOut.write( model + "" );
		linRegOut.flush();
		linRegOut.close();
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
		double adjective = 0; 
		double pronoun = 0;
		double verb = 0;
		double superlative = 0;
		double comparative = 0;
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
			
		types[0] = pronoun;
		types[1] = adjective;
		types[2] = verb;
		types[3] = superlative;
		types[4] = comparative;
			
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