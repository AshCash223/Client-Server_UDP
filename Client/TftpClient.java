import java.io.*;
import java.io.IOException;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length != 3) { //if all expected items are not parsed to the system make the user retry
            System.err.println("usage: <hostname>, <portnumber> and <file-requested>"); //display msg for expected inputs
            return;
        }

        //declare all the variable inputs
        String hostName = args[0]; 
        int portNumb = Integer.parseInt(args[1]);
        String fileName = args[2];

        try {

            DatagramSocket cs = new DatagramSocket(); //clients datagram socket
            InetAddress ia = InetAddress.getByName(hostName); //ip address
            byte[] rrq = new byte[fileName.length() + 1]; //byte array for the request packet
            rrq[0] = 1; //declare type
            byte[] fileNameBytes = fileName.getBytes(); //Convert the file name to a byte array
            System.arraycopy(fileNameBytes, 0, rrq, 1, fileNameBytes.length);//combine the byte arrays to form the request packet
            DatagramPacket rp = new DatagramPacket(rrq, rrq.length, ia, portNumb); //rp = request packet
            cs.send(rp); // client server sends the rrq packet
            int expectedBN = 1; // expected block number
            FileOutputStream fos = new FileOutputStream(fileName); //file output stream to write the file
            System.out.println("Sent request, waiting for response"); // msg to indicate request has been sent

            while (true) {

                byte[] buffer = new byte[514]; //buffer to receive data packet
                DatagramPacket response = new DatagramPacket(buffer, buffer.length); //datagram packet for response packets
                cs.receive(response); //receiving packet
                byte[] recievedPacket = new byte[514]; 
                recievedPacket = response.getData(); //extracts data from the receivedPacket from response
                if (response.getLength() < 2) {
                    return; //return if packet is invalid
                }
                int blocknumber = recievedPacket[1]; //extracting the block numebr

                if (recievedPacket[0] == 4) { // Check if the packet is an error packet

                    String errorMsg = new String(recievedPacket, 1, response.getLength() - 1); //Extract the error message from the packet (starting at byte 1)
                    System.out.println("Error received: " + errorMsg); 
                    return; //exit if file does not exist
                }

                if (recievedPacket[0] != 2) { //pointless packed if its not a data packet

                    System.out.println("wrong packet " + recievedPacket[0]);
                    return;
                }

                byte[] ackPacket = new byte[2]; // creating an ack packet to send the server
                ackPacket[0] = 3; // initializing the ackPacket's type
                ackPacket[1] = (byte) blocknumber; // adding the block number to the second
                DatagramPacket ackResponse = new DatagramPacket(ackPacket, ackPacket.length, ia, portNumb); // forging the ack response

                if (expectedBN == blocknumber) { //if expected and current block numbers are equal

                    fos.write(recievedPacket, 2, response.getLength() - 2); //transmit the data packet
                    System.out.println("transfered data packet number: " + blocknumber + " Acknowledgment sending...");
                    cs.send(ackResponse); // sending the ackResponse to the server
                    expectedBN++;
                }
            }
        } catch (SocketTimeoutException ex) { //timeout exception
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
