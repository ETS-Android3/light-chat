package com.github.fernthedev.core.encryption.codecs.fastjson;

import com.alibaba.fastjson.JSON;
import com.github.fernthedev.core.PacketRegistry;
import com.github.fernthedev.core.packets.Packet;
import com.github.fernthedev.core.encryption.EncryptedBytes;
import com.github.fernthedev.core.encryption.EncryptedPacketWrapper;
import com.github.fernthedev.core.encryption.PacketWrapper;
import com.github.fernthedev.core.encryption.RSA.IEncryptionKeyHolder;
import com.github.fernthedev.core.encryption.RSA.NoSecretKeyException;
import com.github.fernthedev.core.encryption.UnencryptedPacketWrapper;
import com.github.fernthedev.core.encryption.util.EncryptionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts encrypted json to a decrypted object
 */
@ChannelHandler.Sharable
public class EncryptedFastJSONObjectDecoder extends StringDecoder {

    protected IEncryptionKeyHolder encryptionKeyHolder;
    protected Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public EncryptedFastJSONObjectDecoder(IEncryptionKeyHolder encryptionKeyHolder) {
        this(Charset.defaultCharset(), encryptionKeyHolder);
    }

    /**
     * Creates a new instance with the specified character set.
     *
     * @param charset
     */
    public EncryptedFastJSONObjectDecoder(Charset charset, IEncryptionKeyHolder encryptionKeyHolder) {
        super(charset);
        this.charset = charset;
        this.encryptionKeyHolder = encryptionKeyHolder;
    }

    /**
     * Returns a string list
     *
     * @param ctx
     * @param msg The data received
     * @param out The returned objects
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        List<Object> tempDecodeList = new ArrayList<>();
        super.decode(ctx, msg, tempDecodeList);

        String decodedStr = (String) tempDecodeList.get(0);
        PacketWrapper packetWrapper = JSON.parseObject(decodedStr, PacketWrapper.class);

        String decryptedJSON;

        if (packetWrapper.encrypt()) {
            packetWrapper = JSON.parseObject(decodedStr, EncryptedPacketWrapper.class);
            decryptedJSON = decrypt(ctx, ((EncryptedPacketWrapper) packetWrapper).getJsonObject());
        } else {
            packetWrapper = JSON.parseObject(decodedStr, UnencryptedPacketWrapper.class);
            decryptedJSON = ((UnencryptedPacketWrapper) packetWrapper).getJsonObject();
        }

        out.add(getParsedObject(packetWrapper.getPacketIdentifier(), decryptedJSON));

    }

    /**
     * Converts the JSON Object into it's former instance by providing the class name
     * @param jsonObject
     * @return
     */
    public Object getParsedObject(String packetIdentifier, String jsonObject) {
        Class<? extends Packet> aClass = PacketRegistry.getPacketClassFromRegistry(packetIdentifier);

        if(aClass.isInstance(Packet.class)) {
            throw new IllegalArgumentException("The class provided is not a packet type. Received: " + aClass);
        }

        return JSON.parseObject(jsonObject, (Class<?>) aClass);
    }

    protected String decrypt(ChannelHandlerContext ctx, EncryptedBytes encryptedString) {

        if(!encryptionKeyHolder.isEncryptionKeyRegistered(ctx, ctx.channel())) throw new NoSecretKeyException();

        SecretKey secretKey = encryptionKeyHolder.getSecretKey(ctx, ctx.channel());

        if (secretKey == null) {
            throw new NoSecretKeyException();
        }


        String decryptedJSON = null;


        try {
            decryptedJSON = EncryptionUtil.decrypt(encryptedString, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return decryptedJSON;
    }

}
