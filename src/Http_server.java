import java.net.*; 
import java.io.*; 
import java.util.*;

public class Http_server
{ 
  //initialize socket and input stream 
  private static int PORT = 8080;
  private static boolean DEBUG = false; 
  
	public static void main(String args[]) throws IOException 
	{ 
    ServerSocket server = new ServerSocket(PORT);
    if (DEBUG) {
    	System.out.println("Listening on port 8080");
    }
    while(true){
      // Client Connection, initialization 
      Socket socket = server.accept();
      InputStreamReader isr = new InputStreamReader(socket.getInputStream());
      BufferedReader in = new BufferedReader(isr);
      Http_parser req = new Http_parser(isr);
      OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
      
      int resp_type = req.parseRequest();
      /* Sample responose
       * HTTP/1.1 200 OK\r\n
		Date: Sun, 26 Sep 2010 20:09:20 GMT\r\n
		Server: Apache/2.0.52 (CentOS)\r\n
		Last-Modified: Tue, 30 Oct 2007 17:00:02 GMT\r\n
		ETag: "17dc6-a5c-bf716880"\r\n
		Accept-Ranges: bytes\r\n
		Content-Length: 2652\r\n
		Keep-Alive: timeout=10, max=100\r\n
		Connection: Keep-Alive\r\n
		Content-Type: text/html;
		charset=ISO-8859-1\r\n
		\r\n
		data data data data data
       */
      if(DEBUG) {
          System.out.println("Request--------------------------------------------------");
          Hashtable headers = req.getHeaders();
          System.out.println(req.getMethod() +" "+req.getRequestURL() + " HTTP/" + req.getVersion());
          System.out.println(headers.toString());
          
      }
      /* Data, Server, Content-Length */
      String response = "HTTP/1.1 " + req.getHttpReply(resp_type) + "\r\n" + req.getDateHeader() + "\r\n" + "Content-Length: 0\r\n";
      socket.getOutputStream().write(response.getBytes("UTF-8"));
      
      if (DEBUG) {
    	  System.out.println("Response--------------------------------------------------");
    	  System.out.println(response);
    	  System.out.println("Closing connection");
      }
      // close connection 
      out.close();
      in.close(); 
      socket.close(); 
    }
  }
} 
