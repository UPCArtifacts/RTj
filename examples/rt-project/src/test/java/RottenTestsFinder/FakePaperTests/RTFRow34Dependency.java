package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

public class RTFRow34Dependency extends AbstractRTestCase {

	@Test
	public void test0() {
		JsonObject js = new JsonObject();
		js.addProperty("test", "v1");

		assertEquals("v1", js.get("test").getAsString());
	}

	@Test
	public void test2() throws IOException {
		Writer writer = new FileWriter("yourfile.csv");
		CSVWriter beanToCsv = new CSVWriter(writer);
		beanToCsv.writeNext(new String[] { "t1", "t2" });
		beanToCsv.close();
	}

}
