# ✍️ 2-1 Redis

<a name="CSuei"></a>
# Q: RedisCluster的安装、部署、扩容和Java客户端调用
(1)搭建Redis5.0集群，要求三主三从，记录下安装步骤<br />(2)能够添加一主一从(Master4和Slaver4)，记录下安装步骤<br />(3)能够通过JedisCluster向RedisCluster添加数据和取出数据<br />

<a name="qCXwT"></a>
# A 一、搭建Redis5.0集群，要求三主三从
<a name="LKiTk"></a>
## 1.1 env 
```bash
yum install -y wget gcc-c++
```
<a name="eqLCS"></a>
## 1.2 download package
```bash
mkdir -p /opt/lagou/software && cd /opt/lagou/software
# 创建集群安装目录/usr/redis-cluster/7001
mkdir -p /usr/redis-cluster/7001
# 下载Redis 5.0
wget http://download.redis.io/releases/redis-5.0.5.tar.gz
# 解压缩
tar -zxf redis-5.0.5.tar.gz .
```
<a name="3q6zo"></a>
## 1.3 make && install
```bash
cd /opt/lagou/software/redis-5.0.5/src
# 编译
make install PREFIX=/usr/redis-cluster/7001
# 拷贝redis.conf文件到7001下
cp /opt/lagou/software/redis-5.0.5/redis.conf /usr/redis-cluster/7001/bin
```


<a name="ty7SM"></a>
## 1.4 conf
```bash
# bind 127.0.0.1
# 设置端口
port 7001
# 开启集群支持
cluster-enabled yes
# 关闭保护模式
protecte-mode no
# 打开守护进程
daemonize yes
```
<a name="MQHcY"></a>
## 1.5  multi redis dir
```bash
cd /usr/redis-cluster/
cp -r 7001 7002
cp -r 7001 7003
cp -r 7001 7004
cp -r 7001 7005
cp -r 7001 7006
```
<a name="hzbjt"></a>
## 1.6 start 6 redis processes
```bash
cd /usr/redis-cluster/7001
./bin/redis-server ./bin/redis.conf

cd /usr/redis-cluster/7002
./bin/redis-server ./bin/redis.conf

cd /usr/redis-cluster/7003
./bin/redis-server ./bin/redis.conf

cd /usr/redis-cluster/7004
./bin/redis-server ./bin/redis.conf

cd /usr/redis-cluster/7005
./bin/redis-server ./bin/redis.conf

cd /usr/redis-cluster/7006
./bin/redis-server ./bin/redis.conf

```
<a name="cKRO8"></a>
## 1.7 create redis-cluster
```bash
 ./redis-cli --cluster create 192.168.101.123:7001 192.168.101.123:7002 192.168.101.123:7003 192.168.101.123:7004 192.168.101.123:7005 192.168.101.123:7006 --cluster-replicas 1

```


<a name="S5su0"></a>
# A 二、添加一主一从
<a name="0O5Th"></a>
## 2.1 create  new node 7007
```bash
mkdir /usr/redis-cluster/7007
cd /opt/lagou/software/redis-5.0.5/src/
make install PREFIX=/usr/redis-cluster/7007
cp /opt/lagou/software/redis-5.0.5/redis.conf /usr/redis-cluster/7007/bin/

cp -r /usr/redis-cluster/7007 /usr/redis-cluster/7008
```
<a name="yQpp3"></a>
## 2.2 change conf
```bash
vim  /usr/redis-cluster/7007/bin/redis.conf
# bind 127.0.0.1
# 设置端口
port 7007
# 开启集群支持
cluster-enabled yes
# 关闭保护模式
protecte-mode no
# 打开守护进程
daemonize yes



vim  /usr/redis-cluster/7008/bin/redis.conf
# bind 127.0.0.1
# 设置端口
port 7008
# 开启集群支持
cluster-enabled yes
# 关闭保护模式
protecte-mode no
# 打开守护进程
daemonize yes
```


