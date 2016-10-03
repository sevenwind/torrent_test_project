package ecwid_test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;


public class DownloadProcess extends Thread {

	private final int SECOND = 1000000000;
	private final int BYTE_ARRAY_SIZE = 102400;

	private Queue<String> queue;
	private int counter;
	public String targetFolderPath;
	public int maxSpeed;
	public int fullSize;
	public AtomicInteger link_to_full_size_counter;
	public AtomicInteger threadComplateCounter;
	
    public void run() {
    	while(true){
   			while(!queue.isEmpty()){
   				String curr_element = this.queue.poll();
   				String [] link_and_filename = curr_element.split(" ");
   				try {
   					downloadFile(targetFolderPath + File.separator + link_and_filename[1], link_and_filename[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
   			threadComplateCounter.incrementAndGet();
   			break;
    	}
    }
    
	DownloadProcess(Queue<String> _queue, int _counter, String _targetFolderPath, int _maxSpeed, AtomicInteger _link_to_full_size_counter, AtomicInteger _threadComplateCounter) {
		this.queue = _queue;
		this.counter = _counter;
		this.targetFolderPath = _targetFolderPath;
		this.maxSpeed = _maxSpeed;
		this.link_to_full_size_counter = _link_to_full_size_counter;
		this.threadComplateCounter = _threadComplateCounter;
	}
	
	public void downloadFile(String filename, String urlString)
	        throws MalformedURLException, IOException {
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try {
	        in = new BufferedInputStream(new URL(urlString).openStream());
	        fout = new FileOutputStream(filename);

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
	        	System.out.println("Thread is "+this.counter+". Downloaded " + current_size / (1024*1024) +" Mb " + current_size / 1024 +" Kb " + current_size +" b");
	        	this.link_to_full_size_counter.addAndGet(current_size);
	        	traceTime = 0;
	        	current_size = 0;
	        }
	        System.out.println("File downloaded!");
	    }catch (MalformedURLException e) {
	    	System.out.println("Error 404! Data from url not found!");
		}catch (Exception e) {
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
