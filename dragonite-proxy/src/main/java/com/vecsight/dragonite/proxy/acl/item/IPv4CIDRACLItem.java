/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl.item;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ACLItemType;
import com.vecsight.dragonite.proxy.exception.InvalidAddressException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4CIDRACLItem implements ACLItem {

    private final int lowest;

    private final int highest;

    private final ACLItemMethod method;

    public IPv4CIDRACLItem(final String string, final ACLItemMethod method) throws InvalidAddressException {
        this.method = method;

        if (!string.contains("/")) {
            throw new InvalidAddressException(string + " is not a valid IPv4 CIDR address");
        }

        final String[] stringSplit = string.split("/");
        if (stringSplit.length != 2) {
            throw new InvalidAddressException(string + " is not a valid IPv4 CIDR address");
        }

        final byte[] bytes;
        try {
            bytes = InetAddress.getByName(stringSplit[0]).getAddress();
            if (bytes.length != 4) throw new InvalidAddressException(string + " is not a valid IPv4 address");
        } catch (final UnknownHostException e) {
            throw new InvalidAddressException(stringSplit[0] + " is not a valid IPv4 address");
        }

        final int mask;
        try {
            final int shift = Integer.parseInt(stringSplit[1]);

            if (shift < 1 || shift > 32)
                throw new InvalidAddressException(string + " is not a valid IPv4 CIDR address");

            mask = (-1) << (32 - shift);
        } catch (final NumberFormatException e) {
            throw new InvalidAddressException(string + " is not a valid IPv4 CIDR address");
        }

        final int addr = ipv4ToInt(bytes);

        lowest = addr & mask;

        highest = lowest + (~mask);

    }

    private int ipv4ToInt(final byte[] bytes) {
        return ((bytes[0] << 24) & 0xFF000000)
                | ((bytes[1] << 16) & 0xFF0000)
                | ((bytes[2] << 8) & 0xFF00)
                | (bytes[3] & 0xFF);
    }

    @Override
    public ACLItemType getType() {
        return ACLItemType.IPv4_CIDR;
    }

    @Override
    public ACLItemMethod getMethod() {
        return method;
    }

    @Override
    public boolean match(final String domain) {
        return false;
    }

    @Override
    public boolean match(final byte[] address) {
        if (address.length != 4) return false;
        final int addr = ipv4ToInt(address);
        return addr >= lowest && addr <= highest;
    }
}
