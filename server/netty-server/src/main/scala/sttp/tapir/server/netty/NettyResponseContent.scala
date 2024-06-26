package sttp.tapir.server.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.stream.{ChunkedFile, ChunkedStream}
import org.reactivestreams.{Processor, Publisher}
import sttp.ws.{WebSocketFrame => SttpWebSocketFrame}

import scala.concurrent.duration.FiniteDuration

sealed trait NettyResponseContent {
  def channelPromise: ChannelPromise
}

object NettyResponseContent {
  final case class ByteBufNettyResponseContent(channelPromise: ChannelPromise, byteBuf: ByteBuf) extends NettyResponseContent
  final case class ChunkedStreamNettyResponseContent(channelPromise: ChannelPromise, chunkedStream: ChunkedStream)
      extends NettyResponseContent
  final case class ChunkedFileNettyResponseContent(channelPromise: ChannelPromise, chunkedFile: ChunkedFile) extends NettyResponseContent
  final case class ReactivePublisherNettyResponseContent(channelPromise: ChannelPromise, publisher: Publisher[HttpContent])
      extends NettyResponseContent
  final case class ReactiveWebSocketProcessorNettyResponseContent(
      channelPromise: ChannelPromise,
      processor: Processor[WebSocketFrame, WebSocketFrame],
      ignorePong: Boolean,
      autoPongOnPing: Boolean,
      decodeCloseRequests: Boolean,
      autoPing: Option[(FiniteDuration, SttpWebSocketFrame.Ping)]
  ) extends NettyResponseContent
}
