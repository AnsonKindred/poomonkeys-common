package poomonkeys.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class Socket
{
	DataInputStream dataInputStream = null;
	DataOutputStream dataOutputStream = null;
	java.net.Socket socket;
	
	public Socket(java.net.Socket socket)
	{
		this.socket = socket;
	}
	
	public void close() throws IOException
	{
		socket.close();
		if (dataInputStream != null) {
			dataInputStream.close();
		}

		if (dataOutputStream != null) {
			dataOutputStream.close();
		}
	}
}
