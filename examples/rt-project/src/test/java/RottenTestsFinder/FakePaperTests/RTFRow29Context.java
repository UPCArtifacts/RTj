package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RTFRow29Context extends AbstractRTestCase {

	@Test
	public void test0() {
		helper(true);
	}

	@Test
	public void test1() {
		helper(false);
	}

	public void helper(boolean t) {
		int i = 10;
		if (t) {

			assertTrue(i >= 10);
		}

	}

	public void helper(int i) {

		assertTrue(i >= 0);

	}

	@Test
	public void test2() {

		int i = 0;
		if (i > 0) {
			helper(true);
		}
	}

	@Test
	public void test3() {

		int i = 0;
		if (i > 0) {
			helper(i);
		}
	}

	public void helper2(int o) {
		if (o > 0)
			assertCall(o);
		else
			assertTrue(o == 0);

	}

	public void assertCall(int o) {
		assertTrue(o > 0);
	};

	@Test
	public void test4() {

		int i = 0;
		helper2(i);

	}

	@Test
	public void test5() {

		int i = 1;
		helper2(i);

	}

	@Test
	public void test6() {

		int i = 1;
		helper3(i);

	}

	public void helper3(int o) {
		if (o > 0)
			assertTrue(o > 0);
		else
			assertTrue(o == 0);

	}

	public void helper4(int o) {
		if (o > 0)
			System.out.println();
		else
			assertTrue(o == 0);

	}

	@Test
	public void test7() {

		int i = 1;
		helper4(i);

	}

	@Test
	public void test8() {

		int i = 1;
		helper5(i);

	}

	public void helper5(int o) {
		if (o > 0)
			System.out.println();
		else
			assertCall(1000);

	}
}
