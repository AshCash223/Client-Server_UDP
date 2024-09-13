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

                byte[] buffer = new byte[514];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                System.out.println("Sent request, waiting for response");
                cs.receive(response);
                //String quote = new String(buffer, 0, response.getLength());
                //System.out.println(quote);
                byte[] getData = new byte[514];
                getData = response.getBytes();
                byte blocknumber = getData[1];

               if(getData[0] == 2) {
                    byte[] ackPacket = new byte[2];
                    ackPacket[0] = 3;
                    ackPacket[1] = blocknumber;
                    DatagramPacket ackResponse = new DatagramPacket(ackPacket, ackPacket.length, ia, portNumb);
                    cs.send(ackResponse);
                 } else if(getData[0] == 4) {

                }
                System.out.println();
                Thread.sleep(3000);

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
