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

import better.files.File
import com.yourmediashelf.fedora.client.FedoraClient
import nl.knaw.dans.bag.v0.DansV0Bag
import nl.knaw.dans.easy.fedora2vault.Command.FeedBackMessage
import nl.knaw.dans.easy.fedora2vault.FoXml.{ getEmd, _ }

import scala.util.{ Failure, Success, Try }
import scala.xml.{ Node, XML }

class EasyFedora2vaultApp(configuration: Configuration) {
  lazy val fedoraIO = new FedoraIO(new FedoraClient(configuration.fedoraCredentials))

  def simpleTransform(datasetId: DatasetId, outputDir: File): Try[FeedBackMessage] = {
    for {
      foXml <- fedoraIO.getObject(datasetId).map(XML.load).tried
      depositor <- getOwner(foXml)
      bag <- DansV0Bag.empty(outputDir).map(_.withEasyUserAccount(depositor))
      emd <- getEmd(foXml)
      _ <- bag.addMetadata(emd, "emd.xml")
      amd <- getEmd(foXml)
      _ <- bag.addMetadata(amd, "amd.xml")
      _ <- getDdm(foXml)
        .map(bag.addMetadata(_, "dataset.xml")).getOrElse(Success(())) // TODO EASY-2683
      _ <- getAgreementsXml(foXml)
        .map(bag.addMetadata(_, "depositor-info/agreements.xml")).getOrElse(Success(()))
      _ <- getMessageFromDepositor(foXml)
        .map(bag.addMetadata(_, "depositor-info/message-from-depositor.txt")).getOrElse(Success(())) // TODO EMD/other/remark?
      _ <- getFilesXml(foXml)
        .map(bag.addMetadata(_, "files.xml")).getOrElse(Success(())) // TODO EASY-2678

      // TODO not mentioned in https://github.com/DANS-KNAW/dans-bagit-profile/blob/master/versions/0.0.0.md#3-metadata-requirements
      _ <- getAdittionalLicense(foXml)
        .map(addMetadata(bag, "ADDITIONAL_LICENSE")).getOrElse(Success(()))
      _ <- getDatasetLicense(foXml)
        .map(addMetadata(bag, "DATASET_LICENSE")).getOrElse(Success(()))
      _ <- bag.save()
      _ = getManifest(foXml).map(compareManifest(bag))
    } yield "???" // TODO what?
  }

  private def addMetadata(bag: DansV0Bag, defaultFileName: String)(streamRoot: Node) = {

    def execute(id: String): Try[Any] = {
      val fileName = getLabel(streamRoot).getOrElse(defaultFileName)
      fedoraIO.getObject(id)
        .map(bag.addMetadata(_, fileName))
        .tried.flatten
    }

    getLocation(streamRoot)
      .map(execute)
      .getOrElse(Failure(new Exception(s"No <contentLocation REF='...'> for $defaultFileName")))
  }

  private def compareManifest(bag: DansV0Bag)(manifest: String): Try[Unit] = {
    // TODO EASY-2678
    val plm = bag.payloadManifests // or sha's from fedora?
    Success(())
  }
}
