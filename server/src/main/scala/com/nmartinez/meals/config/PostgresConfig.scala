package com.nmartinez.meals.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class PostgresConfig(
                           numThreads: Int,
                           url: String,
                           username: String,
                           password: String
                         ) derives ConfigReader
