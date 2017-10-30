import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;

public class ZkClientDemo {
	static ZkClient zk;
	static String connectStr = "192.168.20.243:2181,192.168.20.243:2182,192.168.20.243:2183";
	static int sessionTimeout = 3000;
	static {
		zk = new ZkClient(connectStr, sessionTimeout, sessionTimeout, new ZkSerializer() {

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
	}

	public static void main(String[] args) {
		
		zk.subscribeChildChanges("/configuration", new IZkChildListener() {
			
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("触发事件："+parentPath);
				for (String child:currentChilds){
					System.out.println("子节点"+child);
				}
			}
		});
		try {
			initData();
			updateData();
			System.in.read();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}finally {
			if (zk !=null)
			zk.close();
		}
	}

	private static void updateData() {
		zk.writeData("/test1", "test");
//		zk.create("/configuration/test", "test", CreateMode.PERSISTENT);
		zk.writeData("/configuration/test", "hello");
	}

	private static void initData() {
		if (!zk.exists("/configuration")){
			zk.createPersistent("/configuration");
		}
		zk.createPersistent("/test1/test1_1/test1_2", true);
//		zk.create("/test3/password", "password", CreateMode.PERSISTENT);
//		zk.writeData("/configuration/userName", "root");
//		zk.writeData("/configuration/password", "password");
		System.out.println(zk.readData("/configuration/userName").toString());
		System.out.println(zk.readData("/configuration/password").toString());
		zk.subscribeDataChanges("/test1", new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				 System.out.println("触发删除事件："+dataPath+"->"+dataPath);
				
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				 System.out.println("触发事件:"+dataPath);
				
			}
		});
//		zk.delete("/test2");
//		zk.deleteRecursive("/test1");
	}

}
