import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

//TODO cache task

fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val resultingImage: Image =
        originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
    val outputImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null)
    return outputImage
}

fun generateImageScaledByAFactor(sourceImage: BufferedImage, factor: Float, originalFile: File, tag: String?, destDir: Directory) {
    val resultingWidth = sourceImage.width / factor
    val resultingHeight = sourceImage.height / factor
    val resultingImage = resizeImage(sourceImage, resultingWidth.toInt(), resultingHeight.toInt())
    val filenamePrefix = if (tag == null) { "generated_" } else { "generated_" + tag + "_" }
    val resultingImagePath = destDir.file(filenamePrefix + originalFile.name)
    ImageIO.write(resultingImage, originalFile.extension, File(resultingImagePath.toString()))
}

fun factorFromTargetSize(sourceImage: BufferedImage, targetSize: Float): Float {
    return minOf(sourceImage.width, sourceImage.height).toFloat() / targetSize // TODO factor could be < 1, handle this case
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

            val sourceImage = ImageIO.read(File(it.path))

            // generate lowerRes
            val lowerResFactor = factorFromTargetSize(sourceImage, 750F)
            generateImageScaledByAFactor(sourceImage, lowerResFactor, it, "lower_res", destDir)

            // generate lowestRes
            val lowestResFactor = factorFromTargetSize(sourceImage, 250F)
            generateImageScaledByAFactor(sourceImage, lowestResFactor, it, "lowest_res", destDir)
        }
    }
}

afterEvaluate {
    project.tasks.named("mergeDebugResources") {
        dependsOn("generateMaps")
    }
    project.tasks.named("mergeReleaseResources") {
        dependsOn("generateMaps")
    }
}