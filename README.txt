FEATURES:
1. Supports GET, HEAD and DELETE requests
2. Returning of binary images (GIF, JPEG and PNG):
	
	After compiling, if you run this program using the /rrr directory (e.g. by running java WebServerMain ../rrr 8080)
	then go to http://localhost:8080/page2.html I have included a gif, png, and a JPEG file that shows I am succesfully returning binary images.

3. Multithreading – support multiple concurrent client connection requests up to a specified limit.

	I have implemented multithreading via implementing runnable within the ConnectionHandler class, and executing its run method from a
	threadpool defined in the Server class (set to five threads by default so it is easy to test). My server closes each HTTP connection after the request
	is handled. On line 89 of the ConnectionHandler class there is a commented out delay which makes the connection between client and server artificially long 
	so that is easy to verify that the server is indeed waiting for previous threads to complete before new connections can be handled.
	
4. Logging – each time requests are made, log them to a file, indicating date/time request type, response code etc.

	Logs can be found within the /src directory inside a file called 'logs.text'. For each request I store 
	the current date and time, as well as the method of the request, the response code and the file type.

5. Supporting other methods in addition to GET and HEAD.

	I have implemented the DELETE method. Within the /rrr directory I have included two files, file1.txt and file2.txt. By using curl command 
	'curl -s -I -X DELETE localhost:8090/file1.txt' the server first responds with 'HTTP/1.1 200 (OK)' and the file will be deleted from the directory.
	Using the same curl command again on file1.txt, the server will now respond with 'HTTP/1.1 204 (No Content)' As this file no longer exists. This 
	request is then logged as appropriate, in line with extension 3.
