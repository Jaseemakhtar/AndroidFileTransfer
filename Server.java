import java.io.*;
import java.net.*;
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(5273);
        } catch (IOException ex) {
            System.out.println("Can't setup server on this port number. ");
        }

        Socket socket = null;
        InputStream in = null;
        OutputStream os = null;
	FileOutputStream out = null;
	DataOutputStream dos = null;
	DataInputStream dis = null;

        try {
	    System.out.println("Waiting for client");
            socket = serverSocket.accept();
	    System.out.println("Connected client: " + socket);
        } catch (IOException ex) {
            System.out.println("Can't accept client connection. ");
        }

        try {
            in = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }

        try {
        dis  = new DataInputStream(in);
        dos = new DataOutputStream(os);
        String ack = dis.readUTF();
        if(ack.equals("receive")){
            dos.writeUTF("ok");
        }else{
            return;
        }
	    String fileName = dis.readUTF();
	    long fileLength = dis.readLong();

	    System.out.println("FileName: " + fileName);
	    System.out.println("FileLength: " + fileLength);
        out = new FileOutputStream(fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found. ");
        }catch (IOException ex){
	        System.out.println("Error: " + ex.getMessage());
	    }

        byte[] bytes = new byte[4 * 1024];

        int count = 0;
	    int total = 0;
        while ((count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
	        total = total + count;
        }

        System.out.println("Bytes Read: " + total);

        out.close();
        in.close();
        socket.close();
        serverSocket.close();
    }
}
