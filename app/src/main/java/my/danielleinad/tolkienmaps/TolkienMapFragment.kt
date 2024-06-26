package my.danielleinad.tolkienmaps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.danielleinad.layeredscalableview.LayerView
import my.danielleinad.layeredscalableview.LayeredScalableView
import my.danielleinad.tolkienmaps.databinding.FragmentTolkienMapBinding
import my.danielleinad.tolkienmaps.resources.CachedXmlResourceParser
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIDetails
import my.danielleinad.layeredscalableview.LayeredScalableView.LayerDescription
import my.danielleinad.tolkienmaps.resources.CachedUnscaledBitmapProvider
import kotlin.math.absoluteValue

open class TolkienMapFragment(private val mapId: String) : Fragment() {
    private lateinit var binding: FragmentTolkienMapBinding
    private var areNonMainLayersShown: Boolean = false
    private val overlaidTolkienMaps: MutableList<OverlaidTolkienMap> = mutableListOf()
    private lateinit var mainLayer: LayerDescription
    private val containerPaint: Paint = Paint()
    private val borderPaint = Paint()
    private var tolkienMapsAreRendered = false
    private lateinit var loaderLayer: LayerDescription

    private lateinit var thisTolkienMap: TolkienMaps.TolkienMap

    init {
        containerPaint.color = Color.argb(20, 0, 50, 250)
        containerPaint.strokeWidth = 5F

        borderPaint.color = Color.DKGRAY
        borderPaint.strokeWidth = 5F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentTolkienMapBinding.inflate(layoutInflater)
        val loaderString = resources.getString(R.string.loading)
        // TODO I don't like the fact that loading screen is implemented as a layer in LayeredScalableView
        loaderLayer = binding.imageView.LayerDescription(CenteredTextLayerView(loaderString), Matrix())
        val tolkienMaps = CachedXmlResourceParser.getTolkienMaps(resources)
        thisTolkienMap = tolkienMaps.get(mapId)?: throw Exception("Unknown map: $mapId")
        val tolkienMapsUIDetails = CachedXmlResourceParser.getTolkienMapsUIDetails(resources)
        if (tolkienMapsUIDetails.getNavigations(mapId).isNullOrEmpty()) {
            binding.showHideOverlaidMaps.visibility = View.INVISIBLE
        }
        val compass = tolkienMapsUIDetails.getCompass(mapId)
            ?: throw Exception("Compass not found for $mapId")
        binding.compassView.setImageResource(compass)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!tolkienMapsAreRendered) {
            binding.imageView.layers.add(loaderLayer)
            binding.imageView.invalidate()
            AsyncRenderer.render(this)
        }
    }

    private fun renderTolkienMaps() {
        constructOverlaidTolkienMaps()

        binding.imageView.layers.add(mainLayer)
        for (layer in overlaidTolkienMaps) {
            binding.imageView.layers.add(layer.map)
            binding.imageView.layers.add(layer.container)
            binding.imageView.layers.add(layer.borders)
        }
        binding.imageView.alignCenterLayer(mainLayer)

        binding.showHideOverlaidMaps.setOnClickListener {
            areNonMainLayersShown = !areNonMainLayersShown
            hideShowOverlaidMaps()
        }

        hideShowOverlaidMaps()

        tolkienMapsAreRendered = true
        loaderLayer.activated = false
    }

    private fun constructOverlaidTolkienMaps() {
        val tolkienMapsUIStructure = CachedXmlResourceParser.getTolkienMapsUIDetails(resources)

        val mainMapRepresentation = tolkienMapsUIStructure.getRepresentation(mapId)
            ?: throw Exception("Representation not found for map $mapId")

        val mainMapOriginalBitmap = CachedUnscaledBitmapProvider.get(resources, mainMapRepresentation.original)
        val mainMapScale = thisTolkienMap.targetHeight / mainMapOriginalBitmap.height
        val mainLayerMatrix = Matrix()
        mainLayerMatrix.postRotate(thisTolkienMap.rotate)
        mainLayerMatrix.postScale(mainMapScale, mainMapScale)
        mainLayerMatrix.postTranslate(thisTolkienMap.translateX, thisTolkienMap.translateY)

        mainLayer = binding.imageView.LayerDescription(
            SubsamplingBitmapLayerView(
                mainMapOriginalBitmap,
                CachedUnscaledBitmapProvider.get(resources, mainMapRepresentation.lowerRes),
                CachedUnscaledBitmapProvider.get(resources, mainMapRepresentation.lowestRes),
            ),
            mainLayerMatrix
        )

        appendOverlaidTolkienMaps(tolkienMapsUIStructure)
    }

    private fun appendOverlaidTolkienMaps(
        tolkienMapsUIDetails: TolkienMapsUIDetails,
    ) {
        val imageView = binding.imageView
        val navigations = tolkienMapsUIDetails.getNavigations(mapId)
            ?: throw Exception("Navigations not found for $mapId")
        val tolkienMaps = CachedXmlResourceParser.getTolkienMaps(resources)
        for (entry in navigations) {
            val overlaidTolkienMapId = entry.key
            val overlaidTolkienMap = tolkienMaps.get(overlaidTolkienMapId)
                ?: throw Exception("Tolkien map with id $overlaidTolkienMapId not found")

            val overlaidTolkienMapRepresentation = tolkienMapsUIDetails.getRepresentation(overlaidTolkienMapId)
                ?: throw Exception("Representation not found for $overlaidTolkienMapId")

            val originalBitmap = CachedUnscaledBitmapProvider.get(resources, overlaidTolkienMapRepresentation.original)
            val lowerResBitmap = CachedUnscaledBitmapProvider.get(resources, overlaidTolkienMapRepresentation.lowerRes)
            val lowestResBitmap = CachedUnscaledBitmapProvider.get(resources, overlaidTolkienMapRepresentation.lowestRes)

            val overlaidTolkienMapScale = overlaidTolkienMap.targetHeight / originalBitmap.height
            val matrix = Matrix()
            matrix.postRotate(overlaidTolkienMap.rotate)
            matrix.postScale(overlaidTolkienMapScale, overlaidTolkienMapScale)
            matrix.postTranslate(overlaidTolkienMap.translateX, overlaidTolkienMap.translateY)

            val mapLayer = imageView.LayerDescription(
                SubsamplingBitmapLayerView(
                    originalBitmap,
                    lowerResBitmap,
                    lowestResBitmap,
                ), matrix
            )
            val action = entry.value
            mapLayer.onDoubleTapListener = {
                findNavController().navigate(action)
                true
            }

            val container = RectangleLayerView(
                originalBitmap.width.toFloat(),
                originalBitmap.height.toFloat(),
                true,
                containerPaint
            )
            val containerLayer = imageView.LayerDescription(container, Matrix(matrix))

            val borders = RectangleLayerView(
                originalBitmap.width.toFloat(),
                originalBitmap.height.toFloat(),
                false,
                borderPaint
            )

            val borderLayer = imageView.LayerDescription(borders, Matrix(matrix))

            val layer = OverlaidTolkienMap(mapLayer, containerLayer, borderLayer)

            containerLayer.onSingleTapConfirmedListener = {
                layer.isMapShown = !layer.isMapShown
                true
            }

            overlaidTolkienMaps.add(layer)
        }
    }

    private fun hideShowOverlaidMaps() {
        for (layerDescription in overlaidTolkienMaps) {
            layerDescription.map.activated = areNonMainLayersShown && layerDescription.isMapShown
            layerDescription.container.activated = areNonMainLayersShown
            layerDescription.borders.activated = areNonMainLayersShown
        }

        binding.imageView.invalidate()
    }

    private class OverlaidTolkienMap(val map: LayerDescription, val container: LayerDescription, val borders: LayerDescription) {
        var isMapShown: Boolean = false
            set(value) {
                field = value
                map.activated = value
            }
    }

    object AsyncRenderer : ViewModel() {
        fun render(tolkienMapFragment: TolkienMapFragment) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    tolkienMapFragment.renderTolkienMaps()
                } catch (_: java.lang.IllegalStateException) {
                    // TODO find a better way to handle it

                    // User clicked "back" button, do nothing
                }
            }
        }
    }
}

