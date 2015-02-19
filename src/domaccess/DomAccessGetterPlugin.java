package domaccess;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

public class DomAccessGetterPlugin extends ProxyPlugin {

	String domDir;
	String app;

	public DomAccessGetterPlugin() {
		this.app = "marctv";
		domDir = "/home/ubuntu/" + app;
	}

	@Override
	public String getPluginName() {
		return "DomAccessGetterPlugin";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient client) {
		return new Plugin(client);
	}

	private void recordResponseContent(Request request, Response response) {
		if (request.getURL().toString().contains(".css"))
			return;
		if (request.getURL().toString().contains(".png"))
			return;
		if (request.getURL().toString().contains(".jpg"))
			return;
		if (request.getURL().toString().contains(".gif"))
			return;
		if (request.getURL().toString().contains(".js"))
			return;
		if (request.getURL().toString().contains(".wav"))
			return;


		try {
			String url = request.getURL().toString();

			if (url.contains(app)
					&& (new String(response.getContent()).trim().toLowerCase().startsWith(
							"<html") || new String(response.getContent()).trim().toLowerCase().startsWith(
									"<!doctype html"))) {
				System.out.println("hit to " + url);
				
				String fileName = URLEncoder.encode(
						request.getURL().toString(), "utf-8");
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(domDir + "/htmls/"
								+ fileName + ".non-well-formed"));
				out.write(response.getContent());
				out.close();
				
				PrintWriter pw = new PrintWriter(new FileWriter(domDir
						+ "/htmls/" + fileName + ".modified"));
				
				
				BufferedReader br = new BufferedReader(new FileReader(domDir + "/html/"
						+ fileName + ".non-well-formed"));
				String line;
				while((line = br.readLine()) != null) {
					if(line.replaceAll(" ", "").toLowerCase().contains("</head>")) {
						pw.println("<script type=\"text/javascript\">var targetJs = \"jquery.marctv-video.js\"</script>\n");
						pw.println("<script type=\"text/javascript\" src=\"http://localhost/log/accessLogger.js\"></script>\n");
					}
					pw.println(line);
				}
				br.close();
				pw.close();
				

				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(domDir + "/htmls/"
								+ fileName + ".modified"));

				ByteArrayOutputStream out1 = new ByteArrayOutputStream();
				byte[] buf = new byte[1024 * 8];
				int len = 0;
				while ((len = in.read(buf)) != -1) {
					out1.write(buf, 0, len);
				}
				in.close();

				response.setContent(out1.toByteArray());
				out1.close();
			}
		} catch (IOException e) {
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

			Response response = mClient.fetchResponse(request);

			if ("200".equals(response.getStatus())) {
				recordResponseContent(request, response);
			}
			return response;
		}

	}
}
