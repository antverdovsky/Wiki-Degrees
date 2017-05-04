package com.antverdovsky.wikideg;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {

		try {
			// expected 0 degrees, Fruit
			Separation s0 = Separation.getSeparation("Fruit", "Fruit");
			System.out.println(s0.getNumDegrees());
			System.out.println(s0.getPath());
			
			// expected 1 degree, Obama -> Trump
			Separation s1 = Separation.getSeparation("Barack Obama", "Donald Trump");
			System.out.println(s1.getNumDegrees());
			System.out.println(s1.getPath());

			// expected 2 degrees
			Separation s2 = Separation.getSeparation("Apple", "Orange (fruit)");
			System.out.println(s2.getNumDegrees());
			System.out.println(s2.getPath());
/*
			Separation s3 = findConnection("Lingua Franca Nova", "United Airlines");
			System.out.println(s3.getNumDegrees());
			System.out.println(s3.getPath());
			
			Separation s4 = findConnection("Animal", "Coca-Cola");
			System.out.println(s4.getNumDegrees());
			System.out.println(s4.getPath());		
			
			Separation s5 = Separation.getSeparation("Sun Chemical", "Bhoot Aaya");
			System.out.println(s5.getNumDegrees());
			System.out.println(s5.getPath());
*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
