import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Handles all HTTP Requests.
 * Contains the methods that read client requests and handle responses
 * @author ID:160014528
 */
public class ConnectionHandler implements Runnable {

    private Socket conn; // socket representing TCP/IP connection to Client
    private InputStream is; // Retrieve data from client on this stream
    private OutputStream os; // Send data to client on this stream
    private static String directory; // The directory that files are served from
    private BufferedReader br; // Read clients data with a Buffered Reader
    private FileWriter out = null; //FileWriter is needed to handle logging

    /**
     * Constructor for the ConnectionHandler object that deals with client requests.
     * Initialises input and output streams, readers and file writers required to handle client request and server response.
     * @param conn The socket that connects the server to the client
     * @param directory The directory this server serves files from
     */
    public ConnectionHandler(Socket conn, String directory) {
        this.conn = conn;
        this.directory = directory;
        try {
            out = new FileWriter("logs.txt", true); // Logs of each request are stored in 'logs.txt' within the /src directory
            is = conn.getInputStream(); // Retrieve data from client on this stream
            br = new BufferedReader(new InputStreamReader(is)); // Read clients data with a Buffered Reader
            os = conn.getOutputStream(); // Send data to client on this stream
        } catch (IOException ioe) {
            System.out.println("ConnectionHandler: " + ioe.getMessage());
        }
    }

    /**
     * This run method is invoked when a thread is executed in the server class.
     */
    public void run() {
        System.out.println("new ConnectionHandler constructed .... ");

        try {
            readClientRequest();
        } catch (Exception e) { // exit for any Exception
            System.out.println("ConnectionHandler.handleClientRequest: " + e.getMessage());

        }
        cleanup(); // cleanup and exit
    }

    /**
     * Handles reading the clients request.
     * Identifies the method of the HTTP request so that the appropriate method for that request can be called.
     * @throws IOException
     * @throws InterruptedException Used when calling thread.sleep() to artificially increase connection time for requests.
     */
    private void readClientRequest() throws IOException, InterruptedException {
        String inputData = ""; //Initialises the inputData string
        String line;
        //Reads the input from the client and separates each line
        while (!(line = br.readLine()).isBlank()) {
            inputData += line + "\r\n";
        }

        String[] requestArray = inputData.split("\r\n"); //Splits client data by line
        String request = requestArray[0]; //Extracts the request as the first line from client data

        System.out.println(request);

        String[] requestParts = request.split(" "); //Splits the clients request by blank space
        String method = requestParts[0]; //For any HTTP Request handled by this server, the first word will be the method
        String targetFile = requestParts[1]; //The second word of the request is the target file
        String version = requestParts[2]; //The third word is the requested version of HTTP for the request

        if (method.equals("HEAD") || method.equals("GET")) {
            handleHeadGetRequests(targetFile, version, method); //Calls the method that handles GET and HEAD requests
        } else if (method.equals("DELETE")) {
            handleDeleteRequests(targetFile, version); //Calls the method that handles DELETE requests
        } else {
            handleUnimplemented(targetFile, version, method); //Calls the method that handles unimplemented requests (501 errors)
        }
        //Used to deliberately slow HTTP requests to highlight how the program needs to wait for available threads.
        //System.out.println("Waiting For 3 Seconds");
        //Thread.sleep(3000);
    }

