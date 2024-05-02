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

//fun generateImageScaledByAFactor(sourceImage: BufferedImage, factor: Float, tag: String) {
//    val previewWidth = sourceImage.width / factor
//    val previewHeight = sourceImage.height / factor
//    val previewImage = resizeImage(sourceImage, previewWidth.toInt(), previewHeight.toInt())
//    val previewImagePath = destDir.file("generated_${tag}_" + it.name)
//    ImageIO.write(previewImage, it.extension, File(previewImagePath.toString()))
//}

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

            //TODO DRY violation

            // generate lowerRes
            val sourceImage = ImageIO.read(File(it.path))
            val factor1 = minOf(sourceImage.width, sourceImage.height).toFloat() / 750F // TODO factor could be < 1, handle this case
            println("lower res factor = $factor1")
            val previewWidth1 = sourceImage.width / factor1
            val previewHeight1 = sourceImage.height / factor1
            val previewImage1 = resizeImage(sourceImage, previewWidth1.toInt(), previewHeight1.toInt())
            val previewImagePath1 = destDir.file("generated_lower_res_" + it.name)
            ImageIO.write(previewImage1, it.extension, File(previewImagePath1.toString()))

            // generate lowestRes
            val factor2 = minOf(sourceImage.width, sourceImage.height).toFloat() / 250F // TODO factor could be < 1, handle this case
            println("lowest res factor = $factor2")
            val previewWidth = sourceImage.width / factor2
            val previewHeight = sourceImage.height / factor2
            val previewImage = resizeImage(sourceImage, previewWidth.toInt(), previewHeight.toInt())
            val previewImagePath = destDir.file("generated_lowest_res_" + it.name)
            ImageIO.write(previewImage, it.extension, File(previewImagePath.toString()))
        }
    }
}

afterEvaluate {
    project.tasks.named("mergeDebugResources") {
        dependsOn("generateMaps")
    }
}