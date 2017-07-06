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

package com.vecsight.dragonite.mux.frame;


import com.vecsight.dragonite.mux.exception.DataLengthMismatchException;
import com.vecsight.dragonite.mux.exception.IncorrectFrameException;
import com.vecsight.dragonite.mux.frame.types.*;

public class FrameParser {

    private final short maxFrameSize;

    private final byte[] bytesBuffer;

    private short position = 0;

    public FrameParser(final short maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        bytesBuffer = new byte[maxFrameSize];
    }

    public Frame tryParseFrame(final byte[] rawBytes) {
        System.arraycopy(rawBytes, 0, bytesBuffer, position, rawBytes.length);
        position += rawBytes.length;

        final byte[] tmpBytes = new byte[position];
        System.arraycopy(bytesBuffer, 0, tmpBytes, 0, position);

        Frame frame = null;
        try {
            frame = parseFrameRaw(tmpBytes);
            position = 0;
        } catch (final IncorrectFrameException e) {
            position = 0;
        } catch (final DataLengthMismatchException ignored) {
        }
        return frame;
    }

    private static Frame parseFrameRaw(final byte[] rawBytes) throws IncorrectFrameException, DataLengthMismatchException {
        if (rawBytes.length >= 2) {
            switch (rawBytes[1]) {
                case FrameType.CREATE:
                    return new CreateConnectionFrame(rawBytes);
                case FrameType.CLOSE:
                    return new CloseConnectionFrame(rawBytes);
                case FrameType.DATA:
                    return new DataFrame(rawBytes);
                case FrameType.PAUSE:
                    return new PauseConnectionFrame(rawBytes);
                case FrameType.CONTINUE:
                    return new ContinueConnectionFrame(rawBytes);
                default:
                    throw new IncorrectFrameException("Unknown Message Type");
            }
        } else {
            throw new IncorrectFrameException("Packet is too short");
        }
    }

}
