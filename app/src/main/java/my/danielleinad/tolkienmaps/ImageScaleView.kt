package my.danielleinad.tolkienmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import kotlin.math.min


class ImageScaleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val imageSourcePaint = Paint()
    private var imageSourceMatrix = Matrix()
    private var cachedPoints: MutableList<XYPoint> = mutableListOf()
    class XYPoint(val x: Float, val y: Float)
    private var needInvalidation = false
    val layers: MutableList<LayerDescription> = mutableListOf()

    private val scaleGestureDetector = ScaleGestureDetector(context, object : OnScaleGestureListener {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            MessageShower.show("on scale!")
            val scaleFactor = scaleGestureDetector.scaleFactor
            val x = scaleGestureDetector.focusX
            val y = scaleGestureDetector.focusY
            imageSourceMatrix.postTranslate(-x , -y)
            imageSourceMatrix.postScale(scaleFactor, scaleFactor)
            imageSourceMatrix.postTranslate(x, y)
            needInvalidation = true
            return true
        }

        override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
            MessageShower.show("scale begins!")
            return true
        }

        override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {
            MessageShower.show("done with scale!")
        }

    })

    private val gestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val currentPoint = XYPoint(e.x, e.y)
            for (i in layers.size-1 downTo 0) {
                val layer = layers[i]

                if (!layer.activated) continue
                val onSingleTapConfirmedListener = layer.onSingleTapConfirmedListener ?: continue
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
            for (i in layers.size-1 downTo 0) {
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
            for (i in layers.size-1 downTo 0) {
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

    interface Layer {
        fun drawItself(canvas: Canvas, matrix: Matrix)
        val width: Float
        val height: Float
    }

//    TODO do these work/make sense as an inner class??
    inner class BitMapLayer(val bitmap: Bitmap, val previewBitmap: Bitmap) : Layer {
        override fun drawItself(canvas: Canvas, matrix: Matrix) {
            val f = FloatArray(9)
            matrix.getValues(f)
            val scaleX = f[Matrix.MSCALE_X]
            val previewScale = bitmap.width.toFloat() / previewBitmap.width.toFloat()
            if (scaleX < 1) {
                val resMatrix = Matrix()
                resMatrix.postScale(previewScale, previewScale)
                resMatrix.postConcat(matrix)
                canvas.drawBitmap(previewBitmap, resMatrix, imageSourcePaint)
            } else {
                canvas.drawBitmap(bitmap, matrix, imageSourcePaint)
            }
        }

        override val width: Float = bitmap.width.toFloat()
        override val height: Float = bitmap.height.toFloat()
    }

    // TODO there is great confusion between what left, top, right, bottom means and width and height properties
    // probably better to remove left, right, top, bottom from here
    inner class RectangleLayer(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val fill: Boolean,
        val paint: Paint) : Layer {
        override fun drawItself(canvas: Canvas, matrix: Matrix) {
            val points = floatArrayOf(left, top, right, top, left, bottom, right, bottom)
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

        override val width: Float = right - left
        override val height: Float = bottom - top
    }

    inner class LayerDescription(val layer: Layer, val initialMatrix: Matrix) {
        var activated: Boolean = true
        var onSingleTapConfirmedListener: (() -> Boolean)? = null // return true to consume the click
        var onSingleTapListener: (() -> Boolean)? = null // return true to consume the click
        var onDoubleTapListener: (() -> Boolean)? = null // return true to consume the click
        var onTouchListener: (() -> Boolean)? = null // return true to consume touch
        fun drawItself(canvas: Canvas) {
            if (!activated) return

            val matrix = Matrix()
            matrix.postConcat(initialMatrix)
            matrix.postConcat(imageSourceMatrix)
            layer.drawItself(canvas, matrix)
        }

        fun contains(point: XYPoint): Boolean {
            // Because rotation is possible, instead of applying transformation matrix to the layer
            // and checking whether the layer contains the point, we apply inverse transformation
            // matrix to the point and then check whether the layer's original rectangle contains the point
            val matrix = Matrix()
            matrix.postConcat(initialMatrix)
            matrix.postConcat(imageSourceMatrix)
            val invertedMatrix = Matrix()
            assert(matrix.invert(invertedMatrix))
            var pointsArray = floatArrayOf(point.x, point.y)
            invertedMatrix.mapPoints(pointsArray)

            val rectRes = RectF(0F, 0F, layer.width, layer.height)

            return rectRes.contains(pointsArray[0], pointsArray[1])
        }
    }

//    private val gestureDetector = GestureDetector(context, object : OnGestureListener {
//        override fun onDown(p0: MotionEvent): Boolean {
//            MessageShower.show("onDown")
//            return true
//        }
//
//        override fun onShowPress(p0: MotionEvent) {
//            MessageShower.show("onShowPress")
//        }
//
//        override fun onSingleTapUp(p0: MotionEvent): Boolean {
//            MessageShower.show("onSingleTapUp")
//            return true
//        }
//
//        override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
//            MessageShower.show("onScroll $p2, $p3")
//            return true
//        }
//
//        override fun onLongPress(p0: MotionEvent) {
//            MessageShower.show("onLongPress")
//        }
//
//        override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
//            MessageShower.show("onFling")
//            return true
//        }
//
//    })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (layer in layers) {
            layer.drawItself(canvas)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (layers.size == 0) return

        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        val imageSource1 = layers[0]
        val imageWidth: Float = imageSource1.layer.width
        val imageHeight: Float = imageSource1.layer.height
        val scaleFactor = min(width / imageWidth, height / imageHeight)
        imageSourceMatrix.postScale(scaleFactor, scaleFactor)

        // centering
        val topOffset = ((bottom - top) / 2) - ((imageSource1.layer.height * scaleFactor) / 2)
        val leftOffset = ((right - left) / 2) - ((imageSource1.layer.width * scaleFactor) / 2)

        imageSourceMatrix.postTranslate(leftOffset, topOffset)

//        setOnClickListener {
//            if (cachedPoints.size == 0) {
//                MessageShower.show("Something went wrong during click: no coordinates detected")
//                return@setOnClickListener
//            }
//
//            val currentPoint = cachedPoints[0]
//
//            for (i in layers.size-1 downTo 0) {
//                val layer = layers[i]
//
//                if (!layer.activated) continue
//                if (layer.onClickListener == null) continue
//                if (!layer.contains(currentPoint)) continue
//
//                val res = layer.onClickListener!!()
//                needInvalidation = true
//                if (res) break
//            }
//            invalidateIfNeeded()
//        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val currentPoint = XYPoint(event.x, event.y)
                    cachedPoints = mutableListOf(currentPoint)
                    MessageShower.show("Action down!")

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
                MotionEvent.ACTION_POINTER_DOWN -> MessageShower.show("pointer down!")
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
                        imageSourceMatrix.postTranslate(dX, dY)
                        needInvalidation = true
                    }

                    cachedPoints.clear()
                    for (i in 0 until numberOfRelevantPoints) {
                        cachedPoints.add(XYPoint(event.getX(i), event.getY(i)))
                    }
                }
            }
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            invalidateIfNeeded()
        }

        return true
    }

    private fun invalidateIfNeeded() {
        if (needInvalidation) {
            invalidate()
            needInvalidation = false
        }
    }
}