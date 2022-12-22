package DHCP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DHCPSocket extends DatagramSocket {
	/**
	 * Default socket timeout (6 second)
	 */
	private int SOCKET_TIMEOUT = 0;

	/**
	 * Default MTU (Maximum Transmission Unit) for ethernet (in bytes)
	 */
	private int mtu = 1500;

	/**
	 * Constructor for creating DHCPSocket on a specific port on the local
	 */
	public DHCPSocket(int inPort) throws SocketException {
		super(inPort);
		setSoTimeout(this.SOCKET_TIMEOUT);
	}	

	/**
	 * Constructor for creating DHCPSocket on a specific local address and port
	 * on the local machine.
	 */
	public DHCPSocket(int inPort, InetAddress lAddr) throws SocketException {
		super(inPort, lAddr);
		setSoTimeout(this.SOCKET_TIMEOUT);
	}

	/**
	 * Sets the Maximum Transfer Unit for the UDP DHCP Packets to be set.
	 * 
	 * @param inSize
	 *            Integer representing desired MTU
	 */
	public void setMTU(int inSize) {
		this.mtu = inSize;
	}

	public int getMTU() {
		return this.mtu;
	}

	/**
	 * Sends a DHCPMessage object to a predifined host.
	 * 
	 * @param inMessage
	 *            Well-formed DHCPMessage to be sent to a server
	 * 
	 * @throws IOException
	 *             If the message could not be sent.
	 */
	public synchronized void send(DHCPMessage inMessage) throws IOException {
		byte data[] = new byte[this.mtu];
		data = inMessage.externalize();
		InetAddress dest = null;
		try {
			dest = InetAddress.getByName(inMessage.getDestinationAddress());
		} catch (UnknownHostException e) {
		}

		DatagramPacket outgoing = new DatagramPacket(data, data.length, dest, inMessage.getPort());

		send(outgoing); // send outgoing message
	}

	/**
	 * Receives a datagram packet containing a DHCP Message into a DHCPMessage
	 * object.
	 * 
	 * @return <code>true</code> if message is received, <code>false</code> if
	 *         timeout occurs.
	 * @param outMessage
	 *            DHCPMessage object to receive new message into
	 */
	public synchronized boolean receive(DHCPMessage outMessage) {
		try {
			DatagramPacket incoming = new DatagramPacket(new byte[this.mtu], this.mtu);
			// gSocket.
			receive(incoming); // block on receive for SOCKET_TIMEOUT

			outMessage.internalize(incoming.getData());
		} catch (java.io.IOException e) {
			System.out.println(e.getMessage());
			return false;
		} // end catch
		return true;
	}
}