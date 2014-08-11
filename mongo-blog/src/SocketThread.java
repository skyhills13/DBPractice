import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.mongodb.DBObject;


public class SocketThread extends Thread {
	
	private static final String WEBAPP_INDEX_HTML = "./webapp/index.html";
	private static final String WEBAPP_POST_TEMPLATE = "./webapp/post-template.html";
	private final static int SERVER_PORT = 3000;

	@Override
	public void run() {
		ServerSocket s = null;

		System.out.println("Webserver starting up on port 80");
		System.out.println("(press ctrl-c to exit)");
		try {
			s = new ServerSocket(SERVER_PORT);
			while (!SocketThread.interrupted()) {
				Socket remote = s.accept();
				System.out.println("Connection, sending data.");
				
				reactToRequest(remote);
				remote.close();
			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
		} finally {
			try {
				s.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private void reactToRequest(Socket remote) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				remote.getInputStream()));
		PrintWriter out = new PrintWriter(remote.getOutputStream());

		String str = ".";
		while (!str.equals("")) {
			str = in.readLine();
			System.out.println(str);
		}
		testRequest(out);
		out.close();
	}

	private void sendHeader(PrintWriter out) {
		out.println("HTTP/1.0 200 OK");
		out.println("Content-Type: text/html");
		out.println("Server: Bot");
		out.println("");
	}

	private void testRequest(PrintWriter out) throws FileNotFoundException, IOException {
		sendHeader(out);
		String responseBody = makeHtmlString();
		out.println(responseBody);
		out.flush();
	}
	
	private String makeHtmlString() throws FileNotFoundException, IOException {
		BufferedReader freader = new BufferedReader(new FileReader(
				WEBAPP_INDEX_HTML));
		String buffer;
		StringBuilder sb = new StringBuilder();
		while ((buffer = freader.readLine()) != null) {
			sb.append(buffer);
		}
		freader.close();
		
		String result = sb.toString();
		
		MongoConnector mongoConnector = new MongoConnector();
		mongoConnector.connectDB();
		List<DBObject> posts = mongoConnector.showPosts();
		StringBuilder postBuilder = new StringBuilder();
		for (DBObject post : posts) {
			String title = (String) post.get("title");
			String date = post.get("last_updated").toString();
			String body = (String) post.get("body");
			String postString = makePostString(title, date, body);
			System.out.println(postString);
			postBuilder.append(postString);
		}
		result = result.replace("$POST", postBuilder.toString());
		
		return result;
	}

	private String makePostString(String title, String date, String body) throws IOException {
		BufferedReader freader = new BufferedReader(new FileReader(
				WEBAPP_POST_TEMPLATE));
		String buffer;
		StringBuilder sb = new StringBuilder();
		while ((buffer = freader.readLine()) != null) {
			sb.append(buffer);
		}
		freader.close();
		String template = sb.toString();
		try {
			template = template.replace("$TITLE", title);
			template = template.replace("$DATE", date);
			template = template.replace("$BODY", body);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return template;
	}
	
}
