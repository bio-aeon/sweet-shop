package su.wps.sweetshop.auth.impl.models

import io.circe.generic.JsonCodec

@JsonCodec
case class Error(key: String, message: String)
