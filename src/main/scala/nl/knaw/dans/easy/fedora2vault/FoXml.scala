package nl.knaw.dans.easy.fedora2vault

import scala.util.Try
import scala.xml.Node

object FoXml {

  private def getStream(streamId: String, rootTag: String, foXml: Node): Try[Node] = Try {
    val node = getStreamRoot(streamId, foXml)
      .getOrElse(throw new Exception(s"Stream with ID $streamId not found"))

    (node \ "datastreamVersion" \ "xmlContent")
      .last
      .descendant
      .filter(_.label == rootTag)
      .last
  }

  private def getStreamRoot(streamId: String, foXml: Node) = {
    (foXml \ "datastream")
      .theSeq
      .filter(n => n \@ "ID" == streamId)
      .lastOption
  }

  def getEmd(foXml: Node): Try[Node] = getStream("EMD", "easymetadata", foXml)

  def getAmd(foXml: Node): Try[Node] = getStream("AMD", "administrative-md", foXml)

  def getRelsExt(foXml: Node): Try[Node] = getStream("RELS-EXT", "RDF", foXml)

  // TODO distinguish between not found and other errors?
  def getDdm(foXml: Node): Option[Node] = getStream("dataset.xml", "DDM", foXml).toOption

  def getAgreementsXml(foXml: Node): Option[Node] = getStream("agreements.xml", "agreements", foXml).toOption

  def getFilesXml(foXml: Node): Option[Node] = getStream("files.xml", "file", foXml).toOption

  def getManifest(foXml: Node): Option[String] = getStreamRoot("manifest-sha1.txt", foXml).flatMap(getLocation)

  def getAdittionalLicense(foXml: Node): Option[Node] = getStreamRoot("ADDITIONAL_LICENSE", foXml)

  // TODO which version(s)?
  def getDatasetLicense(foXml: Node): Option[Node] = getStreamRoot("DATASET_LICENSE", foXml)

  def getMessageFromDepositor(foXml: Node): Option[Node] = getStreamRoot("message-from-depositor.txt", foXml)

  /**
   * @param node root of a stream
   * @return fedora-id
   */
  def getLocation(node: Node): Option[String] = {
    (node \ "contentLocation")
      .flatMap(_.attribute("REF"))
      .flatten.headOption.map(_.text) // TODO head or last?
  }

  /**
   * @param node root of a stream
   * @return file name
   */
  def getLabel(node: Node): Option[String] = {
    (node \ "datastreamVersion")
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
