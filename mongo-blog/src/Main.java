import java.net.UnknownHostException;


public class Main {

	public static void main(String[] args) throws UnknownHostException {
		
		SocketThread socketThread = new SocketThread();
		socketThread.start();
		
		MongoConnector mongoConnector = new MongoConnector();
		mongoConnector.connectDB();
	}

}
