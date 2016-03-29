/*
This code makes use of Gson and Stanford NLP part of speech tagger basic, so also be sure to install it and
add it to your classpath.
*/


import com.google.gson.*;
import java.util.*;
import java.io.*;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class WordTypes
{
	public static HashSet<String> goodPronouns = new HashSet<String>();
	public static void main(String[] args) throws IOException
	{
		Scanner sc_resturant_review = new Scanner(new File("yelp_academic_dataset_review_restaurants.json")); //make sure this is the right file 
		MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
		FileWriter out = new FileWriter(new File("Word Type.arff"));
		JsonParser parser = new JsonParser();
		int counter = 0;

		out.write("%Title: Types of Words\n@RELATION wordTypes\n");
		out.write("@ATTRIBUTE pronounRatio NUMUERIC\n");
		out.write("@ATTRIBUTE adjectiveRatio NUMERIC\n");
		out.write("@ATTRIBUTE verbRatio NUMERIC\n");
		out.write("@ATTRIBUTE superlativeRatio NUMERIC\n");
		out.write("@ATTRIBUTE comparativeRatio NUMERIC\n");
		out.write("@DATA\n");
		
		setUpPronounList();
		
		while (sc_resturant_review.hasNextLine() && counter < 10)
		{		
			JsonElement element = parser.parse(sc_resturant_review.nextLine());
			String review = element.getAsJsonObject().get("text").getAsString();
			JsonObject votes = (JsonObject) element.getAsJsonObject().get("votes");
			int useful = votes.get("useful").getAsInt();
			
			String tag = tagger.tagString(review);
			int length = 0;
			int adjective = 0; 
			int pronoun = 0;
			int verb = 0;
			int superlative = 0;
			int comparative = 0;
			String[] words = tag.split(" ");
			for(int i = 0; i < words.length; i++)
			{
				String[] splitter = words[i].split("_");
				if(splitter.length != 2)
				{
					continue;
				}
				
				//condition this so punctuation does not count
				length++;
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
			
			double pronounRatio = (double)pronoun / (double)length;
			double adjectiveRatio = (double)adjective / (double)length;
			double verbRatio = (double)verb / (double)length;
			double superlativeRatio = (double)superlative / (double)adjective;
			double comparativeRatio = (double)comparative / (double)adjective;
			counter++;
			String line = pronounRatio + "," + adjectiveRatio + "," + verbRatio + "," + superlativeRatio + "," + comparativeRatio + "\n";
			//String line = "Review " + counter + " has usefulness " + useful +  " Length: " + length + " pronoun ratio: " + pronounRatio + " adjectiveRatio: " + adjectiveRatio + " verbRatio: " + verbRatio;
			out.write(line + "\n");
		}
		
		sc_resturant_review.close();
		out.close();
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