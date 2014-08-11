import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			e.printStackTrace();
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
		Boolean isPost = false, isUpdate = false;
		StringBuilder bodyString = new StringBuilder();
		String str = ".";
		int length = 0;
		
		while (!str.equals("")) {
			str = in.readLine();
			System.out.println(str);
			if (str.contains("GET / ")) {
				getRequest(out);
				break;
			} else if (str.contains("POST / ")) {
				System.out.println("this is post req");
				isPost = true;
			} else if (str.contains("POST /update")) {
				isUpdate = true;
			} else if (str.contains("DELETE /")) {
				String postId = str.substring(8);
				deleteRequest(Integer.parseInt(postId));
				break;
			}
			if (str.startsWith("Content-Length: ")) { // get the
				// content-length
				int index = str.indexOf(':') + 1;
				String len = str.substring(index).trim();
				length = Integer.parseInt(len);
			}
			
			if (str.equals("") && length > 0) {
                int read;
                while ((read = in.read()) != -1) {
                	if (read == '+') read = ' ';
                	bodyString.append((char) read);
                    if (bodyString.length() == length)
                        break;
                }
            }
		}
		if (isPost) postRequest(bodyString.toString());
		if (isUpdate) updateRequest(bodyString.toString());
		out.close();
	}

	private void deleteRequest(int parseInt) throws UnknownHostException {
		MongoConnector mongoConnector = new MongoConnector();
		mongoConnector.connectDB();
		
	}

	private void updateRequest(String reqBody) throws UnknownHostException {
		MongoConnector mongoConnector = new MongoConnector();
		mongoConnector.connectDB();
		
	}

	private void postRequest(String reqBody) throws UnknownHostException {
		MongoConnector mongoConnector = new MongoConnector();
		mongoConnector.connectDB();
		Map<String, String> requestParameterMap = parseRequestBody(reqBody);
		mongoConnector.writePost(requestParameterMap.get("title"), requestParameterMap.get("body"));
	}
	
	private Map<String, String> parseRequestBody(String requestBody) {
		Map<String, String> result = new HashMap<String, String>();
		String[] parameters = requestBody.split("&");
		for (String parameter : parameters) {
			int splitPoint = parameter.indexOf("=");
			String key = parameter.substring(0, splitPoint);
			String value = parameter.substring(splitPoint+1);
			System.out.println("key: "+key+", value: "+value);
			result.put(key, value);
		}
		return result;
	}

	private void sendHeader(PrintWriter out) {
		out.println("HTTP/1.0 200 OK");
		out.println("Content-Type: text/html");
		out.println("Server: Bot");
		out.println("");
	}

	private void getRequest(PrintWriter out) throws FileNotFoundException, IOException {
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
