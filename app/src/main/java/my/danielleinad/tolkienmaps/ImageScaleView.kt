package my.danielleinad.tolkienmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import kotlin.math.min


class ImageScaleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val imageSourcePaint = Paint()
    private var imageSourceMatrix = Matrix()
    private var cachedPoints: MutableList<XYPoint> = mutableListOf()
    inner class XYPoint(val x: Float, val y: Float)
    private var needInvalidation = false
    val layers: MutableList<LayerDescription> = mutableListOf()

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

    inner class RectangleLayer(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val fill: Boolean,
        val paint: Paint) : Layer {
        override fun drawItself(canvas: Canvas, matrix: Matrix) {
            // TODO better name for resRect!!
            val resRect = RectF(left, top, right, bottom)
            matrix.mapRect(resRect)
            if (fill) {
                canvas.drawRect(resRect, paint)
            } else {
                canvas.drawLine(resRect.left, resRect.top, resRect.right, resRect.top, paint)
                canvas.drawLine(resRect.left, resRect.bottom, resRect.right, resRect.bottom, paint)
                canvas.drawLine(resRect.left, resRect.top, resRect.left, resRect.bottom, paint)
                canvas.drawLine(resRect.right, resRect.top, resRect.right, resRect.bottom, paint)
            }
        }

        override val width: Float = right - left
        override val height: Float = top - bottom
    }

    inner class LayerDescription(val layer: Layer, val initialMatrix: Matrix) {
        var activated: Boolean = true
        fun drawItself(canvas: Canvas) {
            if (!activated) return

            val matrix = Matrix()
            matrix.postConcat(initialMatrix)
            matrix.postConcat(imageSourceMatrix)
            layer.drawItself(canvas, matrix)
        }
    }

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
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
//        MessageShower.show("Touch event has happened!")
        if (event != null) {
//            gestureDetector.onTouchEvent(event)
            needInvalidation = false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cachedPoints = mutableListOf(XYPoint(event.x, event.y))
                    MessageShower.show("Action down!")
                }
                MotionEvent.ACTION_POINTER_DOWN -> MessageShower.show("pointer down!")
                MotionEvent.ACTION_MOVE -> {
                    val pointerCount = event.pointerCount
                    val numberOfRelevantPoints = min(pointerCount, 2)
                    if (numberOfRelevantPoints != cachedPoints.size) {
                        // TODO remove debug message
                        MessageShower.show("Number pointers change!")
                    } else if (numberOfRelevantPoints != 0) {
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

            if (needInvalidation) {
                invalidate()
            }
        }
//        imageSourceMatrix.postScale(0.95F, 0.95F)
//        invalidate()
        return true
    }
}