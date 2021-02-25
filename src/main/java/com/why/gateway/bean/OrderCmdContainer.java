package com.why.gateway.bean;

import com.google.common.collect.Lists;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class OrderCmdContainer {
    // 要把一个类定义成单例，第一步定义一个静态变量类成员，第二部把构造函数定义成私有的，第三步对外提供一个静态的访问方法，来获取这个私有的静态成员变量
    private static OrderCmdContainer ourInstance = new OrderCmdContainer();
    private OrderCmdContainer(){}

    public static OrderCmdContainer getInstance(){
        return ourInstance;
    }

    ///////////////////////////////

    private final BlockingDeque<OrderCmd> queque = new LinkedBlockingDeque<>();

    public int size(){
        return queque.size();
    }

    public boolean cache(OrderCmd cmd){
        return queque.offer(cmd);
    }

    public List<OrderCmd> getAll(){
        List<OrderCmd> msgList = Lists.newArrayList();
        int count = queque.drainTo(msgList);

        if(count == 0){
            return null;
        }else{
            return msgList;
        }
    }


}
