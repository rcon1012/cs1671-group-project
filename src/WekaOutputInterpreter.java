import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WekaOutputInterpreter {
	
	public static void main(String[] args) {
		HashMap<String, Double> wordScores = new HashMap<String, Double>();
		try {
			for(int i = 0; i < 10; i++) {
				BufferedReader br = new BufferedReader(new FileReader("unigrams" + i + "_results.txt"));
				String line = null;
				while((line = br.readLine()) != null) {
					String regex = "([-]{0,1}0\\.\\d+)";
					Matcher matcher = Pattern.compile(regex).matcher(line);
					Double score = null;
					if(matcher.find()) {
			 			score = Double.valueOf(matcher.group(0));
			 		}
					String word = null;
					int indexBegin = line.indexOf("*");
					int indexEnd = line.indexOf("+");
					word = line.substring(indexBegin + 2, indexEnd-1);
					
					if(!wordScores.containsKey(word)) {
						wordScores.put(word, score);
					}
					else {
						wordScores.put(word, wordScores.get(word) + score);
					}
				}
				br.close();
			}
			ArrayList<Score> allScores = new ArrayList<Score>(); 
			for(String key : wordScores.keySet()) {
				allScores.add(new Score(wordScores.get(key), key));
			}
			for(Score score : allScores) {
				if(score.score < 0) {
					score.score *= -1;
				}
			}
			allScores.sort(new ScoreComparator());
			ArrayList<Score> finalScores = new ArrayList<Score>();
			for(int i = 0; i < 100; i++) {
				finalScores.add(new Score(wordScores.get(allScores.get(i).word), allScores.get(i).word));
			}
			BufferedWriter myBw = new BufferedWriter(new FileWriter("useful_words.txt"));
			for(Score score : finalScores) {
				myBw.write(score.word);
				myBw.newLine();
			}
			myBw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
