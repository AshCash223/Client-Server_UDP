import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
            byte[] rrq; //byte array for the requset packet
            rrq = fileName.getBytes(); //converting the file name to bytes

            DatagramPacket rp = new DatagramPacket(rrq, rrq.length, ia, portNumb);//rp = request packet
            cs.send(rp); //client server sends the rrq packet

        }catch(Exception e){
            System.err.println(e);
        }
    }

}
