import java.net.*;
import java.io.*;
import java.util.*;

class TftpServerWorker extends Thread
{
    private DatagramPacket req;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;

    private void sendfile(String filename)
    {
        File file = new File(filename);
        if (!file.exists()) {
            //send an error for a file not being found for the client
            return;
        }
        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */
	return;
    }

    public void run()
    {

        try {
            byte[] requestData = req.getData();
            if (requestData[0] == 0 && requestData[1] == RRQ) { // Check if it's an RRQ using the first 2 bytes
                String filename = new String(requestData, 2, req.getLength() - 2); //making a string constructor extracting file at index 2
                System.out.println("Received RRQ for file: " + filename);
                sendfile(filename);
            } else {
                System.out.println("Invalid request received.");
                //sending the error packet to the client
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	return;
    }

    public TftpServerWorker(DatagramPacket req)
    {
	    this.req = req;
        start(); //calling start instead of run directly to create a new thread
    }
}

class TftpServer{
        
    public void start_server()
    {
        try {
            DatagramSocket ds = new DatagramSocket();
            System.out.println("TftpServer on port " + ds.getLocalPort()); //gets local port number for the datagram socket

            for(;;) {
            byte[] buf = new byte[1472]; //creating a byte array with a byte limit of 1472 bytes
            DatagramPacket p = new DatagramPacket(buf, 1472); //creating a datagram packet with the contents of buf(byte array) alongside max number of bytes
            ds.receive(p); //send the datagram packet to be recived by the datagram socket
            
            TftpServerWorker worker = new TftpServerWorker(p); //calling the TftpServerWorker class and parsing the datagram packet on to it
            worker.start(); //starting the code into the TftpServerWorker

            }
        }
        catch(Exception e) { //display error message if an exception is caught
            System.err.println("Exception: " + e);
        }

        return;
    }

            public static void main(String args[])
            {
            TftpServer d = new TftpServer();
            d.start_server();
            }
}