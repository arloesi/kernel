package kernel.network

import io.netty.channel.Channel

class Connection(val channel:Channel) {
    def send(msg:Object) = channel.writeAndFlush(msg)
}
