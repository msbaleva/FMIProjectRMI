package rmi.huffman.frequency;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;


public class CounterCallable implements Callable<long[]> {
	private File file;
	private long offset;
	private long chunkSize;
	private int num;
	private boolean isQuiet;

	public CounterCallable(File file, long offset, long chunkSize, int num, boolean isQuiet) {
	        this.file = file;
	        this.offset = offset;
	        this.chunkSize = chunkSize;
	        this.num = num;
	        this.isQuiet = isQuiet;
	    }

	@Override
	public long[] call() {
		long startTime = System.currentTimeMillis();
        if (!isQuiet) {
        	System.out.print("Thread-" + num +  " started.\n");
        }
		long[] frequencyTable = new long[256];
		try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file))) {
			reader.skip(offset);

			int readChunkSize = 8 * 1024;
			int step = (int) (chunkSize / readChunkSize);
			byte[] readChunk = new byte[readChunkSize + 1];
			for (long i = 0; i < step + 1; i++) {
				int toRead = readChunkSize;
				if (i == step) {
					toRead = (int) (chunkSize % readChunkSize);
				}
				int readBytes = reader.read(readChunk, 0, toRead);
				for (int j = 0; j < readBytes; j++) {
					frequencyTable[readChunk[j]]++;
				}
			}		
					
			
				
				
			
				

		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
        if (!isQuiet) {
        	System.out.print("Thread-" + num + " stopped.\nThread-" + num +" execution time was (millis):" + (endTime - startTime) + "\n");
        
        }
		return frequencyTable;
	}


}
