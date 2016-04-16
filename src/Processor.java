import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Processor extends Thread {

	private Socket socket;
	private InputStream in;
	private PrintStream out;
	private final static String WEB_ROOT = "";
	public final static String err400 = "Bad Request";
    public final static String err404 = "Not Found";

	public Processor() {

	}

	public Processor(Socket socket) {
		try {
			in = socket.getInputStream();
			
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void sendErrorMessage (int errorCode, String errorMessage) {
		out.println("HTTP/1.0" + errorCode +  errorMessage);
		out.println("content-type: text/html");
		out.println();
		out.println("<html>");
		out.println("<title>Error Message");
		out.println("</title>");
		out.println("<body>");
		out.println(errorCode + " " + errorMessage);
		out.println("</body>");
		out.println("</html>");
		
		out.flush();
		out.close();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

    public void run() {
    	sendFile(parse(in));
    	
    }
    
    public String parse(InputStream in) {
    	
    	String fileName = "";
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	try{
    		String httpMsg = br.readLine();
    		System.out.println("HttpMessage" + httpMsg);
    		String[] content = httpMsg.split("");
    		if(content == null || content.length != 3) {
    			sendErrorMessage(400, err400);
    			
    		}
    		fileName = content[1];
    		
    		
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
    	return fileName;
    	
    }
	
	
	public void sendFile (String fileName) {
		File file = new File(WEB_ROOT + fileName);
		if (!file.exists()) {
			sendErrorMessage(404, err404);
			return;
		}
		try {
			InputStream inputStream = new FileInputStream(file);
			byte content[] = new byte[(int) file.length()];
			inputStream.read(content);
			
			out.println("HTTP/1.1 200 queryfile");
            out.println("content-length:" + content.length);
            out.println();
            out.write(content);
            out.flush();
            out.close();
            inputStream.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
