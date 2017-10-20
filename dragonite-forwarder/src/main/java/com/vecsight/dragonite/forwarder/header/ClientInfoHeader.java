/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.header;

import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * VERSION  1 SB
 * downMbps 2 US
 * upMbps   2 US
 * nameLen  1 UB
 * nameStr  [length]
 * verLen   1 UB
 * verStr   [length]
 * osLen    1 UB
 * osStr    [length]
 */

public class ClientInfoHeader {

    private static final byte VERSION = ForwarderGlobalConstants.PROTOCOL_VERSION;

    public static final int FIXED_LENGTH = 8;

    private int downMbps, upMbps;

    private String name, appVer, osName;

    public ClientInfoHeader(final int downMbps, final int upMbps, final String name, final String appVer, final String osName) {
        this.downMbps = downMbps;
        this.upMbps = upMbps;
        this.name = name;
        this.appVer = appVer;
        this.osName = osName;
    }

    public ClientInfoHeader(final byte[] header) throws IncorrectHeaderException {
        final BinaryReader reader = new BinaryReader(header);

        try {

            final byte remoteVersion = reader.getSignedByte();

            if (remoteVersion != VERSION) {
                throw new IncorrectHeaderException("Incorrect version (" + remoteVersion + ", should be " + VERSION + ")");
            }

            downMbps = reader.getUnsignedShort();
            upMbps = reader.getUnsignedShort();

            name = new String(reader.getBytesGroupWithByteLength(), ForwarderGlobalConstants.STRING_CHARSET);

            appVer = new String(reader.getBytesGroupWithByteLength(), ForwarderGlobalConstants.STRING_CHARSET);

            osName = new String(reader.getBytesGroupWithByteLength(), ForwarderGlobalConstants.STRING_CHARSET);

        } catch (final BufferUnderflowException e) {
            throw new IncorrectHeaderException("Incorrect header length");
        }
    }

    public int getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(final int downMbps) {
        this.downMbps = downMbps;
    }

    public int getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(final int upMbps) {
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

        final BinaryWriter writer = new BinaryWriter(getFixedLength() + nameBytes.length + appVerBytes.length + osNameBytes.length);

        writer.putSignedByte(VERSION)
                .putUnsignedShort(downMbps)
                .putUnsignedShort(upMbps)
                .putBytesGroupWithByteLength(nameBytes)
                .putBytesGroupWithByteLength(appVerBytes)
                .putBytesGroupWithByteLength(osNameBytes);

        return writer.toBytes();
    }
}
