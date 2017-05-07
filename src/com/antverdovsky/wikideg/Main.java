package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		try {
		double a = System.nanoTime();
		Separation s100 =  new Separation("Buzz! Junior: Monster Rumble", "Kayode McKinnon");
		System.out.println(s100.getNumDegrees());
		System.out.println(s100.getPath());
		System.out.println(Separation.getEmbeddedPath(s100.getPath()));	
		
		
		System.out.println(System.nanoTime() - a);	
		
		Separation s101 =  new Separation("Barnet London Borough Council election, 1964", "USS Enterprise (NCC-1701-D)");
		System.out.println(s101.getNumDegrees());
		System.out.println(s101.getPath());
		System.out.println(Separation.getEmbeddedPath(s101.getPath()));
		
			/*
			Separation s3 =  new Separation("Lingua Franca Nova", "United Airlines");
			System.out.println(s3.getNumDegrees());
			System.out.println(s3.getPath());
			System.out.println(Separation.getEmbeddedPath(s3.getPath()));
			
			// expected 0 degrees, Fruit
			Separation s0 =  new Separation("Fruit", "Fruit");
			System.out.println(s0.getNumDegrees());
			System.out.println(s0.getPath());
			System.out.println(Separation.getEmbeddedPath(s0.getPath()));
			
			// expected 1 degree, Obama -> Trump
			Separation s1 =  new Separation("Barack Obama", "Donald Trump");
			System.out.println(s1.getNumDegrees());
			System.out.println(s1.getPath());
			System.out.println(Separation.getEmbeddedPath(s1.getPath()));

			// expected 2 degrees
			Separation s2 =  new Separation("Apple", "Orange (fruit)");
			System.out.println(s2.getNumDegrees());
			System.out.println(s2.getPath());
			System.out.println(Separation.getEmbeddedPath(s2.getPath()));
			
			Separation s4 =  new Separation("Animal", "Coca-Cola");
			System.out.println(s4.getNumDegrees());
			System.out.println(s4.getPath());	
			System.out.println(Separation.getEmbeddedPath(s4.getPath()));
			
			Separation s5 =  new Separation("Sun Chemical", "Bhoot Aaya");
			System.out.println(s5.getNumDegrees());
			System.out.println(s5.getPath());
			System.out.println(Separation.getEmbeddedPath(s5.getPath()));
			
			Separation s7 =  new Separation("Eyjeaux", "Miley (given name)");
			System.out.println(s7.getNumDegrees());
			System.out.println(s7.getPath());
			System.out.println(Separation.getEmbeddedPath(s7.getPath()));
			
			Separation s6 =  new Separation("Banana", "Musa velutina");
			System.out.println(s6.getNumDegrees());
			System.out.println(s6.getPath());
			System.out.println(Separation.getEmbeddedPath(s6.getPath()));
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
