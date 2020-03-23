/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy

import java.nio.file.Paths

import better.files.StringExtensions
import nl.knaw.dans.bag.DansBag
import org.joda.time.format.{ DateTimeFormatter, ISODateTimeFormat }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.util.Try
import scala.xml.{ Node, PrettyPrinter, Utility }

package object fedora2vault {

  type DatasetId = String
  type Depositor = String

  val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()

  def now: String = DateTime.now(DateTimeZone.UTC).toString(dateTimeFormatter)

  val prologue = """<?xml version='1.0' encoding='UTF-8'?>"""

  implicit class XmlExtensions(val elem: Node) extends AnyVal {

    def serialize: String = {
      val printer = new PrettyPrinter(160, 2)
      val trimmed = Utility.trim(elem)
      prologue + "\n" + printer.format(trimmed)
    }
  }
  implicit class BagExtensions(val bag: DansBag) extends AnyVal {
    def addMetadataFile(content: Node, target: String): Try[Any] = {
      addMetadataFile(content.serialize, target)
    }

    def addMetadataFile(content: String, target: String): Try[Any] = {
      bag.addTagFile(content.inputStream, Paths.get(s"metadata/$target"))
    }
  }
}
