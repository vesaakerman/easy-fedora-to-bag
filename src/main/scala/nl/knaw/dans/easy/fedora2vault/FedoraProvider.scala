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

import java.io.InputStream

import com.yourmediashelf.fedora.client.FedoraClient
import resource.{ ManagedResource, managed }

class FedoraProvider(fedoraClient: FedoraClient) {
  // variant of https://github.com/DANS-KNAW/easy-export-dataset/blob/6e656c6e6dad19bdea70694d63ce929ab7b0ad2b/src/main/scala/nl.knaw.dans.easy.export/FedoraProvider.scala

  def getObject(datasetId: DatasetId): ManagedResource[InputStream] = {
    managed(FedoraClient.getObjectXML(datasetId).execute(fedoraClient))
      .flatMap(response => managed(response.getEntityInputStream))
  }

  def disseminateDatastream(objectId: String, streamId: String): ManagedResource[InputStream] = {
    managed(FedoraClient.getDatastreamDissemination(objectId, streamId).execute(fedoraClient))
      .flatMap(response => managed(response.getEntityInputStream))
  }

}
