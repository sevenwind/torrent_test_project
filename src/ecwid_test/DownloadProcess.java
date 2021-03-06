package ecwid_test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import ecwid_test.Downloader.DownloadObject;


public class DownloadProcess extends Thread {

	private final int SECOND = 1000000000;
	private final int BYTE_ARRAY_SIZE = 102400;

	private Queue<DownloadObject> queue;
	private int counter;
	public String targetFolderPath;
	public int maxSpeed;
	public int fullSize;
	public AtomicInteger link_to_full_size_counter;
	public AtomicInteger threadComplateCounter;
	
    public void run() {
    	while(true){
   			while(!queue.isEmpty()){
   				DownloadObject curr_element = this.queue.poll();
   				try {
   					downloadFile(curr_element);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
   			threadComplateCounter.incrementAndGet();
   			break;
    	}
    }
    
	DownloadProcess(Queue<DownloadObject> _queue, int _counter, String _targetFolderPath, int _maxSpeed, AtomicInteger _link_to_full_size_counter, AtomicInteger _threadComplateCounter) {
		this.queue = _queue;
		this.counter = _counter;
		this.targetFolderPath = _targetFolderPath;
		this.maxSpeed = _maxSpeed;
		this.link_to_full_size_counter = _link_to_full_size_counter;
		this.threadComplateCounter = _threadComplateCounter;
	}
	
	public void downloadFile(DownloadObject item)
	        throws MalformedURLException, IOException {
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try {
	    	if(item.parentName.length() > 0){
	    		if(Files.exists(Paths.get(targetFolderPath + File.separator + item.parentName)))
	    			Files.createSymbolicLink(Paths.get(targetFolderPath + File.separator + item.name), Paths.get(targetFolderPath + File.separator + item.parentName));
	    	}else{
		        in = new BufferedInputStream(new URL(item.link).openStream());
		        fout = new FileOutputStream(targetFolderPath + File.separator + item.name);
	
		        final byte data[] = new byte[this.maxSpeed > BYTE_ARRAY_SIZE ? BYTE_ARRAY_SIZE : this.maxSpeed];
		        int count = 1;
		        int current_size = 0;
		        while (count > 0) {
		        	long start = System.nanoTime();
		        	long end = 0;
		        	long traceTime = 0;
		        	
		        	while(traceTime < SECOND) {
			        	if(current_size < this.maxSpeed) {
				        	count = in.read(data, 0, this.maxSpeed > BYTE_ARRAY_SIZE ? BYTE_ARRAY_SIZE : this.maxSpeed);
				        	if(count == -1){
				        		break;
				        	}
				        	current_size+=count;
				        	
				        				        	
				        	fout.write(data, 0, count);
			        	}
			        	end = System.nanoTime();
			            traceTime = end-start;
		        	}
		        	System.out.println("Thread is "+this.counter+". Downloaded " + current_size / (1024*1024) +" Mb or " + current_size / 1024 +" Kb or " + current_size +" b");
		        	this.link_to_full_size_counter.addAndGet(current_size);
		        	traceTime = 0;
		        	current_size = 0;
		        }
		        System.out.println("File downloaded!");
	    	}
	    }
	    catch (MalformedURLException e) {
	    	System.out.println("Error 404! Data from url not found!");
		}
	    catch(UnsupportedOperationException e){
	    	System.out.println("This OS doesn't support creating Sym links");
	    }
	    catch (Exception e) {
			e.getMessage();
		}
	    finally {
	        if (in != null) {
	            in.close();
	        }
	        if (fout != null) {
	            fout.close();
	        }
	    }
	}
}
