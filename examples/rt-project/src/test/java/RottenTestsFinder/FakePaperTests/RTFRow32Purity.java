package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import testcore.Person;

public class RTFRow32Purity extends AbstractRTestCase {

	@Test
	public void test0() {
		Person p = new Person();
		int v = p.run(true);
		assertTrue(v == 0);
	}

	@Test
	public void test1() {
		Person p = new Person();
		int v = p.run(false);
		assertTrue(v == 1);
	}

	@Test
	public void test2() {
		Person p = new Person();
		int v = p.run();
		assertTrue(v == 2);
	}

	@Test
	public void test3() {
		Person p = new Person();
		int v = p.run();
		assertTrue(v == 2);
	}
}
