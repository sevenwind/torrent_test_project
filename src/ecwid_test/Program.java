package ecwid_test;

import java.util.*;

public class Program {

	public static void main(String[] args) {
		try{
			Map<String, String> dictionary = new HashMap<String, String>();
			List<String> keys = new ArrayList<String>(Arrays.asList("n", "l", "f", "o"));
			int threadsNum = 0;
			String maxSpeed = "";
			String pathToFileWithLinks = "";
			String pathToTargetFolder = "";

		    for (int i=0; i < args.length; i++) {
		        switch (args[i].charAt(0)) {
		        case '-':
			        dictionary.put(args[i].substring(1), args[i+1]);	     
			        i= i+1;
		        break;
		        default:
		        	System.out.println("default");
		        break;
		        }
		    }
		    for (Map.Entry<String, String> item : dictionary.entrySet()) {
		    	System.out.println("Params is " + item.getKey() + " " + item.getValue());
		    	if(keys.contains(item.getKey())){
		    		switch (item.getKey().replace("-", "")) {
					case "n":
						threadsNum = Integer.parseInt(item.getValue());
						break;
					case "l":
						maxSpeed = item.getValue();
						break;
					case "f":
						pathToFileWithLinks = item.getValue();
						break;
					case "o":
						pathToTargetFolder = item.getValue();
						break;
					default:
						break;
					}
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
