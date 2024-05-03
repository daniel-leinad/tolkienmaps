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

        val tolkienMapsUIStructure: Document = builder.parse(resourceDir.file("tolkien_maps_ui_details.xml").asFile)
        tolkienMapsUIStructure.documentElement.normalize()

//        val compasses: Document = builder.parse(resourceDir.file("compasses.xml").asFile)
//        compasses.documentElement.normalize()

        val requiredMaps: MutableList<String> = mutableListOf()
        val mapsUiStructure: MutableSet<String> = mutableSetOf()
        val mapsCompasses: MutableSet<String> = mutableSetOf()

        addMapsToCollection(tolkienMaps, requiredMaps)
        addMapsToCollection(tolkienMapsUIStructure, mapsUiStructure)
//        addMapsToCollection(compasses, mapsCompasses)

        val errors: MutableList<String> = mutableListOf()
        for (requiredMap in requiredMaps) {
            if (!mapsUiStructure.contains(requiredMap)) {
                errors.add("Map $requiredMap not found in tolkien_maps_ui_structure.xml")
            }
//            if (!mapsCompasses.contains(requiredMap)) {
//                errors.add("Map $requiredMap not found in compasses.xml")
//            }
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