package rmi.huffman.frequency;

import java.util.concurrent.ExecutionException;


public class FrequencyTableGenerator {
	static String fileName;
	static int countThreads;
	static int granularityLevel;
	static boolean isQuiet;
	static boolean isStatic;	
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		long startTime = System.currentTimeMillis();
		System.out.print("Main thread started.\n");
		isQuiet = false;
		granularityLevel = 1; 
		isStatic = true;
		for (int i=0; i < args.length; i++) {
			
			switch(args[i]) {
				case "-f" : fileName = args[++i]; break;
				case "-t" : countThreads = Integer.parseInt(args[++i]); break;
				case "-q" : isQuiet = true; break;
				case "-g" : granularityLevel = Integer.parseInt(args[++i]); break;
				case "-d" : isStatic = !(args[++i].equals("dynamic")); break;
			}
			
		}
		
		
		System.out.print("Number of threads in current run: " + countThreads + "\nDistribution:");
		if (isStatic) {
			System.out.print(" Static\n");
		} else {
			System.out.print(" Dynamic\n");
		}
		
	
	
		FrequencyTable generatedFrequencyTable = FrequencyTable.generateFrequencyTable(fileName, countThreads, granularityLevel, isStatic, isQuiet);
		
		long endTime = System.currentTimeMillis();
		
		System.out.print("Main thread stopped.\nTotal execution time for current run was  " + (endTime - startTime) + "millis.\n");
		
		
		if (!isQuiet) {
		generatedFrequencyTable.printFrequencyTable();
		}
	}

}
