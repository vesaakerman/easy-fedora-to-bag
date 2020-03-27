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
package nl.knaw.dans.easy.fedora2vault

import scala.util.Try
import scala.xml.Node

object FoXml {

  private def getStream(streamId: String, rootTag: String, foXml: Node): Try[Node] = Try {
    val node = getStreamRoot(streamId, foXml)
      .getOrElse(throw new Exception(s"Stream with ID $streamId not found"))
    if(!controlGroup(node).contains("X"))
      throw new Exception(s"Stream with ID $streamId does not have control group X")

    (node \\ "xmlContent")
      .last
      .descendant
      .filter(_.label == rootTag)
      .last
  }

  private def getStreamRoot(streamId: String, foXml: Node): Option[Node] = {
    (foXml \ "datastream")
      .theSeq
      .filter(n => n \@ "ID" == streamId)
      .lastOption
  }

  def getEmd(foXml: Node): Try[Node] = getStream("EMD", "easymetadata", foXml)

  def getAmd(foXml: Node): Try[Node] = getStream("AMD", "administrative-md", foXml)

  def getRelsExt(foXml: Node): Try[Node] = getStream("RELS-EXT", "RDF", foXml)

  // TODO EASY_ITEM_CONTAINER_MD in http://deasy.dans.knaw.nl:8080/fedora/objects/easy-dataset:10/objectXML

  def getDdm(foXml: Node): Option[Node] = getStream("dataset.xml", "DDM", foXml).toOption

  def getFilesXml(foXml: Node): Option[Node] = getStream("files.xml", "file", foXml).toOption

  def getAgreementsXml(foXml: Node): Option[Node] = getStream("agreements.xml", "file", foXml).toOption

  def getManifest(foXml: Node): Option[String] = {
    val streamId = "manifest-sha1.txt"
    getStreamRoot(streamId, foXml)
      .map(_ => streamId)
  }

  def getMessageFromDepositor(foXml: Node): Option[Node] = getStreamRoot("message-from-depositor.txt", foXml)

  def managedStreamLabel(foXml: Node, id: String): Option[String] = {
    getStreamRoot(id, foXml)
      .withFilter(controlGroup(_).contains("M"))
      .flatMap(getLabel)
  }

  private def controlGroup(streamRoot: Node): Option[String] = {
    streamRoot.attribute("CONTROL_GROUP").map(_.text)
  }

  def getLabel(streamRoot: Node): Option[String] = {
    (streamRoot \ "datastreamVersion")
      .flatMap(_.attribute("LABEL"))
      .flatten.headOption.map(_.text)
  }

  def getOwner(foXml: Node): Try[String] = Try {
    (foXml \ "objectProperties" \ "property")
      .filter(_.attribute("NAME").exists(_.map(_.text).exists(_.endsWith("#ownerId"))))
      .flatMap(_.attribute("VALUE"))
      .flatten.headOption.map(_.text)
      .getOrElse(throw new Exception("""FoXml has no <foxml:property NAME="info:fedora/fedora-system:def/model#ownerId" VALUE="???"/>"""))
  }
}
