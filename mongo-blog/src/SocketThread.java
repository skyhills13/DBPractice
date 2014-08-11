import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketThread extends Thread {
	
	private static final String WEBAPP_RESULT_HTML = "./webapp/result.html";
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
			if ("/test".equals(str))
				testRequest(out);
		}
	}

	private void sendHeader(PrintWriter out) {
		out.println("HTTP/1.0 200 OK");
		out.println("Content-Type: text/html");
		out.println("Server: Bot");
		out.println("");
	}

	private void testRequest(PrintWriter out) {
		sendHeader(out);
		String responseBody = "you requested /test"; 
		out.println(responseBody);
		out.flush();
	}
	
	private String makeHtmlString() throws FileNotFoundException, IOException {
		BufferedReader freader = new BufferedReader(new FileReader(
				WEBAPP_RESULT_HTML));
		String buffer;
		StringBuilder sb = new StringBuilder();
		while ((buffer = freader.readLine()) != null) {
			sb.append(buffer);
		}
		freader.close();
		
		String result = sb.toString();
		result = result.replace("$TITLE", "post title");
		result = result.replace("$DATE", "today");
		result = result.replace("$CONTENT", "I am ");
		
		return result;
	}
	
}
