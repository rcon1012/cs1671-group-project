import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;


public class UnigramExtractor {
	public static void main(String[] args) {
		// eliminate most common words
		HashSet<String> commonWords = new HashSet<String>(); 
		try {
			BufferedReader br = new BufferedReader(new FileReader("most_common_words.txt"));
			String line = null;
			while((line = br.readLine()) != null) {
				commonWords.add(line.replace(" ", "").replace("\t", ""));
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("yelp_academic_dataset_review_restaurants_undersample.json"));

			JsonParser parser = new JsonParser();
			
			String line = null;
			for(int j = 0; j < 10; j++) {
				BufferedWriter bw = new BufferedWriter(new FileWriter("unigrams" + j + ".arff"));
				
				FastVector atts = new FastVector();
				atts.addElement(new Attribute("text", (FastVector) null));
				atts.addElement(new Attribute("usefulness"));
				Instances data = new Instances("my_data", atts, 0);
				int count = 0;
				System.out.println("done unigrams " + j);
				while(((line = br.readLine()) != null) && count++ < 100000) {
					double vals[] = new double[data.numAttributes()];
					JsonElement element = parser.parse(line);
					String review = element.getAsJsonObject().get("text").getAsString();
					DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(review));
					ArrayList<String> tokenized = null;
					for (List<HasWord> sentence : dp) {
						tokenized = new ArrayList<String>(); 
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
							word = word.toLowerCase();
							if(!commonWords.contains(word)) {
								tokenized.add(word);
							}
						}
					}
					review = "";
					for(String word : tokenized) {
						review += word + " ";
					}
					JsonObject votes = (JsonObject) element.getAsJsonObject().get("votes");
					double useful = votes.get("useful").getAsDouble();
					vals[0] = data.attribute(0).addStringValue(review);
					vals[1] = useful;
					data.add(new Instance(1.0, vals));
				}
				bw.write(data.toString());
				bw.close();
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
