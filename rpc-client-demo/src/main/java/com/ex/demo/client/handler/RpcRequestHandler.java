package com.ex.demo.client.handler;

import com.ex.demo.client.global.Environment;
import com.ex.demo.remoting.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.FixedChannelPool;

public class RpcRequestHandler extends ChannelInboundHandlerAdapter  {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	 super.channelActive(ctx);
    }
     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        
    	RpcResponse response = (RpcResponse) msg;
        
        Environment.getResponseBlockingQueue(response.getRequestId()).put(response);
        
        FixedChannelPool pool = Environment.getRegisteredChannelPoolMap().get(Environment.getHost());
        Channel channel = ctx.channel();
        pool.release(channel);
    }
    
}
