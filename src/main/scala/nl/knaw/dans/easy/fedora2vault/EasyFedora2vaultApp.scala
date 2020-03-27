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

import scala.util.{ Success, Try }
import scala.xml.{ Elem, XML }

class EasyFedora2vaultApp(configuration: Configuration) {
  lazy val fedoraProvider = new FedoraProvider(new FedoraClient(configuration.fedoraCredentials))

  def simpleTransform(datasetId: DatasetId, outputDir: File): Try[FeedBackMessage] = {

    def managedMetadataStream(foXml: Elem, streamId: String, bag: DansV0Bag, metadataFile: String) = {
      getStreamRoot(streamId, foXml)
        .flatMap(getLabel)
        .map { label =>
          val extension = label.split("[.]").last
          fedoraProvider.disseminateDatastream(datasetId, streamId)
            .map(bag.addMetadataStream(s"$metadataFile.$extension"))
            .tried.flatten
        }
    }

    for {
      foXml <- fedoraProvider.getObject(datasetId).map(XML.load).tried
      depositor <- getOwner(foXml)
      bag <- DansV0Bag.empty(outputDir).map(_.withEasyUserAccount(depositor))
      emd <- getEmd(foXml)
      _ <- bag.addMetadataXml("emd.xml")(emd)
      amd <- getEmd(foXml)
      _ <- bag.addMetadataXml("amd.xml")(amd)
      _ <- getDdm(foXml)
        .map(bag.addMetadataXml("dataset.xml")).getOrElse(Success(())) // TODO EASY-2683
      _ <- getMessageFromDepositor(foXml)
        .map(bag.addMetadataXml("depositor-info/message-from-depositor.txt")).getOrElse(Success(())) // TODO EMD/other/remark?
      _ <- getFilesXml(foXml)
        .map(bag.addMetadataXml("files.xml")).getOrElse(Success(())) // TODO EASY-2678
      _ <- managedMetadataStream(foXml, "agreements.xml", bag, "depositor-info/agreements").getOrElse(Success(()))
      _ <- managedMetadataStream(foXml, "ADDITIONAL_LICENSE", bag, "license").getOrElse(Success(())) // TODO how to store this in a valid way?
      _ <- managedMetadataStream(foXml, "DATASET_LICENSE", bag, "depositor-info/depositor-agreement").getOrElse(Success(())) // TODO versions
      _ <- bag.save()
      _ = getManifest(foXml).map(compareManifest(bag))
    } yield "???" // TODO what?
  }

  private def compareManifest(bag: DansV0Bag)(manifest: String): Try[Unit] = {
    // TODO EASY-2678
    val plm = bag.payloadManifests // or sha's from fedora?
    Success(())
  }
}
