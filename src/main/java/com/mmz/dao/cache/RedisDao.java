package com.mmz.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.mmz.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author : mengmuzi
 * create at:  2019-07-23  10:00
 * @description: 在dao包下新建一个cache目录，用来存取缓存
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip , int port){
        jedisPool = new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)(先拿到字节数组然后反序列化成对象)
                // 采用自定义序列化 protostuff：pojo(getter,setter的对象)
                /**
                 *  * 序列化和反序列化
                 *  序列化是处理对象流的机制，就是将对象的内容进行流化，可以对流化后的对象进行读写操作，
                 *  也可以将流化后的对象在网络间传输。反序列化就是将流化后的对象重新转化成原来的对象。
                 *
                 *  在Java中内置了序列化机制，通过implements Serializable来标识一个对象实现了序列化接口，
                 *  不过其性能并不高。
                 *
                 * 由于Jedis并没有实现内部序列化操作，而Java内置的序列化机制性能又不高，我们是一个秒杀系统，
                 * 需要考虑高并发优化，在这里我们采用开源社区提供的更高性能的自定义序列化工具 protostuff。
                 */
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存中获取到
                if(bytes != null){
                    //空对象
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    // seckill被反序列化
                    return seckill;
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                jedis.close();
            }

        }catch(Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    public String putSeckill(Seckill seckill){
        //set Object(Seckill) -> 序列化 -> byte[]
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                // 超时缓存
                int timeout = 60*60;
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        return null;

    }
}
