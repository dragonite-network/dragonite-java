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
import java.util.Arrays;

public class IPv6ACLItem implements ACLItem {

    private final byte[] bytes;

    private final ACLItemMethod method;

    public IPv6ACLItem(final String string, final ACLItemMethod method) throws InvalidAddressException {
        this.method = method;
        try {
            bytes = InetAddress.getByName(string).getAddress();
            if (bytes.length != 16) throw new InvalidAddressException(string + " is not a valid IPv6 address");
        } catch (final UnknownHostException e) {
            throw new InvalidAddressException(string + " is not a valid IPv6 address");
        }
    }

    public IPv6ACLItem(final byte[] bytes, final ACLItemMethod method) {
        this.method = method;
        this.bytes = bytes;
    }

    @Override
    public ACLItemType getType() {
        return ACLItemType.IPv6;
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
        return Arrays.equals(address, bytes);
    }
}
