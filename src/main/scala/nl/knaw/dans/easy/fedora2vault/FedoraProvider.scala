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
