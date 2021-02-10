package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.LinkedList;

/**
 * WriteFlowController used to write data via channelPipeline.
 */
public class WriteFlowController {
    protected ByteBuf buffer;
    private Future balWriteCallback;

    WriteFlowController(ByteBuf buffer, Future callback) {
        this.balWriteCallback = callback;
        this.buffer = buffer;
    }

    public WriteFlowController(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public synchronized void writeData(Channel channel, LinkedList<WriteFlowController> writeFlowControllers) {
        channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
            completeCallback(future);
        });
        writeFlowControllers.remove(this);
    }

    private void completeCallback(ChannelFuture future) {
        if (future.isSuccess()) {
            balWriteCallback.complete(null);
        } else {
            balWriteCallback.complete(Utils
                    .createSocketError("Failed to write data: " + future.cause().getMessage()));
        }
    }
}