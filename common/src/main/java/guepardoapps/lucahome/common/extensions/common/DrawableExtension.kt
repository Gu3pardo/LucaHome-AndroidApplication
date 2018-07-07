package guepardoapps.lucahome.common.extensions.common

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.drawable.shapes.Shape

/**
 * Solution from
 * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
 *
 * @param shape     shape - OvalShape, RectShape or RoundRectShape
 * @param height    height of the shape to return
 * @param width     width of the shape to return
 * @param color     color of the new shape
 * @param padding   padding used in the new shape
 * @return returns a shape
 */
fun ShapeDrawable.drawShape(
        shape: Shape,
        width: Int,
        height: Int,
        color: Int,
        padding: Int): ShapeDrawable {
    val shapeDrawable = ShapeDrawable(shape)
    shapeDrawable.intrinsicHeight = height
    shapeDrawable.intrinsicWidth = width
    shapeDrawable.paint.color = color
    shapeDrawable.setPadding(padding, padding, padding, padding)
    return shapeDrawable
}

/**
 * Solution from
 * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
 *
 * @param height  height of the shape to return
 * @param width   width of the shape to return
 * @param color   color of the new shape
 * @param padding padding used in the new shape
 * @return returns a shape as a circle
 */
fun ShapeDrawable.drawCircle(
        width: Int,
        height: Int,
        color: Int,
        padding: Int): ShapeDrawable {
    return this.drawShape(OvalShape(), width, height, color, padding)
}

/**
 * Solution from
 * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
 *
 * @param height  height of the shape to return
 * @param width   width of the shape to return
 * @param color   color of the new shape
 * @param padding padding used in the new shape
 * @return returns a shape as a rectangle
 */
fun ShapeDrawable.drawRectangle(
        width: Int,
        height: Int,
        color: Int,
        padding: Int): ShapeDrawable {
    return this.drawShape(RectShape(), width, height, color, padding)
}

/**
 * Solution from
 * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
 *
 * @param height     height of the shape to return
 * @param width      width of the shape to return
 * @param color      color of the new shape
 * @param padding    padding used in the new shape
 * @param outerRadii the radii to the corners
 * @return returns a shape as a rectangle with rounded corners
 */
fun ShapeDrawable.drawRoundCornerRectangle(
        width: Int,
        height: Int,
        color: Int,
        padding: Int,
        outerRadii: FloatArray): ShapeDrawable {
    return this.drawShape(RoundRectShape(outerRadii, null, null), width, height, color, padding)
}

/**
 * Solution from
 * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
 *
 * @param orientation  orientation of the gradient
 * @param startColor   color at the start
 * @param endColor     color at the end
 * @param shape        the shape
 * @param cornerRadius radius at the corners
 * @return returns a gradient shape
 */
fun ShapeDrawable.drawGradientDrawable(
        orientation: GradientDrawable.Orientation,
        startColor: Int,
        endColor: Int,
        shape: Int,
        cornerRadius: Float): GradientDrawable {
    val gradient = GradientDrawable(orientation, intArrayOf(startColor, endColor))
    gradient.shape = shape /*GradientDrawable.RECTANGLE*/
    gradient.cornerRadius = cornerRadius
    return gradient
}