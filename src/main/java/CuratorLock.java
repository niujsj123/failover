import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

public class CuratorLock {
	private static final String CONNECTION_STRING = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";

	private static final int SESSION_TIMEOUT = 5000;

	private static String root = "/curator_lock";

	public static void doLock(CuratorFramework cf) {
		  System.out.println(Thread.currentThread().getName()+" try to get lock");
		  InterProcessMutex lock = new InterProcessMutex(cf, root) ;
		try {
			if (lock.acquire(5,TimeUnit.SECONDS)){
				System.out.println(Thread.currentThread().getName() + " hold lock");
                Thread.sleep(5000);
//                System.out.println("22222");
			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} finally{
			try {
//				System.out.println(Thread.currentThread().getName() +"   3333333");
				lock.release();
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();
		Semaphore smp = new Semaphore(10);
		for (int i = 0; i < 10; i++) {
			Runnable runnable = () -> {
				try {
					CuratorFramework cf = CuratorFrameworkFactory.newClient(CONNECTION_STRING,
							new RetryNTimes(10, SESSION_TIMEOUT));
					cf.start();
					smp.acquire();
					doLock(cf);
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
