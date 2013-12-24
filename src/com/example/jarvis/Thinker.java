package com.example.jarvis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Thinker {
	public static final String FAILED_TO_UNDERSTAND_REPLY = "Sorry I couldn't understand that, sir";
	public static final String SAVED_MEMORY_REPLY = "ok, sir";

	/*public static void main(String[] args) {
		File f = new File("input.txt");
		try {
			Scanner fin = new Scanner(f);
			String line;
			while(fin.hasNextLine())
			{
				
				line = fin.nextLine();
				System.out.println(line);
				String arr[] = line.split("\\W+");
				for (int i = 0; i < arr.length; i++) {
					System.out.println(arr[i] + "---");
					
				}
				System.out.println(think(line));
				if(line.split("\\W+")[0].equals("Q"))
				{
					line.replace("Q:", "");
					String res[] = Memory.getMostCommonMemories(line);
					System.out.println(Memory.composeStatment(res));
					for (int i = 0; i < res.length; i++) {
						System.out.println(res[i]);
					}
					System.out.println("--------------");
				}
				else
				{
					Memory.saveMemory(line);
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
	public static String think(String statment)
	{
		statment = statment.toLowerCase();
		
		String s = "when i tell you", s2 = "learn that";
//		System.out.println(statment.substring(0, s2.length()));
		if(statment.length() >= s.length() && statment.substring(0, s.length()).equals(s)){
//			System.out.println("hey");
			statment = statment.replace(s, "");
			String[] qa = statment.split("you should");
			if(qa.length != 2)
				return FAILED_TO_UNDERSTAND_REPLY;
			Memory.saveMemory(qa[1]);
			String[]q = qa[0].split("\\W+");
			for (int i = 0; i < q.length; i++) {
				Memory.getMemory(q[i]).connectToMemory(qa[1], (double) Memory.MAX_STR_LENGTH);
			}
			return SAVED_MEMORY_REPLY;
		}
		else if(statment.length() >= s2.length() && statment.substring(0, s2.length()).equals(s2))
		{
//			System.out.println("hey2");
			statment = statment.replace(s2, "");
			Memory.saveMemory(statment);
			return SAVED_MEMORY_REPLY;
		}
		else
		{
			String res = Memory.composeStatment(Memory.getMostCommonMemories(statment));
			if(res != "")
				return res;
			return FAILED_TO_UNDERSTAND_REPLY;
		}
	}
}
