import java.io.*;
import java.util.*;
import java.math.*;
/* data structure to hold every word that is in a review training
file. Uses a hashset for quick retrieval of data */
public class AllWords {
	
	public HashSet<String> hs;
	
	public AllWords( ){
		hs = new HashSet<String>();
	}
	
	public boolean add( String s ){
		if( hs.contains( s ) == false ){
			hs.add( s );
			return true;
		}
		else{ return false; }	//already in the hashmap
	}
	public void addAll( String s ){
		String[] ar = s.split(" ");
		for( int i = 0; i < ar.length ; i++){
			this.add( ar[i] );
		}
	}
	public boolean contains( String s ){
		if( hs.contains( s ) ){
			return true;
		}
		else{ return false; }
	}
	public void addUnigram( Unigram u ){
		//add all the words in a unigram
		Set<String> set = u.unigram.keySet();
		Object[] ar = set.toArray();
		for( int i = 0; i < ar.length; i++ ){
			String s = (String)ar[i];
			this.add( s );
		}
	}
	public String[] getWords(){
		//return a string array of all the words in the hashset
		Object[] ar = hs.toArray();
		String[] ret = new String[ar.length];
		for( int i = 0; i < ar.length; i++ ){
			ret[i] = (String)ar[i];
		}
		return ret;
	}
}