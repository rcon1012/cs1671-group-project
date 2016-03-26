/*
Using yelp_academic_dataset_business_restaurants.json and 
yelp_academic_dataset_review.json, this code generates a new file
only including reviews for restaurants. Make sure those two files
are in your current directory.

This code makes use of Gson, so also be sure to install it and
add it to your classpath.
*/


import com.google.gson.*;
import java.util.*;
import java.io.*;

public class SaveRestaurantReviews
{
	public static void main(String[] args) throws IOException
	{
		Scanner sc_business = new Scanner(new File("yelp_academic_dataset_business_restaurants.json"));
		Scanner sc_review = new Scanner(new File("yelp_academic_dataset_review.json"));
		FileWriter out = new FileWriter(new File("yelp_academic_dataset_review_restaurants.json"));
		JsonParser parser = new JsonParser();
		// Store all of the business IDs of restaurants so that we can 
		// see whether a review was written for a restaurant or for some 
		// other business
		HashSet<String> ids = new HashSet<String>();
		
		while (sc_business.hasNextLine())
		{		
			String line = sc_business.nextLine();
			JsonElement element = parser.parse(line);
			String id = element.getAsJsonObject().get("business_id").getAsString();
			ids.add(id);
		}
		System.out.println("Done creating hash set");
		
		int counter = 0;		
		while (sc_review.hasNextLine())
		{
			if (counter % 10000 == 0) System.out.println(counter);
			String line = sc_review.nextLine();
			JsonElement element = parser.parse(line);
			String id = element.getAsJsonObject().get("business_id").getAsString();
			if (ids.contains(id))
			{
				out.write(line + "\n");
			}
			counter++;
		}
		
		sc_business.close();
		sc_review.close();
	}
}