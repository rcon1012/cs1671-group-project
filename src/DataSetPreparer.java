/*
Note:
You'll need to increase your heap size to run this.
	java -Xmx4g DataSetPreparer


CLASSES THIS CLASS IS BASED ON

CreateRestaurantFile
- input: yelp_academic_dataset_business.json
- output: yelp_academic_dataset_business_restaurants.json

RandomizeReviews
- input: yelp_academic_dataset_review_restaurants.json
- output: review_restaurants_randomized.json

SaveRestaurantReviews
- input: yelp_academic_dataset_business_restaurants.json
- input: yelp_academic_dataset_review.json
- output: yelp_academic_dataset_review_restaurants.json


THIS CLASS

DataSetPreparer
- input: yelp_academic_dataset_business.json
- input: yelp_academic_dataset_review.json
- output: yelp_academic_dataset_business_restaurants.json
- output: yelp_academic_dataset_review_restaurants.java
	- In randomized order, with the most recent reviews thrown out
*/

import com.google.gson.*;

import java.util.*;
import java.io.*;
import java.text.*;

public class DataSetPreparer
{
	final static int TOTAL_BUSINESSES = 77445;
	final static int TOTAL_RESTAURANT_BUSINESSES = 25071;
	final static int TOTAL_REVIEWS = 2225213;
	
	// This number excludes all reviews written after (but not on)
	// Nov 24, 2015
	final static int TOTAL_RESTAURANT_REVIEWS = 1334246;
	
	// Total number of restaurant reviews (including
	// the most recent: 1363242
	
	public static void main(String[] args) throws IOException, ParseException
	{		
		Scanner sc_business = new Scanner(
			new File("yelp_academic_dataset_business.json"));
		Scanner sc_review = new Scanner(
			new File("yelp_academic_dataset_review.json"));
		FileWriter out_business = new FileWriter(
			new File("yelp_academic_dataset_business_restaurants.json"));
		FileWriter out_review = new FileWriter(
			new File("yelp_academic_dataset_review_restaurants.json"));
		FileWriter out_review_undersample = new FileWriter(
				new File("yelp_academic_dataset_review_restaurants_undersample.json"));
		
		// Store all of the business IDs of restaurants so that we can 
		// see whether a review was written for a restaurant or for some 
		// other business
		HashSet<String> restaurant_ids = new HashSet<String>();
		JsonParser parser = new JsonParser();
		
		System.out.println("Filtering out non-restaurant business data...");
		int counter = 0;
		while ( sc_business.hasNextLine() )
		//for (int i=0; i<10000; i++)
		{
			if (counter % 1000 == 0) System.out.println( 
				( counter*100/TOTAL_BUSINESSES ) + "%");;
			String line = sc_business.nextLine();
			//JsonElement element = parser.parse(line);
			//JsonArray cats = element.getAsJsonObject().getAsJsonArray("categories");
			//String id = element.getAsJsonObject().get("business_id").getAsString();
			JsonObject obj = parser.parse(line).getAsJsonObject();
			JsonArray cats = obj.getAsJsonArray("categories");
			JsonPrimitive r = new JsonPrimitive("Restaurants");
			if (cats.contains(r))
			{
				String id = obj.get("business_id").getAsString();
				restaurant_ids.add(id);
				out_business.write(line + "\n");
			}
			counter++;
		}
		sc_business.close();
		out_business.close();
		System.out.println("Done filtering out non-restaurant business data.");
		
		// Will store all of the restaurant reviews in memory, so that
		// we can write them out in randomized order. This is the most
		// memory-intensive part of the program.
		String[] restaurant_reviews = new String[TOTAL_RESTAURANT_REVIEWS];
		
		System.out.println("Filtering out non-restaurant reviews...");
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		// The most recent review was posted on Dec 24, 2015
		// The cutoff is one month before: any reviews posted
		// after Nov 24, 2015 will be excluded. (Reviews posted exactly on
		// Nov 24, 2015 are included.)
		Date cutoff = df.parse("2015-11-24");
		
		counter = 0;
		int ind = 0;
		while (sc_review.hasNextLine())
		//for (int i=0; i<100000; i++)
		{
			if (counter % 10000 == 0) System.out.println( 
				( counter*100/TOTAL_REVIEWS ) + "%");
			String line = sc_review.nextLine();
			JsonObject obj = parser.parse(line).getAsJsonObject();
			String id = obj.get("business_id").getAsString();
			Date currDate = df.parse(obj.get("date").getAsString());
			if (restaurant_ids.contains(id) && !cutoff.before(currDate)) 
			{
				restaurant_reviews[ind] = line;
				ind++;
			}
			counter++;
		}
		sc_review.close();
		
		System.out.println("Done filtering out non-restaurant reviews.");
		System.out.println("Generating random ordering...");
		
		
		// This list contains the indices of reviews in restaurant_reviews.
		// After shuffling the list we can use it to access reviews from the
		// array in random order.
		List<Integer> indices = new ArrayList<Integer>(TOTAL_RESTAURANT_REVIEWS);
		for (int i=0; i<TOTAL_RESTAURANT_REVIEWS; i++)
		{
			indices.add(i);
		}
		
		// I picked the a specific seed here so that every time the code is
		// run we'll end up with the same random permutation of indices.
		Random r = new Random(100);
		Collections.shuffle(indices, r);
		
		System.out.println("Done generating random ordering.");
		System.out.println(
			"Writing restaurant reviews to file in randomized order...");
		
		counter = 0;
		for (Integer i : indices)
		{
			if (counter % 10000 == 0) System.out.println( 
				( counter*100/TOTAL_RESTAURANT_REVIEWS ) + "%");
			out_review.write(restaurant_reviews[i] + "\n");
			JsonElement element = parser.parse(restaurant_reviews[i]);
			JsonObject votes = (JsonObject) element.getAsJsonObject().get("votes");
			double usefulness = votes.get("useful").getAsDouble();
			if(usefulness == 0 && (r.nextInt() % 2) == 0) {
				out_review_undersample.write(restaurant_reviews[i] + "\n");
			}
			counter++;
		}
		out_review.close();
		out_review_undersample.close();
		System.out.println(
			"Done writing restaurant reviews to file in randomized order.");
	}
}