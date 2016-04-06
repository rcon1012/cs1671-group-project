import com.google.gson.*;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.functions.LinearRegression;
/*
Saves the most useful words, based on unigram data of reviews
into a text file.
*/
public class GetUsefulWords
{
	public static void main(String[] args) throws IOException
	{
		HashSet<String> mostUsefulWords = createUsefulWordsFile();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("BestWords.txt"));
		Object[]words2 = mostUsefulWords.toArray();
		for( int i = 0; i < words2.length; i++ ){
			String s2 = (String)words2[i];
			bw.write(s2+"\n");
			bw.flush();
			//System.out.println( s2+"\tweight= "+ allWordsAndWeights.get(s2) );
		}
		bw.flush();
		bw.close();
	}
	public static HashSet<String> createUsefulWordsFile() throws IOException
	{
		//allWordsAndWeights will hold every word, and the weight that weka gives it
		HashMap<String,Double> allWordsAndWeights = new HashMap<String,Double>();
		
		//startingReview is the review that was left off on and will start back up again on
		MyNum startingReview = new MyNum( 0 );
		
		//call getUsefulWords() this many times and create a hashmap with the words and weights
		//corresponding to reviews from the json
		for( int i = 0; i < 4; i++ ){
			
			//usefulWords is the words and weights of a chunk of the reviews
			HashMap<String,Double> usefulWords = getUsefulWords( startingReview );
			System.out.println("startingReview = "+startingReview.num);
			
			//go through each word in usefulWords and add it to allWordsAndWeights.
			Set<String> set = usefulWords.keySet();
			Object[] words = set.toArray();
			for( int j = 0; j < words.length ; j++ ){
				String s = (String)words[j];
				if( allWordsAndWeights.containsKey( s ) ){
					//average them if it is already in there
					allWordsAndWeights.put( s, (allWordsAndWeights.get(s)+usefulWords.get(s))/2 );
				}
				else{
					allWordsAndWeights.put( s, usefulWords.get(s) );
				}
			}
		}
		
		
		//use the arraylist weights to store all the weights, this way we can get
		//the largest (absolute value) weights of words
		ArrayList<Double> weights = new ArrayList<Double>();
		Set<String> tmp = allWordsAndWeights.keySet();
		Object[] temp2 = tmp.toArray();
		for( int i = 0; i < temp2.length ; i++){
			String tempStr = (String)temp2[i];
			Double d = allWordsAndWeights.get(tempStr);
			if( d < 0 ){ d = d*-1.0; }
			weights.add( Double.valueOf(d) );
		}
		
		Collections.sort( weights );	//sort them in descending order
		
		//top is the list of the largest weights according to weka
		ArrayList<Double> top = new ArrayList<Double>();
		
		//add the top weights
		for( int i = weights.size() - 200; i < weights.size() ; i++ ){
			//weights is sorted according to descending order of magnitude of the weight
			top.add( weights.get(i) );
		}
		
		//ret is the hashset of the most weighted words that will be returned
		HashSet<String> ret = new HashSet<String>();
		Set<String> set2 = allWordsAndWeights.keySet();
		Object[] words2 = set2.toArray();
		//go through every word in allWordsAndWeights. if a word has one of the top weights,
		//add it to the return hashset
		for( int i = 0; i < words2.length; i++ ){
			String s = (String)words2[i];
			Double val = allWordsAndWeights.get( s );
			if( top.contains( val ) ){
				ret.add( s );
			}
		}
		
		return ret;
	}
	public static HashMap<String,Double> getUsefulWords(MyNum startingReview) throws IOException
	{
		Scanner reviews = new Scanner(new File("yelp_academic_dataset_review.json"));
		JsonParser parser = new JsonParser();
		ArrayList<ReviewUnigram> reviewList = new ArrayList<ReviewUnigram>();	//holds every review's unigram and stuff
		TextProcessor tp = new TextProcessor();	//takes out things that arent words
		AllWords everyWord = new AllWords();	//holds the entire vocabulary
		int c = 0;
		HashMap<Integer,Integer> counts = new HashMap<Integer,Integer>();
		int total = 0;
		
		long begin = System.nanoTime();
		long end = 0;
		
		//Determine which reviews are good to find the most useful words and
		//add them to a list of unigrams for each review
		for( int i = 0; i < startingReview.num ; i++ ){
			String skipLine = reviews.nextLine();
		}
		while( reviews.hasNext() ){	
			startingReview.increment();
			String r = reviews.nextLine();
			
			JsonElement element = parser.parse(r);
			String text = element.getAsJsonObject().get("text").getAsString();
			String use = element.getAsJsonObject().get("votes").getAsJsonObject().get("useful").getAsString();
			String date = element.getAsJsonObject().get("date").getAsString();
			
			//only use reviews created before 2015
			int year = Integer.parseInt( date.substring(0,4) );
			if( year >=2015 ){
				continue;
			}
			
			//Don't use reviews that are super short
			if( text.length() < 90 ){
				continue;
			}
			
			//Don't use too many reviews, weka will take too long
			if( total > 3){
				break;
			}
			
			//Use a wide variety of reviews (regarding usefulness)
			Integer useful = Integer.parseInt( use );
			if( counts.containsKey(useful) == false ){
				counts.put( useful, 0 );
			}
			Integer numUse = counts.get( useful );
			if( numUse > 3 ){ continue; }
			counts.put( useful, numUse+1 );
			total++;
			
			//add the review to the list of reviews to be used
			ReviewUnigram ru = new ReviewUnigram();
			text = tp.processString( text );
			ru.add( text );
			ru.setUseful( Integer.parseInt(use) );
			
			//add words in the unigram to a structure that'll hold every word seen
			everyWord.addAll(text);
			
			reviewList.add( ru );
		}
		
		end = System.nanoTime();
		System.out.println("getting review took: "+(end-begin)/1000000000 );
		begin = end;
		
		//use the everyWord data structure to make sure every review has a unigram entry for every word
		for( int i = 0; i < reviewList.size() ; i++ ){
			ReviewUnigram ru = reviewList.get(i);
			ru.fill( everyWord );
		}
		
		//Make the ARFF file
		File file = new File( "AllWordsInReviews.arff" );
		if( !file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile() );
		BufferedWriter out = new BufferedWriter( fw);
		
		out.write(	"@RELATION ReviewUnigramData\n\n");
		
		
		//insert all the attributes (every word in the unigrams)
		//HashSet<String> topWords = pickAttributes();
		String [] vocab = everyWord.getWords();
		//ArrayList<String> attributes = new ArrayList<String>();
		for( int i = 0; i < vocab.length; i++ ){
			String attName = vocab[i];
			out.write(	"@ATTRIBUTE \""+attName+"\"\tNUMERIC\n");
			out.flush();
		}
		out.write(	"@ATTRIBUTE \"usefulness\"\tNUMERIC\n");
		
		out.write(	"\n@DATA\n");
		//go through every review, and every word in the reviews unigram, then add the 
		//log probability of each word and the usefulness rating to the data portion of the arff
		for( int i = 0; i < reviewList.size() ; i++){
			ReviewUnigram curr = reviewList.get(i);
			for( int j = 0; j < vocab.length; j++ ){
				out.write( curr.getLogProb( vocab[j] ) + "" );
				//if( j != vocab.length-1 ){
					out.write(",");
				//}
			}
			out.write(curr.useful+"");
			out.write("\n");
			out.flush();
		}
		out.flush();
		out.close();
		
		end = System.nanoTime();
		System.out.println("making .arrff took: "+(end-begin)/1000000000 );
		begin = end;
		
		Instances data = new Instances( new BufferedReader( new FileReader("AllWordsInReviews.arff")));
		data.setClassIndex( data.numAttributes()-1);
		//build model
		LinearRegression model = new LinearRegression();
		String output= "";
		try{
			model.buildClassifier(data);
			//System.out.println(model);
			output = model + "";
			//Instance test = data.lastInstance();
			//double num = model.classifyInstance( test );
			//System.out.println("testing: "+num);
		}
		catch( Exception e){
			System.out.println("exception");
		}
		
		end = System.nanoTime();
		System.out.println("building linear regression took: "+(end-begin)/1000000000 );
		begin = end;
		
		String[] weights = output.split("\n");
		HashMap<String,Double> ret = new HashMap<String,Double>();
		for( int i = 5; i < weights.length - 1; i++ ){
			String line = weights[i];
			line = line.replaceAll(" ","");
			line = line.replaceAll("\\+","!");
			line = line.replaceAll("\\*","!");
			String [] tokens = line.split("!");
			ret.put( tokens[1], Double.parseDouble( tokens[0] ) );
		}
		
		return (ret);
	}
	private static class MyNum
	{
		public int num;
		public MyNum( int n ){
			num = n;
		}
		public void increment(){
			num++;
		}
	}
}