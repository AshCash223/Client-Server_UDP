import java.io.*;
import java.io.IOException;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                "usage: <hostname>, <portnumber> and <file-requested>");
            return;
        }

        String hostName = args[0];
        int portNumb = Integer.parseInt(args[1]);
        String fileName = args[2];

        try {
            DatagramSocket cs = new DatagramSocket(); // clients datagram socket
            InetAddress ia = InetAddress.getByName(hostName);

            byte[] rrq = new byte[fileName.length() + 1]; // byte array for the request packet
            rrq[0] = 1; // converting the file name to bytes

            // Convert the file name to a byte array
            byte[] fileNameBytes = fileName.getBytes();

            // Copy the file name bytes into the rrq array starting from index 1
            System.arraycopy(fileNameBytes, 0, rrq, 1, fileNameBytes.length);

            DatagramPacket rp = new DatagramPacket(rrq, rrq.length, ia, portNumb); // rp = request packet
            cs.send(rp); // client server sends the rrq packet

            int expectedBN = 1; // expected block number
            FileOutputStream fos = new FileOutputStream(fileName); //file output stream to write the file
            System.out.println("Sent request, waiting for response"); // msg to indicate request has been sent


            while (true) {
                byte[] buffer = new byte[514]; // buffer to receive data blocks
                DatagramPacket response = new DatagramPacket(buffer, buffer.length); // datagram packet for received
                cs.receive(response);

                byte[] recievedPacket = new byte[514]; // receivedPacket byte array to extract and

                recievedPacket = response.getData(); // extracts receivedPacket from response
                if (response.getLength() < 2) {
                    return;
                }

                int blocknumber = recievedPacket[1]; // the block number should be in the 2nd

                // System.out.println(expectedBN); testing values
                // System.out.println(blocknumber);

                if (recievedPacket[0] == 4) { // Check if the packet is an error packet
                    // Extract the error message from the packet (starting at byte 1)
                    String errorMsg = new String(recievedPacket, 1, response.getLength() - 1);

                    // Print the error message
                    System.out.println("Error received: " + errorMsg);

                    return; // Exit or handle the error appropriately
                }

                if (recievedPacket[0] != 2) {
                    System.out.println("wrong packet " + recievedPacket[0]);
                    return;
                }

                byte[] ackPacket = new byte[2]; // creating an ack packet to send the server
                ackPacket[0] = 3; // initializing the ackPacket's type
                ackPacket[1] = (byte) blocknumber; // adding the block number to the second

                DatagramPacket ackResponse = new DatagramPacket(ackPacket, ackPacket.length, ia, portNumb); // forging the ack response

                if (expectedBN == blocknumber) {
                    fos.write(recievedPacket, 2, response.getLength() - 2);
                    System.out.println("transfered data packet number: " + blocknumber + " Acknowledgment sending...");
                    cs.send(ackResponse); // sending the ackResponse to the server
                    expectedBN++;
                }
            }
        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