    /**
     * Returns the filepath of the file the HTTP request referred to.
     * @param path The path that the client pointed to in the HTTP request
     * @return The working path of the requested file
     */
    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html"; //If the HTTP request has target "/" it refers to the index.html file
        }
        //System.out.println("requested path is: " +"../"+ WebServerMain.getPath());
        return Paths.get(directory, path); //returns the path of the directory given in the arguments and the path from request
    }

    /**
     * Handles Get and Head Requests, giving details about the requested file and in the case
     * of a Get request, delivering this file to the client.
     * @param targetFile Requested file by the client
     * @param version Requested HTTP version given by client
     * @param method Method of HTTP request given by client
     * @throws IOException
     */
    private void handleHeadGetRequests(String targetFile, String version, String method) throws IOException {
        //Initialise fileType, content and status for a standard 404 error, the case if the target file does not exist
        String fileType = "Unknown";
        byte[] content = "<h1> Error 404 Not Found<h1>".getBytes();
        String status = "404 Not Found";

        Path filePath = getFilePath(targetFile);
        //If the target file exists, define fileType content and status as appropriate for an existing file
        if (Files.exists(filePath)) {
            fileType = Files.probeContentType(filePath);
            content = Files.readAllBytes(filePath);
            status = "200 OK";
        }
        //Outputs to the client the appropriate headers for their request
        os.write((version + " " + status + "\r\n").getBytes());
        os.write(("My Java Web Server" + "\r\n").getBytes()); //needs a test written to check! :)
        os.write(("Content-Length: " + content.length + "\r\n").getBytes());
        os.write(("Content-Type: " + fileType + "\r\n").getBytes());
        os.write(("\r\n").getBytes());
        //Outputs to the client the requested content if they sent a GET request
        if (method.equals("GET")) {
            os.write(content);
        }
        os.write(("\r\n\r\n").getBytes());
        os.flush();
        //Calls the method that logs this handled request
        logRequest(method, status, fileType); //Logs this response in logs.txt with relevant information
    }

    /**
     * Handles Delete Requests, deleting the designated file from the working directory.
     * @param targetFile Requested file by the client
     * @param version Requested HTTP version given by client
     * @throws IOException
     */
    private void handleDeleteRequests(String targetFile, String version) throws IOException {

        Path filePath = getFilePath(targetFile);
        if (Files.exists(filePath)) { //Check If the target file exists
            try { //If the file exists, try deleting it
                Files.delete(filePath);
                os.write((version + " 200 (OK) " + "\r\n").getBytes()); //Return 200 (OK) showing file was deleted
                os.write(("My Java Web Server" + "\r\n").getBytes());
                os.write(("\r\n").getBytes());
                os.write(("\r\n\r\n").getBytes());
                os.flush();
                logRequest("Delete", "200 (OK)", Files.probeContentType(filePath)); //Logs this response in logs.txt with relevant information
            } catch (IOException e) {
                e.printStackTrace();
                os.write((version + " 202 (Accepted) " + "\r\n").getBytes()); //Return 202 Showing the file exists but 'queued' for deletion
                os.write(("My Java Web Server" + "\r\n").getBytes());
                os.write(("\r\n\r\n").getBytes());
                os.flush();
                logRequest("Delete", "202 (Accepted)", Files.probeContentType(filePath)); //Logs this response in logs.txt with relevant information
            }
        } else {
            os.write((version + " 204 (No Content) " + "\r\n").getBytes()); //Return 204 Showing the requested file doesn't exist.
            os.write(("My Java Web Server" + "\r\n").getBytes());
            os.write(("\r\n\r\n").getBytes());
            os.flush();
            logRequest("Delete", "204 (No Content)", "Unknown"); //Logs this response in logs.txt with relevant information
        }

    }

    /**
     * Handles requests not currently supported by this server, throwing a 501 error.
     * Such requests could include PUT or POST requests
     * @param targetFile Requested file by the client
     * @param version Requested HTTP version given by client
     * @param method Method of HTTP request given by client
     * @throws IOException
     */
    private void handleUnimplemented(String targetFile, String version, String method) throws IOException {
        os.write(("HTTP/1.1 501 Not Implemented" + "\r\n").getBytes());
        os.write(("My Java Web Server" + "\r\n").getBytes());
        os.write(("\r\n\r\n").getBytes());
        os.flush();
        logRequest(method, "501 Not Implemented", "Unknown"); //Logs this response in logs.txt with relevant information
    }

    /**
     * Handles logging any response from the server.
     * @param method The type of HTTP request that was responded to
     * @param status The status of the request
     * @param fileType The type of file that was referenced by the client
     */
    private void logRequest(String method, String status, String fileType) {
        PrintWriter log = new PrintWriter(out);
        Date date = java.util.Calendar.getInstance().getTime(); //Retrieves the current date and time at execution a string
        //Writes to one line in logs.txt all desired information about the request
        log.write("Date and Time of Response: " + date
                + " HTTP Request method = " + method
                + " Status of Request: " + status
                + " FileType (If applicable): " + fileType
                + "\r\n");
        //Logs can be found inside the /src directory at file 'logs.txt'
        log.flush();
    }

    /**
     * Closes the input and output streams, buffered reader and socket.
     */
    private void cleanup() {
        System.out.println("ConnectionHandler: ... cleaning up and exiting ... ");
        try {
            //Close all input and output streams
            br.close();
            is.close();
            conn.close();
            out.close();

        } catch (IOException ioe) {
            System.out.println("ConnectionHandler:cleanup " + ioe.getMessage());
        }
    }
}
