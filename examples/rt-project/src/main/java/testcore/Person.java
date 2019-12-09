package testcore;

public class Person {

	int age;

	public int run(boolean then) {

		if (then) {
			return 0;
		} else {
			return 1;
		}

	}

	public int run() {
		run(true);
		run(false);
		return 2;
	}
}
