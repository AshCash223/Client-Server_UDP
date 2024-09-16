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
        try {
            DatagramSocket ds = new DatagramSocket(); // Create a new socket for sending DATA
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("file " + filename + " does not exist");
                // Convert the string "" to a byte array
                byte[] errorMessage = "file does not exist".getBytes(); // Convert the string to a byte array

                byte[] errorPacket = new byte[1 + errorMessage.length];

                errorPacket[0] = 4; // Error type byte

                // Copy the error message bytes into the errorPacket starting from index 1
                System.arraycopy(errorMessage, 0, errorPacket, 1, errorMessage.length);

                // Create the DatagramPacket with the error packet and send it
                DatagramPacket ep = new DatagramPacket(errorPacket, errorPacket.length, req.getAddress(), req.getPort()); 
                ds.send(ep); // Send the packet
            }

            System.out.println("file exists");

            try (FileInputStream fis = new FileInputStream(file)) {
                InetAddress clientAddress = req.getAddress(); // gets the ip of the client
                int clientPort = req.getPort(); // gets the port numb of the client
                byte[] buf = new byte[512];
                int blockNumber = 1;
                int bytesRead; // byte reader
                
                while ((bytesRead = fis.read(buf)) != -1) {

                    byte[] array = new byte[514]; // byte array for data packets
                    
                    array[0] = DATA;
                    array[1] = (byte) blockNumber;

                    System.out.println(bytesRead);
                    System.arraycopy(buf, 0, array, 2, buf.length); // copies the content of buf start from 0 then copy arrays content then add the buf.length
                    DatagramPacket sendPacket = new DatagramPacket(array, array.length, clientAddress, clientPort);

                    if(bytesRead < 512){
                        sendPacket = new DatagramPacket(array, bytesRead + 2, clientAddress, clientPort);
                        Retransmit(ds, sendPacket, blockNumber); // Send DATA packet
                        break;
                    } else{
                        Retransmit(ds, sendPacket, blockNumber); // Send DATA packet
                    }
                    System.out.println("Sent block #" + blockNumber);
                    blockNumber++;
                }
                System.out.println("File transmitted");

            } catch (IOException e) {
                System.err.println("Input and Output error: " + e);
            }
        } catch (Exception e) {

        }
        return;
    }

    private boolean Retransmit(DatagramSocket ds, DatagramPacket dp, int blockNumber)
    {
        int attempts = 0;

        System.out.println("received ack " + blockNumber);
        try {
            ds.setSoTimeout(1000);

            while (attempts < 5) {
                ds.send(dp); // Send DATA packet

                // Receive the ACK
                byte[] ackBuffer = new byte[2]; // Buffer to receive the ACK
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                ds.receive(ackPacket); // Block until ACK is received

                if (ackPacket.getLength() == 2 && ackPacket.getData()[0] == ACK && blockNumber == ackPacket.getData()[1]) {
                    return true;
                }

                attempts++;
            }
        } catch (SocketTimeoutException er) {
            attempts++;
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }
        return false;
    }

    public void run()
    {
        try {
            byte[] requestData = req.getData();
            if (requestData[0] == RRQ) { // Check if it's an RRQ using the first 2 bytes
                String filename = new String(requestData, 1, req.getLength() - 1); // making a string constructor extracting file at index 2
                System.out.println("Received RRQ for file: " + filename);
                sendfile(filename); // calling the send file method and parsing the file name to it
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public TftpServerWorker(DatagramPacket req)
    {
        this.req = req;
        // start(); //calling start instead of run directly to create a new thread
    }
}

class TftpServer
{
    public void start_server()
    {
        try {
            DatagramSocket ds = new DatagramSocket();
            System.out.println("TftpServer on port " + ds.getLocalPort()); // gets local port number for the datagram socket

            for (;;) {
                byte[] buf = new byte[1472]; // creating a byte array with a byte limit of 1472 bytes
                DatagramPacket p = new DatagramPacket(buf, 1472); // creating a datagram packet with the contents of buf(byte array) alongside max number of bytes
                ds.receive(p); // send the datagram packet to be received by the datagram socket
                
                TftpServerWorker worker = new TftpServerWorker(p); // calling the TftpServerWorker class and parsing the datagram packet on to it
                worker.start(); // starting the code into the TftpServerWorker
            }
        } catch (Exception e) { // display error message if an exception is caught
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
