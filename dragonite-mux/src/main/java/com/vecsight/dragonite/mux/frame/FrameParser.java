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

    private final FrameBuffer frameBuffer;

    public FrameParser(final short maxFrameSize) {
        frameBuffer = new FrameBuffer(maxFrameSize);
    }

    public Frame tryParseFrame(final byte[] rawBytes) {
        frameBuffer.add(rawBytes);

        Frame frame = null;
        try {
            frame = parseFrameRaw(frameBuffer.get());
            frameBuffer.reset();
        } catch (final IncorrectFrameException e) {
            frameBuffer.reset();
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
