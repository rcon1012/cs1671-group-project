import java.io.*;
import java.util.*;
import java.math.*;

public class Unigram {
	
	public HashMap<String,Integer> unigram;
	public int count;
	
	public Unigram(){
		unigram = new HashMap<String,Integer>();
		count = 0;
	}
	
	//Pass in a string and add all the words to the unigram
	public void makeWithString( String review ) throws IOException{
		String[]ar = review.split("\n");
		if( ar.length == 0 ){ return; }
		for( int i = 0; i < ar.length ; i++){
			String line = ar[i];
			if( line.length() == 0 ){ continue; }
			String [] tokens = line.split(" ");
			if( tokens.length == 0 ){ continue; }
			for( int j = 0; j < tokens.length; j++ ){
				String word = tokens[j];
				word = word.toLowerCase();
				if( unigram.containsKey(word) == false ){
					unigram.put(word, 1);
				}
				else {
					unigram.put( word, unigram.get(word)+1 );
				}
				count++;
			}
		}
	}
	
	public void addOne(String word)
	{
		if( unigram.containsKey(word) == false )
		{
			unigram.put(word, 1);
		}
		else 
		{
			unigram.put( word, unigram.get(word)+1 );
		}
	}
	
	
	//Returns the LOG probability of the string passed in.
	public double getProb( String s ){
		double total = Double.parseDouble( count+"" );
		double num = Double.parseDouble(unigram.get( s )+"");
		if( num == 0 ){ return 0; }
		double logTotal = Math.log10(total);
		double logNum = Math.log10(num);
		return logNum-logTotal;
	}
	
	
	public String perplexity(String line ){
		MathContext mc = new MathContext( 128 );
		String [] ar = line.split(" ");
		BigDecimal perplex;
		if( unigram.containsKey(ar[0].toLowerCase() ) ){
			perplex = new BigDecimal( unigram.get(ar[0].toLowerCase() ) , mc );
		}
		else{ perplex = new BigDecimal( 1, mc ); }
		perplex = perplex.divide( new BigDecimal( count , mc ) , mc );
		for( int i = 1 ; i < ar.length ; i++ ){
			BigDecimal num;
			if( unigram.containsKey( ar[i].toLowerCase() )){
				num = new BigDecimal( unigram.get( ar[i].toLowerCase()) , mc );
			}
			else{ num = new BigDecimal( unigram.get("<Unk>") ); }
			num = num.divide( new BigDecimal( count, mc ) , mc );
			perplex = perplex.multiply( num );
		}
		
		BigDecimal one = new BigDecimal(1);
		perplex = one.divide( perplex , mc );
		//(perplexity)^(1/N) == (perp x 10^64)^(1/N) / 10^(1/N)^64
		//Scale is used to make the perplexity into a larger number
		BigDecimal scale = new BigDecimal(10);
		scale = scale.pow(64 , mc);
		perplex = perplex.multiply( scale , mc );
		//Val is the double value of perplexity used to take the root of it
		double val = perplex.doubleValue();
		val = Math.pow( val, (1.0/ar.length) );
		perplex = new BigDecimal( val, mc );
		//div is used to scale the perplexity back down
		double div = Math.pow( 10.0, (1.0/ar.length) );
		scale = new BigDecimal( div, mc );
		scale = scale.pow( 64, mc );
		perplex = perplex.divide( scale, mc );
		return perplex.toString();
		
	}
	public boolean contains( String s ){
		if( unigram.containsKey( s ) ){
			return true;
		}
		else{ return false; }
	}
	public void add( String s ){
		unigram.put( s, 0 );
	}
	public void addAllWords( AllWords all ){
		String [] ar = all.getWords();
		for( int i = 0; i < ar.length ; i++ ){
			String s = ar[i];
			if( this.contains( s ) == false ){
				this.add( s );
			}
		}
	}
	
	public void develop( String fn )throws IOException{
		return;
	}
	public void develop1( String fn )throws IOException{
		return;
	}
	public void view(){
		Set<String> set = unigram.keySet();
		Object[] ar = set.toArray();
		for( int i = 0; i < ar.length ; i++ ){
			String s = (String)ar[i];
			System.out.print(s+":"+unigram.get(s)+"\t");
		}
	}
}