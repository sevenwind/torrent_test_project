package ecwid_test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader {
	
	private final int kilobyteSize = 1024;
	private final int megabyteSize = 1024*1024;
	
	public AtomicInteger [] statistic_full_download_size;
	public AtomicInteger threadComplateCounter = new AtomicInteger(0);
	
	private long start_time = 0;
	
	public String targetFolderPath;
	public int maxSpeed;
	public int threadsNum;
	private Queue<String> queue;

	public Downloader(int _threadsNum, String _maxSpeed, String _filePath, String _targetFolderPath) throws IOException{
		this.queue = new ConcurrentLinkedQueue<String>();
		this.threadsNum = _threadsNum;
		this.targetFolderPath = _targetFolderPath;
		if(new File(_targetFolderPath).exists() == false){
			throw new IOException("Folder " + _targetFolderPath + " not exists!");
		}
		try{
			Scanner scnr = new Scanner(new File(_filePath));
			while(scnr.hasNextLine()){
				this.queue.add(scnr.nextLine());
			}
			scnr.close();
			Boolean haveSuffix = _maxSpeed.toLowerCase().indexOf("k") >= 0 || _maxSpeed.toLowerCase().indexOf("m") >= 0;
			NumberFormat nf = NumberFormat.getInstance();
			if(haveSuffix){
				this.maxSpeed = _maxSpeed.toLowerCase().indexOf("k") > 0 
						? nf.parse(_maxSpeed).intValue()*kilobyteSize 
								: nf.parse(_maxSpeed).intValue()*megabyteSize;
			}else{
				this.maxSpeed = nf.parse(_maxSpeed).intValue();
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public void downloadFiles(){
		start_time = System.nanoTime();
		statistic_full_download_size = new AtomicInteger [this.threadsNum];
		DownloadProcess[] threads = new DownloadProcess[this.threadsNum];
		
		for(int i = 0;i < this.threadsNum; i++){
			statistic_full_download_size[i] = new AtomicInteger(0);
			threads[i] = new DownloadProcess(this.queue, i, this.targetFolderPath, this.maxSpeed / this.threadsNum, statistic_full_download_size[i], this.threadComplateCounter);
			threads[i].start();
		}
		
		while(true){
			if(threadComplateCounter.get() == threadsNum){
				int full_downloaded_byte_size = 0;
				for (AtomicInteger thread_downloaded_byte_size : statistic_full_download_size) {
					full_downloaded_byte_size += thread_downloaded_byte_size.get();
				}
				System.out.println("Full download size is " + full_downloaded_byte_size / megabyteSize + " Mb or " + full_downloaded_byte_size / kilobyteSize + " Kb or " + full_downloaded_byte_size + " b");
				long end_time = System.nanoTime();
	            long traceTime = end_time-start_time;
	            System.out.println("Full time for download: " + Double.valueOf(traceTime) / 1000000000 + " sec");
				break;
			}
		}
	}
}
