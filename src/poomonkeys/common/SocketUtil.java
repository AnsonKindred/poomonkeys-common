package poomonkeys.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;

public class SocketUtil 
{

	static ArrayList<InetAddress> otherIPs = new ArrayList<InetAddress>();
	static DatagramSocket socket;
	static ArrayList<SocketListener> socketListeners = new ArrayList<SocketListener>();
	
	static void addSocketListener(SocketListener socketListener)
	{
		socketListeners.add(socketListener);
	}
	
	public static void connectToIP(String ip)
	{
		try 
		{
			SocketUtil.socket = new DatagramSocket(8888);
		}
    	catch (SocketException e) {e.printStackTrace();}
		
		new Thread(new OpenConnectionTask(ip)).start();
	}
	
	public static void hostGame() throws IOException
	{
		try 
		{
			SocketUtil.socket = new DatagramSocket(8888);
		}
    	catch (SocketException e) {e.printStackTrace();}
		
		new Thread(new ListenForIncomingTask()).start();
	}
	
	public static void sendMessage(String msg) throws IOException
	{
		new Thread(new SendMessageTask(msg)).start();
	}
	
	public static void _sendMessage(String msg) throws IOException
	{
		int msg_length = msg.length();
    	byte[] message = msg.getBytes();
    	DatagramPacket message_packet;
    	
    	for(int i = 0; i < otherIPs.size(); i++) {
	    	message_packet = new DatagramPacket(message, 
												msg_length, 
												otherIPs.get(i), 
												8888);
	    	socket.send(message_packet);
    	}
	}
	
	public static void _sendMessage(String msg, InetAddress ip) throws IOException
	{
		int msg_length = msg.length();
    	byte[] message = msg.getBytes();
    	DatagramPacket message_packet;
    	message_packet = new DatagramPacket(message, 
											msg_length, 
											ip, 
											8888);
    	socket.send(message_packet);
	}
	
	public static Response _waitForResponse() throws IOException
	{
		byte[] messageBytes = new byte[1500];
		DatagramPacket p = new DatagramPacket(messageBytes, messageBytes.length);
		// blocks, waiting for message
		socket.receive(p);
		Response r = new Response();
		String message = new String(messageBytes, 0, p.getLength());
		r.ip = p.getAddress();
    	r.data = message.split(",");
    	
    	return r;
	}
	
	public static String getLocalIP() 
	{
		String ipAddrStr = "";
		try 
		{
			Socket s = new Socket("www.google.com", 80); // any site at all
			InetAddress ip = s.getLocalAddress();
			ipAddrStr = ip.getHostAddress();
			s.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ipAddrStr;
	}
	
	public static void fireIncomingMessageEvent(int socket_index, String[] data)
	{
		for(int l = 0; l < socketListeners.size(); l++)
		{
			socketListeners.get(l).incomingMessage(socket_index, data);
		}
	}
	
	public static void firePlayerJoinedEvent()
	{
		for(int l = 0; l < socketListeners.size(); l++)
		{
			socketListeners.get(l).playerJoined();
		}
	}

}

class Response
{
	InetAddress ip;
	String[] data;
}

class ListenForIncomingTask implements Runnable
{
	private Exception exception;

    public void run() 
    {
    	PortMapping desiredMapping =
    	        new PortMapping(
    	                8888,
    	                SocketUtil.getLocalIP(),
    	                PortMapping.Protocol.UDP,
    	                "My Port Mapping"
    	        );

    	UpnpService upnpService =
    	        new UpnpServiceImpl(
    	                new PortMappingListener(desiredMapping)
    	        );

    	upnpService.getControlPoint().search();
    	
    	boolean disconnected = false;
    	
    	Response r = null;
    	while(!disconnected) {
    		try {
				r = SocketUtil._waitForResponse();
			} catch (IOException e) {e.printStackTrace();}
    		
			InetAddress ip = r.ip;
			int socket_index = 0;
			boolean found = false;
			System.out.println("Message: ");
			System.out.println(Arrays.toString(r.data));
			System.out.println("Existing IPs: ");
			System.out.println(SocketUtil.otherIPs.toString());
			for(; socket_index < SocketUtil.otherIPs.size(); socket_index++) {
				if(SocketUtil.otherIPs.get(socket_index).equals(ip)) {
					found = true;
					break;
				}
			}
			
			if(found) 
			{
				SocketUtil.fireIncomingMessageEvent(socket_index, r.data);
			}
			else 
			{
				// We don't have this ip yet, probably a new player
				if(r.data[0].equals("connect")) 
				{
					// brand new player, add their ip and send them the list
					// of other players
		    		try {
		    			String other_ips_string = Arrays.toString(SocketUtil.otherIPs.toArray());
		    			other_ips_string.replaceAll("\\[\\]", "");
		    			System.out.println("Sending other ip string: "+other_ips_string);
						SocketUtil._sendMessage(other_ips_string, r.ip);
					} catch (IOException e) {e.printStackTrace();}
					SocketUtil.otherIPs.add(r.ip);
					SocketUtil.firePlayerJoinedEvent();
				}
				else if(r.data[0].equals("join")) 
				{
					// the connecting player has already received a player list
					// from someone else, just need to add their ip to our list
					SocketUtil.otherIPs.add(r.ip);
					SocketUtil.firePlayerJoinedEvent();
				}
			}
    	}
    }

    protected void onPostExecute(String obj) 
    {
    }
}

class OpenConnectionTask implements Runnable
{
	private String ip;
	
	OpenConnectionTask(String ip)
	{
		this.ip = ip;
	}

    public void run() 
    {
    	try {
    		// add the 'server' player's ip to the list
			SocketUtil.otherIPs.add(InetAddress.getByName(ip));
		} 
    	catch (UnknownHostException e1) {e1.printStackTrace();} 
    	
    	// Send an 'connect' message to the ip we're opening a connection to
    	// This will cause the receiving client to respond with a list of
    	// IPs for any other clients (this is probably hilariously insecure)
    	try {
			SocketUtil._sendMessage("connect", SocketUtil.otherIPs.get(0));
		} catch (IOException e) {e.printStackTrace();}
    	
    	Response r = null;
		try {
			r = SocketUtil._waitForResponse();
		} catch (IOException e) {e.printStackTrace();}
		
    	for(int i = 0; i < r.data.length; i++) {
    		System.out.println("other player: " + r.data[i]);
    		if(r.data[i].length() == 0) continue;
    		// Also send a join message to all other clients so that they
    		// will have this client's IP
    		try {
				SocketUtil._sendMessage("join", InetAddress.getByName(r.data[i]));
	    		SocketUtil.otherIPs.add(InetAddress.getByName(r.data[i]));
			} catch (IOException e) {e.printStackTrace();}
    		
    		// Add a player for each client
    		SocketUtil.firePlayerJoinedEvent();
    	}

    	// start listening for incoming messages
    	new Thread(new ListenForIncomingTask()).start();
    }

    protected void onPostExecute(String obj) 
    {
    }
}

class SendMessageTask implements Runnable
{
	private String msg;
	
	public SendMessageTask(String msg)
	{
		this.msg = msg;
	}
		
    public void run() 
    {
        try {
        	SocketUtil._sendMessage(msg);
        } catch (Exception e) {}
    }

    protected void onPostExecute(String obj) 
    {
    }
}
