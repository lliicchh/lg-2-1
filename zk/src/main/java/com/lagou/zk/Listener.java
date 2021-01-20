package com.lagou.zk;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;


public class Listener {
    private static Logger logger = LoggerFactory.getLogger(Listener.class);

    // zk服务器地址信息
    public static final String serverstring = "linux121:2181,linux122:2181,linux123:2181";
    // 获取ZkClient对象
    private static ZkClient zkClient = new ZkClient(serverstring);
    // 保存数据库配置信息的节点路径
    private static String path = "/webapp/dblinkcfg";

    /**
     * 监听
     *
     * @throws IOException
     */
    public static void monitor() throws IOException {
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            public void handleDataChange(String dataPath, Object data) throws Exception {
                logger.info("zk中的数据库配置信息发生修改！尝试重新获取数据库连接池...");
                // 重新获取配置信息
                String cfg = zkClient.readData(dataPath, true);
                Properties pro = new Properties();
                Utils.loadData(pro, cfg);
                // 释放旧的连接池
                ConnectionManager.clearPool();
                // 创建新的连接池
                Utils.createDbPool(pro);
            }

            public void handleDataDeleted(String dataPath) throws Exception {
                logger.error("zk中的数据库配置信息已被删除！");
            }
        });
    }
}