<a name="q8eYK"></a>
## 2.3  start the new master
```bash
cd /usr/redis-cluster/7007/bin
./redis-server redis.conf

# add new node into cluster
./redis-cli --cluster add-node 192.168.101.123:7007 192.168.101.123:7001
```
<a name="4gYJO"></a>
## 2.4 add slots for new master 7007
```bash
 ./redis-cli --cluster reshard 192.168.101.123:7007
```
<a name="TBTD0"></a>
## 2.5  start the new slave
```bash
cd /usr/redis-cluster/7008/bin
./redis-server redis.conf

# add new node into cluster
./redis-cli --cluster add-node  192.168.101.123:7008  192.168.101.123:7007 --cluster-slave --cluster-master-id  f187ff47daa654f66635a36ccea67bb8d1eb9c17 # 7007's ID

```
<a name="YJN22"></a>
# 2.6 check the results
```bash
[root@linux123 bin]#  ./redis-cli -h 127.0.0.1 -p 7001 -c
127.0.0.1:7001> CLUSTER NODES
3696fe0976eebb735d080286578bf0f3d9feb1f9 192.168.101.123:7004@17004 slave 911ea41d4f2cbc1fb27c80defada8b99c9ec44b7 0 1611033204504 4 connected
a954c554916363cc483860455a29561bce19d7e3 192.168.101.123:7008@17008 slave f187ff47daa654f66635a36ccea67bb8d1eb9c17 0 1611033205513 7 connected
f187ff47daa654f66635a36ccea67bb8d1eb9c17 192.168.101.123:7007@17007 master - 0 1611033206000 7 connected 0-998 5461-6461 10923-11921
11d4802a00e244138992ca5b8817a6ff5f58af25 192.168.101.123:7003@17003 master - 0 1611033202000 3 connected 11922-16383
911ea41d4f2cbc1fb27c80defada8b99c9ec44b7 192.168.101.123:7001@17001 myself,master - 0 1611033204000 1 connected 999-5460
b2ac599ef0eabb75d864a08e6789b4751b03b5eb 192.168.101.123:7006@17006 slave 11d4802a00e244138992ca5b8817a6ff5f58af25 0 1611033204000 6 connected
d933c0d7236b08164d3e73f17b8e194568d24ab6 192.168.101.123:7005@17005 slave 6df84b257f62d8b6a4c2199231cc3a0cb74c2525 0 1611033207529 5 connected
6df84b257f62d8b6a4c2199231cc3a0cb74c2525 192.168.101.123:7002@17002 master - 0 1611033206521 2 connected 6462-10922

```
<a name="3Cfba"></a>
# A 三、JedisCluster api
```java
public class TestJedis {
    private static JedisCluster jedis;

    static {
        // 添加集群的服务节点Set集合
        Set<HostAndPort> hostAndPortsSet = new HashSet<HostAndPort>();
        // 添加节点
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7001));
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7002));
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7003));
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7004));
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7005));
        hostAndPortsSet.add(new HostAndPort("192.168.101.123", 7006));

        // Jedis连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(10);
        // 最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(50);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(0);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(2000); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(true);
        jedis = new JedisCluster(hostAndPortsSet, jedisPoolConfig);
    }

    /**
     * 测试key:value数据
     */
    @Test
    public void testKey() throws InterruptedException {
        System.out.println("判断某个键是否存在：" + jedis.exists("username"));
        // test set
        System.out.println("<'user:name:1','tom'>的键值对：" + jedis.set("user:name:1", "timo"));
        // test exists
        System.out.println("是否存在:" + jedis.exists("username"));

        // test expire
        System.out.println("设置键user:name:1的过期时间为10s:" + jedis.expire("user:name:1", 10));
        TimeUnit.SECONDS.sleep(2); // 线程睡眠2秒
        System.out.println("查看键user:name:1的剩余生存时间：" + jedis.ttl("user:name:1"));
        System.out.println("移除键user:name:1的生存时间：" + jedis.persist("user:name:1"));

    }
}
```
