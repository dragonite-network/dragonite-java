package com.vecsight.dragonite.forwarder.header;

import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;

import java.nio.ByteBuffer;

/*
 * VERSION  1B
 * downMbps 2B
 * upMbps   2B
 * nameLen  1B
 * nameStr  [length]
 * verLen   1B
 * verStr   [length]
 * osLen    1B
 * osStr    [length]
 */

public class ClientInfoHeader {

    private static final byte VERSION = ForwarderGlobalConstants.PROTOCOL_VERSION;

    public static final int FIXED_LENGTH = 8;

    private short downMbps, upMbps;

    private String name, appVer, osName;

    public ClientInfoHeader(final short downMbps, final short upMbps, final String name, final String appVer, final String osName) {
        this.downMbps = downMbps;
        this.upMbps = upMbps;
        this.name = name;
        this.appVer = appVer;
        this.osName = osName;
    }

    public ClientInfoHeader(final byte[] header) throws IncorrectHeaderException {
        final ByteBuffer buffer = ByteBuffer.wrap(header);
        final byte remoteVersion = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectHeaderException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }

        downMbps = buffer.getShort();
        upMbps = buffer.getShort();

        final byte nameLen = buffer.get();
        final byte[] rawName = new byte[nameLen];
        buffer.get(rawName);
        name = new String(rawName, ForwarderGlobalConstants.STRING_CHARSET);

        final byte verLen = buffer.get();
        final byte[] rawVer = new byte[verLen];
        buffer.get(rawVer);
        appVer = new String(rawVer, ForwarderGlobalConstants.STRING_CHARSET);

        final byte osLen = buffer.get();
        final byte[] rawOs = new byte[osLen];
        buffer.get(rawOs);
        osName = new String(rawOs, ForwarderGlobalConstants.STRING_CHARSET);
    }

    public short getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(final short downMbps) {
        this.downMbps = downMbps;
    }

    public short getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(final short upMbps) {
        this.upMbps = upMbps;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(final String appVer) {
        this.appVer = appVer;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(final String osName) {
        this.osName = osName;
    }

    public static byte getVersion() {
        return VERSION;
    }

    public static int getFixedLength() {
        return FIXED_LENGTH;
    }

    public byte[] toBytes() {
        final byte[] nameBytes = name.getBytes(ForwarderGlobalConstants.STRING_CHARSET);
        final byte[] appVerBytes = appVer.getBytes(ForwarderGlobalConstants.STRING_CHARSET);
        final byte[] osNameBytes = osName.getBytes(ForwarderGlobalConstants.STRING_CHARSET);

        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + nameBytes.length + appVerBytes.length + osNameBytes.length);
        buffer.put(VERSION);
        buffer.putShort(downMbps);
        buffer.putShort(upMbps);
        buffer.put((byte) nameBytes.length);
        buffer.put(nameBytes);
        buffer.put((byte) appVerBytes.length);
        buffer.put(appVerBytes);
        buffer.put((byte) osNameBytes.length);
        buffer.put(osNameBytes);
        return buffer.array();
    }
}
