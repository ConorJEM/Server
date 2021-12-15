/**
 * Main Class that creates the server object and handles arguments.
 * @author ID:160014528
 */
public class WebServerMain {

    private static String directory;
    private static int port;

    /**
     * Constructor for WebServerMain. Creates a Server object.
     * @param args The directory and port selected when this program is run.
     */
    public static void main(String[] args) {
        try {
            directory = args[0]; // The first argument is the name of the directory the server serves files from
            port = Integer.parseInt(args[1]); // The port that the server listens on
            Server s = new Server(directory, port); //Creates a server
        } catch (Exception e) {
            System.out.println("Usage: java WebServerMain <document_root> <port>"); //
        }
    }

}