class SubsamplingBitmapLayerView(
    private val original: Bitmap,
    private val lowerRes: Bitmap,
    private val lowestRes: Bitmap,
    ) : LayerView {
    override fun drawItself(canvas: Canvas, matrix: Matrix, context: LayeredScalableView.Context) {
        val f = FloatArray(9)
        matrix.getValues(f)
        val scaleX = f[Matrix.MSCALE_X]
        val skewX = f[Matrix.MSKEW_X]
        val finalScale = scaleX + skewX.absoluteValue // TODO this is absolutely mathematically wrong

        val correctingFactor = if (context.isMoving) {
            // Use lower resolution when user moves/scales the map
            0.5
        } else {
            1.0
        }
        val resultingWidth = original.width * finalScale * correctingFactor

        if (resultingWidth < lowestRes.width) {
            val lowestResScale = original.width.toFloat() / lowestRes.width.toFloat()
            val resMatrix = Matrix()
            resMatrix.postScale(lowestResScale, lowestResScale)
            resMatrix.postConcat(matrix)
            canvas.drawBitmap(lowestRes, resMatrix, null)
        } else if (resultingWidth < lowerRes.width) {
            val lowerResScale = original.width.toFloat() / lowerRes.width.toFloat()

            val resMatrix = Matrix()
            resMatrix.postScale(lowerResScale, lowerResScale)
            resMatrix.postConcat(matrix)
            canvas.drawBitmap(lowerRes, resMatrix, null)
        } else {
            canvas.drawBitmap(original, matrix, null)
        }
    }

    override val width: Float = original.width.toFloat()
    override val height: Float = original.height.toFloat()
}

class RectangleLayerView(
    override val width: Float,
    override val height: Float,
    private val fill: Boolean,
    private val paint: Paint
) : LayerView {

    override fun drawItself(canvas: Canvas, matrix: Matrix, context: LayeredScalableView.Context) {
        val points = floatArrayOf(0F, 0F, width, 0F, 0F, height, width, height)
        matrix.mapPoints(points)
        if (fill) {
            val path = Path()
            path.fillType = Path.FillType.EVEN_ODD
            path.moveTo(points[0], points[1])
            path.lineTo(points[2], points[3])
            path.lineTo(points[6], points[7])
            path.lineTo(points[4], points[5])
            path.close()

            canvas.drawPath(path, paint)
        } else {
            canvas.drawLine(points[0], points[1], points[2], points[3], paint)
            canvas.drawLine(points[2], points[3], points[6], points[7], paint)
            canvas.drawLine(points[4], points[5], points[6], points[7], paint)
            canvas.drawLine(points[0], points[1], points[4], points[5], paint)
        }
    }
}

class CenteredTextLayerView(private val text: String) : LayerView {
    override fun drawItself(canvas: Canvas, matrix: Matrix, context: LayeredScalableView.Context) {
        val paint = Paint()
        paint.textSize = 50F
        canvas.drawText(text, (canvas.width.toFloat() / 2) - 100, (canvas.height.toFloat() / 2), paint)
    }

    override val width = 0F
    override val height = 0F
}