/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.conn;

import com.vecsight.dragonite.mux.exception.ConnectionAlreadyExistsException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.mux.frame.Frame;
import com.vecsight.dragonite.mux.frame.FrameParser;
import com.vecsight.dragonite.mux.frame.types.*;

import java.util.*;

public class Multiplexer {

    private final PacketSender packetSender;

    private final FrameParser frameParser;

    private final Map<Short, MultiplexedConnection> connectionMap = new HashMap<>();

    private final Object connLock = new Object();

    private final Queue<MultiplexedConnection> acceptQueue = new LinkedList<>();

    private final Object acceptLock = new Object();

    private volatile boolean alive = true;

    public Multiplexer(final PacketSender packetSender, final int maxFrameSize) {
        this.packetSender = packetSender;
        frameParser = new FrameParser(maxFrameSize);
    }

    public MultiplexedConnection createConnection(final short connID) throws ConnectionAlreadyExistsException, MultiplexerClosedException {
        if (alive) {
            synchronized (connLock) {
                if (!connectionMap.containsKey(connID)) {
                    final MultiplexedConnection connection = new MultiplexedConnection(this, connID, packetSender);
                    connectionMap.put(connID, connection);
                    packetSender.sendPacket(new CreateConnectionFrame(connID).toBytes());
                    return connection;
                } else {
                    throw new ConnectionAlreadyExistsException();
                }
            }
        } else {
            throw new MultiplexerClosedException();
        }
    }

    public MultiplexedConnection acceptConnection() throws InterruptedException, MultiplexerClosedException {
        if (alive) {
            synchronized (acceptLock) {
                MultiplexedConnection connection;
                while ((connection = acceptQueue.poll()) == null && alive) {
                    acceptLock.wait();
                }
                if (connection != null)
                    return connection;
                else
                    throw new MultiplexerClosedException();
            }
        } else {
            throw new MultiplexerClosedException();
        }
    }

    protected void removeConnectionFromMap(final short connID) {
        synchronized (connLock) {
            connectionMap.remove(connID);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void close() {
        if (alive) {
            alive = false;
            synchronized (connLock) {
                for (final Iterator<Map.Entry<Short, MultiplexedConnection>> it = connectionMap.entrySet().iterator(); it.hasNext(); ) {
                    final Map.Entry<Short, MultiplexedConnection> entry = it.next();
                    entry.getValue().closeNoRemove();
                    it.remove();
                }
            }
            synchronized (acceptLock) {
                acceptQueue.clear();
                acceptLock.notifyAll();
            }
        }
    }

    public void onReceiveBytes(final byte[] rawBytes) {
        final Frame frame = frameParser.feed(rawBytes);
        if (frame != null) {
            processFrame(frame);
        }
    }

    private void processFrame(final Frame frame) {
        if (frame instanceof DataFrame) {

            final short connID = ((DataFrame) frame).getConnectionID();
            final MultiplexedConnection connection = connectionMap.get(connID);

            if (connection != null) connection.addFrame(frame);

        } else if (frame instanceof CloseConnectionFrame) {

            final short connID = ((CloseConnectionFrame) frame).getConnectionID();
            final MultiplexedConnection connection = connectionMap.get(connID);

            if (connection != null) {
                connection.closeSender();
                connection.addFrame(frame);
            }

        } else if (frame instanceof CreateConnectionFrame) {

            final short connID = ((CreateConnectionFrame) frame).getConnectionID();
            final MultiplexedConnection connection;
            synchronized (connLock) {
                connection = new MultiplexedConnection(this, connID, packetSender);
                connectionMap.put(connID, connection);
            }

            synchronized (acceptLock) {
                acceptQueue.add(connection);
                acceptLock.notifyAll();
            }
        } else if (frame instanceof PauseConnectionFrame) {
            final short connID = ((PauseConnectionFrame) frame).getConnectionID();
            final MultiplexedConnection connection = connectionMap.get(connID);

            if (connection != null) {
                connection.pauseSend();
            }
        } else if (frame instanceof ContinueConnectionFrame) {
            final short connID = ((ContinueConnectionFrame) frame).getConnectionID();
            final MultiplexedConnection connection = connectionMap.get(connID);

            if (connection != null) {
                connection.continueSend();
            }
        }
    }

}
