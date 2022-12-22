package DHCP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Random;

import org.xml.sax.ext.DeclHandler;

public class DHCPMessage implements Serializable {
	public static final byte OP_REQUEST = 1;
	public static final byte OP_REPLY = 2;
	public static final byte DHCPDISCOVER = 1;
	public static final byte DHCPOFFER = 2;
	public static final byte DHCPREQUEST = 3;
	public static final byte DHCPDECLINE = 4;
	public static final byte DHCPACK = 5;
	public static final byte DHCPNAK = 6;
	public static final byte DHCPRELEASE = 7;
	public static final byte DHCPINFORM = 8;
	public static final int CLIENT_PORT = 68; // client port (by default)
	public static final int SERVER_PORT = 67; // server port (by default)
	public static InetAddress BROADCAST_ADDR = null;
	public static InetAddress YIPADDR = null;

	public static InetAddress getYIPADDR() {
		return YIPADDR;
	}
	public static void setYIPADDR(InetAddress yIPADDR) {
		YIPADDR = yIPADDR;
	}

	// -----------------------------------------------------------
	// Fields defining a dhcp message
	// -----------------------------------------------------------
	/**
	 * Operation Code.<br>
	 **/
	private byte op;

	/**
	 * Networktype as defined by
	 */
	private byte htype;

	/**
	 * Hardware address length (e.g. '6' for ethernet).
	 */
	private byte hlen;

	/**
	 * Client sets to zero, optionally used by relay-agents when booting via a relay-agent.
	 */
	private byte hops;

	/**
	 * Transaction ID, a random number chosen by the client, used by the client
	and server to associate messages and responses between a client and a
	server.
	 */
	private int xid;

	/**
	 * Filled in by client, seconds elapsed since client started trying to boot.
	 */
	private short secs;

	/**
	 * Flags for this message.<br>
	 * The leftmost bit is defined as the BROADCAST (B) flag.
	 */
	private short flags;

	/**
	 * Client IP address; filled in by client in DHCPREQUEST if verifying
	previously allocated configuration parameters.
	 */
	private byte ciaddr[] = new byte[4];

	/**
	 * 'your' (client) IP address.
	 */
	private byte yiaddr[] = new byte[4];

    /**
	 * IP address of next server to use in bootstrap; returned in DHCPOFFER,
	 * DHCPACK and DHCPNAK by server.
	 */
	private byte siaddr[] = new byte[4];

	/**
	 * Relay agent IP address, used in booting via a relay-agent.
	 */
	private byte giaddr[] = new byte[4];

	/**
	 * Client hardware address.
	 */
	private byte chaddr[] = new byte[16];

	/**
	 * Optional server host name, null terminated string.
	 */
	private byte sname[] = new byte[64];

	/**
	 * Boot file name, null terminated string; "generic" name or null in
	 * DHCPDISCOVER, fully qualified directory-path name in DHCPOFFER.
	 */
	private byte file[] = new byte[128];
	/**
	 * global port variable for this message
	 */
	private int gPort;

	/**
	 * The destination IP-Adress of this message
	 */
	private InetAddress destination_IP;

	static {
		try {
			BROADCAST_ADDR = InetAddress.getByName("255.255.255.255");
			YIPADDR = InetAddress.getByName("0.0.0.0");
			// broadcast address(by default)
		} catch (UnknownHostException e) {
			// Broadcast address must always exist
		}
	}
	
