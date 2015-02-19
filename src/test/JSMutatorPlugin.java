package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

public class JSMutatorPlugin extends ProxyPlugin {
	private String mutantFilePath, domain, targetJs;
	private Plugin plugin;

	public JSMutatorPlugin(String domain, String targetJs, String mutantFilePath) {
		this.mutantFilePath = mutantFilePath;
		this.domain = domain;
		this.targetJs = targetJs;
	}

	@Override
	public String getPluginName() {
		return "JSMutatorPlugin";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient client) {
		plugin = new Plugin(client);
		return plugin;
	}

	private void rewriteResponseContent(Request request, Response response) {

		try {

			String connectUrl = request.getURL().toString();

			if (connectUrl.contains(domain)
					&& connectUrl.contains(targetJs)) {
				FileOutputStream outResponse = new FileOutputStream(
						"./response_before");
				outResponse.write(response.getContent());
				outResponse.close();

				File responseBeforeFile = new File("./response_before");
				File responseAfterFile = new File("./response_after");
				ProcessBuilder pb = new ProcessBuilder();
				pb.redirectInput(new File(mutantFilePath));
				pb.command(new String[] { "patch",
						responseBeforeFile.getAbsolutePath(), "-o",
						responseAfterFile.getAbsolutePath() });
				Process p = pb.start();
				p.waitFor();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				for (String line = br.readLine(); line != null; line = br
						.readLine()) {
					if (line.contains("FAILED")) {
						System.out.println(line);
						return;
					}
				}

				System.out.println("hit to " + connectUrl);
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream("./response_after"));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024 * 8];
				int len = 0;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				in.close();
				response.setContent(out.toByteArray());
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class Plugin implements HTTPClient {
		private HTTPClient mClient;

		public Plugin(HTTPClient client) {
			mClient = client;
		}

		public Response fetchResponse(Request request) throws IOException {
			// remove if-modified-since and if-none-matche to avoid 304
			request.deleteHeader("If-Modified-Since");
			request.deleteHeader("If-None-Match");
			// response to client
			Response response = mClient.fetchResponse(request);
			if ("200".equals(response.getStatus())
					&& new File(mutantFilePath).exists()) {
				// rewrite response to client
				rewriteResponseContent(request, response);
			}
			return response;
		}

	}

}
