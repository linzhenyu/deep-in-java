import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 启动服务端
 * @author linzy
 * @version Server.java v 0.1 2021 - 01 - 04 3:58 下午 linzy
 */
public class Server {
    private int port;

    public Server(int port){
        this.port = port;
    }

    public void run() throws Exception{
        //NioEventLoopGroup是用来处理IO操作的多线程事件循环器
        //boss用来接收进来的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //用来处理已经被接收的连接;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap sBootstrap = new ServerBootstrap();

            sBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("本群已建立");

            //绑定端口,开始接收进来的连接
            ChannelFuture future = sBootstrap.bind(port).sync();
            //等待服务器socket关闭
            //在本例子中不会发生,这时可以关闭服务器了
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            System.out.println("本群已解散");
        }

    }

    public static void main(String[] args) throws Exception {
        new Server(8080).run();
    }
}