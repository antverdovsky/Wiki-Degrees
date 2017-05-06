package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
	static class Downloader implements Runnable {
		ArrayList<String> myTask;
		List<String> writeTo;			// thread safe!
		
		public Downloader(ArrayList<String> task, List<String> writeTo) {
			this.myTask = task;
			this.writeTo = writeTo;
		}
		
		@Override
		public void run() {
			for (String s : myTask) {
				ArrayList<String> linksOf = new ArrayList<String>();
				try {
					linksOf = (new LinksFetcher()).getLinks(s, "");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println(s);
				this.writeTo.addAll(linksOf);
			}
		}
	}
	
	private static ArrayList<String> singleThread() throws IOException {
		ArrayList<String> links = (new LinksFetcher()).getLinks("Apple", "");
		ArrayList<String> ret = new ArrayList<String>();
		
		for (String s : links) {
			ArrayList<String> linksOf = (new LinksFetcher()).getLinks(s, "");
			System.out.println(s);
			ret.addAll(linksOf);
		}
		
		return ret;
	}
	
	private static List<String> multiThread() throws IOException {
		ArrayList<String> links = (new LinksFetcher()).getLinks("Apple", "");
		List<String> ret = Collections.synchronizedList(new ArrayList<String>());
		System.out.println(links);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		int numThreads = 25;
		int perThread = links.size() / numThreads;
		for (int i = 0; i < numThreads; ++i) {
			int fromIndex = i * perThread;
			int toIndex = fromIndex + perThread;
			
			if (toIndex == 0) {
				fromIndex = 0;
				toIndex = links.size();
			}
			
			ArrayList<String> task = new ArrayList<String>(
					links.subList(fromIndex, toIndex));
			
			Thread t = new Thread(new Downloader(task, ret));
			threads.add(t);
			t.start();
		}
		
		for (Thread t : threads)
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return ret;
	}
	
	public static void main(String[] args) {
		int i = 1;
		
		if (i == 0) {
			try {
				double a = System.currentTimeMillis();
				ArrayList<String> single = singleThread();
				
	//			System.out.println(single);
				System.out.println(single.size());
				System.out.println(System.currentTimeMillis() - a);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				double a = System.currentTimeMillis();
				List<String> multi = multiThread();
				
	//			System.out.println(multi);
				System.out.println(multi.size());
				System.out.println(System.currentTimeMillis() - a);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/*
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
}
