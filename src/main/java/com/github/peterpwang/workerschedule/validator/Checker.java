package com.github.peterpwang.workerschedule.validator;

public class Checker {
 
    public static boolean checkEmpty(String input) {
        return (input == null || input.trim().length() == 0);
    }
 
    public static boolean checkEmpty(Integer input) {
        return (input == null);
    }
 
    public static boolean checkSize(String input, int min, int max) {
		int length = 0;
		if (input!=null) length = input.trim().length();
        return (length < min || length > max);
    }
 
    public static boolean checkZeroOrPositive(Integer input) {
        return (input == null || input < 0);
    }
}