import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

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
            System.err.println("wrong");
            //send an error for a file not being found for the client
            return;
        }

        System.out.println("file exists");
        try (FileInputStream fis = new FileInputStream(file)) {
            DatagramSocket ds = new DatagramSocket(); // Create a new socket for sending DATA
            InetAddress clientAddress = req.getAddress(); //gets the ip of the client
            int clientPort = req.getPort(); //gets the port numb of the client
            byte[] buf = new byte[512];
            int blockNumber = 1;
            int bytesRead; //byte reader

            while ((bytesRead = fis.read(buf)) != -1) {

                byte[] array = new byte[514];
                array[0] = DATA;
                array[1] = (byte)blockNumber;

                System.arraycopy(buf, 0, array, 2, buf.length); //copies the content of buf start from 0 then coppy arrays content then add the buf.lenght
                    
                    DatagramPacket sendPacket = new DatagramPacket(array, array.length, clientAddress, clientPort);
                    ds.send(sendPacket); // Send DATA packet
                    System.out.println("Sent block #" + blockNumber);

                    boolean recievedAckcieved = AckTrueOrfalase();

                    Thread.sleep(3000); //3 second delay for testing
                   
                blockNumber++;
            }

        }catch(Exception e){
            System.err.println("wrong");
        }
        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */
	return;
}

    public boolean AckTrueOrfalase(){

        return true;
    }

    public void run()
    {

        try {
            byte[] requestData = req.getData();
            if (requestData[0] == RRQ) { // Check if it's an RRQ using the first 2 bytes
                String filename = new String(requestData, 1, req.getLength() - 1); //making a string constructor extracting file at index 2
                System.out.println("Received RRQ for file: " + filename);
                sendfile(filename); //calling the send file method and parsing the file name to it
            } else if(requestData[0] == ACK){
                //String recievedAck = new String(requestData, 1, req.getLength() - 1); //making a string constructor extracting file at index 2
                int recievedBN = requestData[1];
                System.out.println("Received Ack for file: " + recievedBN);
                Thread.sleep(3000); //3 second delay for testing
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
        //start(); //calling start instead of run directly to create a new thread
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