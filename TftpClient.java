import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

public class TftpClient {

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("usage: <hostname>, <portnumber> and <file-requested>");
            return;
        }

        String hostName = args[0];
        int portNumb = Integer.parseInt(args[1]);
        String fileName = args[2];

        try{

            DatagramSocket cs = new DatagramSocket(); //clients datagram socket
            InetAddress ia = InetAddress.getByName(hostName);

            byte[] rrq = new byte[fileName.length() + 1]; //byte array for the requset packet
            rrq[0] = 1; //converting the file name to bytes
        
            // Convert the file name to a byte array
            byte[] fileNameBytes = fileName.getBytes();

            // Copy the file name bytes into the rrq array starting from index 1
            System.arraycopy(fileNameBytes, 0, rrq, 1, fileNameBytes.length);


            DatagramPacket rp = new DatagramPacket(rrq, rrq.length, ia, portNumb);//rp = request packet
            cs.send(rp); //client server sends the rrq packet

            while (true) {

                byte[] buffer = new byte[514]; //buffer to recieve data blocks
                DatagramPacket response = new DatagramPacket(buffer, buffer.length); //datagram packet for recieved packets of data
                System.out.println("Sent request, waiting for response"); //indication the request has been sent
                cs.receive(response);
                byte[] recievedPacket = new byte[514]; //recievedPacket byte array to extract and validate recievedPacket packets
                recievedPacket = response.getData(); //extracts recievedPacket from response
                int blocknumber = recievedPacket[1]; //the block number should be in the 2nd index in the recievedPacket array

               if(recievedPacket[0] == 2) { //if the recievedPacket packet type is a 2 then recieved packet is a data packet
                    byte[] ackPacket = new byte[2]; //creating an ack packet to send the server
                    ackPacket[0] = 3; //initializing the ackpackets type
                    ackPacket[1] = (byte)blocknumber; //adding the block number to the second index of the ackpacket
                    DatagramPacket ackResponse = new DatagramPacket(ackPacket, ackPacket.length, ia, portNumb); //forging the ack response
                    cs.send(ackResponse); //sending the ackresponse to the server
                 } else if(recievedPacket[0] == 4) { //if the recievedPackets type is a 4 then an error packet was sent
                    //when error packet is recived what should you do?
                }
                System.out.println();
                Thread.sleep(3000); //3 second delay for testing

            }
        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public boolean ackrecived(boolean recived){

        return recived;
    }

}
