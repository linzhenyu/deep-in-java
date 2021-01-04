import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author linzy
 * @version ServerHandler.java v 0.1 2021 - 01 - 04 3:59 下午 linzy
 */
public class ServerHandler extends SimpleChannelInboundHandler<String>{

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 收到新的客户端连接
     * channel存入ChannelGroup列表
     * 通知列表中其他客户端channel
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext){
        //收到新的客户端连接
        Channel newClient = channelHandlerContext.channel();
        //通知列表中其他客户端channel
        for(Channel channel:channels){
            channel.writeAndFlush("欢迎"+newClient.remoteAddress()+"加入\n");
        }
        //channel存入ChannelGroup列表
        channels.add(channelHandlerContext.channel());
    }

    /**
     * 服务端断开客户端连接
     * channel从ChannelGroup中移除
     * 通知列表中其他客户端channel
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {
        //服务端断开客户端连接
        Channel oldClient = channelHandlerContext.channel();
        //通知列表中其他客户端channel
        for(Channel channel:channels){
            channel.writeAndFlush(oldClient.remoteAddress()+"离开了\n");
        }
        //channel从ChannelGroup中移除
        channels.remove(channelHandlerContext.channel());
    }


    /**
     * 某客户端发话
     * 通知列表中其他客户端channel
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        String response;
        response = channelHandlerContext.channel().remoteAddress() + "说" + s + "\n";
        for(Channel channel:channels){
            channel.writeAndFlush(response);
        }
    }
}