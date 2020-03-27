package nl.knaw.dans.easy.fedora2vault

import java.io.FileInputStream

import better.files.File
import nl.knaw.dans.easy.fedora2vault.fixture.{ FileSystemSupport, TestSupportFixture }
import org.scalamock.scalatest.MockFactory
import resource.managed

import scala.util.Success
import scala.xml.Elem

class AppSpec extends TestSupportFixture with MockFactory with FileSystemSupport {

  override def beforeEach(): Unit = {
    super.beforeEach()
    if (testDir.exists) testDir.delete()
    testDir.createDirectories()
  }

  private val nameSpaceRegExp = """ xmlns:[a-z]+="[^"]*"""" // these attributes have a variable order
  private val samples = File("src/test/resources/sample-foxml")

  private class MockedApp(expectedObjects: File*) extends EasyFedora2vaultApp(null) {
    override lazy val fedoraProvider: FedoraProvider = mock[FedoraProvider]
    (fedoraProvider.getObject(_: String)) expects * once() returning
      managed(new FileInputStream(expectedObjects.head.toJava))
    expectedObjects.tail.foreach(file =>
      (fedoraProvider.disseminateDatastream(_: String, _:String)) expects (*,*) once() returning
        managed(new FileInputStream(file.toJava))
    )
  }

  "simpleTransform" should "produce a bag with EMD" in {
    val emd = <emd:easymetadata xmlns:emd="http://easy.dans.knaw.nl/easy/easymetadata/" xmlns:eas="http://easy.dans.knaw.nl/easy/easymetadata/eas/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/" emd:version="0.1">
                  <emd:title>
                      <dc:title>Incomplete metadata</dc:title>
                  </emd:title>
              </emd:easymetadata>
    (testDir / "fo.xml").write(createFoXml(emd, "easyadmin").serialize)

    new MockedApp(testDir / "fo.xml")
      .simpleTransform("easy-dataset:17", testDir / "bag") shouldBe Success("???")

    (testDir / "bag" / "bag-info.txt").contentAsString should startWith("EASY-User-Account: easyadmin")
    (testDir / "bag" / "metadata" / "emd.xml").contentAsString.replaceAll(nameSpaceRegExp, "") shouldBe
      emd.serialize.replaceAll(nameSpaceRegExp, "")
  }

  it should "process DepositApi" in {
    new MockedApp(
      samples / "DepositApi.xml",
      (testDir / "agreement.xml").write("lalala"),
      (testDir / "additional-license").write("rabarbera"),
      (testDir / "dataset-license").write("blablabla"),
    ).simpleTransform("easy-dataset:17", testDir / "bag") shouldBe Success("???")

    (testDir / "bag" / "metadata" / "depositor-info/depositor-agreement.pdf").contentAsString shouldBe "blablabla"
    (testDir / "bag" / "metadata" / "license.pdf").contentAsString shouldBe "rabarbera"
    (testDir / "bag" / "metadata").list.toSeq.map(_.name).sortBy(identity) shouldBe
      Seq("amd.xml", "dataset.xml", "depositor-info", "emd.xml", "files.xml", "license.pdf")
  }

  it should "process TalkOfEurope" in {
    new MockedApp(
      samples / "TalkOfEurope.xml",
      (testDir / "dataset-license").write("rabarbera"),
    ).simpleTransform("easy-dataset:12", testDir / "bag") shouldBe Success("???")

    (testDir / "bag" / "metadata" / "depositor-info/depositor-agreement.pdf").contentAsString shouldBe "rabarbera"
    (testDir / "bag" / "metadata").list.toSeq.map(_.name).sortBy(identity) shouldBe
      Seq("amd.xml", "depositor-info", "emd.xml")
  }

  it should "process streaming" in {
    new MockedApp(samples / "streaming.xml")
      .simpleTransform("easy-dataset:13", testDir / "bag") shouldBe Success("???")

    (testDir / "bag" / "metadata").list.toSeq.map(_.name)
      .sortBy(identity) shouldBe Seq("amd.xml", "emd.xml")
  }

  private def createFoXml(emd: Elem, owner: DatasetId) = {
    // reduced variant of http://deasy.dans.knaw.nl:8080/fedora/objects/easy-dataset:1/objectXML
    <foxml:digitalObject VERSION="1.1" PID="easy-dataset:1"
                   xmlns:foxml="info:fedora/fedora-system:def/foxml#"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd">
        <foxml:objectProperties>
            <foxml:property NAME="info:fedora/fedora-system:def/model#state" VALUE="Inactive"/>
            <foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="DDM example 2 deposited with sword-v1 and UNSPECIFIED as format"/>
            <foxml:property NAME="info:fedora/fedora-system:def/model#ownerId" VALUE={ owner }/>
            <foxml:property NAME="info:fedora/fedora-system:def/model#createdDate" VALUE="2016-11-22T13:11:20.341Z"/>
            <foxml:property NAME="info:fedora/fedora-system:def/view#lastModifiedDate" VALUE="2020-03-17T06:13:44.896Z"/>
        </foxml:objectProperties>
        <foxml:datastream ID="EMD" STATE="A" CONTROL_GROUP="X" VERSIONABLE="false">
            <foxml:datastreamVersion ID="EMD.1" LABEL="Descriptive metadata for this dataset" CREATED="2016-11-22T13:11:22.765Z" MIMETYPE="text/xml" FORMAT_URI="http://easy.dans.knaw.nl/easy/easymetadata/" SIZE="8119">
                <foxml:contentDigest TYPE="SHA-1" DIGEST="b7f5d6b48483f1f9038e220baae4ec24f768b19a"/>
                <foxml:xmlContent>
                    { emd }
                </foxml:xmlContent>
            </foxml:datastreamVersion>
        </foxml:datastream>
    </foxml:digitalObject>
  }
}
