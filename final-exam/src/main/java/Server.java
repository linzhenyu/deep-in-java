import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class Server {
    public static void main(String[] args) throws Exception {
        // 创建两个线程池
        NioEventLoopGroup acceptEventExecutors = new NioEventLoopGroup();
        NioEventLoopGroup readEventExecutors = new NioEventLoopGroup();

        // 1、创建ServerBootstrap
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // 2.配置ServerBootstrap
        ChannelFuture channelFuture = serverBootstrap
                // 设置netty线程模型
                .group(acceptEventExecutors, readEventExecutors)
                // 设置Netty、类型
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 链式
                        ChannelPipeline pipeline = channel.pipeline();

                        // 添加入栈的处理器 ---》把ByteBuf转成字符串
                        pipeline.addLast(new HttpServerCodec());

                        // 添加入栈处理器 --》把字符串转成HttpFullRequest对象
                        pipeline.addLast(new HttpObjectAggregator(1024));

                        pipeline.addLast(new HttpServer());
                    }
                })
                // 绑定端口的操作是异步的
                .bind(8080);
        // 阻塞
        channelFuture.sync();
        System.out.println("HTTP服务端启动成功。。。");
    }
}