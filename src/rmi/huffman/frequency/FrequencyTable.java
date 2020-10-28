package rmi.huffman.frequency;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class FrequencyTable {
	private String fileName;
	private int threadCount;
	private int granularityLevel;
	private boolean isQuiet;
	private long[] resultTable;
	private Thread[] threads;

	private FrequencyTable(String fileName, int threadCount, int granularityLevel, boolean isQuiet) {
		this.fileName = fileName;
		this.threadCount = threadCount;
		this.granularityLevel = granularityLevel;
		this.isQuiet = isQuiet;
		this.resultTable = new long[256];
		this.threads = new Thread[threadCount];
	}

	public static FrequencyTable generateFrequencyTable(String fileName, int numThreads, int granularity,
			boolean isStatic, boolean isQuiet) throws InterruptedException {
		FrequencyTable frequencyTable = new FrequencyTable(fileName, numThreads, granularity, isQuiet);
		if (isStatic) {
			frequencyTable.staticDistribution();
		} else {
			frequencyTable.dynamicDistribution();
		}
		return frequencyTable;
	}

	

	private void staticDistribution() throws InterruptedException {
		File file = new File(fileName);
		boolean fileExists = file.exists();
		if (fileExists) {
			long fileLength = file.length();
			long chunkCount = threadCount * granularityLevel;
			long chunkSize;
			Collection<long[]> partTables = new ArrayList<>();
			if (fileLength % chunkCount == 0) {
				chunkSize = fileLength / chunkCount;
			} else {
				chunkSize = fileLength / (chunkCount - 1);
			}
				
				int i;
				for (i = 0; i < chunkCount - 1; i++) {
					int threadNum = i % threadCount;
					if (i > 0 && (threadNum == 0)) {
						for (int j = 0; j < threadCount; j++) {
							threads[j].join();
							partTables.add(((CounterThread) threads[j]).getTable());
						}

					}
					threads[threadNum] = new CounterThread(file, i * chunkSize, chunkSize, threadNum + 1, isQuiet);
					threads[threadNum].start();
					
				}
				if (threadCount == 1 && granularityLevel > 1) {
					threads[0].join();
					partTables.add(((CounterThread) threads[0]).getTable());
				}
				
				long remainingStart = (chunkCount - 1) * chunkSize;
				int threadNum = i % threadCount;
				threads[threadNum] = new CounterThread(file, remainingStart, fileLength - remainingStart, threadNum+1, isQuiet);
				threads[threadNum].start();

			
		

				for (Thread t : threads) {
					try {
						t.join();
						partTables.add(((CounterThread) t).getTable());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (long[] partFreqTable : partTables) {
					for(int j = 0; j < 256; j++) {
						resultTable[j] += partFreqTable[j];
						
					}
				}
				
		}

	}

	public void dynamicDistribution() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File file = new File(fileName);
		long fileLength = file.length();
		long chunkCount = threadCount * granularityLevel;
		long chunkSize;

		if (fileLength % chunkCount == 0) {
			chunkSize = fileLength / chunkCount;
		} else {
			chunkSize = fileLength / (chunkCount - 1);
		}
		List<Callable<long[]>> callables = new ArrayList<Callable<long[]>>();
		int i;
		for (i = 0; i < (chunkCount - 1); i++) {
			callables.add(new CounterCallable(file, i * chunkSize, chunkSize, (i % threadCount) +1, isQuiet));
		}

		long remainingOffset = (chunkCount - 1) * chunkSize;
		callables.add(new CounterCallable(file, remainingOffset, fileLength - remainingOffset, (i % threadCount) +1, isQuiet));

		Collection<Future<long[]>> partialResultTables = executor.invokeAll(callables);
		executor.shutdown();
		while(!executor.isTerminated()) {}
		
		for (Future<long[]> futureTable : partialResultTables) {
			long[] table = null;
			try {
				table = futureTable.get();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 0; j < 256;j++) {
				resultTable[j] += table[j];
			}
			
		}
	}
         
	public void printFrequencyTable() {
		System.out.println("Symbol - Occurences");
		for (int i = 0; i < 256; i++) {
			char c = (char) i;
			if (resultTable[i] != 0) {
				if (c == '\n') {
					 System.out.println("newline   - " + resultTable[i]);
				} else if (c == ' ') {
					System.out.println("space   - " + resultTable[i]);
				} else {
					System.out.println("   " + c + "   - " + resultTable[i]);
				}
			}
			
    	    
		}
	}

}
