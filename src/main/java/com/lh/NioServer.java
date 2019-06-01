package com.lh;

/**
 * @Author: LH
 * @Date: 2019/6/1 10:54
 * @Version 1.0
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO服务器端
 */
public class NioServer {
    /**
     * 启动
     */
    public void start() throws IOException {
        //七个步骤

        // 1.创建selector
        Selector selector = Selector.open();//快捷键：alt+enter

        // 2.通过ServerSocketChannel创建Channel通道

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3.为Channel绑定监听端口

        serverSocketChannel.bind(new InetSocketAddress(8008));

        //4.**设置Channel为非阻塞模式【重要】**

        serverSocketChannel.configureBlocking(false);

        //5.将Channel注册到Selector上[SelectionKey]
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功！");//sout

        //6.循环等待新连接的接入
        for (; ; ) {

            //获取可用Channel数量 【why】
            int readyChannels = selector.select();
            //[why]  【防止空轮询】【linux】
            if (readyChannels == 0) {
                continue;
            }
            //获取可用Channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                //selectionKey实例

                SelectionKey selectionKey = (SelectionKey) iterator.next();
                //**移除Set中的selectionkey**[重要！！因为获取后就会存放在Set里，不移除会变得越来越多]
                iterator.remove();

                //7.根据就绪状态，调用相应业务逻辑


                //如果是接入事件
                if (selectionKey.isAcceptable()) {

                    acceptHandler(serverSocketChannel, selector);
                }


                //如果是可读事件

                if (selectionKey.isReadable()) {

                    readHandler(selectionKey, selector);
                }


            }


        }


    }

    /*
     * 接入事件处理器【注释快捷键 ctrl+shift +/】
     *
     * */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {

        //如果是接入事件，创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        //将socketChannel设置为非阻塞模式
        socketChannel.configureBlocking(false);

        //channel 注册到selector上,监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //回复客户端的提示消息
        socketChannel.write(Charset.forName("UTF-8").encode("欢迎进入聊天室，啦啦啦请文明发言"));
    }






    /*
     *
     * 可读事件处理器
     * */

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {

        //要从Selectionkey获取就绪的channel

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        //创建buffer

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //循环读取客户端的请求信息
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {

            //byteBuffer本来是写的模式，要切换的读模式
            byteBuffer.flip();

            //读取buffer里面的内容
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }

        //再将channel 注册到selector上,监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //将客户端发送的请求信息广播
        if (request.length() > 0) {
            //System.out.printl("::"+request);
            //广播到其他的客户端
            broadCast(selector,socketChannel, request);
        }
    }


    /*
    *
    * 广播到其他的客户端
    *
    * */

    private  void broadCast(Selector selector,SocketChannel sourceChannel,String request){

       //获取到所有已经接入的客户端Channel[使用的是keys]

        Set<SelectionKey> selectionKeySet = selector.keys();

       //循环向所有Channel广播信息
        selectionKeySet.forEach(selectionKey -> {

            //获取当前channel

           Channel targetChannel= selectionKey.channel();

            //剔除掉发消息的客户端[是当前类的实例而且不是发送消息的客户端]

            if(targetChannel instanceof SocketChannel && targetChannel != sourceChannel ){

                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });




    }







    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();


    }
}