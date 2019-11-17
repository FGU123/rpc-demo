package com.ex.demo.client.handler;

import java.util.concurrent.SynchronousQueue;

import com.ex.demo.client.ServiceClient;
import com.ex.demo.client.global.Environment;
import com.ex.demo.remoting.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.FixedChannelPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPCRequestHandler extends ChannelInboundHandlerAdapter  {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	 super.channelActive(ctx);
    }
     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response= (RpcResponse) msg;
        
        Environment.queueMap.putIfAbsent(response.getRequestId(), new SynchronousQueue<Object>());
        Environment.queueMap.get(response.getRequestId()).put(response.getResult());
        
        FixedChannelPool pool = ServiceClient.poolMap.get(Environment.host);
        Channel channel = ctx.channel();
        log.info("released channel: "+channel.id());
        pool.release(channel);
    }
    
}
