package com.gtel.ekyc.roundcornerlayout;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class CanvasRounder {

    @NonNull private final Path path;
    private RectF rectF;
    @NonNull public CornersHolder cornersHolder;

    public float getTopLeftCornerRadius() {
        return this.cornersHolder.getTopLeftCornerRadius();
    }

    public final void setTopLeftCornerRadius(float value) {
        this.cornersHolder.setTopLeftCornerRadius(value);
        this.resetPath();
    }

    public final float getTopRightCornerRadius() {
        return this.cornersHolder.getTopRightCornerRadius();
    }

    public final void setTopRightCornerRadius(float value) {
        this.cornersHolder.setTopRightCornerRadius(value);
        this.resetPath();
    }

    public final float getBottomRightCornerRadius() {
        return this.cornersHolder.getBottomRightCornerRadius();
    }

    public final void setBottomRightCornerRadius(float value) {
        this.cornersHolder.setBottomRightCornerRadius(value);
        this.resetPath();
    }

    public final float getBottomLeftCornerRadius() {
        return this.cornersHolder.getBottomLeftCornerRadius();
    }

    public final void setBottomLeftCornerRadius(float value) {
        this.cornersHolder.setBottomLeftCornerRadius(value);
        this.resetPath();
    }

    public final float getCornerRadius() {
        return this.getTopLeftCornerRadius() == this.getTopRightCornerRadius()
                        && this.getTopRightCornerRadius() == this.getBottomRightCornerRadius()
                        && this.getBottomRightCornerRadius() == this.getBottomLeftCornerRadius()
                ? this.getTopLeftCornerRadius()
                : 0.0F;
    }

    public final void setCornerRadius(float value) {
        this.cornersHolder.setTopLeftCornerRadius(value);
        this.cornersHolder.setTopRightCornerRadius(value);
        this.cornersHolder.setBottomRightCornerRadius(value);
        this.cornersHolder.setBottomLeftCornerRadius(value);
        this.resetPath();
    }

    public final void round(@NotNull Canvas canvas, @NotNull Function1 drawFunction) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        Intrinsics.checkParameterIsNotNull(drawFunction, "drawFunction");
        int save = canvas.save();
        canvas.clipPath(this.path);
        drawFunction.invoke(canvas);
        canvas.restoreToCount(save);
    }

    public final void updateSize(int currentWidth, int currentHeight) {
        this.rectF = new RectF(0.0F, 0.0F, currentWidth, currentHeight);
        this.resetPath();
    }

    private final void resetPath() {
        this.path.reset();
        CornerExtensions.addRoundRectWithRoundCorners(
                this.path,
                this.rectF,
                this.getTopLeftCornerRadius(),
                this.getTopRightCornerRadius(),
                this.getBottomRightCornerRadius(),
                this.getBottomLeftCornerRadius());
        this.path.close();
    }

    public CanvasRounder(@NotNull CornersHolder cornersHolder) {
        super();
        Intrinsics.checkParameterIsNotNull(cornersHolder, "cornersHolder");
        this.cornersHolder = cornersHolder;
        this.path = new Path();
        this.rectF = new RectF(0.0F, 0.0F, 0.0F, 0.0F);
    }
}
