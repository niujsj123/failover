package master;

import java.io.Serializable;

public class ServerData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int serverId;
	private String serverName;
	
	public ServerData(int serverId, String serverName) {
		super();
		this.serverId = serverId;
		this.serverName = serverName;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
}
