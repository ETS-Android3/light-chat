package com.github.fernthedev.lightchat.core.encryption.codecs;

public interface JSONHandler {
    <T> T fromJson(String decodedStr, Class<T> packetWrapperClass);

    String toJson(Object msg);
}
