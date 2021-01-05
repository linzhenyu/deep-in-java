import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

class HttpServer extends SimpleChannelInboundHandler<FullHttpRequest> {

    final String GET_REQUEST = "GET";

    final String FILE_PATH = System.getProperty("user.dir") + "/final-exam/ftp/";

    final String CONTEXT_TYPE = "Context-type";

    final String HTML_TYPE = "text/html;charset=UTF-8";

    final String CONTENT_LENGTH = "Content-Length";

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {

        // 获取用户的uri
        String uri = request.getUri();
        String name = request.getMethod().name();

        // 只支持GEt请求
        if (!GET_REQUEST.equals(name)) {
            // 响应浏览器
            responseError("只支持GET请求", channelHandlerContext);
            return;
        }

        File file = new File(FILE_PATH + URLDecoder.decode(uri, "UTF-8"));

        if (!file.exists()) {
            responseError("文件不存在。。。", channelHandlerContext);
            return;
        }

        if (file.isFile()) {
            // 如果是一个文件，就执行下载
            responseFileCopy(file, channelHandlerContext);
        } else {
            // 如果是一个目录就显示子目录
            responseDir(channelHandlerContext, file, uri);
        }

    }

    /**
     * 文件下载
     * @param file
     * @param channelHandlerContext
     */
    private void responseFileCopy(File file, ChannelHandlerContext channelHandlerContext) {

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(CONTEXT_TYPE, "application/octet-stream");
        response.headers().add(CONTENT_LENGTH, file.length());

        Channel channel = channelHandlerContext.channel();

        FileInputStream ips = null;
        try {
            ips = new FileInputStream(file);
            byte[] by = new byte[1024];
            int read = -1;
            System.out.println("下载进度：00.0%");
            float temp = 0;
            while ((read = ips.read(by, 0, by.length)) != -1) {
                response.content().writeBytes(by, 0, read);
                float percentage = (float)(100-(read/by.length)*10);
                if (temp - percentage != 0) {
                    System.out.println("下载进度：" + percentage + "%");
                }
                temp = percentage;
            }
            channel.writeAndFlush(response);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ips != null) {
                try {
                    ips.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 响应目录信息
     * @param channelHandlerContext
     * @param file
     * @param uri
     */
    private void responseDir(ChannelHandlerContext channelHandlerContext, File file, String uri) {

        StringBuffer buffer = new StringBuffer();
        // 获取目录的子文件
        File[] files = file.listFiles();

        for (File file1 : files) {
            if ("/".equals(uri)) {
                buffer.append("<li><a href= '" + uri + file1.getName() + "'>" + file1.getName() + "</a></li>");
            } else {
                buffer.append("<li><a href= '" + uri + File.separator + file1.getName() + "'>" + file1.getName() + "</a></li>");
            }
        }
        responseClient(buffer.toString(), channelHandlerContext);
    }

    /**
     * 响应客户端
     * @param text
     * @param channelHandlerContext
     */
    public void responseClient(String text, ChannelHandlerContext channelHandlerContext) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        // 设置响应头
        response.headers().add(CONTEXT_TYPE, HTML_TYPE);

        String msg = "<html> \t<meta charset=\"UTF-8\" />" + text + "</html>";

        try {
            response.content().writeBytes(msg.getBytes("UTF-8"));
            channelHandlerContext.channel().writeAndFlush(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 响应错误信息
     * @param text
     * @param channelHandlerContext
     */
    public void responseError(String text, ChannelHandlerContext channelHandlerContext) {
        String msg = "<h1>" + text + "</h1>";
        responseClient(msg, channelHandlerContext);
    }
}