package com.vecsight.dragonite.forwarder.network;

import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.mux.exception.SenderClosedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Pipe {

    private volatile long pipedBytes = 0;

    private final short bufferSize;

    public Pipe(short bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void pipe(InputStream inputStream, MultiplexedConnection connection) throws IOException, SenderClosedException, InterruptedException {
        int len;
        byte[] buf = new byte[bufferSize];
        while ((len = inputStream.read(buf)) > 0) {
            pipedBytes += len;
            connection.send(Arrays.copyOf(buf, len));
        }
    }

    public void pipe(MultiplexedConnection connection, OutputStream outputStream) throws ConnectionNotAliveException, InterruptedException, IOException {
        int len;
        byte[] buf;
        while ((buf = connection.read()) != null) {
            pipedBytes += buf.length;
            outputStream.write(buf);
        }
    }

}
