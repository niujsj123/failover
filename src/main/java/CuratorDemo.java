import java.io.IOException;
import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class CuratorDemo {
	static String connectStr = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";
	static int sessionTimeout = 3000;

	public static void main(String[] args) {
		CuratorFramework cf = CuratorFrameworkFactory.newClient(connectStr, sessionTimeout, sessionTimeout,
				new ExponentialBackoffRetry(sessionTimeout, 10));
		cf.start();
//		create(cf);
//		update(cf);
//		delete(cf);
//		transaction(cf);
		listener(cf);
		create(cf);
		update(cf);
		update(cf);
//		listener2(cf);
		
		System.out.println(cf.getState()); //获取连接状态
		try {
			System.in.read();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}finally{
			if(cf!=null)
				cf.close();
		}
	}

	private static void listener(CuratorFramework cf) {
		PathChildrenCache pc = new PathChildrenCache(cf, "/test8", true);
		try {
			pc.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
			pc.getListenable().addListener(new PathChildrenCacheListener() {
				
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					System.out.println(event.getType()+"事件监听2");
					
				}
			});
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	private static void listener2(CuratorFramework cf) {
		try {
			cf.getData().usingWatcher(new CuratorWatcher() {
				
				@Override
				public void process(WatchedEvent event) throws Exception {
					System.out.println("event:"+ event.getType());// TODO Auto-generated method stub
					
				}
			}).forPath("/test6");
			cf.setData().forPath("/test6","88".getBytes());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	@SuppressWarnings("deprecation")
	private static void transaction(CuratorFramework cf) {
		try {
			Collection<CuratorTransactionResult> collection = cf.inTransaction().create().forPath("/test6").and().create().forPath("/test7").and().commit();
		    for (CuratorTransactionResult res:collection){
		    	System.out.println(res.getResultStat()+"-->"+ res.getForPath() +"-->" + res.getType());
		    }
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	private static void delete(CuratorFramework cf) {
		try {
			Void void1 = cf.delete().deletingChildrenIfNeeded().forPath("/test1/test1_3");
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	private static void update(CuratorFramework cf) {
		try {
//			Stat stat = cf.setData().forPath("/test1/test1_3","hello".getBytes());
			Stat stat = cf.setData().forPath("/test8/test1_3","555".getBytes());
			System.out.println(stat.toString());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}

	private static void create(CuratorFramework cf) {
		try {
			String forPath = cf.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath("/test8/test1_3","test".getBytes());
			 System.out.println(forPath);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}
}
