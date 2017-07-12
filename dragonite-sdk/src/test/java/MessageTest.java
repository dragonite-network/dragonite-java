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

import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.msg.types.ACKMessage;
import com.vecsight.dragonite.sdk.msg.types.CloseMessage;
import com.vecsight.dragonite.sdk.msg.types.DataMessage;
import com.vecsight.dragonite.sdk.msg.types.HeartbeatMessage;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public final class MessageTest {

    @Test
    public void dataMessageSerialization() throws DragoniteException {
        final String text = "Hello...?!";
        final DataMessage message = new DataMessage(666, text.getBytes());
        final DataMessage newMessage = new DataMessage(message.toBytes());
        assertEquals(666, newMessage.getSequence());
        assertEquals(text, new String(newMessage.getData()));
    }

    @Test
    public void ackMessageSerialization() throws DragoniteException {
        final int[] ints = new int[]{3, 4, 5, 6, 7};
        final ACKMessage message = new ACKMessage(ints, 5);
        final ACKMessage newMessage = new ACKMessage(message.toBytes());
        assertEquals(newMessage.getConsumedSeq(), 5);
        assertArrayEquals(newMessage.getSequenceList(), ints);
    }

    @Test
    public void closeMessageSerialization() throws DragoniteException {
        final CloseMessage message = new CloseMessage(101, (short) 2);
        final CloseMessage newMessage = new CloseMessage(message.toBytes());
        assertEquals(newMessage.getSequence(), 101);
        assertEquals(newMessage.getStatus(), 2);
    }

    @Test
    public void heartbeatMessageSerialization() throws DragoniteException {
        final HeartbeatMessage message = new HeartbeatMessage(233);
        final HeartbeatMessage newMessage = new HeartbeatMessage(message.toBytes());
        assertEquals(newMessage.getSequence(), 233);
    }

}
