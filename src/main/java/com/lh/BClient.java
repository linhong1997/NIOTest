package com.lh;

import java.io.IOException;

/**
 * @Author: LH
 * @Date: 2019/6/1 14:11
 * @Version 1.0
 */
public class BClient {
    public static void main(String[] args) throws IOException {
        NioClient nioClient =new NioClient();
        nioClient.start("BClient");

    }
}
