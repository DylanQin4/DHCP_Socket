package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Process;
import java.nio.charset.StandardCharsets;

import DHCP.*;

public class Client {
    public static void runCmd(String ip) {
        String serverDNS = "196.192.32.5";
        String subnet_mask = "255.255.255.0";
        System.out.println("Change IP to " + ip);
        String[] command = { "cmd", };
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println("netsh int ip set address WI-FI static " + ip + " " + subnet_mask);
            stdin.println("netsh int ip set dns WI-FI static " + serverDNS + " primary");
            stdin.close();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ipToString(byte[] ip, String separator){
        String[] hex = new String[ip.length];
        for (int i = 0; i < ip.length; i++) {
            hex[i] = String.format("%16f", ip[i]);
        }
        return String.join(separator, hex);
    }

    public static void main(String[] args) {
        try {
            System.out.println("--------Client--------\n");
            DHCPMessage dhcpMessage = new DHCPMessage();
            InetAddress IPAddress = InetAddress.getByName("localhost"); /* UDP subnet broadcast address */
            int sPort = dhcpMessage.SERVER_PORT; /* UDP server port number */
            int cPort = dhcpMessage.CLIENT_PORT; /* UDP server port number */		
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();  //   Client hardware address

            DHCPSocket dhcpSocket = new DHCPSocket(cPort);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ByteArrayInputStream bais;
            ObjectInputStream ois;

            // ----------SEND DHCPDISCOVER----------
            DHCPMessage DHCPDISCOVER = dhcpMessage.DHCPDISCOVER(mac, IPAddress.getAddress());
            oos.writeObject(DHCPDISCOVER);
            oos.flush();
            // get the byte array of the object
            byte[] Buf= baos.toByteArray();
            int number = Buf.length;
            // int -> byte[]
            for (int i = 0; i < 4; ++i) {
                int shift = i << 3; // i * 8
                sendData[3-i] = (byte)((number & (0xff << shift)) >>> shift);
            }
            DatagramPacket packetDISCOVER = new DatagramPacket(sendData, sendData.length, IPAddress, sPort);
            dhcpSocket.send(packetDISCOVER);
            // now send the payload
            packetDISCOVER = new DatagramPacket(Buf, Buf.length, IPAddress, sPort);
            dhcpSocket.send(packetDISCOVER);
            System.out.println("--[DHCP Discover] Send to Server DHCP");
            
            // ----------RECEIVE DHCPOFFER----------
            DatagramPacket packetOFFER = new DatagramPacket(receiveData, receiveData.length);
            dhcpSocket.receive(packetOFFER);
            int len = 0;
            // byte[] -> int
            for (int i = 0; i < 4; ++i) {
                len |= (receiveData[3-i] & 0xff) << (i << 3);
            }
            // now we know the length of the payload
            byte[] buffer = new byte[len];
            packetOFFER = new DatagramPacket(buffer, buffer.length );
            dhcpSocket.receive(packetOFFER);
            bais = new ByteArrayInputStream(buffer);
            ois = new ObjectInputStream(bais);
            dhcpMessage = (DHCPMessage)ois.readObject();
            System.out.println("--[DHCP Offer] Received from Server");

            // ----------SEND DHCPREQUEST----------
            DHCPMessage DHCPREQUEST = dhcpMessage.DHCPREQUEST(dhcpMessage.getXid(), dhcpMessage.getChaddr());
            oos.writeObject(DHCPREQUEST);
            oos.flush();
            Buf= baos.toByteArray();
            number = Buf.length;
            // int -> byte[]
            for (int i = 0; i < 4; ++i) {
                int shift = i << 3; // i * 8
                sendData[3-i] = (byte)((number & (0xff << shift)) >>> shift);
            }
            DatagramPacket packetREQUEST = new DatagramPacket(sendData, sendData.length, IPAddress, sPort);
            dhcpSocket.send(packetREQUEST);
            // now send the payload
            packetREQUEST = new DatagramPacket(Buf, Buf.length, IPAddress, sPort);
            dhcpSocket.send(packetREQUEST);
            System.out.println("--[DHCP Request] Send to Server DHCP");

            // ---------RECEIVE DHCPACK---------
            DatagramPacket packetPACK = new DatagramPacket(receiveData, receiveData.length);
            dhcpSocket.receive(packetPACK);
            len = 0;
            // byte[] -> int
            for (int i = 0; i < 4; ++i) {
                len |= (receiveData[3-i] & 0xff) << (i << 3);
            }
            // now we know the length of the payload
            buffer = new byte[len];
            packetPACK = new DatagramPacket(buffer, buffer.length );
            dhcpSocket.receive(packetPACK);
            bais = new ByteArrayInputStream(buffer);
            ois = new ObjectInputStream(bais);
            dhcpMessage = (DHCPMessage)ois.readObject();
            System.out.println("--[DHCP Pack] Received from Server\n");
            // CHANGE IP
            String yIP = new String(dhcpMessage.getYiaddr());
            System.out.println("IP: "+yIP);
            runCmd(yIP);

            dhcpSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
        }
    }
    public void sendPackDISCOVER(){

    }

}
