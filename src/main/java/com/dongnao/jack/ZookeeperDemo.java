package com.dongnao.jack;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

public class ZookeeperDemo {
	private static final int SESSION_TIMEOUT=3000;

    public static void main(String[] args) throws NoSuchAlgorithmException {
    	
        /*ZooKeeper zk=null;
        try {
            zk=new ZooKeeper("120.77.22.187:2181", SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("触发事件："+event.getType());
                }
            });
//            createDemo(zk);
//            updateDemo(zk);
//            deleteDemo(zk);
//            aclDemo(zk);
            watcherDemo(zk);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }finally{
            if(zk!=null){
                try {
                    zk.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/
    	
    	ZooKeeper zk =null;
    	try {
			zk = new ZooKeeper("192.168.20.243:2181", SESSION_TIMEOUT, new Watcher() {
				
				@Override
				public void process(WatchedEvent event) {
					System.out.println("event:"+event.getType()+",status:"+event.getState());
					
				}
			});
			createDemo(zk);
			updateDemo(zk);
			deleteDemo(zk);
			aclDemo(zk);
			watcherDemo(zk);
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (KeeperException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}finally {
			if (zk!=null)
				try {
					zk.close();
				} catch (InterruptedException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
		}
    	
    }
    private static void createDemo(ZooKeeper zk) throws KeeperException, InterruptedException {
        /*if(zk.exists("/node_2",true)==null){
            zk.create("/node_2","abc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(new String(zk.getData("/node_2",true,null)));
        }*/
    	if(zk.exists("/test2", true)==null){
    		zk.create("/test2","hello".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
    		System.out.println(new String(zk.getData("/test2", true, null)));
    	}
    }

    private static void aclDemo(ZooKeeper zk) throws KeeperException, InterruptedException, NoSuchAlgorithmException {
        /*if(zk.exists("/node_3",true)==null){
            ACL acl=new ACL(ZooDefs.Perms.ALL,new Id("digest", DigestAuthenticationProvider.generateDigest("root:root")));
            List<ACL> acls=new ArrayList<ACL>();
            acls.add(acl);
            zk.create("/node_3","abc".getBytes(), acls, CreateMode.PERSISTENT);
            System.out.println(new String(zk.getData("/node_3",true,null)));
        }
        zk.addAuthInfo("digest","root:root".getBytes());
        System.out.println(new String(zk.getData("/node_3",true,null)));*/
    	if (zk.exists("/test3", true)==null){
    		ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest",DigestAuthenticationProvider.generateDigest("root:root")));
    		List<ACL> acls = new ArrayList<>();
    		acls.add(acl);
    		zk.create("/test3", "abc".getBytes(), acls, CreateMode.PERSISTENT);
    		System.out.println(new String(zk.getData("/test3",true,null)));
    	}
    	zk.addAuthInfo("digest", "root:root".getBytes());
    	System.out.println(new String(zk.getData("/test3",true,null)));
    }

    private static void updateDemo(ZooKeeper zk) throws KeeperException, InterruptedException {
        /*zk.setData("/node_2","www".getBytes(),-1);
        System.out.println(new String(zk.getData("/node_2",true,null)));*/
        zk.setData("/test2", "ww".getBytes(), -1);
        System.out.println(new String(zk.getData("/test2", true, null)));
    }

    private static void deleteDemo(ZooKeeper zk) throws KeeperException, InterruptedException {
//        zk.delete("/test2",-1);
        zk.delete("/test2",-1);
//        System.out.println(new String(zk.getData("/test2",true,null)));
    }

    private static void watcherDemo(final ZooKeeper zk) throws KeeperException, InterruptedException {
        /*if(zk.exists("/node_4",true)==null) {
            zk.create("/node_4", "abc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        byte[] rsByt = zk.getData("/node_4", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("触发节点事件：" + event.getPath());
                try {
                    System.out.println(new String(zk.getData(event.getPath(),true,null)));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        System.out.println(new String(rsByt));
        zk.setData("/node_4","pksyz".getBytes(),-1);*/
    //    System.out.println(new String(zk.getData("/node_2",true,null)));
    	if (zk.exists("/test2", true)==null){
    		zk.create("/test2", "cc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    	}
    	byte[] data = zk.getData("/test2", new Watcher() {
			
			@Override
			public void process(WatchedEvent event) {
				System.out.println("trigger node event:"+event.getPath());
				try {
					System.out.println(new String(zk.getData("/test2", true, null)));
				} catch (KeeperException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
			}
		},null);
    	System.out.println(new String(data));
    	 zk.setData("/test2","yyy".getBytes(),-1);
    	 System.out.println(new String(zk.getData("/test2",true,null)));
    }
}
