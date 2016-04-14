import java.io.*;
import java.util.*;
import java.math.*;

public class ReviewUnigram {
	
	public Unigram uni;
	public int useful;
	
	public ReviewUnigram(){
		uni = new Unigram();
		useful = -1;
	}
	public void setUseful( int use ){
		useful = use;
	}
	public void add( String s ) throws IOException{
		uni.makeWithString( s );
	}
	public void fill( AllWords a ){
		uni.addAllWords( a );
	}
	public void addOne(String word)
	{
		uni.addOne(word);
	}
	public double getLogProb( String s ){
		return uni.getProb( s );
	}
}