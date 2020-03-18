package RottenTestsFinder.FakePaperTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RTFRow27Abstract extends AbstractRTestCase2 {

	@Test
	public void test0() {
		int i = 10;
		if (i > 11) {
			this.goodHelper();
		}

	}

	@Test
	public void test1() {
		int i = 10;
		if (i > 11) {
			this.localHelper();
		}

	}

	@Test
	public void test2() {
		int i = 10;
		if (i > 11) {
			this.goodHelperWrapper();
		}

	}

	@Test
	public void test3() {
		int i = 10;
		if (i > 11) {
			RTFRow26Assume.aMethodHelper();
		}

	}

	@Test
	public void test4() {
		int i = 10;
		if (i > 11) {
			RTFRow26Assume.farMethodHelper();
		}

	}

	@Test
	public void test5() {
		this.afarhelperCall();

	}

	@Test
	public void test6() {
		this.aSimpleFarhelperCall();

	}

	public void localHelper() {
		assertTrue(1 > 0);
	}

	public void afarhelperCall() {
		int i = 10;
		if (i > 12) {
			RTFRow26Assume.farMethodHelper();
		}
	}

	public void aSimpleFarhelperCall() {

		RTFRow26Assume.aFarConditionalCall();

	}

}
