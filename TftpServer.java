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

            DatagramSocket ds = new DatagramSocket(); // datagram socket for sending packets from the server to client
            File file = new File(filename); //create file object for requested file name
            if (!file.exists()) { //check if file exists in the server
                System.err.println("file " + filename + " does not exist"); //error message for the server to recieve
                byte[] errorMessage = "file does not exist".getBytes(); //Convert the string to a byte array
                byte[] errorPacket = new byte[1 + errorMessage.length]; 
                errorPacket[0] = ERROR; //set the type for packet
                System.arraycopy(errorMessage, 0, errorPacket, 1, errorMessage.length); //array copy to combine the byte arrays for the error packet
                DatagramPacket ep = new DatagramPacket(errorPacket, errorPacket.length, req.getAddress(), req.getPort()); //create and send the data packet to the user
                ds.send(ep); 
            }

            //file exits so continue code
            System.out.println("file exists"); 

            try (FileInputStream fis = new FileInputStream(file)) {//input stream to read file bytes

                InetAddress clientAddress = req.getAddress(); //ip of client
                int clientPort = req.getPort(); //port of client
                byte[] buf = new byte[512]; //buf byte array to read 512 bytes at a time
                int blockNumber = 1; //data block id
                int bytesRead; //byte reader
                
                while ((bytesRead = fis.read(buf)) != -1) {//while file has bytes to read

                    byte[] array = new byte[514]; //byte array for data packets (514, 2 more bytes for type and block number)                    
                    array[0] = DATA; //declare type and add block number
                    array[1] = (byte) blockNumber; 
                    System.arraycopy(buf, 0, array, 2, buf.length); //assembeling the data packet to send to the client
                    DatagramPacket sendPacket; 
                    if(bytesRead < 512){ //data packets less than 512 bytes
                        sendPacket = new DatagramPacket(array, bytesRead + 2, clientAddress, clientPort); //adjust the size of datagram packet 
                    } else{ //data packets that are 512 bytes
                       sendPacket = new DatagramPacket(array, array.length, clientAddress, clientPort);
                    }
                    sendPackets(ds, sendPacket, blockNumber); //call sendPackets method

                    //indicating packet sent and moving on to the next data packet in the loop
                    
                    System.out.println("received ACK number: " + blockNumber); //indicating ack 
                    blockNumber++;
                }
                System.out.println("++++++++++++++++++");
                System.out.println("+File transmitted+"); //file is finished being transmitted
                System.out.println("++++++++++++++++++");

            } catch (IOException e) { //catching a input and output exception
                System.err.println("Input and Output error: " + e);
            }
        } catch (Exception e) { //catch for any other exception
            System.err.println("system error: " + e);
        }
        return;
    }

    private boolean sendPackets(DatagramSocket ds, DatagramPacket dp, int blockNumber)
    {
        
        System.out.println("Sending data packet: " + blockNumber); 
        int attempts = 0; 
        try {
            ds.setSoTimeout(5000); //setting a timeout for the datagram socket to ensure ACK is received

            while (attempts < 5) {

                ds.send(dp); //Send DATA packet
                //Receiving the ACK
                byte[] ackBuffer = new byte[2]; 
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                ds.receive(ackPacket); 

                if (ackPacket.getLength() == 2 && ackPacket.getData()[0] == ACK && blockNumber == ackPacket.getData()[1]) {
                    //return true if ACK packet is received successfully else retranmit datapacket again
                    return true;
                }
                attempts++;
            }
        } catch (SocketTimeoutException er) {//timeout exception therefore attemp another retransmission
            attempts++;
        } catch (IOException e) {//input output exception
            System.err.println(e);
            return false;
        }
        return false; //give up and move on to the next datapacket after 5 attempts
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
