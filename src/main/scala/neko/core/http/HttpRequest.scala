package neko.core.http

import java.io.{InputStream, BufferedInputStream}

case class HttpRequest(
    line: HttpRequestLine,
    header: HttpRequestHeader,
    body: String
) {

  def asString: String = {
    List(line.asString, header.asString, "", body).mkString("\n")
  }

}

object HttpRequest {

  def fromInputStream(in: InputStream): HttpRequest = {
    val bin = new BufferedInputStream(in)
    def getFirstHalf(): List[String] = {
      val CS = '\r'.toByte // 10
      val LF = '\n'.toByte // 13

      @annotation.tailrec
      def loop(lines: List[String], bytes: List[Byte]): (List[String], List[Byte]) = {
        val b = bin.read().toByte
        if (b == LF)
          bytes match {
            case CS :: Nil => (lines, Nil)
            case _         => loop(new String(bytes.reverse.toArray, "UTF-8").stripLineEnd :: lines, Nil)
          }
        else loop(lines, b :: bytes)
      }
      loop(Nil, Nil)._1.reverse
    }
    val firstHalf: List[String] = getFirstHalf()

    val line   = HttpRequestLine.fromString(firstHalf.head)
    val header = HttpRequestHeader.fromString(firstHalf.tail)
    val body = header.contentLength match {
      case None => ""
      case Some(length) => {
        val bytes = new Array[Byte](length)
        bin.read(bytes, 0, length)
        new String(bytes, "UTF-8") // headerのcontentTypeを読む
      }
    }
    HttpRequest(line, header, body)
  }

}
