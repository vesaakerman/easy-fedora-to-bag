package nl.knaw.dans.easy.fedora2vault

import java.io.InputStream

import com.yourmediashelf.fedora.client.FedoraClient
import resource.{ ManagedResource, managed }

class FedoraIO(fedoraClient: FedoraClient) {

  def getObject(datasetId: DatasetId): ManagedResource[InputStream] = {
    // copy of https://github.com/DANS-KNAW/easy-export-dataset/blob/6e656c6e6dad19bdea70694d63ce929ab7b0ad2b/src/main/scala/nl.knaw.dans.easy.export/FedoraProvider.scala#L55-L58
    managed(FedoraClient.getObjectXML(datasetId).execute(fedoraClient))
      .flatMap(response => managed(response.getEntityInputStream))
  }
}
