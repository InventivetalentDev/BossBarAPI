package org.inventivetalent.bossbar.reflection;

public abstract class MathUtil {

	public static int floor(double d1) {
		int i = (int) d1;
		return d1 >= i ? i : i - 1;
	}

	public static int d(float f1) {
		int i = (int) f1;
		return f1 >= i ? i : i - 1;
	}

}
