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

package com.vecsight.dragonite.sdk.msg;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.msg.types.ACKMessage;
import com.vecsight.dragonite.sdk.msg.types.CloseMessage;
import com.vecsight.dragonite.sdk.msg.types.DataMessage;
import com.vecsight.dragonite.sdk.msg.types.HeartbeatMessage;

public final class MessageParser {

    public static Message parseMessage(final byte[] msgBytes) throws IncorrectMessageException {
        if (msgBytes.length >= 2) {
            try {
                switch (MessageType.fromByte(msgBytes[1])) {
                    case DATA:
                        return new DataMessage(msgBytes);
                    case CLOSE:
                        return new CloseMessage(msgBytes);
                    case ACK:
                        return new ACKMessage(msgBytes);
                    case HEARTBEAT:
                        return new HeartbeatMessage(msgBytes);
                    default:
                        throw new IncorrectMessageException("Unknown Message Type");
                }
            } catch (final IllegalArgumentException e) {
                throw new IncorrectMessageException("Unknown Message Type");
            }
        } else {
            throw new IncorrectMessageException("Packet is too short");
        }
    }

}
