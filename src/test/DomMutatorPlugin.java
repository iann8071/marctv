package test;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.PrintWriter;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

public class DomMutatorPlugin extends ProxyPlugin {

	String domDir;
	String app;

	public DomMutatorPlugin(String app) {
		this.app = app;
		domDir = "/home/ubuntu/app/" + app + "/dom";
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
						new FileOutputStream(domDir + "/html/"
								+ fileName + ".non-well-formed"));
				out.write(response.getContent());
				out.close();
				
				PrintWriter pw = new PrintWriter(new FileWriter(domDir
						+ "/html/" + fileName + ".modified"));
				
				
				BufferedReader br = new BufferedReader(new FileReader(domDir + "/html/"
						+ fileName + ".non-well-formed"));
				String line;
				while((line = br.readLine()) != null) {
					pw.println(line);
					if(line.replaceAll(" ", "").toLowerCase().contains("<head>")) {
						pw.println("<script type=\"text/javascript\" src=\"http://localhost/mutator/mutants.js\"></script>\n");
						pw.println("<script type=\"text/javascript\" src=\"http://localhost/mutator/mutator_manual.js\"></script>\n");
					}
				}
				br.close();
				pw.close();
				/*
				Document document = Jsoup.parse(new File(domDir
						+ "/html/" + fileName + ".non-well-formed"), "UTF-8");
				document.outputSettings().prettyPrint(true);
				
				bw.write(document.outerHtml());
				bw.close();

				document = Jsoup.parse(document.outerHtml(), "UTF-8");
				document.outputSettings().prettyPrint(false);
				Element headerElement = document.getElementsByTag("head")
						.first();
				headerElement
						.prepend("<script type=\"text/javascript\">\n"
								+ readAccessLoggerJs() + "</script>");
				 */
				

				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(domDir + "/html/"
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
				/*
				 * new File(domAccessGetterDir + "/html/" + fileName +
				 * ".modified") .delete(); new File(domAccessGetterDir +
				 * "/html/" + fileName + ".non-well-formed").delete();
				 */
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String readAccessLoggerJs() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(
				domDir + "/accessLogger.js"));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = (in.readLine())) != null) {
			out.append(line + "\n");
		}
		in.close();
		return out.toString();
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
