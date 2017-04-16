package com.irontomato.siteclone.common;

import org.apache.commons.codec.digest.DigestUtils;

public class CommonUtils {

    public static String digest(String data) {
        return DigestUtils.md5Hex(data);
    }
}
