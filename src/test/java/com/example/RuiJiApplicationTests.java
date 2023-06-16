package com.example;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
class RuiJiApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testSring() {
        redisTemplate.opsForValue().set("add","北京",10, TimeUnit.SECONDS);
        String add = (String) redisTemplate.opsForValue().get("add");
        System.out.println(add);
    }

    @Test
    void testSet(){
        SetOperations setOperations = redisTemplate.opsForSet();

        setOperations.add("myset","a","b","c","d");

        //取值
        Set myset = setOperations.members("myset");
        for (Object o : myset) {
            System.out.println(o);
        }

        //删除
        setOperations.remove("myset","a");

    }

    @Test
    void testZSet(){
        //有序set
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        zSetOperations.add("myZSet","a",1.0);
        zSetOperations.add("myZSet","b",1.1);
        zSetOperations.add("myZSet","c",1.2);
        zSetOperations.add("myZSet","d",1.0);


        zSetOperations.remove("myZSet","a");
//        zSetOperations.add("myZSet","a",1.3);

        zSetOperations.incrementScore("myZSet","d",1.1);

        Set myZSet = zSetOperations.range("myZSet", 0, -1);
        for (Object o : myZSet) {
            System.out.println(o);
        }

    }

    @Test
    void testHash(){
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("001","name","小米");
        hashOperations.put("001","sex","女");

        String name = (String) hashOperations.get("001", "name");
        System.out.println(name);

        Set keys = hashOperations.keys("001");
        for (Object key : keys) {
            System.out.println(key);
        }

        List values = hashOperations.values("001");
        for (Object value : values) {
            System.out.println(value);
        }
    }

    @Test
    void testList(){
        ListOperations listOperations = redisTemplate.opsForList();

        //存值
        listOperations.rightPush("mylist","left1");
        listOperations.leftPushAll("mylist","left2","left3","left4","left5");

        //取值
        List<String> mylist = listOperations.range("mylist", 0, -1);
        for (Object o : mylist) {
            System.out.println(o);
        }

        //出队列
        Long size = listOperations.size("mylist");
        for (Long i = Long.valueOf(0); i < size; i++) {
            String element = (String) listOperations.rightPop("mylist");
            System.out.println(element);
        }
    }

}
