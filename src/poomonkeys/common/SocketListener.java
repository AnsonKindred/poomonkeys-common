package poomonkeys.common;

public interface SocketListener
{
	public void playerJoined();
	public void incomingMessage(int socket_id, String[] data);
}