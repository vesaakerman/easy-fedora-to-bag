package nl.knaw.dans.easy.fedora2vault.fixture

import better.files.File
import better.files.File.currentWorkingDirectory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.enablers.Existence

trait FileSystemSupport extends BeforeAndAfterEach {
  this: TestSupportFixture =>

  implicit def existenceOfFile[FILE <: better.files.File]: Existence[FILE] = _.exists

  override def beforeEach(): Unit = {
    super.beforeEach()

    if (testDir.exists) testDir.delete()
    testDir.createDirectories()
  }

  lazy val testDir: File = currentWorkingDirectory / "target" / "test" / getClass.getSimpleName
}
