// You'll need to increase your heap size to run this.
// java -Xmx4g RandomizeReviews

import java.util.*;
import java.io.*;

public class RandomizeReviews
{
	final static int TOTAL_REVIEWS = 1363242;
	
	public static void main(String[] args) throws IOException
	{
		Scanner sc = new Scanner(new File("yelp_academic_dataset_review_restaurants.json"));
		FileWriter out = new FileWriter("review_restaurants_randomized.json");
		String[] reviews = new String[TOTAL_REVIEWS];
		
		int ind = 0;
		while (sc.hasNextLine())
		{
			if (ind % 10000 == 0) System.out.println(ind);
			String line = sc.nextLine();
			reviews[ind] = line;
			ind++;
		}
		
		System.out.println("All reviews stored in array.");
		System.out.println("Writing randomized reviews to file...");
		
		List<Integer> allIndices = new ArrayList<Integer>(TOTAL_REVIEWS);
		for (int i=0; i<TOTAL_REVIEWS; i++)
		{
			allIndices.add(i);
		}
		
		Random r = new Random();
		Collections.shuffle(allIndices, r);
		
		int counter = 0;
		for ( Integer i : allIndices )
		{
			if (counter % 10000 == 0) System.out.println(counter);
			out.write(reviews[i] + "\n");
			counter++;
		}
		
		out.close();
		sc.close();
	}
}