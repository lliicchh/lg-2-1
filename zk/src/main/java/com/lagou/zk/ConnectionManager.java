package com.lagou.zk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据库连接管理器
 */
public class ConnectionManager {
    private static List<Connection> pool = new LinkedList<Connection>();

    public static String Url = "";
    public static String USERNAME = "";
    public static String PASSWORD = "";
    public static String DRIVER = "";
    public static int initCount;
    public static int maxCount;
    public static int currentCount;
    private static volatile ConnectionManager instance = null;

    private ConnectionManager() {
        init();
    }

    public static ConnectionManager getInstance() {
        if (null == instance) {
            synchronized (ConnectionManager.class) {
                if (null == instance) {
                    return new ConnectionManager();
                }
            }
        }
        return instance;
    }

    public static void init() {
        addConnection();
    }

    public static void addConnection() {
        for (int i = 0; i < initCount; i++) {
            try {
                pool.add(createConnection());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection createConnection() throws ClassNotFoundException {
        Connection conn = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(Url, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 从连接池中获取连接
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        synchronized (pool) {
            if (pool.size() > 0) {
                System.out.println("Current Connection size is:" + pool.size());
                return pool.get(0);
            } else if (currentCount < maxCount) {
                Class.forName(DRIVER);
                Connection conn = createConnection();
                pool.add(conn);
                currentCount++;
                return conn;
            } else {
                throw new SQLException("Current Connection is Zero");
            }
        }
    }

    /**
     * 清空连接池,释放连接
     */
    public static void clearPool() {
        for (Connection connection : pool) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        pool = new LinkedList<Connection>();
    }

    public static void release(Connection conn) {
        pool.remove(conn);
    }
}