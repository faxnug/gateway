package com.why.gateway.bean.handler;

import com.sun.istack.internal.NotNull;
import com.why.gateway.bean.GatewayConfig;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;

@Log4j2
@RequiredArgsConstructor
public class ConnHandler implements Handler<NetSocket> {
    @NotNull
    private GatewayConfig config;

    // 包头[包体长度 int + 校验和byte + src short + dst short + 消息类型 short + 消息状态 byte + 包编号 long]
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    @Override
    public void handle(NetSocket netSocket) {
        IMsgHandler msgHandler = new MsgHandler(config.getBodyCodec());
        msgHandler.onConnect(netSocket);

        //1.parser
        final RecordParser parser =  RecordParser.newFixed(PACKET_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            // 包头[包体长度 int + 校验和 byte + src short + dst short + 消息类型 short + 消息状态 byte + 包编号 long]
            int bodyLength = -1;
            byte checkSum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte status = -1;
            long packetNo = -1;

            @Override
            public void handle(Buffer buffer) {
                if(bodyLength == -1){
                    //读到包头
                    bodyLength = buffer.getInt(0);
                    checkSum = buffer.getByte(4);
                    msgSrc = buffer.getShort(5);
                    msgDst = buffer.getShort(7);
                    msgType = buffer.getShort(9);
                    status = buffer.getByte(11);
                    packetNo = buffer.getLong(12);
                    parser.fixedSizeMode(bodyLength);
                }else{
                    //读取数据
                    byte[] bodyBytes = buffer.getBytes();
                    //
                    CommonMsg msg;
                    if(checkSum != config.getCs().getChecksum(bodyBytes)){
                        log.error("illegal byte body exist from client:{}",netSocket.remoteAddress());
                        return;
                    }else{
                        if(msgDst != config.getId()){
                            log.error("recv error msgDst dst:{} from client:{}", msgDst, netSocket.remoteAddress());
                            return;
                        }

                        msg = new CommonMsg();
                        msg.setBodyLength(bodyBytes.length);
                        msg.setChecksum(checkSum);
                        msg.setMsgSrc(msgSrc);
                        msg.setMsgDst(msgDst);
                        msg.setMsgType(msgType);
                        msg.setStatus(status);
                        msg.setMsgNo(packetNo);
                        msg.setBody(bodyBytes);
                        msg.setTimestamp(System.currentTimeMillis());

                        msgHandler.onCounterData(msg);

                        //reset
                        bodyLength = -1;
                        checkSum = -1;
                        msgSrc = -1;
                        msgDst = -1;
                        msgType = -1;
                        status = -1;
                        packetNo = -1;
                        parser.fixedSizeMode(PACKET_HEADER_LENGTH);

                    }
                }
            }
        });

        netSocket.handler(parser);

        //2.异常 退出 处理器
        netSocket.closeHandler(close -> {
            msgHandler.onDisConnect(netSocket);
        });

        netSocket.exceptionHandler(e -> {
            msgHandler.onException(netSocket, e);
            netSocket.close();
        });
    }
}
