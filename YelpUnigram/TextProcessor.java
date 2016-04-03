import java.io.*;
import java.util.*;

public class TextProcessor {
	//Takes a string and then processes it so that it only has words, and not
	//special characters or anything like that.
	BufferedWriter out;
	
	public TextProcessor(){
		
	}
	public TextProcessor( String fileName ) throws IOException{
		//process( fileName );
	}
	
	public String processString( String s ) throws IOException{
		
		s = s.replaceAll("\n"," ");
		String [] lines = s.split(" ");
		//System.out.println("\ncoming into   tp"+s);
		String ret = "";
		for( int i = 0; i < lines.length; i++ ){
			String word = remove( lines[i] );
			if( word.length() == 0 ){
				continue;
			}
			char last = 'a';
			if( word.length() >1 ){
				last =  word.charAt(word.length()-1);
			}
			if( last=='!'||last=='?' || (last=='.' && isPeriod(word)) ){
				word = word.substring(0,word.length()-1);
			}
			if( word.length() == 0 ){ continue; }
			char first = word.charAt(0);
			if( (first<65) || ( first>90 && first<97 ) || (first>122) ){
				continue;
			}
			for( int j = 0; j<word.length(); j++ ){
				if( word.charAt(j) == '\n' ){
					continue; }
			}
			ret = ret + word + " ";
		}
		//System.out.println("\ncoming out of tp"+ret);
		return ret.toLowerCase();
	}
	private boolean isPeriod( String s ){
		//determine if the string ending in a period is the ending
		//of a sentence or not
		ArrayList<String> punct = new ArrayList<String>();
		punct.add("mr.");
		punct.add("mrs.");
		punct.add("dr.");
		punct.add("etc");
		punct.add("mz.");
		punct.add("ms.");
		punct.add("i.e.");
		punct.add("a.d.");
		punct.add("b.c.");
		punct.add("d.c.");
		punct.add("est.");
		punct.add("ft.");
		punct.add("gen.");
		punct.add("inc.");
		punct.add("jan.");
		punct.add("feb.");
		punct.add("mar.");
		punct.add("apr.");
		punct.add("may.");
		punct.add("jun.");
		punct.add("jul.");
		punct.add("aug.");
		punct.add("sept.");
		punct.add("sep.");
		punct.add("oct.");
		punct.add("nov.");
		punct.add("dec.");
		punct.add("mt.");
		punct.add("rev.");
		punct.add("sgt.");
		punct.add("univ.");
		punct.add("vol.");
		punct.add("vs.");
		punct.add("yd.");
		punct.add("jr.");
		punct.add("sr.");
		punct.add("min.");
		punct.add("m.");
		punct.add("a.m.");
		punct.add("p.m.");
		punct.add("ave.");
		punct.add("st.");
		punct.add("rd.");
		punct.add("pvt.");
		punct.add("blvd.");
		
		if(	punct.contains( s.toLowerCase()) ){
			return false;
		}
		return true;
	}
	private String remove( String s ){
		//remove unnecessary punctuation
		ArrayList<String> punct = new ArrayList<String>();
		punct.add(",");
		punct.add("\\");
		punct.add("\"");
		punct.add(":");
		punct.add(";");
		punct.add("<");
		punct.add(">");
		punct.add("{");
		punct.add("}");
		punct.add("(");
		punct.add(")");
		punct.add("+");
		punct.add("=");
		punct.add("$");
		punct.add("%");
		punct.add("/");
		punct.add("#");
		punct.add("^");
		punct.add("*");
		punct.add("|");
		punct.add("-");
		
		String ret = "";
		char[] ar = s.toCharArray();
		for( int i = 0 ; i < ar.length ; i++){
			/*if( punct.contains(ar[i]+"") == false ){
				ret = ret + ar[i];
			}*/
			if( ar[i]<65 || (ar[i]>90 && ar[i]<97) || ar[i]>122){
			}
			else{ ret = ret+ar[i]; }
		}
		return ret;
	}
	
}