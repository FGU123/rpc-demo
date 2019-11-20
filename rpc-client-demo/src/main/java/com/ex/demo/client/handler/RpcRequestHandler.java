package com.ex.demo.client.handler;

import com.ex.demo.client.global.Environment;
import com.ex.demo.remoting.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.FixedChannelPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcRequestHandler extends ChannelInboundHandlerAdapter  {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	 super.channelActive(ctx);
    }
     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        
    	RpcResponse response= (RpcResponse) msg;
        
        Environment.getResultBlockingQueue(response.getRequestId()).put(response.getResult());
        
        FixedChannelPool pool = Environment.getRegisteredChannelPoolMap().get(Environment.getHost());
        Channel channel = ctx.channel();
        log.info("released channel [id={}] back to pool", channel.id());
        pool.release(channel);
    }
    
}
