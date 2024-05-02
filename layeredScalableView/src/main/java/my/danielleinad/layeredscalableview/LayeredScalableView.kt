package my.danielleinad.layeredscalableview

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.min


class LayeredScalableView(context: android.content.Context, attrs: AttributeSet) : View(context, attrs) {
    val layers: MutableList<LayerDescription> = mutableListOf()

    private var containerMatrix = Matrix()

    private var needInvalidation = false
    private var isMoving = false

    private var cachedPoints: MutableList<XYPoint> = mutableListOf()

    private object synchronizedLayoutAccess {
        private var layoutData: LayoutData? = null
        private val layoutMonitor = Object()

        fun updateLayout(newLayout: LayoutData) {
            synchronized(layoutMonitor) {
                layoutData = newLayout
                layoutMonitor.notifyAll()
            }
        }
        fun waitForLayout(): LayoutData {
            val layout: LayoutData
            synchronized(layoutMonitor) {
                var localLayout = layoutData
                while (localLayout == null) {
                    layoutMonitor.wait()
                    localLayout = layoutData
                }
                layout = localLayout
            }
            return layout
        }
    }

    private data class LayoutData(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    fun alignCenter() {
        val layout = synchronizedLayoutAccess.waitForLayout()

        if (layers.size == 0) return

        // TODO does this belong here?
        val width = (layout.right - layout.left).toFloat()
        val height = (layout.bottom - layout.top).toFloat()
        val imageSource1 = layers[0]
        val imageWidth: Float = imageSource1.layerView.width
        val imageHeight: Float = imageSource1.layerView.height
        val scaleFactor = min(width / imageWidth, height / imageHeight)
        containerMatrix.postScale(scaleFactor, scaleFactor)

        // centering
        val topOffset = ((layout.bottom - layout.top) / 2) - ((imageSource1.layerView.height * scaleFactor) / 2)
        val leftOffset = ((layout.right - layout.left) / 2) - ((imageSource1.layerView.width * scaleFactor) / 2)

        containerMatrix.postTranslate(leftOffset, topOffset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (layer in layers) {
            layer.drawItself(canvas)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        synchronizedLayoutAccess.updateLayout(LayoutData(left, top, right, bottom))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val currentPoint = XYPoint(event.x, event.y)
                    cachedPoints = mutableListOf(currentPoint)

                    for (i in layers.size-1 downTo 0) {
                        val layer = layers[i]

                        if (!layer.activated) continue
                        val onTouchListener = layer.onTouchListener ?: continue
                        if (!layer.contains(currentPoint)) continue

                        val res = onTouchListener()
                        needInvalidation = true

                        if (res) break
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val pointerCount = event.pointerCount
                    val numberOfRelevantPoints = min(pointerCount, 2)
                    if ((numberOfRelevantPoints == cachedPoints.size)
                        && (numberOfRelevantPoints != 0)) {
                        var xCachedSum = 0F
                        var yCachedSum = 0F
                        var xNewSum = 0F
                        var yNewSum = 0F
                        for (i in 0 until numberOfRelevantPoints) {
                            xCachedSum += cachedPoints[i].x
                            yCachedSum += cachedPoints[i].y
                            xNewSum += event.getX(i)
                            yNewSum += event.getY(i)
                        }

                        val dX = (xNewSum - xCachedSum) / numberOfRelevantPoints
                        val dY = (yNewSum - yCachedSum) / numberOfRelevantPoints

                        containerMatrix.postTranslate(dX, dY)
                        needInvalidation = true

                        if ((dX > 0) || (dY > 0)) {
                            isMoving = true
                        }
                    }

                    cachedPoints.clear()
                    for (i in 0 until numberOfRelevantPoints) {
                        cachedPoints.add(XYPoint(event.getX(i), event.getY(i)))
                    }
                }
                else -> isMoving = false
            }
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            invalidateIfNeeded()
        }

        return true
    }

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
                val scaleFactor = scaleGestureDetector.scaleFactor
                val x = scaleGestureDetector.focusX
                val y = scaleGestureDetector.focusY
                containerMatrix.postTranslate(-x, -y)
                containerMatrix.postScale(scaleFactor, scaleFactor)
                containerMatrix.postTranslate(x, y)
                needInvalidation = true
                return true
            }

            override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {
            }

        })

    private val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val currentPoint = XYPoint(e.x, e.y)
                for (i in layers.size - 1 downTo 0) {
                    val layer = layers[i]

                    if (!layer.activated) continue
                    val onSingleTapConfirmedListener =
                        layer.onSingleTapConfirmedListener ?: continue
                    if (!layer.contains(currentPoint)) continue

                    val res = onSingleTapConfirmedListener()
                    needInvalidation = true

                    if (res) break
                }

                invalidateIfNeeded()

                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val currentPoint = XYPoint(e.x, e.y)
                for (i in layers.size - 1 downTo 0) {
                    val layer = layers[i]

                    if (!layer.activated) continue
                    val onSingleTapListener = layer.onSingleTapListener ?: continue
                    if (!layer.contains(currentPoint)) continue

                    val res = onSingleTapListener()
                    needInvalidation = true

                    if (res) break
                }

                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val currentPoint = XYPoint(e.x, e.y)
                for (i in layers.size - 1 downTo 0) {
                    val layer = layers[i]

                    if (!layer.activated) continue
                    val onDoubleTapListener = layer.onDoubleTapListener ?: continue
                    if (!layer.contains(currentPoint)) continue

                    val res = onDoubleTapListener()
                    needInvalidation = true

                    if (res) break
                }

                return true
            }
        })

    private fun invalidateIfNeeded() {
        if (needInvalidation) {
            invalidate()
            needInvalidation = false
        }
    }

    // TODO does this make sense as an inner class?
    inner class LayerDescription(val layerView: LayerView, private val initialMatrix: Matrix) {
        var activated: Boolean = true
        var onSingleTapConfirmedListener: (() -> Boolean)? = null // return true to consume the click
        var onSingleTapListener: (() -> Boolean)? = null // return true to consume the click
        var onDoubleTapListener: (() -> Boolean)? = null // return true to consume the click
        var onTouchListener: (() -> Boolean)? = null // return true to consume touch

        fun drawItself(canvas: Canvas) {
            if (!activated) return

            val matrix = Matrix()
            matrix.postConcat(initialMatrix)
            matrix.postConcat(containerMatrix)

            val context = Context(isMoving)
            layerView.drawItself(canvas, matrix, context)
        }

        fun contains(point: XYPoint): Boolean {
            // Because rotation is possible, instead of applying transformation matrix to the layer
            // and checking whether the layer contains the point, we apply inverse transformation
            // matrix to the point and then check whether the layer's original rectangle contains the point
            val matrix = Matrix()
            matrix.postConcat(initialMatrix)
            matrix.postConcat(containerMatrix)
            val invertedMatrix = Matrix()
            assert(matrix.invert(invertedMatrix))
            val pointsArray = floatArrayOf(point.x, point.y)
            invertedMatrix.mapPoints(pointsArray)

            val rectRes = RectF(0F, 0F, layerView.width, layerView.height)

            return rectRes.contains(pointsArray[0], pointsArray[1])
        }
    }

    class Context(val isMoving: Boolean)
}

class XYPoint(val x: Float, val y: Float)

interface LayerView {
    fun drawItself(canvas: Canvas, matrix: Matrix, context: LayeredScalableView.Context)
    val width: Float
    val height: Float
}