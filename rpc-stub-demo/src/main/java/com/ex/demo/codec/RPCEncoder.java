package com.ex.demo.codec;

import com.ex.demo.utils.HessianSerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
 
@SuppressWarnings("rawtypes")
public class RPCEncoder extends MessageToByteEncoder {
 
    private Class<?> genericClass;
 
    public RPCEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
 
    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = HessianSerializeUtil.serialize(in);
            out.writeBytes(data);
        }
    }
}
