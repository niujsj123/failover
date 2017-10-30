import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import net.sf.json.JSON;

public class ZkClientDistributeLock {

	private ZkClient zk;
	private String root = "/cd5";
	private String myNode;
	private String waitNode;
	private CountDownLatch cdl;

	private static final String CONNECTION_STRING = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";

	private static final int SESSION_TIMEOUT = 5000;

	public ZkClientDistributeLock() {
		try {
				zk = new ZkClient(CONNECTION_STRING, SESSION_TIMEOUT, SESSION_TIMEOUT, new ZkSerializer() {

					@Override
					public byte[] serialize(Object data) throws ZkMarshallingError {
						try {
							return String.valueOf(data).getBytes("UTF-8");
						} catch (UnsupportedEncodingException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						return null;
					}

					@Override
					public Object deserialize(byte[] bytes) throws ZkMarshallingError {
						try {
							return new String(bytes,"UTF-8");
						} catch (UnsupportedEncodingException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						return null;
					}

				});
				if (!zk.exists(root)){
					zk.createPersistent(root);
				}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
//			ex.printStackTrace();
		}

	}

	public void lock() {
		if (tryLock()){
			System.out.println("Thread "+ Thread.currentThread().getName() +" and "+ myNode+ " - hold lock");
			return;
		}
		waitLock();
		lock();
	}

	private void waitLock() {
		try {
		    IZkDataListener listener = new IZkDataListener() {
				
				@Override
				public void handleDataDeleted(String dataPath) throws Exception {
					// TODO Auto-generated method stub
					if (cdl != null){
						cdl.countDown();
						System.out.println("delete Thread "+ Thread.currentThread().getName() + " "+ root+"/"+myNode);
					}
				}
				
				@Override
				public void handleDataChange(String dataPath, Object data) throws Exception {
					// TODO Auto-generated method stub
					
				}
			};
			if (zk.exists(root+"/"+waitNode)){
				zk.subscribeDataChanges(root+"/"+waitNode, listener);
				System.out.println(" subscribe for "+ root+"/"+waitNode);
				System.out.println("Thread "+ Thread.currentThread().getName() + " waiting for "+ root+"/"+myNode);
				cdl = new CountDownLatch(1);
//				cdl.await(SESSION_TIMEOUT,TimeUnit.MILLISECONDS);
				cdl.await();
//				cdl =null;
				System.out.println("end Thread "+ Thread.currentThread().getName() + " "+ root+"/"+myNode);
				zk.unsubscribeDataChanges(root+"/"+waitNode, listener);
				System.out.println(" unsubscribe for "+ root+"/"+waitNode);
			}
		} /*catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} */catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
//		System.out.println("222222222");
	}

	private boolean tryLock() {
		String str = "/lock_";
		try {
			if (myNode == null || myNode.length()<= 0) {
				myNode = zk.createEphemeralSequential(root + str, 1);
				System.out.println("Thread " + Thread.currentThread().getName() + " " + myNode + " create success");
			}
			List<String> children = zk.getChildren(root);
			Collections.sort(children);
//			System.out.println(com.mongodb.util.JSON.serialize(children));
			if (myNode.equals(root+"/"+children.get(0))){
//				System.out.println("dddd");
				return true;
			}
			String subNode = myNode.substring(myNode.lastIndexOf("/")+1);
//			System.out.println(subNode);
			waitNode = children.get(Collections.binarySearch(children, subNode)-1);
//			System.out.println(waitNode +" create success");
			return false;
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} /*catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}*/
		return false;
	}

	public void unlock() {
		 System.out.println("UnLock = "+ myNode);
		try {
			zk.delete(myNode);
//			myNode =null;
//			zk.close();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} /*catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}*/
	}

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();
		Semaphore smp = new Semaphore(10);
		CountDownLatch cdl = new CountDownLatch(10);
		for (int i = 0; i < 10; i++) {
			Runnable runnable =()->{
				try {
//					smp.acquire();
					cdl.await();
					ZkClientDistributeLock demo = new ZkClientDistributeLock();
					demo.lock();
//					System.out.println("333");
					Thread.sleep(1000);
					demo.unlock();
//					smp.release();
				} catch (InterruptedException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}	
			};
//			service.execute(runnable);
			new Thread(runnable).start();
			cdl.countDown();
		}
//        service.shutdown();
	}

}
