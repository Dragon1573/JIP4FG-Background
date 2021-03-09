/*
 * Copyright (c) 2021 Dragon1573. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is “Incompatible With Secondary Licenses”,
 * as defined by the Mozilla Public License, v. 2.0.
 */

package wang.dragon1573

import java.io.IOException
import java.util.Properties

import org.apache.log4j.Logger

object utils {
  private val LOGGER = Logger.getLogger(utils.getClass)

  def loadProperties: Properties = {
    val properties = new Properties()
    try {
      properties.loadFromXML(
        this.getClass.getClassLoader.getResourceAsStream("wang/dragon1573/jdbc.xml")
        )
    } catch {
      case e: IOException => LOGGER.error("Load JDBC configurations failed!", e)
    }
    properties
  }
}
