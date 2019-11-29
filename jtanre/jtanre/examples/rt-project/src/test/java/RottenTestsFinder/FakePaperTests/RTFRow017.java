package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RTFRow017 extends AbstractRTestCase {

	@Test
	public void test0() {
		int a = 0;
		if (false) {
			System.out.println("then branch");
			assertTrue(a < 0);
		} else {
			System.out.println("else branch");
			assertTrue(a >= 0);
		}
	}

	@Test
	public void test1() {
		int a = 0;
		assertTrue(a >= 0);
		if (false) {
			System.out.println("then branch");
			assertTrue(a < 0);
		} else {
			System.out.println("else branch");
		}
	}

	@Test
	public void test2() {
		int a = 0;
		assertTrue(a >= 0);
		if (false) {
			System.out.println("then branch");
			assertTrue(a < 0);
		} else {
			this.goodHelper();
		}
	}

	@Test
	public void test3() {
		int a = 0;
		assertTrue(a >= 0);
		if (false) {
			System.out.println("then branch");
			assertTrue(a < 0);
		} else {
			System.out.println("else branch");
		}
		this.goodHelper();
	}

	@Test
	public void test4() {
		mytesthelper();
	}

	private void mytesthelper() {
		if (false) {
			assertTrue(10 > 8);
		} else {
			assertTrue(10 > 9);
		}

	}
}
