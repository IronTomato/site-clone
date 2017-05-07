package com.irontomato.siteclone.common;

import java.io.IOException;
import java.io.InputStream;

public interface Digester {

    String digest(String data);

    String digest(byte[] data);

    String digest(InputStream data) throws IOException;
}
