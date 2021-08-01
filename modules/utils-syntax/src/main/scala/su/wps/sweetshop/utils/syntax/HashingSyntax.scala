package su.wps.sweetshop.utils.syntax

import java.nio.charset.StandardCharsets
import java.util.Base64

trait HashingSyntax {
  def fromBase64(s: String): String =
    new String(Base64.getDecoder.decode(s.getBytes(StandardCharsets.UTF_8)))
}
