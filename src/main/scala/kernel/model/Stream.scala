package kernel.model

class Stream(val objectGraph:ObjectGraph, val schema:JsonObject) {
}

object Stream {
    class MarshallGraph(val graph:ObjectGraph, val schema:JsonObject) {
  }

    class Graph(val mappings:java.util.Map[String,Mapping[MarshallGraph]]) {
        def getJsonSchema():String = {
            val json = new JsonObject()

            for(i <- mappings.values()) {
              json.putObject(i.name, i.graph.schema)
            }

            json.toString()
        }
    }

  class Marshalling(context:JAXBContext) extends Builder[MarshallGraph] {
    def build(mapping:Mapping[MarshallGraph],mappings:java.util.Map[String,Mapping[MarshallGraph]]):MarshallGraph = {
        val root = new MarshallGraph(JAXBHelper.getJAXBContext(context).createObjectGraph(mapping.`type`), new JsonObject())

        def addSubGroup(mapping:Mapping[MarshallGraph],root:Subgraph) {
            for(i <- mapping.properties) {
                if(i.schema != null) {
                    val node = root.addSubgraph(i.mapping.getAttributeName())
                    addSubGroup(mappings.get(i.schema),node)
                } else {
                    root.addAttributeNodes(i.mapping.getAttributeName())
                }
            }
        }

        for(i <- mapping.properties) {
            if(i.schema != null) {
                val node = root.graph.addSubgraph(i.mapping.getAttributeName())
                addSubGroup(mappings.get(i.schema),node)
            } else {
                root.graph.addAttributeNodes(i.mapping.getAttributeName())
            }
        }

        val json = root.schema
        json.putString("id", mapping.name)
        json.putString("type","object")
        val props = new JsonObject()
        json.putObject("properties", props)

        for(i <- mapping.properties) {
            val p = new JsonObject()

            val x =
              if(i.mapping.isCollectionMapping()) {
                p.putString("type","array")
                p.putArray("default", new JsonArray())
                val x = new JsonObject()
                p.putObject("items", x)
                x
              } else {
                p.putObject("default", null)
                p
              }

            if(!i.mapping.isRelationalMapping()) {
              x.putString("type", typeName.get(i.`type`))
            } else {
              x.putString("$ref", "/"+i.`type`.getSimpleName().toLowerCase()+"/"+i.view.getSimpleName().toLowerCase()+"#")

              val r = new JsonObject()
              p.putObject("relation", r)
              r.putString("property", i.mapping.getRelationshipPartner().getAttributeName())
            }
        }

        root
    }
  }
}
