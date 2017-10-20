/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl.item;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ACLItemType;

public class DomainSuffixACLItem implements ACLItem {

    private final String domain;

    private final ACLItemMethod method;

    public DomainSuffixACLItem(final String domain, final ACLItemMethod method) {
        this.domain = domain;
        this.method = method;
    }

    @Override
    public ACLItemType getType() {
        return ACLItemType.DOMAIN_SUFFIX;
    }

    @Override
    public ACLItemMethod getMethod() {
        return method;
    }

    @Override
    public boolean match(final String domain) {
        return this.domain.equalsIgnoreCase(domain) || endsWithIgnoreCase(domain, "." + this.domain);
    }

    @Override
    public boolean match(final byte[] address) {
        return false;
    }

    private static boolean endsWithIgnoreCase(final String str, final String suffix) {
        if (str == null || suffix == null) {
            return str == null && suffix == null;
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        final int strOffset = str.length() - suffix.length();
        return str.regionMatches(true, strOffset, suffix, 0, suffix.length());
    }
}
