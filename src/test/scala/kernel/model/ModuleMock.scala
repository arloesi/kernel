package kernel.model

import java.util._
import scala.collection.JavaConversions._

import com.google.inject._
import javax.inject.Named
import org.jukito.JukitoModule

class ModuleMock extends JukitoModule {
  override def configureTest() {
  }

  @Provides @Singleton @Named("mapping.properties")
  def provideMappingProperties() = {
    val properties = new HashMap[String,String]()
    properties.put("name", "value")
    properties
  }
}