package my.danielleinad.tolkienmaps

import android.util.Log
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
import my.danielleinad.tolkienmaps.databinding.FragmentMiddleEarthMapBinding
import my.danielleinad.tolkienmaps.resources.CachedXmlResourceParser
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIStructure
import my.danielleinad.layeredscalableview.LayeredScalableView.LayerDescription
import kotlin.math.absoluteValue

const val TAG = "TolkienMapFragment"

open class TolkienMapFragment(private val mapId: String) : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding //TODO this is wrong
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

        binding = FragmentMiddleEarthMapBinding.inflate(layoutInflater)
        val loaderString = resources.getString(R.string.loading)
        loaderLayer = binding.imageView.LayerDescription(CenteredTextLayerView(loaderString), Matrix())
        val tolkienMaps = CachedXmlResourceParser.getTolkienMaps(resources)
        thisTolkienMap = tolkienMaps.maps[mapId]?: throw Exception("Unknown map: $mapId")
        if (thisTolkienMap.positions.size == 0) {
            binding.showHideOverlaidMaps.visibility = View.INVISIBLE
        }
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
        val tolkienMapsUIStructure = CachedXmlResourceParser.getTolkienMapsUIStructure(resources)

        val mainMapRepresentation = tolkienMapsUIStructure.representations[mapId]
            ?: throw Exception("Representation not found for map $mapId")

        mainLayer = binding.imageView.LayerDescription(
            OptimizedBitmapLayerView(
                mainMapRepresentation.bitmap,
                mainMapRepresentation.lowerRes,
                mainMapRepresentation.lowestRes,
            ),
            Matrix()
        )

        val initialMatrix = Matrix()
        appendMapLayersRecursively(thisTolkienMap, initialMatrix, tolkienMapsUIStructure)
    }

    private fun appendMapLayersRecursively(
        tolkienMap: TolkienMaps.TolkienMap,
        initialMatrix: Matrix,
        tolkienMapsUIStructure: TolkienMapsUIStructure,
    ) {
        val imageView = binding.imageView
        for (position in tolkienMap.positions) {
            val matrix = Matrix()
            matrix.postRotate(position.rotate)
            matrix.postScale(position.scale, position.scale)
            matrix.postTranslate(position.translateX, position.translateY)
            matrix.postConcat(initialMatrix)

            val otherMapId = position.map.id
            val otherMapRepresentation = tolkienMapsUIStructure.representations[otherMapId]?: throw Exception("Representation not found for")

            val mapLayer = imageView.LayerDescription(
                OptimizedBitmapLayerView(
                    otherMapRepresentation.bitmap,
                    otherMapRepresentation.lowerRes,
                    otherMapRepresentation.lowestRes,
                ), matrix
            )
            val action = tolkienMapsUIStructure.actions[Pair(mapId, otherMapId)]
            if (action == null) {
                Log.w(TAG, "Action not found for map $otherMapId")
            } else {
                mapLayer.onDoubleTapListener = {
                    findNavController().navigate(action)
                    true
                }
            }

            val container = RectangleLayerView(
                otherMapRepresentation.bitmap.width.toFloat(),
                otherMapRepresentation.bitmap.height.toFloat(),
                true,
                containerPaint
            )
            val containerLayer = imageView.LayerDescription(container, Matrix(matrix))

            val borders = RectangleLayerView(
                otherMapRepresentation.bitmap.width.toFloat(),
                otherMapRepresentation.bitmap.height.toFloat(),
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

            appendMapLayersRecursively(position.map, matrix, tolkienMapsUIStructure)
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
                tolkienMapFragment.renderTolkienMaps()
            }
        }
    }
}

class OptimizedBitmapLayerView(private val original: Bitmap, private val lowerRes: Bitmap, private val lowestRes: Bitmap) : LayerView {
    override fun drawItself(canvas: Canvas, matrix: Matrix, context: LayeredScalableView.Context) {
        val f = FloatArray(9)
        matrix.getValues(f)
        val scaleX = f[Matrix.MSCALE_X]
        val skewX = f[Matrix.MSKEW_X]
        val finalScale = scaleX + skewX.absoluteValue // TODO this is absolutely mathematically wrong

        val correctingFactor = if (context.isMoving) {
            0.5
        } else {
            // TODO why do we need this factor in this case
            1.5
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