	// -----------------------------------------------------------
	// Messages
	// -----------------------------------------------------------
	public DHCPMessage DHCPDISCOVER(byte[] mac, byte[] cIP)throws Exception{
		DHCPMessage dhcpMessage = new DHCPMessage();
		dhcpMessage.setHtype(DHCPDISCOVER);
		dhcpMessage.setOp(OP_REQUEST);
		dhcpMessage.setHlen((byte)6);
		dhcpMessage.setHops((byte)0);
		dhcpMessage.setXid(new Random().nextInt());
		dhcpMessage.setChaddr(mac);
		dhcpMessage.setCiaddr(cIP);
		dhcpMessage.setSecs((byte)0);
		dhcpMessage.setFlags((short)0x8000);
		return dhcpMessage;
	}
	public DHCPMessage DHCPOFFER(int xID, byte[] gateway, byte[] mac, byte[] yIP, byte[] sIP, byte[] cIP){
        DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.setHtype(DHCPOFFER);
		dhcpMessage.setOp(OP_REPLY);
		dhcpMessage.setXid(xID);
		dhcpMessage.setGiaddr(gateway);
		dhcpMessage.setChaddr(mac);
		dhcpMessage.setHlen((byte)6);
		dhcpMessage.setSecs((byte)0);
		dhcpMessage.setYiaddr(yIP);
		dhcpMessage.setSiaddr(sIP);
		dhcpMessage.setCiaddr(cIP);
		dhcpMessage.setHops((byte)1);
        return dhcpMessage;
    }
	public DHCPMessage DHCPREQUEST(int xID, byte[] mac){
		DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.setHtype(DHCPREQUEST);
		dhcpMessage.setOp(OP_REQUEST);
		dhcpMessage.setXid(xID);
		dhcpMessage.setChaddr(mac);
		dhcpMessage.setHlen((byte) 6);
		dhcpMessage.setSecs((byte)0);
		dhcpMessage.setHops((byte)1);
        return dhcpMessage;
	}
	public DHCPMessage DHCPACK(int xID, byte[] yIP, InetAddress YIPADDR, byte[] sIP, byte[] cIP, byte[] mac, byte[] gateway){
		DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.setHtype(DHCPACK);
		dhcpMessage.setOp(OP_REPLY);
		dhcpMessage.setXid(xID);
		dhcpMessage.setYiaddr(yIP);
		dhcpMessage.setYIPADDR(YIPADDR);
		dhcpMessage.setSiaddr(sIP);
		dhcpMessage.setCiaddr(cIP);
		dhcpMessage.setChaddr(mac);
		dhcpMessage.setGiaddr(gateway);
		dhcpMessage.setHlen((byte) 6);
		dhcpMessage.setSecs((byte)0);
		dhcpMessage.setHops((byte)1);
        return dhcpMessage;
	}
	public DHCPMessage DHCPDECLINE(){
		DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.setHtype(DHCPDECLINE);
        return dhcpMessage;
	}
	public DHCPMessage DHCPRELEASE(int xID, byte[] cIP, byte[] mac){
		DHCPMessage dhcpMessage = new DHCPMessage();
        dhcpMessage.setHtype(DHCPRELEASE);
		dhcpMessage.setOp(OP_REQUEST);
		dhcpMessage.setXid(xID);
		dhcpMessage.setCiaddr(cIP);
		dhcpMessage.setChaddr(mac);
		dhcpMessage.setHlen((byte) 6);
		dhcpMessage.setSecs((byte)0);
		dhcpMessage.setHops((byte)1);
        return dhcpMessage;
	}

	// -----------------------------------------------------------
	// Constructors
	// -----------------------------------------------------------

	/**
	 * Creates empty DHCPMessage object, initializes the object, sets the host
	 * to the broadcast address, the local subnet, binds to the default server
	 * port.
	 */
	public DHCPMessage() {
		initialize();

		this.destination_IP = BROADCAST_ADDR;
		this.gPort = SERVER_PORT;
	}

	public DHCPMessage(DHCPMessage inMessage) {
		initialize();

		this.destination_IP = BROADCAST_ADDR;
		this.gPort = SERVER_PORT;
		this.op = inMessage.getOp();
		this.htype = inMessage.getHtype();
		this.hlen = inMessage.getHlen();
		this.hops = inMessage.getHops();
		this.xid = inMessage.getXid();
		this.secs = inMessage.getSecs();
		this.flags = inMessage.getFlags();
		this.ciaddr = inMessage.getCiaddr();
		this.yiaddr = inMessage.getYiaddr();
		this.siaddr = inMessage.getSiaddr();
		this.giaddr = inMessage.getGiaddr();
		this.chaddr = inMessage.getChaddr();
		this.sname = inMessage.getSname();
		this.file = inMessage.getFile();
	}

