package com.irontomato.siteclone.common;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class Md5Digester implements Digester {
    @Override
    public String digest(String data) {
        return DigestUtils.md5Hex(data);
    }

    @Override
    public String digest(byte[] data) {
        return DigestUtils.md5Hex(data);
    }

    @Override
    public String digest(InputStream data) throws IOException {
        return DigestUtils.md5Hex(data);
    }
}
