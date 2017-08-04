/*
 * VECTORSIGHT CONFIDENTIAL
 * ------------------------
 * Copyright (c) [2015] - [2017]
 * VectorSight Systems Co., Ltd.
 * All Rights Reserved.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Toby Huang <t@vecsight.com>, June 2017
 */

package com.vecsight.dragonite.proxy.acl;

import com.vecsight.dragonite.proxy.acl.item.*;
import com.vecsight.dragonite.proxy.exception.ACLException;
import com.vecsight.dragonite.proxy.exception.InvalidAddressException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ACLFileParser {

    private static final Pattern INFO_PATTERN = Pattern.compile("(\\w+?):(.+)");

    private static final Pattern RULE_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+?),([a-zA-Z0-9\\-.:/]+),([a-zA-Z]+)");

    public static ParsedACL parse(final Reader reader) throws IOException, ACLException {

        String title = "No title", author = "Unknown";

        ACLItemMethod defaultMethod = ACLItemMethod.PROXY;

        final List<ACLItem> aclItemList = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;

                line = line.trim();

                if (line.startsWith("#") || line.startsWith("//") || line.length() == 0) {
                    //Just comments
                    continue;
                }

                //Check for info
                final Matcher infoMatcher = INFO_PATTERN.matcher(line);
                if (infoMatcher.matches()) {
                    final String key = infoMatcher.group(1);
                    final String value = infoMatcher.group(2);
                    switch (key.toLowerCase()) {
                        case "title":
                            title = value.trim();
                            break;
                        case "author":
                            author = value.trim();
                            break;
                        case "default":
                            try {
                                defaultMethod = methodFromString(value.trim());
                            } catch (final ACLException e) {
                                throw new ACLException("Line " + lineCount + " - " + e.getMessage());
                            }
                            break;
                        default:
                            throw new ACLException("Line " + lineCount + " - Unknown field name: " + key);
                    }
                    continue;
                }

                //Check for rule
                final Matcher ruleMatcher = RULE_PATTERN.matcher(line);
                if (ruleMatcher.matches()) {
                    final String type = ruleMatcher.group(1);
                    final String address = ruleMatcher.group(2);
                    final String method = ruleMatcher.group(3);

                    final ACLItemType aclItemType;
                    try {
                        aclItemType = typeFromString(type);
                    } catch (final ACLException e) {
                        throw new ACLException("Line " + lineCount + " - " + e.getMessage());
                    }

                    final ACLItemMethod aclItemMethod;
                    aclItemMethod = methodFromString(method);
                    try {
                        switch (aclItemType) {
                            case IPv4:
                                aclItemList.add(new IPv4ACLItem(address, aclItemMethod));
                                break;
                            case IPv4_CIDR:
                                aclItemList.add(new IPv4CIDRACLItem(address, aclItemMethod));
                                break;
                            case IPv6:
                                aclItemList.add(new IPv6ACLItem(address, aclItemMethod));
                                break;
                            case IPv6_CIDR:
                                aclItemList.add(new IPv6CIDRACLItem(address, aclItemMethod));
                                break;
                            case DOMAIN:
                                aclItemList.add(new DomainACLItem(address, aclItemMethod));
                                break;
                            case DOMAIN_SUFFIX:
                                aclItemList.add(new DomainSuffixACLItem(address, aclItemMethod));
                                break;
                        }
                    } catch (final InvalidAddressException e) {
                        throw new ACLException("Line " + lineCount + " - Invalid address: " + e.getMessage());
                    }

                    continue;
                }

                throw new ACLException("Line " + lineCount + " - Invalid syntax format");
            }

            return new ParsedACL(title, author, defaultMethod, aclItemList);
        }

    }

    private static ACLItemType typeFromString(final String type) throws ACLException {
        final ACLItemType aclItemType;
        switch (type.toLowerCase()) {
            case "domain":
                aclItemType = ACLItemType.DOMAIN;
                break;
            case "domain-suffix":
                aclItemType = ACLItemType.DOMAIN_SUFFIX;
                break;
            case "ipv4":
                aclItemType = ACLItemType.IPv4;
                break;
            case "ipv4-cidr":
                aclItemType = ACLItemType.IPv4_CIDR;
                break;
            case "ipv6":
                aclItemType = ACLItemType.IPv6;
                break;
            case "ipv6-cidr":
                aclItemType = ACLItemType.IPv6_CIDR;
                break;
            default:
                throw new ACLException("Unknown rule type: " + type);
        }
        return aclItemType;
    }

    private static ACLItemMethod methodFromString(final String method) throws ACLException {
        final ACLItemMethod aclItemMethod;
        switch (method.toLowerCase()) {
            case "direct":
                aclItemMethod = ACLItemMethod.DIRECT;
                break;
            case "proxy":
                aclItemMethod = ACLItemMethod.PROXY;
                break;
            case "reject":
                aclItemMethod = ACLItemMethod.REJECT;
                break;
            default:
                throw new ACLException("Unknown connect method: " + method);
        }
        return aclItemMethod;
    }


}
