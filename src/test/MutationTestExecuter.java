package test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import jscover.Main;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import proxy.ProxyStarter;

public class MutationTestExecuter {

	// coverage:0, js:1, html:2
	public static int TEST_MODE = 1;
	public static int MUTANT_START = 0;
	public static int MUTANT_END = 1;
	
	public void start(){
		try {
			Result result;
			ProxyStarter proxy;
			switch (TEST_MODE) {
			case 0:
				final Main main = new Main();
				Thread server = new Thread(new Runnable() {
		            public void run() {
		            	main.runMain(new String[]{
		                        "-ws",
		                        "--proxy",
		                        "--local-storage",
		                        "--only-instrument-reg=.*ko-calendar.js.*",
		                        "--report-dir=/var/www/html/jscover"
		                });
		            }
		        });
				
				File jsonFile = new File("/var/www/html/jscover/jscoverage.json");
		        if (jsonFile.exists())
		            jsonFile.delete();
		        server.start();
		        result = JUnitCore.runClasses(MarcTVCoverageTest.class);
		        main.stop();
				break;
			case 1:
				
				int unkilledMutant = 0;
				int killedMutant = 0;
				long time = 0;
				for (int i = MUTANT_START; i < MUTANT_END; i++) {
					System.out.println("mutant " + i);
					proxy = new ProxyStarter("wordpress");
					proxy.start("jquery.marctv-video.js", "./mutants/js/mutant" + i + ".diff");
					PrintWriter pw = null;
					try {
						result = JUnitCore.runClasses(MarcTVMutationTest.class);
						pw = new PrintWriter(new OutputStreamWriter(
								new FileOutputStream(new File("./result/log.txt"), true)));
						if (result.wasSuccessful()) {
							pw.println("mutant " + i + ": unkilled");
							System.out.println("mutant " + i + ": unkilled");
							unkilledMutant++;
						} else {
							time += result.getRunTime();
							pw.println("time: " + result.getRunTime());
							System.out.println("time: " + result.getRunTime());
							for (Failure f : result.getFailures()) {
								pw.println("test method:"
										+ f.getDescription().getMethodName());
								System.out.println("test method:"
										+ f.getDescription().getMethodName());
								pw.println("test method:" + f.getException());
							}
							pw.println("mutant " + i + ": killed");
							System.out.println("mutant " + i + ": killed");
							killedMutant++;
						}
						pw.close();
						pw = null;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						proxy.stop();
						if (pw != null) {
							pw.close();
							pw = null;
						}
					}
				}

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(
								new File("./result/log.txt"), true)));
				pw.println("total time:" + time);
				System.out.println("total time:" + time);
				pw.println("score:"
						+ (((double) killedMutant) / (killedMutant + unkilledMutant)));
				System.out.println("score:"
						+ (((double) killedMutant) / (killedMutant + unkilledMutant)));
				pw.close();
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
