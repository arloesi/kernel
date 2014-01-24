package kernel.model

import org.junit._
import org.junit.Assert._

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._

import com.google.inject._
import com.google.inject.Guice._

import kernel.schema._

import org.eclipse.persistence.internal.sessions.AbstractSession
import org.eclipse.persistence.descriptors.ClassDescriptor

class SchemaTest {
  import Schema._
  import SchemaMock._

  var injector:Injector = _
  var session:AbstractSession = _

  @Before
  def setup() {
    injector = createInjector(new Module("schema", VIEWS))
    session = injector.getInstance(classOf[AbstractSession])
  }

  @Test
  def classMappingTest() {
    val mapped = session.getClassDescriptor(classOf[MappedClass])
    val unmapped = session.getClassDescriptor(classOf[UnmappedClass])

    assertNotNull(mapped)
    assertNotNull(unmapped)

    assertNotNull(classMapping(mapped))
    assertNull(classMapping(unmapped))
  }
}