	public DHCPMessage(DHCPMessage inMessage, InetAddress inServername, int inPort) {
		initialize();

		this.destination_IP = inServername;
		this.gPort = inPort;

		this.op = inMessage.getOp();
		this.htype = inMessage.getHtype();
		this.hlen = inMessage.getHlen();
		this.hops = inMessage.getHops();
		this.xid = inMessage.getXid();
		this.secs = inMessage.getSecs();
		this.flags = inMessage.getFlags();
		this.ciaddr = inMessage.getCiaddr();
		this.yiaddr = inMessage.getYiaddr();
		this.siaddr = inMessage.getSiaddr();
		this.giaddr = inMessage.getGiaddr();
		this.chaddr = inMessage.getChaddr();
		this.sname = inMessage.getSname();
		this.file = inMessage.getFile();
	}

	public DHCPMessage(DHCPMessage inMessage, InetAddress inServername) {
		initialize();

		this.destination_IP = inServername;
		this.gPort = SERVER_PORT;

		this.op = inMessage.getOp();
		this.htype = inMessage.getHtype();
		this.hlen = inMessage.getHlen();
		this.hops = inMessage.getHops();
		this.xid = inMessage.getXid();
		this.secs = inMessage.getSecs();
		this.flags = inMessage.getFlags();
		this.ciaddr = inMessage.getCiaddr();
		this.yiaddr = inMessage.getYiaddr();
		this.siaddr = inMessage.getSiaddr();
		this.giaddr = inMessage.getGiaddr();
		this.chaddr = inMessage.getChaddr();
		this.sname = inMessage.getSname();
		this.file = inMessage.getFile();
	}


	public DHCPMessage(InetAddress inServername, int inPort) {
		initialize();

		this.destination_IP = inServername;
		this.gPort = inPort;
	}

	public DHCPMessage(InetAddress inServername) {
		initialize();

		this.destination_IP = inServername;
		this.gPort = SERVER_PORT;
	}

	public DHCPMessage(int inPort) {
		initialize();

		this.destination_IP = BROADCAST_ADDR;
		this.gPort = inPort;
	}

	public DHCPMessage(byte[] ibuf) {
		initialize();
		internalize(ibuf);

		this.destination_IP = BROADCAST_ADDR;
		this.gPort = SERVER_PORT;
	}

	public DHCPMessage(byte[] ibuf, InetAddress inServername, int inPort) {
		initialize();
		internalize(ibuf);

		this.destination_IP = inServername;
		this.gPort = inPort;
	}

	public DHCPMessage(byte ibuf[], int inPort) {
		initialize();
		internalize(ibuf);

		this.destination_IP = BROADCAST_ADDR;
		this.gPort = inPort;
	}

	public DHCPMessage(byte[] ibuf, InetAddress inServername) {
		initialize();
		internalize(ibuf);

		this.destination_IP = inServername;
		this.gPort = SERVER_PORT;
	}

