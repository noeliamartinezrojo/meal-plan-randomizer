package com.nmartinez.meals.config

import com.comcast.ip4s.{Host, Port}
import pureconfig._
import pureconfig.generic.derivation.default._
import pureconfig.error.CannotConvert

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  given hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(
        CannotConvert(hostString, Host.getClass.toString, s"invalid host string: $hostString")
      )
  }

  given portReader: ConfigReader[Port] = ConfigReader[Int].emap { portInt =>
    Port
      .fromInt(portInt)
      .toRight(
        CannotConvert(
          portInt.toString,
          Port.getClass.toString,
          s"invalid port number: ${portInt.toString}"
        )
      )
  }
}
