package org.jim.core.cache.redis;

import org.jim.core.utils.RedisUtil;
import org.jim.core.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wchao
 * @modify 2016-08-29 增加了set(final String key, final Object value)和<T> T get(final String key,final Class<T> clazz)
 */
@SuppressWarnings({"deprecation"})
public  class JedisTemplate implements  Serializable{  
	
	private static final long serialVersionUID = 9135301078135982677L;
	private static final Logger logger = LoggerFactory.getLogger(JedisTemplate.class);
	private static volatile JedisTemplate instance = null;
    private StringRedisTemplate redisTemplate = SpringContextUtils.getBean(StringRedisTemplate.class);
	private JedisTemplate (){}

	
	public static JedisTemplate me() throws Exception{
		 if (instance == null) { 
	        	synchronized (JedisTemplate.class) {
					if(instance == null){
						instance = new JedisTemplate();
					}
				}
	     }
		 return instance;
	}

   /**
    * 模糊获取所有的key
    * @return
    */
   public Set<String> keys(String likeKey){
       String[] keyArray = RedisUtil.matchKeyRight(likeKey);
       return new HashSet<>(Arrays.asList(keyArray));
   }

   /** 
    * 删除模糊匹配的key 
    * @param likeKey 模糊匹配的key 
    * @return 删除成功的条数 
    */  
   public void delKeysLike(final String likeKey) {
       Set<String> keySet = keys(likeKey);
       RedisUtil.delKey(keySet.toArray(new String[keySet.size()]));
   }

 
   /** 
    * 删除 
    * @param key 匹配的key 
    * @return 删除成功的条数 
    */  
   public void delKey(final String key) {
       RedisUtil.delKey(key);
   }  

   /* ======================================Strings====================================== */  


   /** 
    * 批量插入字符串数据，并设置存活时长
    * @param pairs 键值对数组{数组第一个元素为key，第二个元素为value} 
    * @return 操作状态的集合 
    */  
   public void batchSetStringEx(final List<PairEx<String,String,Integer>> pairs) {
       for (PairEx<String, String,Integer> pair : pairs) {
           RedisUtil.set(pair.getKey(), pair.getValue(), pair.getExpire(), TimeUnit.SECONDS);
       }
   }

    /**
     * @author dave
     * 向缓存中设置对象
     * @param key
     * @param value
     * @return
     */
    public Boolean set(final String key, final Object value) {
        return RedisUtil.set(key, value);
    }

    /**
     * @author dave
     * 向缓存中设置对象有有效期
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public Boolean set(final String key, final Object value, final int expire) {
        return RedisUtil.set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * @author dave
     * 根据key 获取对象
     * @param key
     * @return
     */
    public <T> T get(final String key,final Class<T> clazz) {
        return RedisUtil.get(key, clazz);
    }

 
   /* ======================================Hashes====================================== */  

   /**
    * 批量更新key的过期时间
    * @param pairDatas
    */
   public void batchSetExpire(List<PairEx<String,Void,Integer>> pairDatas){
	   if(pairDatas == null || pairDatas.size() == 0) {
           return;
       }
       for(PairEx<String,Void,Integer> pairEx : pairDatas){
           RedisUtil.expire(pairEx.getKey(), pairEx.getExpire(), TimeUnit.SECONDS);
       }
   }

    /**
     * 写入hash缓存
     *
     * @param key    缓存key
     * @param map 数据
     * @param expire 过期时间
     */
    public Boolean hPutAll(final String key, Map<String, Object> map, long expire) {
        RedisUtil.hPutAll(key, map, expire, TimeUnit.SECONDS);
        return true;
    }

    /**
     * 单个写入hash缓存
     *
     * @param key    缓存key
     * @param hashKey map的key
     * @param value  缓存值
     */
    public void hPut(final String key, final String hashKey, final Object value) {
        RedisUtil.hPut(key, hashKey, value);
    }

    /**
     * 获取指定hash
     * @param key
     * @return
     */
    public Map<Object, Object> hGetAll(String key) {
        return RedisUtil.hGetAll(key);
    }

    /**
     * 获取指定hash的指定值
     * @param key 缓存key
     * @param hashKey map的key
     * @param cls 需要映射的实体类值
     * @return
     */
    public <T> T hGet(String key, String hashKey, Class<T> cls) {
        return RedisUtil.hGet(key, hashKey, cls);
    }

    /**
     * 删除hash的指定值
     * @param key 缓存key
     * @param hashKeys map的key
     */
    public void hDelete(String key, String... hashKeys) {
        RedisUtil.hDelete(key, hashKeys);
    }

