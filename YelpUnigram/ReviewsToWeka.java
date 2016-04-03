import com.google.gson.*;
import java.util.*;
import java.io.*;
/*
This is the main file to take reviews from the academic_dataset_review.json
and make unigrams for every review.  Each word in the review along with the
usefulness rating of the review is a separte attribute for the arff file 
that is saved to be put into weka
*/
public class ReviewsToWeka
{
	public static void main(String[] args) throws IOException
	{
		Scanner reviews = new Scanner(new File("yelp_academic_dataset_review.json"));
		JsonParser parser = new JsonParser();
		ArrayList<ReviewUnigram> reviewList = new ArrayList<ReviewUnigram>();	//holds every review's unigram and stuff
		TextProcessor tp = new TextProcessor();	//takes out things that arent words
		AllWords everyWord = new AllWords();	//holds the entire vocabulary
		int c = 0;
		boolean stop = false;
		HashMap<Integer,Integer> counts = new HashMap<Integer,Integer>();
		int total = 0;
		while( reviews.hasNext() && stop == false ){	//CHANGE THIS TO USE MORE REVIEWS
			String r = reviews.nextLine();
			
			JsonElement element = parser.parse(r);
			String text = element.getAsJsonObject().get("text").getAsString();
			String use = element.getAsJsonObject().get("votes").getAsJsonObject().get("useful").getAsString();
			
			//if( Integer.parseInt(use) < 2 ){ c--; continue; }	//gets rid of reviews with ratings under 1
			Integer useful = Integer.parseInt( use );
			if( total > 10000 ){break;}
			if( counts.containsKey(useful) == false ){
				counts.put( useful, 0 );
			}
			Integer numUse = counts.get( useful );
			if( numUse > 30 ){ continue; }
			counts.put( useful, numUse+1 );
			total++;
			
			ReviewUnigram ru = new ReviewUnigram();
			text = tp.processString( text );
			ru.add( text );
			ru.setUseful( Integer.parseInt(use) );
			
			everyWord.addAll(text);
			
			reviewList.add( ru );
		}
		
		//use the everyWord data structure to make sure every review has a unigram entry for every word
		for( int i = 0; i < reviewList.size() ; i++ ){
			ReviewUnigram ru = reviewList.get(i);
			ru.fill( everyWord );
		}
		
		//Make the ARFF file
		File file = new File( "ReviewUnigramData.arff" );
		if( !file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile() );
		BufferedWriter out = new BufferedWriter( fw);
		
		out.write(	"@RELATION ReviewUnigramData\n\n");
		out.write(	"@ATTRIBUTE \"usefulness\"\tNUMERIC\n");
		
		//insert all the attributes (every word in the unigrams)
		HashSet<String> topWords = pickAttributes();
		String [] vocab = everyWord.getWords();
		ArrayList<String> attributes = new ArrayList<String>();
		for( int i = 0; i < vocab.length; i++ ){
			String attName = vocab[i];
			if( topWords.contains( attName ) == false ){
				continue;
			}
			if( attributes.contains( attName ) == false ){
				attributes.add( attName );
			}
			char f = attName.charAt(0);
			if( f=='0'||f=='1'||f=='2'||f=='3'||f=='4'||f=='5'||f=='6'||f=='7'||f=='8'||f=='9'){
				attName = "N"+attName;
			}
			out.write(	"@ATTRIBUTE \""+attName+"\"\tNUMERIC\n");
			out.flush();
		}
		
		out.write(	"\n@DATA\n");
		//go through every review, and every word in the reviews unigram, then add the 
		//log probability of each word and the usefulness rating to the data portion of the arff
		for( int i = 0; i < reviewList.size() ; i++){
			ReviewUnigram curr = reviewList.get(i);
			out.write(curr.useful+",");
			for( int j = 0; j < attributes.size(); j++ ){
				out.write( curr.getLogProb(attributes.get(j)) + ""  );
				if( j != attributes.size()-1 ){
					out.write(",");
				}
			}
			out.write("\n");
			out.flush();
		}
		out.flush();
		out.close();
		
		
	}	
	public static HashSet<String> pickAttributes () throws IOException{
			Scanner results = new Scanner(new File("WekaResults.txt"));
			/*for( int i = 0; i < 16; i++ ){
				results.nextLine();
			}*/
			ArrayList<Double> ar = new ArrayList<Double>();
			HashMap<String, Double> weights = new HashMap<String,Double>();
			while( results.hasNext() ){
				String line = results.nextLine();
				line = line.replaceAll(" ","");
				line = line.replaceAll("\\+","!");
				line = line.replaceAll("\\*","!");
				String [] tokens = line.split("!");
				for( int j = 0; j < tokens.length; j++ ){
					//System.out.println(j+":"+tokens[j]);
				}
				weights.put( tokens[1], Double.parseDouble(tokens[0]) );
				double d = Double.parseDouble(tokens[0]);
				if( d < 0 ){ d = d*-1.0; }
				ar.add( Double.valueOf(d) );
			}
			Collections.sort( ar );
			ArrayList<Double> top = new ArrayList<Double>();
			for( int i = ar.size() - 200; i < ar.size() ; i++ ){
				top.add( ar.get(i) );
			}
			HashSet<String> ret = new HashSet<String>();
			Set<String> set = weights.keySet();
			Object[] words = set.toArray();
			//String s = (String)ar[i];
			for( int i = 0; i < words.length; i++ ){
				String s = (String)words[i];
				Double val = weights.get( s );
				if( top.contains( val ) ){
					ret.add( s );
				}
			}
			return ret;
		}
}