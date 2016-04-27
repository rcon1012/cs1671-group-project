
public class Score {
		public double score;
		public String word;
		
		public Score(double score, String word) {
			this.score = score;
			this.word = word;
		}
		
		public int compareTo(Score s) {
			if(this.score - s.score > 0) {
				return 1;
			}
			else {
				return -1;
			}
		}
		
		public String toString() {
			return this.word + " " + this.score;
		}
}

