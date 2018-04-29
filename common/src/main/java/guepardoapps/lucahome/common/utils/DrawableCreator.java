package guepardoapps.lucahome.common.utils;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.NonNull;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DrawableCreator {

    /**
     * Solution from
     * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
     *
     * @param shapeType type of the shape - OvalShape, RectShape or RoundRectShape
     * @param height    height of the shape to return
     * @param width     width of the shape to return
     * @param color     color of the new shape
     * @param padding   padding used in the new shape
     * @return returns a shape
     */
    public static ShapeDrawable DrawShape(@NonNull Shape shapeType, int width, int height, int color, int padding) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(shapeType);
        shapeDrawable.setIntrinsicHeight(height);
        shapeDrawable.setIntrinsicWidth(width);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.setPadding(padding, padding, padding, padding);
        return shapeDrawable;
    }

    /**
     * Solution from
     * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
     *
     * @param height  height of the shape to return
     * @param width   width of the shape to return
     * @param color   color of the new shape
     * @param padding padding used in the new shape
     * @return returns a shape
     */
    public static ShapeDrawable DrawCircle(int width, int height, int color, int padding) {
        return DrawShape(new OvalShape(), width, height, color, padding);
    }

    /**
     * Solution from
     * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
     *
     * @param height  height of the shape to return
     * @param width   width of the shape to return
     * @param color   color of the new shape
     * @param padding padding used in the new shape
     * @return returns a shape
     */
    public static ShapeDrawable DrawRectangle(int width, int height, int color, int padding) {
        return DrawShape(new RectShape(), width, height, color, padding);
    }

    /**
     * Solution from
     * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
     *
     * @param height  height of the shape to return
     * @param width   width of the shape to return
     * @param color   color of the new shape
     * @param padding padding used in the new shape
     * @return returns a shape
     */
    public static ShapeDrawable DrawRoundCornerRectange(int width, int height, float[] outerRadii, int color, int padding) {
        return DrawShape(new RoundRectShape(outerRadii, null, null), width, height, color, padding);
    }

    /**
     * Solution from
     * https://takeoffandroid.com/creating-shapes-using-shapedrawable-and-gradientdrawable-a6ad6e6044ff
     *
     * @param startColor   color at the start
     * @param endColor     color at the end
     * @param cornerRadius radius at the corners
     * @return returns a gradient shape
     */
    public static GradientDrawable DrawGradientDrawable(GradientDrawable.Orientation orientation, int startColor, int endColor, int shape, float cornerRadius) {
        GradientDrawable gradient = new GradientDrawable(orientation, new int[]{startColor, endColor});
        gradient.setShape(shape/*GradientDrawable.RECTANGLE*/);
        gradient.setCornerRadius(cornerRadius);
        return gradient;
    }
}
