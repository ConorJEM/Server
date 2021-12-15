import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Server Class.
 * Initiates the server and continuously listens on the port given as an argument. Handles threading.
 * @author ID:
 */
public class Server {

    private ServerSocket ss;
    private static final int MAX_THREADS = 5;

    /**
     * Constructor for the Server Object.
     * Creates ConnectionHandler objects to deal with clients HTTP requests.
     * @param directory The directory that this server serves files from
     * @param port The port this server listens on
     */
    public Server(String directory, int port) {

        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS); //Creates a thread pool with the desired number of max threads

        try {
            ss = new ServerSocket(port);
            System.out.println("Server is listening on port " + port + "...");

            while (true) { //Server continuously listens for connections

                Socket conn = ss.accept();
                System.out.println("Server has a new request from " + conn.getInetAddress());
                //When a connection is created, a connection handler object is made to satisfy the HTTP request
                Runnable ch = new ConnectionHandler(conn, directory);
                threadPool.execute(ch); //The connection handle object is executed by an available thread from the thread pool
            }
        } catch (IOException e) {
            System.out.println("Error has occurred: server turning off");
            e.printStackTrace();
        }

    }

}
