/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl;

import com.vecsight.dragonite.proxy.acl.item.ACLItem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ParsedACL {

    private final String title;

    private final String author;

    private final ACLItemMethod defaultMethod;

    private final List<ACLItem> items;

    private final ConcurrentHashMap<String, ACLItemMethod> domainCacheMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<ByteBuffer, ACLItemMethod> ipCacheMap = new ConcurrentHashMap<>();

    public ParsedACL(final String title, final String author, final ACLItemMethod defaultMethod, final List<ACLItem> items) {
        this.title = title;
        this.author = author;
        this.defaultMethod = defaultMethod;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public List<ACLItem> getItems() {
        return items;
    }

    public ACLItemMethod getDefaultMethod() {
        return defaultMethod;
    }

    public ACLItemMethod checkDomain(final String address) {
        final ACLItemMethod cachedMethod = domainCacheMap.get(address);
        if (cachedMethod != null) return cachedMethod;

        byte[] ip;
        try {
            ip = InetAddress.getByName(address).getAddress();
        } catch (final UnknownHostException e) {
            ip = null;
        }

        for (final ACLItem item : items) {
            if (item.match(address) || (ip != null && item.match(ip))) {
                domainCacheMap.put(address, item.getMethod());
                return item.getMethod();
            }
        }
        return defaultMethod;
    }

    public ACLItemMethod checkIP(final byte[] ip) {
        final ACLItemMethod cachedMethod = ipCacheMap.get(ByteBuffer.wrap(ip));
        if (cachedMethod != null) return cachedMethod;

        for (final ACLItem item : items) {
            if (item.match(ip)) {
                ipCacheMap.put(ByteBuffer.wrap(ip), item.getMethod());
                return item.getMethod();
            }
        }

        return defaultMethod;
    }

}