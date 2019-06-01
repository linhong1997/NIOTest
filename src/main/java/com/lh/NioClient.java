package com.lh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @Author: LH
 * @Date: 2019/6/1 12:07
 * @Version 1.0
 */
/*
*  NIO客户端
*
* */
public class NioClient {
    //启动
    public  void start(String nickName) throws IOException {

        //连接服务器端

       SocketChannel socketChannel= SocketChannel.open(
                new InetSocketAddress("127.0.0.1",8008)
                );

       //接收服务器端的响应

        //[开一个线程，用来专门接收服务器端的响应]
       //selector,socketChannel,注册
        Selector selector =Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();



        //向服务器端发送数据
        System.out.println("请输入发送信息");
        Scanner sc =new Scanner(System.in);
        while(sc.hasNextLine()){

        String request = sc.nextLine();
        if(request != null &&request.length() > 0){
            socketChannel.write(Charset.forName("UTF-8").encode(nickName +":"+request));


        }

        }
    }


    public static void main(String[] args) throws IOException {
        //NioClient nioClient =new NioClient();
       // nioClient.start();
    }

}
