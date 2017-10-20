/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl.item;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ACLItemType;

public interface ACLItem {

    ACLItemType getType();

    ACLItemMethod getMethod();

    boolean match(final byte[] address);

    //DO NOT resolve and match with hostname's IPs!!! Just return false if it's an IP address item
    boolean match(final String domain);

}
