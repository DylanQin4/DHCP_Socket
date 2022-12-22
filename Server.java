package server;

import java.io.*;
import java.net.*;
import java.util.*;

import DHCP.*;

public class Server {
    public static String macToString(byte[] mac, String separator){
        String[] hex = new String[mac.length];
        for (int i = 0; i < mac.length; i++) {
            hex[i] = String.format("%02X", mac[i]);
        }
        return String.join(separator, hex);
    }

    //Loading the ips from the text file,
    //returning one IP and removing it from the file.
    public static String popOneIP(String fileName){

        // This will reference one line at a time
        String line = null;

        Vector<String> IPs = new Vector();
        String IP = "";

        try {
            // Prepare BufferedReader.
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                IPs.add(line);
            }

            //save the last element to var to be returned
            IP = IPs.lastElement().toString();

            //remove last element just used
            IPs.removeElement(IPs.lastElement());
            //IPs.trimToSize();   //trim capacity

            //print to check
            //System.out.println(IPs.toString());
            System.out.println("IP Count: " + IPs.size() + " -> " + (IPs.size()-1) + " left\n");

            //overwrite file with updated list
            File newIPs = new File(fileName);
            FileWriter IPWriter = new FileWriter(newIPs, false);// false to overwrite.

            //loop through vector to write all IPS
            for(int i = 0; i < IPs.size(); i++){
                IPWriter.write(IPs.get(i) + "\n");
            }
            IPWriter.close();

            // close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }

        return IP;
    }

    public static String isExistMac(String mac){
        try {
            // Create Reader to read a file.
            Reader reader = new FileReader("data/MACForIPStatic.txt");
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            String[] row = null;
            while((line = br.readLine())!= null) {
                row = line.split("/");
                if (row.length == 2 && row[0].compareTo(mac) == 0) {
                    return row[1];
                }
            }
            br.close();
        } catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        return "0";
    }

    public static void main(String[] args) {
        try {
            DHCPMessage dhcpMessage = new DHCPMessage();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            int sPort = dhcpMessage.SERVER_PORT;
            int cPort = dhcpMessage.CLIENT_PORT;		
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            String yIP = "";    //Your IP client
            InetAddress IPClient;
            System.out.print("\n--UDP Server on port: " + sPort);

            DHCPSocket dhcpSocket = new DHCPSocket(sPort, IPAddress);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ByteArrayInputStream bais;
            ObjectInputStream ois;

            while (true) {
                /* Waiting for client's message */
                System.out.println("\n******** Waiting For Next Client ********\n");
                
                //---------RECEIVE DHCPDISCOVER FROM CLIENT---------
                DatagramPacket packetDISCOVER = new DatagramPacket(receiveData, receiveData.length );
                dhcpSocket.receive(packetDISCOVER);
    
                int len = 0;
                // byte[] -> int
                for (int i = 0; i < 4; ++i) {
                    len |= (receiveData[3-i] & 0xff) << (i << 3);
                }
                // now we know the length of the payload
                byte[] buffer = new byte[len];
                packetDISCOVER = new DatagramPacket(buffer, buffer.length );
                dhcpSocket.receive(packetDISCOVER);
    
                bais = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(bais);
                dhcpMessage = (DHCPMessage)ois.readObject();
                IPClient = InetAddress.getByAddress(dhcpMessage.getCiaddr());// get IP Client
                if (isExistMac(macToString(dhcpMessage.getChaddr(), "-")).compareTo("0") != 0) {
                    System.out.println("Static IP");
                    yIP = isExistMac(macToString(dhcpMessage.getChaddr(), "-"));
                } else {
                    System.out.println("Dynamic IP");
                    yIP = popOneIP("data/IPaddress.txt");
                }

                // --------SEND DHCPOFFER---------
                DHCPMessage DHCPOFFER = dhcpMessage.DHCPOFFER(dhcpMessage.getXid(), 
                                                                dhcpMessage.getGiaddr(), 
                                                                dhcpMessage.getChaddr(), 
                                                                yIP.getBytes(),
                                                                dhcpMessage.getSiaddr(), 
                                                                dhcpMessage.getCiaddr());
                oos.writeObject(DHCPOFFER);
                oos.flush();
                // get the byte array of the object
                byte[] Buf= baos.toByteArray();
                int number = Buf.length;
                // int -> byte[]
                for (int i = 0; i < 4; ++i) {
                    int shift = i << 3; // i * 8
                    sendData[3-i] = (byte)((number & (0xff << shift)) >>> shift);
                }
                DatagramPacket packetOFFER = new DatagramPacket(sendData, sendData.length, IPAddress, cPort);
                dhcpSocket.send(packetOFFER);
                // now send the payload
                packetOFFER = new DatagramPacket(Buf, Buf.length, IPAddress, cPort);
                dhcpSocket.send(packetOFFER);
                System.out.println("--[DHCP Offer]");

                //---------RECEIVE DHCPREQUEST FROM CLIENT---------
                DatagramPacket packetREQUEST = new DatagramPacket(receiveData, receiveData.length);
                dhcpSocket.receive(packetREQUEST);
                len = 0;
                // byte[] -> int
                for (int i = 0; i < 4; ++i) {
                    len |= (receiveData[3-i] & 0xff) << (i << 3);
                }
                // now we know the length of the payload
                buffer = new byte[len];
                packetREQUEST = new DatagramPacket(buffer, buffer.length );
                dhcpSocket.receive(packetREQUEST);
                bais = new ByteArrayInputStream(buffer);
                ois = new ObjectInputStream(bais);
                dhcpMessage = (DHCPMessage)ois.readObject();
                System.out.println("--[DHCP Request] Received from Client");

                // --------SEND DHCPACK---------
                DHCPMessage DHCPACK = dhcpMessage.DHCPACK(dhcpMessage.getXid(), 
                                                            yIP.getBytes(), 
                                                            InetAddress.getByName(yIP),
                                                            dhcpMessage.getSiaddr(), 
                                                            dhcpMessage.getCiaddr(), 
                                                            dhcpMessage.getChaddr(), 
                                                            dhcpMessage.getGiaddr());
                
                oos.writeObject(DHCPACK);
                oos.flush();
                // get the byte array of the object
                Buf= baos.toByteArray();
                number = Buf.length;
                // int -> byte[]
                for (int i = 0; i < 4; ++i) {
                    int shift = i << 3; // i * 8
                    sendData[3-i] = (byte)((number & (0xff << shift)) >>> shift);
                }
                DatagramPacket packetPACK = new DatagramPacket(sendData, sendData.length, IPAddress, cPort);
                dhcpSocket.send(packetPACK);
                // now send the payload
                packetPACK = new DatagramPacket(Buf, Buf.length, IPAddress, cPort);
                dhcpSocket.send(packetPACK);
                
                System.out.println("--[DHCP Pack] Sent with " + yIP + "\n");

                System.out.println("MAC Client " + macToString(dhcpMessage.getChaddr(), "-"));
                
            }

            
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
        }
    }
}
