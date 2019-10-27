package com.ex.demo.codec;

import java.util.List;

import com.ex.demo.utils.HessianSerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
 
public class RPCDecoder extends ByteToMessageDecoder {
    
	private Class<?> genericClass;
 
    public RPCDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
 
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final int length = in.readableBytes();
        final byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        Object obj = HessianSerializeUtil.deserialize(bytes, genericClass);
        out.add(obj);
    }
 
}
