import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.util.*;


public class WebServlet {

    /*
Our first implementation of the Web server will be multi-threaded, where the processing of each incoming request will
take place inside a separate thread of execution. This allows the server to service multiple clients in parallel, or to
perform multiple file transfers to a single client in parallel. When we create a new thread of execution, we need to
pass to the Thread's constructor an instance of some class that implements the Runnable interface. This is the reason
that we define a separate class called HttpRequest.
 */


    public static void main(String argv[]) throws Exception {
        //Set the port number
        int port = 8080;

        //establish socket
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = new Socket();

        /*
        open a socket and wait for a TCP connection request. Because we will be servicing request messages indefinitely,
         we place the listen operation inside of an infinite loop. This means we will have to terminate the Web server
         by pressing ^C on the keyboard.
         */
        //Establish the listen socket
        //?
        //Process HTTP service requests in an infinite loop
        while (true) {
            socket = serverSocket.accept();
  /*
        When a connection request is received, we create an HttpRequest object, passing to its constructor a reference
        to the Socket object that represents our established connection with the client.
         */
            //Construct an object to process the HTTP request message
            HttpRequest request = new HttpRequest(socket);

            //create a new thread to process the request
            Thread thread = new Thread(request);

            //start the thread
            thread.start();

        }


    }

    static final class HttpRequest implements Runnable {
        final static String CTRLF = "\r\n";
        Socket socket;

        //constructor
        public HttpRequest(Socket socket) throws Exception {
            this.socket = socket;
        }

        //Implement the run method of the runnable interface
        public void run() {
            try {
                processRequest();

            } catch (Exception e) {
                System.out.println(e);
            }
        }

        public void processRequest() throws Exception {
            //Get a reference to the socket's input and output streams
            InputStream is = socket.getInputStream();
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            Reader reader = new InputStreamReader(is);
            //set up input stream filters
            //?
            BufferedReader br = new BufferedReader(reader);
            //continued

            String requestLine = br.readLine();

            //extract filename from requestline
            StringTokenizer tokens = new StringTokenizer(requestLine);
            tokens.nextToken(); //skip over next method which should be 'get'
            String fileName = tokens.nextToken();
            // Prepend a "." so that file request is within the current directory.
            fileName = "." + fileName;

            //Open the requested file
            FileInputStream fis = null;
            boolean fileExists = true;

            try{
                fis = new FileInputStream(fileName);
            } catch(FileNotFoundException e){
                fileExists = false;
            }

            //display the current request line
            System.out.println();
            System.out.println(requestLine);

            //get and display the header lines
            String headerLine = null;
            while ((headerLine = br.readLine()).length() != 0) {
                System.out.println(headerLine);
            }
            //construct the response message
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            if(fileExists){
                statusLine = "HTTP/1.1 200 OK" + CTRLF;
                contentTypeLine = "Content-type: " + contentType(fileName) + CTRLF;

            }else {
                statusLine = "HTTP/1.1 404 Not Found" + CTRLF;;
                contentTypeLine = "Content-type: " + contentType(fileName) + CTRLF;;
                entityBody = "<HTML>" +
                        "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                        "<BODY>Not Found</BODY></HTML>";
            }

            // Send the status line.
            os.writeBytes(statusLine);

// Send the content type line.
            os.writeBytes(contentTypeLine);

// Send a blank line to indicate the end of the header lines.
            os.writeBytes(CTRLF);

            // Send the entity body.
            if (fileExists)	{
                sendBytes(fis, os);
                fis.close();
            } else {
                os.writeBytes(entityBody);
            }

            //close streams and socket
            os.close();
            br.close();
            socket.close();
        }



        private static void sendBytes(FileInputStream fis, OutputStream os)
                throws Exception
        {
            // Construct a 1K buffer to hold bytes on their way to the socket.
            byte[] buffer = new byte[1024];
            int bytes = 0;

            // Copy requested file into the socket's output stream.
            while((bytes = fis.read(buffer)) != -1 ) {
                os.write(buffer, 0, bytes);
            }
        }

        private static String contentType(String fileName)
        {
            if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                return "text/html";
            }

            if(fileName.endsWith(".gif")){
		        return "image/gif";
        }
            if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
		        return "image/jpg";
        }
            if(fileName.endsWith(".png")){
                return "image/png";
            }
            return "application/octet-stream";
        }
    }
}

