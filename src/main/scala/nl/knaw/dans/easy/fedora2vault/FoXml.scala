package nl.knaw.dans.easy.fedora2vault

import scala.util.{ Success, Try }
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

  def getOwner(foXml: Node): Try[String] = {
    // <foxml:objectProperties>
    //   <foxml:property NAME="info:fedora/fedora-system:def/model#ownerId" VALUE="???"/>
    // <foxml:objectProperties>
    (foXml \ "objectProperties" \ "property").text // TODO
    Success("TODO")
  }
}
