package test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import jscover.Main;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class MutationTestExecuter {

	// coverage:0, js:1, html:2
	public static int TEST_MODE = 0;
	public static String TEST_CLASS = "test.MarcTVCoverageTest";
	
	public void start(){
		try {
			switch (TEST_MODE) {
			case 0:
				final Main main = new Main();
				Thread server = new Thread(new Runnable() {
		            public void run() {
		            	main.runMain(new String[]{
		                        "-ws",
		                        "--proxy",
		                        "--local-storage",
		                        "--only-instrument-reg=.*jquery.marctv-video.js.*",
		                        "--report-dir=/var/www/html/jscover"
		                });
		            }
		        });
				
				File jsonFile = new File("/var/www/html/jscover/jscoverage.json");
		        if (jsonFile.exists())
		            jsonFile.delete();
		        server.start();
		        //System.setProperty("proxy_port", "3129");
		        Result result = JUnitCore.runClasses(Class.forName(TEST_CLASS));
		        main.stop();
				break;
			case 1:
				break;
			case 2:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MutationTestExecuter().start();
	}
}
