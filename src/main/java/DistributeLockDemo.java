import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import net.sf.json.JSON;

public class DistributeLockDemo {

	private ZooKeeper zk;
	private String root = "/bc";
	private String myNode;
	private String waitNode;
	private CountDownLatch cdl;

	private static final String CONNECTION_STRING = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";

	private static final int SESSION_TIMEOUT = 5000;

	public DistributeLockDemo() {
		try {
			zk = new ZooKeeper(CONNECTION_STRING, SESSION_TIMEOUT, new Watcher() {

				@Override
				public void process(WatchedEvent event) {
					if (DistributeLockDemo.this.cdl != null) {
						System.out.println("-------");
						DistributeLockDemo.this.cdl.countDown();
					}

				}
			});
			Stat stat = zk.exists(root, false);
			if (stat == null) {
				zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
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
			Stat stat = zk.exists(root+"/"+waitNode, true);
			if (stat !=null){
				System.out.println("Thread "+ Thread.currentThread().getName() + " waiting for "+ root+"/"+waitNode);
				cdl = new CountDownLatch(1);
//				cdl.await(SESSION_TIMEOUT,TimeUnit.MILLISECONDS);
				cdl.await();
//				cdl =null;
			}
			System.out.println("end Thread "+ Thread.currentThread().getName() + " "+ root+"/"+waitNode);
		} catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
//		System.out.println("222222222");
	}

	private boolean tryLock() {
		String str = "/lock_";
		try {
			myNode = zk.create(root+str, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("Thread "+ Thread.currentThread().getName() + myNode +" create success");
			List<String> children = zk.getChildren(root, false);
			Collections.sort(children);
//			System.out.println(com.mongodb.util.JSON.serialize(children));
			if (myNode.equals(root+"/"+children.get(0))){
//				System.out.println("dddd");
				return true;
			}
			String subNode = myNode.substring(myNode.lastIndexOf("/")+1);
//			System.out.println(subNode);
			waitNode = children.get(Collections.binarySearch(children, subNode)-1);
			return false;
		} catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		return false;
	}

	public void unlock() {
		 System.out.println("UnLock = "+ myNode);
		try {
			zk.delete(myNode, -1);
			myNode =null;
			zk.close();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();
		Semaphore smp = new Semaphore(10);
		for (int i = 0; i < 10; i++) {
			Runnable runnable =()->{
				try {
					smp.acquire();
					DistributeLockDemo demo = new DistributeLockDemo();
					demo.lock();
//					System.out.println("111111111");
					Thread.sleep(2000);
					demo.unlock();
					smp.release();
				} catch (InterruptedException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}	
			};
			service.execute(runnable);
		}
        service.shutdown();
	}

}
