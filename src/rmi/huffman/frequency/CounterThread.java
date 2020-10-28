package rmi.huffman.frequency;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class CounterThread extends Thread{
	private File file;
	private long offset;
	private long chunkSize;
	private int num;
	private boolean isQuiet;
	private long[] frequencyTable;
	
	public CounterThread(File file, long offset, long chunkSize, int num, boolean isQuiet) {
        this.file = file;
        this.offset = offset;
        this.chunkSize = chunkSize;
        this.num = num;
        this.isQuiet = isQuiet;
        this.frequencyTable = new long[256];
    }
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
        if (!isQuiet) {
        	System.out.print("Thread-" + num +  " started.\n");
       }
		//long[] frequencyTable = new long[256];
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
			
				
//				for (int i = 0; i < 256; i++) {
//					 FrequencyTable.resultTable[i] += frequencyTable[i];
//				}
			
				

		} catch (IOException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
        if (!isQuiet) {
        	System.out.print("Thread-" + num + " stopped.\nThread-" + num +" execution time was (millis):" + (endTime - startTime) + "\n");
        
        }
	}
	long[] getTable() {
		return frequencyTable;
	}
}
