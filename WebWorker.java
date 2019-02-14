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
// Program 2 -- Java WebServer with images

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;

public class WebWorker implements Runnable
{

// Variable Declarations
private Socket socket;
String userDirectory = System.getProperty("user.dir");
WebHelper helper = new WebHelper();

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
        
        if(helper.getErrorCode() == 200) {
        
			if(contentFile.contains(".html"))
				helper.setMimeType("text/html");
			else if(contentFile.contains(".gif"))
				helper.setMimeType("image/gif");
			else if(contentFile.contains(".jpeg") || contentFile.contains(".jpg"))
				helper.setMimeType("image/jpeg");
			else if(contentFile.contains(".png"))
				helper.setMimeType("image/png");
			else if(contentFile.contains(".ico"))
				helper.setMimeType("image/x-icon");
			else
				helper.setMimeType("text/html");
				
		} 
		else 
            helper.setMimeType("text/html");
        
        writeHTTPHeader(os, helper.getMimeType(), contentFile);
        writeContent(os, helper.getMimeType(), contentFile);
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
                for(int i = 0; i < path.length(); i++)
                    if(path.charAt(i) == ' ')
                        path = path.substring(0,i);
            }
            System.err.println("Request line: ("+line+")");
            if (line.length()==0) 
                break;
        } 
        catch (Exception e) {
            System.err.println("Request error: "+e);
            break;
        }
    }

	if(helper.checkFileExists(userDirectory+path) == true)
        helper.setErrorCode(200);
	else
		helper.setErrorCode(404);
    return path;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String contentPath) throws Exception
{
    String path = userDirectory + contentPath;
    helper.setDate("MST");
	try {
		FileReader contentFile = new FileReader(path);
        os.write("HTTP/1.1 200 OK\n".getBytes());
        System.out.println("Content Collected: "+path+" successfully!");
    }
    // If file does not exist: errorcode - 404
    catch(FileNotFoundException fnfe) {
        os.write("HTTP/1.1 404 ERROR\n".getBytes());
    }
    
    // Write all data of header
    os.write("Date: ".getBytes());
    os.write(helper.getDate().getBytes());
    os.write("\n".getBytes());
    os.write("Server: Mason's very own server\n".getBytes());
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
private void writeContent(OutputStream os, String contentType, String contentPath) throws Exception
{
	String content = "";
	String path = userDirectory + contentPath;
	
	if(contentType.contains("text/html")) {
        // If the file exists
        try {
            File fileName = new File(path);
            BufferedReader inBuffer = new BufferedReader(new FileReader(fileName));
            
            // Loop to write out the contents on the line from the given file
            while((content = inBuffer.readLine()) != null) {
                if(content.contains("<cs371date>"))
					content = helper.getDate(); // Replace <cs371date> tag with today's date
                if(content.contains("<cs371server>"))
					content = "This is my ID tag"; // Replace <cs371server> with specified string
                os.write(content.getBytes());
                os.write( "\n".getBytes());
            }
        } 
        catch(FileNotFoundException fnfe) { // If file does not exist
            System.err.println("ERROR: File "+path+" does not exist!");
            write404Content(os,path);
        }
    }
	else if(contentType.contains("image")) {
		try{
            File file = new File(path);
            int fileLength = (int) file.length();
            FileInputStream inputStream = new FileInputStream(file);
            
            byte allBytes[] = new byte[fileLength];
            
            inputStream.read(allBytes);
            os.write(allBytes);
		} catch (FileNotFoundException fnfe) {
            System.err.println("ERROR: Image not found at: " +path);
		}
	} else {
		write404Content(os,path);
	}
}

    /**
    * A Simple method to put into OutputStream the ERRORCODE 404 content
    * @param os is the OutputStream object to write to
    * @param path is the path of the object being requested
    **/
    public void write404Content(OutputStream os, String path) throws Exception
    {
        os.write("<html>\n<body bgcolor = \"#87CEFA\">\n".getBytes());
        os.write("<h1><b>404: Not Found</b></h1>\n".getBytes());
        os.write("The page you are looking for does not exist!\n".getBytes());
        os.write("Unable to locate:".getBytes());
        os.write(path.getBytes());
    }

} // end class
