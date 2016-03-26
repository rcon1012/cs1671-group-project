import java.io.File;
import java.io.FileWriter;


public class PrepareFiles {
	public static void main(String[] args) {
		// create train file
		try(FileWriter fwTrain = new FileWriter(new File("yelp_train_file.json"))) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// create dev file
		try(FileWriter fwDev = new FileWriter(new File("yelp_dev_file.json"))) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		// create test file
		try(FileWriter fwTest = new FileWriter(new File("yelp_test_file.json"))) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
