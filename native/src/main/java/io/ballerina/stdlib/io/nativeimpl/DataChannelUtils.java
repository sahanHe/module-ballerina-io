/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.io.nativeimpl;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.io.channels.base.Channel;
import io.ballerina.stdlib.io.channels.base.DataChannel;
import io.ballerina.stdlib.io.channels.base.Representation;
import io.ballerina.stdlib.io.utils.IOConstants;
import io.ballerina.stdlib.io.utils.IOUtils;

import java.io.IOException;
import java.nio.ByteOrder;

import static io.ballerina.stdlib.io.utils.IOConstants.DATA_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *DataChannels.
 *
 * @since 1.1.0
 */
public class DataChannelUtils {

    private static final String IS_CLOSED = "isClosed";

    private DataChannelUtils() {
    }

    /**
     * Returns the relevant byte order.
     *
     * @param byteOrder byte order defined through ballerina api.
     * @return byte order mapped with java equivalent.
     */
    private static ByteOrder getByteOrder(String byteOrder) {
        switch (byteOrder) {
            case "BE":
                return ByteOrder.BIG_ENDIAN;
            case "LE":
                return ByteOrder.LITTLE_ENDIAN;
            default:
                return ByteOrder.nativeOrder();
        }
    }

    public static void initReadableDataChannel(BObject dataChannelObj, BObject byteChannelObj, BString order) {
        try {
            ByteOrder byteOrder = getByteOrder(order.getValue());
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
            dataChannelObj.addNativeData(IS_CLOSED, false);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            throw IOUtils.createError(message);
        }
    }

    public static Object readInt16(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_16).getValue();
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readInt32(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_32).getValue();
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readInt64(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_64).getValue();
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat32(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_32);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat64(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_64);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readBool(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readBoolean();
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readString(BObject dataChannelObj, long nBytes, BString encoding) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        if (channel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                return StringUtils.fromString(channel.readString((int) nBytes, encoding.getValue()));
            } catch (IOException e) {
                String msg = "Error occurred while reading string: " + e.getMessage();
                return IOUtils.createError(msg);
            }
        }
    }

    public static Object readVarInt(BObject dataChannelObj) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.VARIABLE).getValue();
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object closeDataChannel(BObject dataChannel) {
        if (isChannelClosed(dataChannel)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannel.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.close();
            dataChannel.addNativeData(IS_CLOSED, true);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static void initWritableDataChannel(BObject dataChannelObj, BObject byteChannelObj, BString order) {
        try {
            ByteOrder byteOrder = getByteOrder(order.getValue());
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            throw IOUtils.createError(message);
        }
    }

    public static Object writeInt16(BObject dataChannelObj, long value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_16);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt32(BObject dataChannelObj, long value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_32);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt64(BObject dataChannelObj, long value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_64);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat32(BObject dataChannelObj, double value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_32);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat64(BObject dataChannelObj, double value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_64);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeBool(BObject dataChannelObj, boolean value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeBoolean(value);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeString(BObject dataChannelObj, BString value, BString encoding) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeString(value.getValue(), encoding.getValue());
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeVarInt(BObject dataChannelObj, long value) {
        if (isChannelClosed(dataChannelObj)) {
            return IOUtils.createError("Data channel is already closed.");
        }
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.VARIABLE);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    private static boolean isChannelClosed(BObject channel) {
        if (channel.getNativeData(IS_CLOSED) != null) {
            return (boolean) channel.getNativeData(IS_CLOSED);
        }
        return false;
    }
}
