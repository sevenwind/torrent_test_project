package ecwid_test;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
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
	private Queue<DownloadObject> queue;

	public Downloader(int _threadsNum, String _maxSpeed, String _filePath, String _targetFolderPath) throws IOException{
		this.queue = new ConcurrentLinkedQueue<DownloadObject>();
		this.threadsNum = _threadsNum;
		this.targetFolderPath = _targetFolderPath;
		if(new File(_targetFolderPath).exists() == false){
			new File(_targetFolderPath).mkdir();
		}
		try{
			List<DownloadObject> downloadObjectsList = new ArrayList<DownloadObject>();
			Scanner scnr = new Scanner(new File(_filePath));
			while(scnr.hasNextLine()){
				String [] linkAndName = scnr.nextLine().split(" ");
				DownloadObject existingObject = getObjIfExistsByLink(downloadObjectsList, linkAndName[0]);
				if (existingObject != null){
					DownloadObject item = new DownloadObject();
					item.link = linkAndName[0];
					item.name = linkAndName[1];
					item.parentName = existingObject.name;
					downloadObjectsList.add(item);
				}else{
					downloadObjectsList.add(new DownloadObject(linkAndName[0], linkAndName[1]));
				}
			}
			scnr.close();
			this.queue.addAll(downloadObjectsList);
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
	
	public DownloadObject getObjIfExistsByLink(List<DownloadObject> list, String link){
		for (DownloadObject downloadObject : list) {
			if(downloadObject.link.equals(link)){
				return downloadObject;
			}
		}
		return null;
	}
	
	class DownloadObject{
		String link;
		String name;
		String parentName;
		
		DownloadObject(){}
		
		DownloadObject(String _link, String _name){
			this.link = _link;
			this.name = _name;
			this.parentName = "";
		}
	}
}
