package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import testcore.Person;

public class RTFRow33Override extends AbstractRTestCase {

	@Override
	public void goodHelper() {
		System.out.println("Overriden!!");
		int i = 10;
		assertTrue(i > 0);

	}

	@Test
	public void test0() {
		Person p = new Person();
		// This calls to goodHelper()
		goodHelperWrapper();
	}

}
