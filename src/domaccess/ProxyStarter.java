package domaccess;


import java.io.File;

import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.model.StoreException;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.proxy.Proxy;

public class ProxyStarter {

	Proxy proxy;

	public void stop() {
		if (proxy != null)
			proxy.stop();
	}
	
	public static void main(String[] args) {
		ProxyStarter ps = new ProxyStarter();
		try {
			ps.start(args[0]);
			while (true) {
				Thread.sleep(1000);
			}
		} catch (StoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start (String app) throws StoreException, InterruptedException {
		Framework framework = new Framework();
		Preferences.setPreference("Proxy.listeners", "127.0.0.1:8080");
		framework.setSession("FileSystem", new File(".conversation"), "");
		proxy = new Proxy(framework);
		proxy.addPlugin(new DomAccessGetterPlugin());
		System.out.println("start");
		proxy.run();
	}
}
