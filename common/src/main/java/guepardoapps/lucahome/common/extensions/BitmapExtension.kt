package guepardoapps.lucahome.common.extensions

import android.graphics.*

/**
 * @param color the color set for the bitmap
 * @return returns a rounded bitmap
 */
fun Bitmap.circleBitmap(color: Int): Bitmap {
    return this.circleBitmap(this.height, this.width, color)
}

/**
 * @param height the height set for the bitmap
 * @param width the width set for the bitmap
 * @param color the color set for the bitmap
 * @return returns a rounded bitmap
 */
fun Bitmap.circleBitmap(height: Int,
                        width: Int,
                        color: Int): Bitmap {
    val output: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)

    val paint = Paint()
    paint.isAntiAlias = true
    paint.color = color
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    val canvas = Canvas(output)
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawOval(rectF, paint)
    canvas.drawBitmap(this, rect, rect, paint)

    this.recycle()

    return output
}