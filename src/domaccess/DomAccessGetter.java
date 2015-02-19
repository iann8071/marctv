package domaccess;
import java.io.IOException;
import java.text.ParseException;
import org.junit.runner.JUnitCore;
import org.owasp.webscarab.model.StoreException;

import test.MarcTVCoverageTest;

public class DomAccessGetter {
	
	public static void main(String[] args) throws IOException, ParseException, StoreException, InterruptedException {
		ProxyStarter ps = new ProxyStarter();
		ps.start("marctv");
		JUnitCore.runClasses(MarcTVCoverageTest.class);
	}
}
