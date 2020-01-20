package neko.server

import java.nio.channels.{ServerSocketChannel, Selector, SelectionKey}
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel

class NioServer(inetSocketAddress: InetSocketAddress) {

  val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
  val selector: Selector = Selector.open()
  serverSocketChannel.socket().bind(inetSocketAddress)
  serverSocketChannel.configureBlocking(false)
  serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

  try {
    while (selector.select() > 0) {
      val iter = selector.selectedKeys.iterator
      while (iter.hasNext()) {
        val key = iter.next()
        if (key.isAcceptable) doAccept(key.channel)
        else if (key.isReadable) doRead(key.channel)
        else if (key.isWritable) doWrite(key.channel, key.attachment)
      }
    }
  }

  def doAccept(channel: SelectableChannel): Unit = ???
  def doRead(channel: SelectableChannel): Unit = ???
  def doWrite(channel: SelectableChannel, response: Response): Unit = ???


}
