package master;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class MasterServer {

	private ZkClient zkClient;
	private String masterNode = "/master";
	private final static String connStr = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";
	private final static int timeout = 5000;
	private ServerData serverData;
	private ServerData masterData;
	private volatile boolean running = false;
	private IZkDataListener zkDataListener;
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private int cc ;

	public MasterServer(ZkClient zkClient, ServerData serverData) {
		this.zkClient = zkClient;
		this.serverData = serverData;
		zkDataListener = new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
//				System.out.println("delete event=="+dataPath);
				executorService.schedule(new Runnable() {
					
					@Override
					public void run() {
//						System.out.println(serverData.getServerName());
						takeMaster();
					}
				}, 5, TimeUnit.SECONDS);
				
				
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
//				System.out.println("--->"+dataPath);
				
			}
		};
	}

	public MasterServer(ZkClient client, ServerData data, int cc) {
		this.zkClient = zkClient;
		this.serverData = serverData;
		this.cc = cc;
		zkDataListener = new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
//				System.out.println("delete event=="+dataPath);
				executorService.schedule(new Runnable() {
					
					@Override
					public void run() {
//						System.out.println(serverData.getServerName());
						takeMaster();
					}
				}, 5, TimeUnit.SECONDS);
				
				
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
//				System.out.println("--->"+dataPath);
				
			}
		};
	}

	public void start() {
		if (running){
			throw new RuntimeException("server 已经启动了");
		}
		running = true;
//		System.out.println("zk="+zkClient);
		zkClient.subscribeDataChanges(masterNode, zkDataListener);
		takeMaster();
	}

	private void takeMaster() {
		if (!running){
			return;
		}
		System.out.println(serverData.getServerName()+"来抢master");
		try {
			zkClient.createEphemeral(masterNode, serverData);
			masterData = serverData;
			System.out.println(serverData.getServerName()+"成功抢到master");
			executorService.execute(new Runnable() {
				
				@Override
				public void run() {
					if (checkMaster()){
						deleteMaster();
					}
				}
			});
		} catch (ZkNodeExistsException ex) {
			ServerData readData = zkClient.readData(masterNode);
//			System.out.println("data="+readData.getServerName());
			if (null==readData){
				takeMaster();
			} else {
				masterData = serverData;
			}
		}
		
		
	}

	protected void deleteMaster() {
		zkClient.delete(masterNode);
//		System.out.println("cc="+cc);
		zkClient.close();
//		cc = true;
		
//		System.out.println("delete:"+serverData.getServerName());
	}

	protected boolean checkMaster() {
		try {
			ServerData data = zkClient.readData(masterNode);
			masterData = data;
			if (masterData.getServerName().equals(serverData.getServerName())) {
				System.out.println(masterData.getServerName()+"=");
				return true;
			}
			System.out.println(masterData.getServerName()+"<>");
			return false;
		} catch (ZkNoNodeException e) {
			return false;
		} catch (ZkInterruptedException e) {
			return checkMaster();
		} catch (ZkException e) {
			return false;
		}
	}

	public void stop() {
		if (!running){
			throw new RuntimeException("server 已经停止了");
		}
		running = false;
		zkClient.subscribeDataChanges(masterNode, zkDataListener);
		releaseMaster();
	}

	private void releaseMaster() {
		if (checkMaster()){
			zkClient.delete(masterNode);
		}
		
	}

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();
		Semaphore smp = new Semaphore(10);
		CountDownLatch c = new CountDownLatch(10);
		for (int i = 0; i < 15; i++) {
			final int idx = i;
			boolean cc = false;
			/*Thread th = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
//						smp.acquire();
						
						ZkClient client = new ZkClient(connStr,timeout,timeout, new SerializableSerializer());
						ServerData data = new ServerData(idx, "#server-"+idx);
						int cc = -1;
						MasterServer server = new MasterServer(client, data);
						c.await();
						server.start();
						
//						smp.release();
					} catch (InterruptedException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					
					
				}
			});
			th.start();
			c.countDown();*/
			service.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						smp.acquire();
						ZkClient client = new ZkClient(connStr,timeout,timeout, new SerializableSerializer());
						ServerData data = new ServerData(idx, "#server-"+idx);
						int cc = -1;
						MasterServer server = new MasterServer(client, data);
						server.start();
						smp.release();
						System.out.println("release ");
					} catch (InterruptedException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
//						service.shutdownNow();
					} finally{
//						service.shutdownNow();
//						smp.release();
					}
					
				}
			});
		}
		System.out.println("stop service");
//		service.shutdown();
//		service.shutdownNow();
		System.out.println("stop service");
		

	}

}
