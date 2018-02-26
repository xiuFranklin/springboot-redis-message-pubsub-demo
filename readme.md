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

## 2，消息发布者、消息订阅者、redis消息监听器容器以及redis监听器注入IOC容器

- redis configuration

```
@Configuration //相当于xml中的beans
public class RedisConfig {

    /**
     * redis消息监听器容器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
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

> 