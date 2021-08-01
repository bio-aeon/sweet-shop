package su.wps.sweetshop.payments.impl.security

import cats.Monad
import cats.syntax.flatMap._
import mouse.any._
import su.wps.sweetshop.payments.impl.security.errors.{DecryptionFailure, SecurityErr}
import su.wps.sweetshop.utils.syntax.raise._
import tofu.Raise

import java.security.KeyPair
import java.util.Base64
import javax.crypto.spec.{PBEKeySpec, PBEParameterSpec}
import javax.crypto.{Cipher, SecretKeyFactory}

trait Crypto[F[_]] {
  def decodeRsa(data: String, rsaKey: KeyPair): F[String]

  def decodeAes(data: String, password: String): F[String]
}

object Crypto {
  def create[F[_]: Monad: Raise[*[_], SecurityErr]](implicit B: Bouncy): Crypto[F] =
    new Impl[F]

  private final class Impl[F[_]: Monad](implicit R: Raise[F, SecurityErr]) extends Crypto[F] {
    val CipherRsa = "RSA/ECB/PKCS1Padding"
    val CipherAes = "PBEWITHMD5AND256BITAES-CBC-OPENSSL"
    val OpenSslSaltIterations = 1
    val OpenSslHeader = "Salted__"
    lazy val OpenSslHeaderLength: Int = OpenSslHeader.length
    lazy val OpenSslSaltLength: Int = OpenSslHeaderLength

    def decodeRsa(b64Data: String, rsaKey: KeyPair): F[String] =
      catchNonFatal {
        cipherRsa <|
          (_.init(Cipher.DECRYPT_MODE, rsaKey.getPrivate)) |>
          (_.doFinal(Base64.getDecoder.decode(b64Data.getBytes))) |>
          (new String(_))
      }(err => DecryptionFailure(err.getMessage))

    def decodeAes(b64Data: String, password: String): F[String] =
      catchNonFatal(Base64.getDecoder.decode(b64Data))(err => DecryptionFailure(err.getMessage))
        .flatMap { decoded =>
          if (OpenSslHeader == new String(decoded.take(OpenSslHeader.length))) {
            catchNonFatal {
              decoded.slice(OpenSslHeaderLength, OpenSslHeaderLength + OpenSslSaltLength) |>
                (x => cipherAes(x, password, Cipher.DECRYPT_MODE)) |>
                (_.doFinal(decoded.drop(OpenSslHeaderLength + OpenSslSaltLength))) |>
                (new String(_))
            }(err => DecryptionFailure(err.getMessage))
          } else {
            R.raise(DecryptionFailure("BadOpenSSL AES container"))
          }
        }

    @inline
    private def cipherRsa: Cipher = Cipher.getInstance(CipherRsa)

    @inline
    private def cipherAes(salt: Array[Byte], password: String, mode: Int): Cipher = {
      val key = SecretKeyFactory
        .getInstance(CipherAes)
        .generateSecret(new PBEKeySpec(password.toCharArray))

      Cipher.getInstance(CipherAes) <|
        (_.init(mode, key, new PBEParameterSpec(salt, OpenSslSaltIterations)))
    }
  }
}
