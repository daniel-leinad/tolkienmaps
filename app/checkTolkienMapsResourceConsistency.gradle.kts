import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import org.w3c.dom.Document

fun addMapsToCollection(doc: Document, collection: MutableCollection<String>) {
    val maps = doc.getElementsByTagName("map")
    var i = 0
    while(i < maps.length) {
        collection.add(maps.item(i).attributes.getNamedItem("id").textContent)
        i++
    }
}

class InconsistentResourcesException(message: String) : Exception(message)

project.tasks.register("checkTolkienMapsResourceConsistency") {
    doFirst {
        val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        val resourceDir = layout.projectDirectory.dir("src").dir("main").dir("res").dir("xml")

        val tolkienMaps: Document = builder.parse(resourceDir.file("tolkien_maps.xml").asFile)
        tolkienMaps.documentElement.normalize()

        val tolkienMapsUIStructure: Document = builder.parse(resourceDir.file("tolkien_maps_ui_structure.xml").asFile)
        tolkienMapsUIStructure.documentElement.normalize()

        val requiredMaps: MutableList<String> = mutableListOf()
        val mapsToCheck: MutableSet<String> = mutableSetOf()

        addMapsToCollection(tolkienMaps, requiredMaps)
        addMapsToCollection(tolkienMapsUIStructure, mapsToCheck)

        val errors: MutableList<String> = mutableListOf()
        for (requiredMap in requiredMaps) {
            if (!mapsToCheck.contains(requiredMap)) {
                errors.add("Map $requiredMap not found in tolkien_maps_ui_structure.xml")
            }
        }

        if (errors.size != 0) {
            throw InconsistentResourcesException(errors.joinToString("\n"))
        }
    }
}

afterEvaluate {
    project.tasks.named("mergeDebugResources") {
        //TODO consider rewriting it using mustRunAfter
        dependsOn("checkTolkienMapsResourceConsistency")
    }
}