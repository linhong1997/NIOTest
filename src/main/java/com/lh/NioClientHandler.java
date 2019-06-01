package com.lh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: LH
 * @Date: 2019/6/1 12:45
 * @Version 1.0
 */
public class NioClientHandler implements  Runnable {
    private Selector selector;

    //自动生成构造器快捷键：alt+insert
    public NioClientHandler(Selector selector) {

     this.selector=selector;

    }

    @Override
    public void run() {
        //从服务器那里把代码搬运过来
        //6.循环等待新连接的接入
        try {
            //try catch 块快捷键  ctrl+alt+t

            for (; ; ) {

            //获取可用Channel数量 【why】
            int readyChannels = selector.select();
            //[why]
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

                //如果是可读事件

                if (selectionKey.isReadable()) {

                    readHandler(selectionKey, selector);
                }


            }


        }
        } catch (IOException e) {
            e.printStackTrace();
        }


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
        //循环读取服务端响应的请求信息
        String response = "";
        while (socketChannel.read(byteBuffer) > 0) {

            //byteBuffer本来是写的模式，要切换的读模式
            byteBuffer.flip();

            //读取buffer里面的内容
            response  += Charset.forName("UTF-8").decode(byteBuffer);
        }

        //再将channel 注册到selector上,监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //将服务器端的信息打印到本地
        if ( response .length() > 0) {
            System.out.println( response );


        }
    }

}

