package kernel.model

import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import com.google.common.collect.Sets._
import com.google.common.collect.Lists._
import com.google.inject._

import javax.xml.bind._
import javax.persistence.Persistence._
import javax.inject.Named

import org.eclipse.persistence._
import org.eclipse.persistence.internal.jpa.{EntityManagerImpl,EntityManagerFactoryImpl}
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl

import org.eclipse.persistence.jaxb.{
  MarshallerProperties,UnmarshallerProperties,
  JAXBMarshaller,JAXBUnmarshaller,JAXBHelper,
  ObjectGraph,Subgraph}

class Module(persistenceUnit:String, views:Set[Class[_]]) {
    type JAXBContextImpl = org.eclipse.persistence.jaxb.JAXBContext

    @Provides @Singleton
    def provideStorage(properties:Storage.Properties):EntityManagerFactoryImpl = {
        createEntityManagerFactory(persistenceUnit,properties.properties)
            .asInstanceOf[EntityManagerFactoryImpl]
    }

    @Provides @Singleton
    def provideDatabaseSession(factory:EntityManagerFactoryImpl):DatabaseSessionImpl = {
        factory.getDatabaseSession()
    }

    @Provides @Singleton @Named("schema")
    def provideSchemaSclasses(session:DatabaseSessionImpl):Set[Class[_]] = {
        val classes:Set[Class[_]] = newLinkedHashSet()

        for(i <- session.getDescriptors().values()) {
            classes.add(i.getJavaClass())
        }

        classes
    }

    @Provides @Singleton
    def provideJAXBContext(@Named("schema") classes:Set[Class[_]], properties:Mapper.Properties):JAXBContext = {
        JAXBContext.newInstance(classes.toArray().map(i => i.asInstanceOf[Class[_]]), properties.properties)
    }

    @Provides @Singleton
    def provideJAXBContextImpl(context:JAXBContext):JAXBContextImpl = {
        JAXBHelper.getJAXBContext(context)
    }

    @Provides @Singleton
    def provideStorageGraph(@Named("schema") classes:Set[Class[_]], session:DatabaseSessionImpl):Storage.Graph = {
        new Storage.Graph(Mapping.createGraph(new Mapping.Persistence(), classes, session, views))
    }

    @Provides @Singleton
    def provideMapperGraph(@Named("schema") classes:Set[Class[_]], context:JAXBContextImpl):Mapper.Graph = {
        new Mapper.Graph(Mapping.createGraph(new Mapping.Marshalling(context), classes, context.getXMLContext().getSession(classes.head), views))
    }
}
