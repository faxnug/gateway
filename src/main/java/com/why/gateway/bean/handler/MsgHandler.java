package com.why.gateway.bean.handler;

import com.why.gateway.bean.OrderCmdContainer;
import io.vertx.core.net.NetSocket;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;
import thirdpart.codec.IBodyCodec;
import thirdpart.order.OrderCmd;

@Log4j2
@AllArgsConstructor
public class MsgHandler implements IMsgHandler {
    private IBodyCodec bodyCodec;

    @Override
    public void onConnect(NetSocket socket) {

    }

    @Override
    public void onDisConnect(NetSocket socket) {

    }

    @Override
    public void onException(NetSocket socket, Throwable e) {

    }

    @Override
    public void onCounterData(CommonMsg msg) {
        OrderCmd orderCmd;

        try{
            orderCmd = bodyCodec.deserialize(msg.getBody(), OrderCmd.class);
            log.info("recv cmd:{}", orderCmd);
//            if(log.isDebugEnabled()){
//                log.debug("recv cmd:{}", orderCmd);
//            }

            if(!OrderCmdContainer.getInstance().cache(orderCmd)){
                log.error("gateway queue insert fail, queque length:{}, order:{}", OrderCmdContainer.getInstance().size(),orderCmd);
            }
        }catch (Exception e){
            log.error("decode order cmd error", e);
            e.printStackTrace();
        }
    }
}
