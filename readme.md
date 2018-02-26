# springboot集成redis实现消息发布订阅模式

## 1，application.properties配置redis以及连接池

```
#redis
spring.redis.host=localhost
spring.redis.port=6379
#spring.redis.password=
spring.redis.database=1
spring.redis.pool.max-active=8
spring.redis.pool.max-wait=-1
spring.redis.pool.max-idle=500
spring.redis.pool.min-idle=0
spring.redis.timeout=0
```

## 2，消息发布者、消息处理者POJO、redis消息监听器容器以及redis监听器注入IOC容器

- redis configuration

```java
@Configuration //相当于xml中的beans
public class RedisConfig {

    /**
     * redis消息监听器容器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
     * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     * @param connectionFactory
     * @param listenerAdapter
     * @return
     */
    @Bean //相当于xml中的bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //订阅了一个叫chat 的通道
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        //这个container 可以添加多个 messageListener
        return container;
    }

    /**
     * 消息监听器适配器，绑定消息处理器，利用反射技术调用消息处理器的业务方法
     * @param receiver
     * @return
     */
    @Bean
    MessageListenerAdapter listenerAdapter(MessageReceiver receiver) {
        //这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receiveMessage”
        //也有好几个重载方法，这边默认调用处理器的方法 叫handleMessage 可以自己到源码里面看
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    /**redis 读取内容的template */
    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}

```
> MessageListenerAdapter通过反射使普通的POJO就可以处理消息。具体情况见MessageListenerAdapter的onMessage方法。


## 3，消息发布者

```java
@EnableScheduling //开启定时器功能
@Component
public class MessageSender {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 2000) //间隔2s 通过StringRedisTemplate对象向redis消息队列chat频道发布消息
    public void sendMessage(){
        stringRedisTemplate.convertAndSend("chat",String.valueOf(Math.random()));
    }
}
```

## 4，普通的消息处理器POJO

```java
@Component
public class MessageReceiver {

    /**接收消息的方法*/
    public void receiveMessage(String message){
        System.out.println("收到一条消息："+message);
    }

}
```
> MessageListenerAdapter通过反射调用receiveMessage方法处理消息


----------

参考资料：

1，《[Spring AMQP 源码分析 07 - MessageListenerAdapter](https://www.cnblogs.com/gordonkong/p/7201898.html)》

2，《[Spring整合JMS(二)——三种消息监听器](http://elim.iteye.com/blog/1893676)》

3，《[redis 消息队列发布订阅模式spring boot实现](http://blog.csdn.net/wuxuyang_7788/article/details/78004897)》