/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.Messages;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJPacketTooBigException;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.protocol.a.NativePacketHeader;
import com.mysql.cj.protocol.a.NativePacketPayload;
import java.io.IOException;
import java.util.Optional;

public class SimplePacketReader
implements MessageReader<NativePacketHeader, NativePacketPayload> {
    protected SocketConnection socketConnection;
    protected RuntimeProperty<Integer> maxAllowedPacket;
    private byte readPacketSequence = (byte)-1;
    NativePacketHeader lastHeader = null;
    NativePacketPayload lastMessage = null;

    public SimplePacketReader(SocketConnection socketConnection, RuntimeProperty<Integer> maxAllowedPacket) {
        this.socketConnection = socketConnection;
        this.maxAllowedPacket = maxAllowedPacket;
    }

    @Override
    public NativePacketHeader readHeader() throws IOException {
        if (this.lastHeader == null) {
            return this.readHeaderLocal();
        }
        NativePacketHeader hdr = this.lastHeader;
        this.lastHeader = null;
        this.readPacketSequence = hdr.getMessageSequence();
        return hdr;
    }

    @Override
    public NativePacketHeader probeHeader() throws IOException {
        this.lastHeader = this.readHeaderLocal();
        return this.lastHeader;
    }

    private NativePacketHeader readHeaderLocal() throws IOException {
        NativePacketHeader hdr = new NativePacketHeader();
        try {
            this.socketConnection.getMysqlInput().readFully(hdr.getBuffer().array(), 0, 4);
            int packetLength = hdr.getMessageSize();
            if (packetLength > this.maxAllowedPacket.getValue()) {
                throw new CJPacketTooBigException(packetLength, this.maxAllowedPacket.getValue().intValue());
            }
        }
        catch (CJPacketTooBigException | IOException e) {
            try {
                this.socketConnection.forceClose();
            }
            catch (Exception exception) {
                // empty catch block
            }
            throw e;
        }
        this.readPacketSequence = hdr.getMessageSequence();
        return hdr;
    }

    @Override
    public NativePacketPayload readMessage(Optional<NativePacketPayload> reuse, NativePacketHeader header) throws IOException {
        if (this.lastMessage == null) {
            return this.readMessageLocal(reuse, header);
        }
        NativePacketPayload buf = this.lastMessage;
        this.lastMessage = null;
        return buf;
    }

    @Override
    public NativePacketPayload probeMessage(Optional<NativePacketPayload> reuse, NativePacketHeader header) throws IOException {
        this.lastMessage = this.readMessageLocal(reuse, header);
        return this.lastMessage;
    }

    private NativePacketPayload readMessageLocal(Optional<NativePacketPayload> reuse, NativePacketHeader header) throws IOException {
        try {
            NativePacketPayload message;
            int packetLength = header.getMessageSize();
            if (reuse.isPresent()) {
                message = reuse.get();
                message.setPosition(0);
                if (message.getByteBuffer().length < packetLength) {
                    message.setByteBuffer(new byte[packetLength]);
                }
                message.setPayloadLength(packetLength);
            } else {
                message = new NativePacketPayload(new byte[packetLength]);
            }
            int numBytesRead = this.socketConnection.getMysqlInput().readFully(message.getByteBuffer(), 0, packetLength);
            if (numBytesRead != packetLength) {
                throw new IOException(Messages.getString("PacketReader.1", new Object[]{packetLength, numBytesRead}));
            }
            return message;
        }
        catch (IOException e) {
            try {
                this.socketConnection.forceClose();
            }
            catch (Exception exception) {
                // empty catch block
            }
            throw e;
        }
    }

    @Override
    public byte getMessageSequence() {
        return this.readPacketSequence;
    }

    @Override
    public void resetMessageSequence() {
        this.readPacketSequence = 0;
    }
}

