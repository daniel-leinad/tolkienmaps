import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

//TODO cache task

fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val resultingImage: Image =
        originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT)
    val outputImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null)
    return outputImage
}

project.tasks.register("generateMaps") {
    doFirst {
        val sourceDir = layout.projectDirectory.dir("image_source").dir("maps_orig")
        val destDir = layout.projectDirectory.dir("src").dir("main").dir("res").dir("drawable")

        // remove generated files
        destDir.getAsFileTree().getFiles().forEach {
            if (it.name.startsWith("generated_")) {
                it.delete()
            }
        }

        // generate files from source
        sourceDir.getAsFileTree().getFiles().forEach {
            println("processing map ${it.name}")

            // copy original
            val destName = "generated_" + it.name
            val path = destDir.file(destName)
            it.copyTo(File(path.toString()), true)

            // create preview
            val sourceImage = ImageIO.read(File(it.path))
            val factor = 3
            val previewWidth = sourceImage.width / factor
            val previewHeight = sourceImage.height / factor
            val previewImage = resizeImage(sourceImage, previewWidth.toInt(), previewHeight.toInt())
            val previewImagePath = destDir.file("generated_preview_" + it.name)
            ImageIO.write(previewImage, it.extension, File(previewImagePath.toString()))
        }
    }
}

afterEvaluate {
    project.tasks.named("mergeDebugResources") {
        dependsOn("generateMaps")
    }
}