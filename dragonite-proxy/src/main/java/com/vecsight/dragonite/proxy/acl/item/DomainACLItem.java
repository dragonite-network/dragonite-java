/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl.item;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ACLItemType;

public class DomainACLItem implements ACLItem {

    private final String domain;

    private final ACLItemMethod method;

    public DomainACLItem(final String domain, final ACLItemMethod method) {
        this.domain = domain;
        this.method = method;
    }

    @Override
    public ACLItemType getType() {
        return ACLItemType.DOMAIN;
    }

    @Override
    public ACLItemMethod getMethod() {
        return method;
    }

    @Override
    public boolean match(final String domain) {
        return this.domain.equalsIgnoreCase(domain);
    }

    @Override
    public boolean match(final byte[] address) {
        return false;
    }
}
