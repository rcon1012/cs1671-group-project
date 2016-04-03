import java.util.*;
import java.io.*;
import java.util.List;

import com.google.gson.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

//import java.text.BreakIterator;

public class Readability
{
	public static void main(String[] args) throws IOException
	{
		Scanner sc = new Scanner(new File("yelp_academic_dataset_review_restaurants.json"));
		FileWriter out = new FileWriter("review_readability.txt");
		JsonParser parser = new JsonParser();
		
		int processed = 0;
		//while (sc.hasNextLine())
		{
			if (processed % 10000 == 0) System.out.println(processed);
			String line = sc.nextLine();
			processed++;
			JsonElement element = parser.parse(line);
			String content = element.getAsJsonObject().get("text").getAsString();
			//System.out.println(content);
			
			StringReader sr = new StringReader(content);
			DocumentPreprocessor dp = new DocumentPreprocessor(sr);
			System.out.println(readability(dp));
			out.write(readability(dp) + "\n");
		}
		out.close();
	}
	
	public static float readability(DocumentPreprocessor doc)
	{
		EnglishSyllableCounter sc = new EnglishSyllableCounter();
		
		int totalSents = 0;
		int totalWords = 0;
		int totalSyllables = 0;
		
		for (List<HasWord> sentence : doc)
		{
			totalSents++;
			for (HasWord w : sentence)
			{
				String s = w.word();
				//System.out.println(s);	
				// Throw out any word that isn't just letters
				if (s.matches("[a-zA-z]+"))
				{
					totalWords++;
					int sylCount = sc.countSyllables(s);
					//System.out.println("Syllables: " + sylCount);
					totalSyllables += sylCount;	
				}	
				//else System.out.println("not letters");
			}
			//System.out.println();
		}
		
/* 		System.out.println("sentences: " + totalSents);
		System.out.println("words: " + totalWords);
		System.out.println("syllables: " + totalSyllables); */
		return (float)(206.835 - 1.015*totalWords/totalSents - 84.6*totalSyllables/totalWords);
	}
}