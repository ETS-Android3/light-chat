package com.github.fernthedev.client.netty;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.EventListener;
import com.github.fernthedev.packets.ConnectedPacket;
import com.github.fernthedev.packets.Packet;
import com.github.fernthedev.universal.EncryptionHandler;
import com.github.fernthedev.universal.StaticHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelHandlerAdapter {

    protected EventListener listener;

    public ClientHandler(Client client,EventListener listener) {
        this.listener = listener;
        this.client = client;
        String os = client.getOSName();

        connectedPacket = new ConnectedPacket(client.name,os, client.getUuid());
    }

    protected Client client;

    @Getter
    protected ConnectedPacket connectedPacket;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        Packet packet;

        if(msg instanceof SealedObject) {

            SealedObject ob = (SealedObject) msg;

            packet = (Packet) EncryptionHandler.decrypt(ob, client.getPrivateKey());

            listener.recieved(packet);
        }else if (msg instanceof Packet){
            packet = (Packet) msg;
            listener.recieved(packet);
        }

        ctx.flush();



        //ctx.close();
    }

    /**
     * Method for Encrypt Plain String Data
     * @param plainText
     * @return encryptedText
     */
    public static String encrypt(String encryptionKey,String plainText) {
        String encryptedText = "";
        try {
            Cipher cipher   = Cipher.getInstance(StaticHandler.getCipherTransformation());
            byte[] key      = encryptionKey.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);

        } catch (Exception E) {
            System.err.println("Encrypt Exception : "+E.getMessage());
        }
        return encryptedText;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Client.getLogger().info("Lost connection to server.");
        client.getClientThread().close();
    }
}
