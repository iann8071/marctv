package test;


import java.io.File;

import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.model.StoreException;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.proxy.ListenerSpec;
import org.owasp.webscarab.plugin.proxy.Proxy;

public class ProxyStarter {

	public static Proxy proxy;
	static String mutantFilePath, domain;

	public ProxyStarter(String domain) {
		ProxyStarter.domain = domain;
	}

	public void stop() {
		if (proxy != null) {
			proxy.stop();
			proxy = null;
		} else {
			System.out.println("proxy not started");
		}
	}

	public void start(String targetJs, String mutantFilePath) throws StoreException,
			InterruptedException {
		Framework framework = new Framework();
		Preferences.setPreference("Proxy.listeners", "127.0.0.1:8080");
		framework.setSession("FileSystem", new File(".conversation"), "");
		proxy = new Proxy(framework);
		proxy.addPlugin(new JSMutatorPlugin(domain, targetJs, mutantFilePath));
		proxy.run();
	}

	public void start() throws StoreException {
		Framework framework = new Framework();
		Preferences.setPreference("Proxy.listeners", "127.0.0.1:8080");
		framework.setSession("FileSystem", new File(".conversation"), "");
		proxy = new Proxy(framework);
		proxy.addPlugin(new DomMutatorPlugin(domain));
		proxy.run();
	}
}
