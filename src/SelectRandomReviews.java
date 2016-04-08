// Two arguments:
// args[0] is the number of reviews you want to select
// args[1] is the maximum proportion of reviews that 
// 		are allowed to have 0 useful votes (ranges from 0 to 1)

import java.util.*;
import java.io.*;

import com.google.gson.*;

public class SelectRandomReviews
{
	final static int TOTAL_REVIEWS = 1363242;
	
	public static void main(String[] args) throws IOException
	{
		int reviewCount = Integer.parseInt(args[0]);
		int maxZeroes = (int) (Float.parseFloat(args[1]) * reviewCount);
		
		Scanner sc = new Scanner(new File("review_restaurants_randomized.json"));
		FileWriter out = new FileWriter("restaurant_review_subset_" + reviewCount + ".json");
		JsonParser parser = new JsonParser();
		
		int added = 0;
		int zeroes = 0;
		boolean maxZeroesReached = false;
		while (added < reviewCount && sc.hasNextLine())
		{
			if (added % 1000 == 0) System.out.println(added);
			String line = sc.nextLine();
			JsonElement element = parser.parse(line);
			int usefulVotes = element.getAsJsonObject().get("votes").getAsJsonObject().get("useful").getAsInt();
			if ( usefulVotes != 0 || !maxZeroesReached)
			{
				out.write(line + "\n");	
				added++;
				if (usefulVotes == 0)
				{
					zeroes++;
					if (zeroes == maxZeroes)
					{
						maxZeroesReached = true;
						System.out.println("Maximum number of zero-vote reviews reached");
					}
				}
			}
		}
		
		// Reached end of file without finding enough non-zero-vote reviews
		if (added < reviewCount)
		{
			System.out.println("Warning: not enough reviews were found with " +
							   "at least one usefulness vote");
		}
		
		out.close();
		sc.close();
		
	}
}