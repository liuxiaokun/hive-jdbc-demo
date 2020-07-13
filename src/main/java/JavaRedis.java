import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class JavaRedis {

    public static void main(String[] args) {
//        Jedis jedis = new Jedis("localhost");
//        jedis.set("foo", "bar");
//        String value = jedis.get("foo");
//        System.out.println(value);

        Set<String> sentinels = new HashSet<String>();
            sentinels.add(new HostAndPort("172.16.60.5", 26379).toString());
        sentinels.add(new HostAndPort("172.16.60.6", 26379).toString());
        sentinels.add(new HostAndPort("172.16.60.7", 26379).toString());

        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("mymaster", sentinels);
        Jedis jedis = jedisSentinelPool.getResource();

        String set = jedis.set("sentinel-test", "sentinel test value");
        System.out.println("set return value :" + set);
        String value = jedis.get("sentinel-test");

        System.out.println("value: " + value);
    }
}