   /* ======================================List====================================== */  
 
   /** 
    * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。 
    * @param key key 
    * @param values value的数组 
    * @return 执行 listPushTail 操作后，表的长度 
    */  
   public Long listPushTail(final String key, final String... values) {
       return RedisUtil.putListAll(key, values);
   }

   /**
    * 从集合中删除值为value的指定元素;
    * @param key
    * @param count
    * @param value
    * @return
    */
   public Long listRemove(final String key , int count , final String value){
       return RedisUtil.listRemove(key, count, value);
   }

 
   /** 
    * 返回list所有元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素，key不存在返回空列表 
    * @param key key 
    * @return list所有元素 
    */  
   public List<String> listGetAll(final String key) {
       return RedisUtil.getList(key);
   }

    /* ======================================ZSet====================================== */

    /**
    * 往有序集合sortSet中添加数据;
    * @param key
    * @param score
    * @param value
    * @return
    */
   public Boolean sortSetPush(final String key ,final double score, final String value){
       return RedisUtil.addZset(key, value, score);
   }
   /**
    * 根据Score获取集合区间数据;
    * @param key
    * @param min score区间最小值
    * @param max scroe区间最大值
    * @return
    */
   public Set<String> sorSetRangeByScore(final String key, final double min, final double max) {
       return RedisUtil.getZsetRangeByScore(key, min, max);
   }
   /**
    * 根据Score获取集合区间数据;
    * @param key
    * @param min score区间最小值
    * @param max scroe区间最大值
    * @param offset 偏移量（类似LIMIT 0,10）
    * @param count 数量
    * @return
    */
   public Set<String> sorSetRangeByScore(final String key, final double min, final double max , final int offset , final int count) {
       return RedisUtil.getZsetRangeByScore(key, min, max, offset, count);
   }

   /* ======================================Pub/Sub====================================== */  
 
   /** 
    * 将信息 message 发送到指定的频道 channel。 
    * 时间复杂度：O(N+M)，其中 N 是频道 channel 的订阅者数量，而 M 则是使用模式订阅(subscribed patterns)的客户端的数量。 
    * @param channel 频道 
    * @param message 信息 
    * @return 接收到信息 message 的订阅者数量。 
    */  
   public void publish(final String channel, final String message) {
       redisTemplate.convertAndSend(channel, message);
   }
   
   /** 
    * 将信息 message 发送到指定的频道 channel。 
    * 时间复杂度：O(N+M)，其中 N 是频道 channel 的订阅者数量，而 M 则是使用模式订阅(subscribed patterns)的客户端的数量。 
    * @param channel 频道 
    * @param messages 要发布的信息集合
    */  
   public void publishAll(final String channel, final List<String> messages) {
	   if(messages == null || messages.size() == 0) {
           return;
       }
       for(String message : messages){
           redisTemplate.convertAndSend(channel, message);
       }
   }

   /* ======================================其它================================= */

    /**
     * 修改一个数据key的时效性
     *
     * @param key     缓存key
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return boolean 是否设置成功
     */
    public void expire(String key, final long timeout, final TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

   /** 
    * 构造Pair键值对 
    * @param key key 
    * @param value value 
    * @return 键值对 
    */  
   public <K, V> Pair<K, V> makePair(K key, V value) {  
       return new Pair<K, V>(key, value);  
   }  
   /** 
    * 构造Pair键值对 
    * @param key key 
    * @param value value
    * @param expire expire
    * @return 键值对 
    */  
   public <K, V , E> PairEx<K, V , E> makePairEx(K key, V value , E expire) {  
       return new PairEx<K, V , E>(key, value , expire);  
   }  
   /** 
    * 键值对 
    * @version V1.0 
    * @author fengjc 
    * @param <K> key 
    * @param <V> value 
    */  
   public class Pair<K,V> {  
 
       private K key;  
       private V value;  
 
       public Pair(K key, V value) {  
           this.key = key;  
           this.value = value;  
       }  
 
       public K getKey() {  
           return key;  
       }  
 
       public void setKey(K key) {  
           this.key = key;  
       }  
 
       public V getValue() {  
           return value;  
       }  
 
       public void setValue(V value) {  
           this.value = value;  
       }  
   }
   public class PairEx<K,V,E> extends Pair<K, V>{
	   
		private E expire;
		
		public PairEx(K key, V value) {
			super(key, value);
		}
		public PairEx(K key, V value,E expire) {
			super(key, value);
			this.expire = expire;
		}
		public E getExpire() {
			return expire;
		}
		public void setExpire(E expire) {
			this.expire = expire;
		} 
	
   }
}
