package com.example.jarvis;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Memory {
	public static final int MAX_STR_LENGTH = 50;
	private static final Double UPDATE_PERCENTAGE = .1;
	static Map<String, Memory> allMemories = new HashMap<String, Memory>();
	String value;
	// boolean talkable, executable, searchable;
	Map<String, Double> edges = new HashMap<String, Double>();

	public Memory(String v) {
		value = v;
		// TODO Auto-generated constructor stub
	}

	public static Memory getMemory(String v) {
		if (allMemories.containsKey(v)) {
			return allMemories.get(v);
		}
		Memory ret = new Memory(v);
		allMemories.put(v, ret);
		return ret;
	}

	public void connectToMemory(String v, Double c) {
		String words[] = v.split("\\W+");
		for (int i = 0; i < words.length; i++) {
			
			if (edges.containsKey(words[i])) {
				edges.put(words[i], edges.get(words[i]) + (c - i) * UPDATE_PERCENTAGE);
			} else {
				getMemory(words[i]);
				edges.put(words[i], (c - i));
			}
			
		}
		
	}

	public static void saveMemory(String Statment) {
		Statment = Statment.toLowerCase();
		String words[] = Statment.split("\\W+");
		for (int i = 0; i < words.length; i++) {
//			System.out.println(words[i]);
			Memory cur = getMemory(words[i]);
			for (int j = i + 1; j < words.length; j++) {
				cur.connectToMemory(words[j], (double) (MAX_STR_LENGTH - j));
			}
		}
//		System.out.println("=====");
	/*	Collections.reverse(Arrays.asList(words));
		for (int i = 0; i < words.length; i++) {
			Memory cur = getMemory(words[i]);
//			System.out.println(words[i]);
			for (int j = i + 1; j < words.length; j++) {
				cur.connectToMemory(words[j], (double) (MAX_STR_LENGTH - 20 - j));
			}
		}*/
		
	}

	public static String[] getMostCommonMemories(String Statment) {
		Statment = Statment.toLowerCase();
		// System.out.println("batee5");
		String words[] = Statment.split("\\W+");
		Map<String, Double> mems = new HashMap<String, Double>();
		for (int i = 0; i < words.length; i++) {
			Map<String, Double> ed = getMemory(words[i]).edges;
			for (Map.Entry<String, Double> entry : ed.entrySet()) {
				if (mems.containsKey(entry.getKey())) {
					// System.out.println(entry.getKey() +
					// entry.getValue().toString());
					mems.put(entry.getKey(),
							mems.get(entry.getKey()) + entry.getValue());
				} else
					mems.put(entry.getKey(), entry.getValue());
			}
		}
		RankedMemory rms[] = new RankedMemory[mems.size()];
		int ind = 0;
		for (Map.Entry<String, Double> entry : mems.entrySet()) {
			rms[ind++] = new RankedMemory(entry.getKey(), entry.getValue());
		}
		Arrays.sort(rms, new Comparator<RankedMemory>() {

			@Override
			public int compare(RankedMemory o1, RankedMemory o2) {
				// TODO Auto-generated method stub
				return (int) (o2.rank - o1.rank);
			}
		});
		String ret[] = new String[rms.length];
		for (int i = 0; i < rms.length; i++) {
			ret[i] = rms[i].memory;
			// System.out.println(rms[i].memory + " " + rms[i].rank.toString());
		}
		System.out.println("=============");
		return ret;
	}

	public static String composeStatment(String[] arr) {
		if (arr.length == 0)
			return "";
		String res = arr[0];
		Memory cur = getMemory(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			if (cur.edges.containsKey(arr[i])) {
				res = res + " " + arr[i];
				cur = getMemory(arr[i]);
			} else
				return res;
		}
		return res;
	}
	// public RankedMemory[] getRelatedMemories()
	// {
	// RankedMemory[] ret = new RankedMemory[edges.size()];
	// int ind = 0;
	// for (Map.Entry<String, Double> entry : edges.entrySet()) {
	// ret[ind ++] = new RankedMemory(entry.getKey(), entry.getValue());
	// }
	// Arrays.sort(ret, new Comparator<RankedMemory>() {
	//
	// @Override
	// public int compare(RankedMemory o1, RankedMemory o2) {
	// // TODO Auto-generated method stub
	// return o2.rank - o1.rank;
	// }
	// });
	// return ret;
	// }
}
