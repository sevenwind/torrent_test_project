package ecwid_test;

import java.util.*;

public class Program {

	public static void main(String[] args) {
		try{
			List<String> keys = new ArrayList<String>(Arrays.asList("-n", "-l", "-f", "-o"));
			int threadsNum = 0;
			String maxSpeed = "";
			String pathToFileWithLinks = "";
			String pathToTargetFolder = "";

		    for(int i = 0; i < args.length-1; i = i + 2) {
		    	switch (args[i]) {
				case "-n":
					threadsNum = Integer.parseInt(args[i+1]);
					break;
				case "-l":
					maxSpeed = args[i+1];
					break;
				case "-f":
					pathToFileWithLinks = args[i+1];
					break;
				case "-o":
					pathToTargetFolder = args[i+1];
					break;
				default:
					System.out.println("Invalid key - " + args[i]);
					break;
				}
		    }
		    
		    if(threadsNum > 0 && maxSpeed.length() > 0 && pathToFileWithLinks.length() > 0 && pathToTargetFolder.length() > 0){
				Downloader downloader = new Downloader(threadsNum, maxSpeed, pathToFileWithLinks, pathToTargetFolder);
				downloader.downloadFiles();
		    }else{
		    	System.out.println("Invalid parametrs!");
		    }
		}catch (Exception e) {
			System.out.println("Error = " + e.getMessage());
		}
	}
}
