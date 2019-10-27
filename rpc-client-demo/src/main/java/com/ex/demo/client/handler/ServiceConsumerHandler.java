package com.ex.demo.client.handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * A real worker for communicate with the server, sending and receiving message.
 * As an in-bound-channel-handler acts like a callable thread model
 */
@Slf4j
public class ServiceConsumerHandler extends ChannelInboundHandlerAdapter implements Callable<Object>{
 
    private ChannelHandlerContext context;
    private RpcResponse rpcResponse;
    private RpcRequest params;
    
	private ConcurrentHashMap<String,SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();
 
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       context = ctx;
    }
 
    /**
     * receive response and read message from server
     */
    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	
    	log.info("receive msg from server: {}", msg);
    	
    	rpcResponse = (RpcResponse)msg;
    	
    	SynchronousQueue<Object> queue = queueMap.get(rpcResponse.getRequestId());

    	queue.put(rpcResponse);
    }
 
    /**
     * send request to server and then wait till channelRead method receive
     * and put response result into a synchronous queue <br>
     * {@link ServiceConsumerHandler#channelRead(ChannelHandlerContext, Object)}
     */
    @Override
	public Object call() throws Exception { // TODO 同步锁，保证
    	
    	SynchronousQueue<Object> queue = new SynchronousQueue<Object>();
    
    	queueMap.put(params.getRequestId(), queue);
        
    	// send
    	context.writeAndFlush(params); 
        
    	log.info("send msg to server: {}", params);
		
    	// here wait, till receive response
    	RpcResponse response = (RpcResponse) queue.take(); 
    	
    	// after return response result, this request would end
    	queueMap.remove(response.getRequestId()); 
    	
    	return response.getResult();
    	
    }
 
    public RpcRequest getParams() {
        return params;
    }
 
    public void setParams(RpcRequest params) {
        this.params = params;
    }
}
