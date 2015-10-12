//  The contents of this file are subject to the Mozilla Public License
//  Version 1.1 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License
//  at http://www.mozilla.org/MPL/
//
//  Software distributed under the License is distributed on an "AS IS"
//  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
//  the License for the specific language governing rights and
//  limitations under the License.
//
//  The Original Code is RabbitMQ.
//
//  The Initial Developer of the Original Code is GoPivotal, Inc.
//  Copyright (c) 2007-2015 Pivotal Software, Inc.  All rights reserved.
//

package com.rabbitmq.client.impl;

import com.rabbitmq.client.AMQP;
import nl.maatkamp.datadiode.model.RmqUdpFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * A socket-based frame handler.
 */

public class SocketFrameHandler implements FrameHandler {
    /**
     * Time to linger before closing the socket forcefully.
     */
    public static final int SOCKET_CLOSING_TIMEOUT = 1;
    public static final String HOST = "localhost";

    // public static final String HOST = "192.168.178.18";
    public static final int PORT = 1234;
    public static final int RMQ_INSIDE = 5674;
    public static final int WAIT_MILLIS = 150;
    private static final Logger log = LoggerFactory.getLogger(SocketFrameHandler.class);
    static SocketFrameHandler socketFrameHandler;
    static int index = 0;
    /** The underlying socket */
    private final Socket _socket;
    /** Socket's inputstream - data from the broker - synchronized on */
    private final DataInputStream _inputStream;
    /** Socket's outputstream - data to the broker - synchronized on */
    private final DataOutputStream _outputStream;
    UnicastSendingMessageHandler unicastSendingMessageHandler =
            new UnicastSendingMessageHandler(HOST, PORT);

    /**
     * @param socket the socket to use
     */
    public SocketFrameHandler(Socket socket) throws IOException {
        _socket = socket;

        _inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        _outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        this.socketFrameHandler = this;
    }

    public static SocketFrameHandler socketFrameHandler() {
        return socketFrameHandler;
    }

    public InetAddress getAddress() {
        return _socket.getInetAddress();
    }

    public InetAddress getLocalAddress() {
        return _socket.getLocalAddress();
    }

    // For testing only
    public DataInputStream getInputStream() {
        return _inputStream;
    }

    public int getPort() {
        return _socket.getPort();
    }

    public int getLocalPort() {
        return _socket.getLocalPort();
    }

    public int getTimeout()
            throws SocketException {
        return _socket.getSoTimeout();
    }

    public void setTimeout(int timeoutMs)
            throws SocketException
    {
        _socket.setSoTimeout(timeoutMs);
    }

    /**
     * Write a 0-8-style connection header to the underlying socket,
     * containing the specified version information, kickstarting the
     * AMQP protocol version negotiation process.
     *
     * @param major major protocol version number
     * @param minor minor protocol version number
     * @throws IOException if there is a problem accessing the connection
     * @see #sendHeader()
     */
    public void sendHeader(int major, int minor) throws IOException {
        synchronized (_outputStream) {
            _outputStream.write("AMQP".getBytes("US-ASCII"));
            _outputStream.write(1);
            _outputStream.write(1);
            _outputStream.write(major);
            _outputStream.write(minor);
            _outputStream.flush();
        }
    }

    /**
     * Write a 0-9-1-style connection header to the underlying socket,
     * containing the specified version information, kickstarting the
     * AMQP protocol version negotiation process.
     *
     * @param major major protocol version number
     * @param minor minor protocol version number
     * @param revision protocol revision number
     * @throws IOException if there is a problem accessing the connection
     * @see #sendHeader()
     */
    public void sendHeader(int major, int minor, int revision) throws IOException {
        synchronized (_outputStream) {
            _outputStream.write("AMQP".getBytes("US-ASCII"));
            _outputStream.write(0);
            _outputStream.write(major);
            _outputStream.write(minor);
            _outputStream.write(revision);
            _outputStream.flush();
        }
    }

    public void sendHeader() throws IOException {
        sendHeader(AMQP.PROTOCOL.MAJOR, AMQP.PROTOCOL.MINOR, AMQP.PROTOCOL.REVISION);
    }

    public SocketFrameHandler getInstance() {
        return this;
    }

    public Frame readFrame() throws IOException {
        synchronized (_inputStream) {
            Frame frame = Frame.readFrom(_inputStream);
            if(frame != null) {
                log.debug("readFrame: addr(" + this.getAddress() + ":" + this.getPort() + ")]: " + frame + ": " + new String(frame.getPayload()));
            }
            log.info("rmq.frame.channel(" + frame.channel + ").type(" + frame.type + ").payload(" + frame.getPayload().length + ")");


            return frame;
        }
    }

    public void writeFrame(Frame frame) throws IOException {
        if(frame != null) {
            log.debug("writeFrame: addr(" + this.getAddress() + ":" + this.getPort() + "))]: " + frame + ": " + new String(frame.getPayload()));
        }

        if (this.getPort() == RMQ_INSIDE) {
            RmqUdpFrame rmqUdpFrame = new RmqUdpFrame(++index, frame.channel, frame.type, frame.getPayload());
            byte[] payload = SerializationUtils.serialize(rmqUdpFrame);

            log.info("rmq.frame.index(" + rmqUdpFrame.getIndex() + ").channel(" + frame.channel + ").type(" + frame.type + ").payload(" + frame.getPayload().length + ")");

            try {
                unicastSendingMessageHandler.handleMessageInternal(new GenericMessage<byte[]>(payload));
                Thread.sleep(WAIT_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        synchronized (_outputStream) {
            frame.writeTo(_outputStream);
        }
    }

    public void flush() throws IOException {
        _outputStream.flush();
    }

    @SuppressWarnings("unused")
    public void close() {
        try { _socket.setSoLinger(true, SOCKET_CLOSING_TIMEOUT); } catch (Exception _e) {}
        try { flush();                                           } catch (Exception _e) {}
        try { _socket.close();                                   } catch (Exception _e) {}
    }
}
