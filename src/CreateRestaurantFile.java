// Filters out all businesses that aren't restaurants

import com.google.gson.*;
import java.util.*;
import java.io.*;

public class CreateRestaurantFile
{
	public static void main(String[] args) throws IOException
	{
		Scanner sc = new Scanner(new File("yelp_academic_dataset_business.json"));
		FileWriter out = new FileWriter(new File("yelp_academic_dataset_business_restaurants.json"));
		JsonParser parser = new JsonParser();
		int counter = 0;
		while (sc.hasNextLine())
		{
			if (counter % 1000 == 0) System.out.println(counter);
			String line = sc.nextLine();
			JsonElement element = parser.parse(line);
			JsonArray cats = element.getAsJsonObject().getAsJsonArray("categories");
			JsonPrimitive r = new JsonPrimitive("Restaurants");
			if (cats.contains(r))
			{
				out.write(line + "\n");
			}
			counter++;
		}
		sc.close();
		out.close();
	}
}