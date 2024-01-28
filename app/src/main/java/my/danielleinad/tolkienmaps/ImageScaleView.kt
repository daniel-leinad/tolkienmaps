package my.danielleinad.tolkienmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import kotlin.math.min


class ImageScaleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    lateinit var imageSource1: Bitmap
    lateinit var imageSource2: Bitmap
    lateinit var imageSource1Preview: Bitmap
    lateinit var imageSource2Preview: Bitmap
    val image2Matrix = Matrix()
    private val imageSourcePaint = Paint()
    private var imageSourceMatrix = Matrix()
    private var cachedPoints: MutableList<XYPoint> = mutableListOf()
    inner class XYPoint(val x: Float, val y: Float)
    private var needInvalidation = false

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
        val matrix2 = Matrix()
        matrix2.postConcat(image2Matrix)
        matrix2.postConcat(imageSourceMatrix)
        drawBitmap(canvas, imageSourceMatrix, imageSource1, imageSource1Preview)
        drawBitmap(canvas, matrix2, imageSource2, imageSource2Preview)
    }

    private fun drawBitmap(canvas: Canvas, matrix: Matrix, image: Bitmap, preview: Bitmap) {
        val f = FloatArray(9)
        matrix.getValues(f)
        val scaleX = f[Matrix.MSCALE_X]
        val previewScale = image.width.toFloat() / preview.width.toFloat()
        if (scaleX < 1) {
            val resMatrix = Matrix()
            resMatrix.postScale(previewScale, previewScale)
            resMatrix.postConcat(matrix)
            canvas.drawBitmap(preview, resMatrix, imageSourcePaint)
        } else {
            canvas.drawBitmap(image, matrix, imageSourcePaint)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        val imageWidth = imageSource1.width.toFloat()
        val imageHeight = imageSource1.height.toFloat()
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