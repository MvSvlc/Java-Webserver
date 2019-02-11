/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection.
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring
* the fact that the entirety of the webserver execution might be handling
* other clients, too.
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format).
*
**/

// Mason Salcido
// CS 371
// Program 1 -- Java WebServer

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

// Variable Declarations
String fileName;
private Socket socket;
int errorCode;
Date date = new Date();
DateFormat dateF = DateFormat.getDateTimeInstance();

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      // Store file name in contentFile for use throughout program
      String contentFile = readHTTPRequest(is);
      writeHTTPHeader(os,"text/html",contentFile);
      writeContent(os, contentFile);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header and return the file path in a String.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String path = "";
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         // Parse the "GET" from request line and recieve file name
         if(line.contains("GET ")) {
			path = line.substring(4);
			for(int i = 0; i < path.length(); i++) {
				if(path.charAt(i) == ' ')
					path = path.substring(0,i);
			}
			path = "." + path;
		 System.err.println("Path collected: " + path);
		}
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return path;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String contentPath) throws Exception
{

    File contentFile = new File(contentPath);
    // Check to see if file exists if so: errorcode - 200
    if(contentFile.exists()) {
        os.write("HTTP/1.1 200 OK\n".getBytes());
        errorCode = 200;
    }
    // If file does not exist: errorcode - 404
    else {
        os.write("HTTP/1.1 404 ERROR\n".getBytes());
        System.err.println("ERROR: File " + contentFile.toString() + " does not exist!");
        errorCode = 404;
    }
    
    // Write all data of header
    os.write("Date: ".getBytes());
    os.write((dateF.format(date)).getBytes());
    os.write("\n".getBytes());
    os.write("Server: Mason's very own server\n".getBytes());
    os.write("Content-Length: 438\n".getBytes());
    os.write("Connection: close\n".getBytes());
    os.write("Content-Type: ".getBytes());
    os.write(contentType.getBytes());
    os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
    return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String contentPath) throws Exception
{
	String content = "";
	String pathCopy = contentPath;
	
	// If the file exists
	if(errorCode == 200) {
        File fileName = new File(pathCopy);
 		BufferedReader inBuffer = new BufferedReader(new FileReader(fileName));
 		
 		// Loop to write out the contents on the line from the given file
 		while((content = inBuffer.readLine()) != null) {
 			if(content.contains("<cs371date>"))
 				content += dateF.format(date); // Replace <cs371date> tag with today's date
 			else if(content.contains("<cs371server>"))
 				content += "This is my ID TAG"; // Replace <cs371server> with specified string
 			os.write(content.getBytes());
 			os.write( "\n".getBytes());
 		}
    } else // If file does not exist
        write404Content(os);
}
/**
* A Simple method to put into OutputStream the ERRORCODE 404
* @param os is the OutputStream object to write to
**/
private void write404Content(OutputStream os) throws Exception
{
    os.write("<body bgcolor = \"#87CEFA\">".getBytes());
    os.write("<h1><b>404: Not Found</b></h1>".getBytes());
    os.write("The page you are looking for does not exist!".getBytes());
}

} // end class
