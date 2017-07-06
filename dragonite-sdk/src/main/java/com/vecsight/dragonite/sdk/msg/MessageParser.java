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
            switch (msgBytes[1]) {
                case MessageType.DATA:
                    return new DataMessage(msgBytes);
                case MessageType.CLOSE:
                    return new CloseMessage(msgBytes);
                case MessageType.ACK:
                    return new ACKMessage(msgBytes);
                case MessageType.HEARTBEAT:
                    return new HeartbeatMessage(msgBytes);
                default:
                    throw new IncorrectMessageException("Unknown Message Type");
            }
        } else {
            throw new IncorrectMessageException("Packet is too short");
        }
    }

}
