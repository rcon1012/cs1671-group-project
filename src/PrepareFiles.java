import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class PrepareFiles {
	public static final double REVIEW_RESTAURANTS_NUM_LINES = 1363242;
	public static void main(String[] args) {
		JsonParser parser = new JsonParser();
		int lineNumber = 0;
		double sectionLine = REVIEW_RESTAURANTS_NUM_LINES/(double)3;
		try(BufferedReader br = new BufferedReader(new FileReader("yelp_academic_dataset_review_restaurants.json"))) {
			// create train file
			try(FileWriter fwTrain = new FileWriter(new File("yelp_train_file.json"))) {
				String line = null;
				while(((line = br.readLine()) != null) && (lineNumber%sectionLine) != 0) {
					fwTrain.write(line);
					lineNumber++;
				}
				lineNumber++;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// create dev file
			try(FileWriter fwDev = new FileWriter(new File("yelp_dev_file.json"))) {
				String line = null;
				while(((line = br.readLine()) != null) && (lineNumber%sectionLine) != 0) {
					fwDev.write(line);
					lineNumber++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			// create test file
			try(FileWriter fwTest = new FileWriter(new File("yelp_test_file.json"))) {
				String line = null;
				while((line = br.readLine()) != null) {
					fwTest.write(line);
					lineNumber++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
