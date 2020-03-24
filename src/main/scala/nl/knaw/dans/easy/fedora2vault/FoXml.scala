package nl.knaw.dans.easy.fedora2vault

import scala.util.Try
import scala.xml.Node

object FoXml {

  private def getStream(streamId: String, rootTag: String, foXml: Node): Try[Node] = Try {
    val xs = (foXml \ "datastream")
      .theSeq
      .filter(n => n \@ "ID" == streamId)
      .last

    (xs \ "datastreamVersion" \ "xmlContent")
      .last
      .descendant
      .filter(_.label == rootTag)
      .last
  }

  def getEmd(foXml: Node): Try[Node] = getStream("EMD", "easymetadata", foXml)

  def getAmd(foXml: Node): Try[Node] = getStream("AMD", "administrative-md", foXml)

  def getDc(foXml: Node): Try[Node] = getStream("DC", "dc", foXml)

  def getRelsExt(foXml: Node): Try[Node] = getStream("RELS-EXT", "RDF", foXml)

  def getOwner(foXml: Node): Try[String] = Try {
    (foXml \ "objectProperties" \ "property")
      .filter(_.attribute("NAME").exists(_.map(_.text).exists(_.endsWith("#ownerId"))))
      .flatMap(_.attribute("VALUE"))
      .flatten.headOption.map(_.text)
      .getOrElse(throw new Exception("""FoXml has no <foxml:property NAME="info:fedora/fedora-system:def/model#ownerId" VALUE="???"/>"""))
  }
}