	public DHCPMessage(DataInputStream inStream) {
		initialize();

		try {
			this.op = inStream.readByte();
			this.htype = inStream.readByte();
			this.hlen = inStream.readByte();
			this.hops = inStream.readByte();
			this.xid = inStream.readInt();
			this.secs = inStream.readShort();
			this.flags = inStream.readShort();
			inStream.readFully(this.ciaddr, 0, 4);
			inStream.readFully(this.yiaddr, 0, 4);
			inStream.readFully(this.siaddr, 0, 4);
			inStream.readFully(this.giaddr, 0, 4);
			inStream.readFully(this.chaddr, 0, 16);
			inStream.readFully(this.sname, 0, 64);
			inStream.readFully(this.file, 0, 128);
			byte[] options = new byte[312];
			inStream.readFully(options, 0, 312);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	// -----------------------------------------------------------
	// Methods
	// -----------------------------------------------------------

	private void initialize() {
		
	}

	/**
	 * Converts a DHCPMessage object to a byte array.
	 */
	public synchronized byte[] externalize() {
		ByteArrayOutputStream outBStream = new ByteArrayOutputStream();
		DataOutputStream outStream = new DataOutputStream(outBStream);

		try {
			outStream.writeByte(this.op);
			outStream.writeByte(this.htype);
			outStream.writeByte(this.hlen);
			outStream.writeByte(this.hops);
			outStream.writeInt(this.xid);
			outStream.writeShort(this.secs);
			outStream.writeShort(this.flags);
			outStream.write(this.ciaddr, 0, 4);
			outStream.write(this.yiaddr, 0, 4);
			outStream.write(this.siaddr, 0, 4);
			outStream.write(this.giaddr, 0, 4);
			outStream.write(this.chaddr, 0, 16);
			outStream.write(this.sname, 0, 64);
			outStream.write(this.file, 0, 128);
		} catch (IOException e) {
			System.err.println(e);
		}

		// extract the byte array from the Stream
		byte data[] = outBStream.toByteArray();

		return data;
	}

	/**
	 * Convert a specified byte array containing a DHCP message into a
	 * DHCPMessage object.
	 */

	public synchronized DHCPMessage internalize(byte[] ibuff) {
		ByteArrayInputStream inBStream = new ByteArrayInputStream(ibuff, 0, ibuff.length);
		DataInputStream inStream = new DataInputStream(inBStream);

		try {
			this.op = inStream.readByte();
			this.htype = inStream.readByte();
			this.hlen = inStream.readByte();
			this.hops = inStream.readByte();
			this.xid = inStream.readInt();
			this.secs = inStream.readShort();
			this.flags = inStream.readShort();
			inStream.readFully(this.ciaddr, 0, 4);
			inStream.readFully(this.yiaddr, 0, 4);
			inStream.readFully(this.siaddr, 0, 4);
			inStream.readFully(this.giaddr, 0, 4);
			inStream.readFully(this.chaddr, 0, 16);
			inStream.readFully(this.sname, 0, 64);
			inStream.readFully(this.file, 0, 128);
		} catch (IOException e) {
			System.err.println(e);
		} // end catch

		return this;
	}


	public void setOp(byte inOp) {
		this.op = inOp;
	}

	public void setHtype(byte inHtype) {
		this.htype = inHtype;
	}

	public void setHlen(byte inHlen) {
		this.hlen = inHlen;
	}

	public void setHops(byte inHops) {
		this.hops = inHops;
	}

	public void setXid(int inXid) {
		this.xid = inXid;
	}

	public void setSecs(short inSecs) {
		this.secs = inSecs;
	}

	public void setFlags(short inFlags) {
		this.flags = inFlags;
	}

	public void setCiaddr(byte[] inCiaddr) {
		this.ciaddr = inCiaddr;
	}

	public void setYiaddr(byte[] inYiaddr) {
		this.yiaddr = inYiaddr;
	}

	public void setSiaddr(byte[] inSiaddr) {
		this.siaddr = inSiaddr;
	}

	public void setGiaddr(byte[] inGiaddr) {
		this.giaddr = inGiaddr;
	}

	public void setChaddr(byte[] inChaddr) {
		this.chaddr = inChaddr;
	}

	public void setSname(byte[] inSname) {
		this.sname = inSname;
	}

	public void setFile(byte[] inFile) {
		this.file = inFile;
	}

	public void setPort(int inPortNum) {
		this.gPort = inPortNum;
	}

	public void setDestinationHost(String inHost) {
		try {
			this.destination_IP = InetAddress.getByName(inHost);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public byte getOp() {
		return this.op;
	}

	public byte getHtype() {
		return this.htype;
	}

	public byte getHlen() {
		return this.hlen;
	}

	public byte getHops() {
		return this.hops;
	}

	public int getXid() {
		return this.xid;
	}

	public short getSecs() {
		return this.secs;
	}

	public short getFlags() {
		return this.flags;
	}

	public byte[] getCiaddr() {
		return this.ciaddr;
	}

	public byte[] getYiaddr() {
		return this.yiaddr;
	}

	public byte[] getSiaddr() {
		return this.siaddr;
	}

	public byte[] getGiaddr() {
		return this.giaddr;
	}

	public byte[] getChaddr() {
		return this.chaddr;
	}

	public byte[] getSname() {
		return this.sname;
	}

	public byte[] getFile() {
		return this.file;
	}

	public int getPort() {
		return this.gPort;
	}

	public String getDestinationAddress() {
		return this.destination_IP.getHostAddress();
	}

}