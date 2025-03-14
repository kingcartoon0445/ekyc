package com.gtel.ekyc.roundcornerlayout;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CornersHolder {

    private float topLeftCornerRadius;
    private float topRightCornerRadius;
    private float bottomRightCornerRadius;
    private float bottomLeftCornerRadius;

    public final float getTopLeftCornerRadius() {
        return this.topLeftCornerRadius;
    }

    public final void setTopLeftCornerRadius(float var1) {
        this.topLeftCornerRadius = var1;
    }

    public final float getTopRightCornerRadius() {
        return this.topRightCornerRadius;
    }

    public final void setTopRightCornerRadius(float var1) {
        this.topRightCornerRadius = var1;
    }

    public final float getBottomRightCornerRadius() {
        return this.bottomRightCornerRadius;
    }

    public final void setBottomRightCornerRadius(float var1) {
        this.bottomRightCornerRadius = var1;
    }

    public final float getBottomLeftCornerRadius() {
        return this.bottomLeftCornerRadius;
    }

    public final void setBottomLeftCornerRadius(float var1) {
        this.bottomLeftCornerRadius = var1;
    }

    public CornersHolder(
            float topLeftCornerRadius,
            float topRightCornerRadius,
            float bottomRightCornerRadius,
            float bottomLeftCornerRadius) {
        this.topLeftCornerRadius = topLeftCornerRadius;
        this.topRightCornerRadius = topRightCornerRadius;
        this.bottomRightCornerRadius = bottomRightCornerRadius;
        this.bottomLeftCornerRadius = bottomLeftCornerRadius;
    }

    public final float component1() {
        return this.topLeftCornerRadius;
    }

    public final float component2() {
        return this.topRightCornerRadius;
    }

    public final float component3() {
        return this.bottomRightCornerRadius;
    }

    public final float component4() {
        return this.bottomLeftCornerRadius;
    }

    @NotNull
    public final CornersHolder copy(
            float topLeftCornerRadius,
            float topRightCornerRadius,
            float bottomRightCornerRadius,
            float bottomLeftCornerRadius) {
        return new CornersHolder(
                topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius);
    }

    // $FF: synthetic method
    // $FF: bridge method
    @NotNull
    public static CornersHolder copy$default(
            @NonNull CornersHolder var0,
            float var1,
            float var2,
            float var3,
            float var4,
            int var5,
            Object var6) {
        if ((var5 & 1) != 0) {
            var1 = var0.topLeftCornerRadius;
        }

        if ((var5 & 2) != 0) {
            var2 = var0.topRightCornerRadius;
        }

        if ((var5 & 4) != 0) {
            var3 = var0.bottomRightCornerRadius;
        }

        if ((var5 & 8) != 0) {
            var4 = var0.bottomLeftCornerRadius;
        }

        return var0.copy(var1, var2, var3, var4);
    }

    @NotNull
    public String toString() {
        return "CornersHolder(topLeftCornerRadius="
                + this.topLeftCornerRadius
                + ", topRightCornerRadius="
                + this.topRightCornerRadius
                + ", bottomRightCornerRadius="
                + this.bottomRightCornerRadius
                + ", bottomLeftCornerRadius="
                + this.bottomLeftCornerRadius
                + ")";
    }

    public int hashCode() {
        return ((Float.floatToIntBits(this.topLeftCornerRadius) * 31
                                                + Float.floatToIntBits(this.topRightCornerRadius))
                                        * 31
                                + Float.floatToIntBits(this.bottomRightCornerRadius))
                        * 31
                + Float.floatToIntBits(this.bottomLeftCornerRadius);
    }

    public boolean equals(@Nullable Object var1) {
        if (this != var1) {
            if (var1 instanceof CornersHolder) {
                CornersHolder var2 = (CornersHolder) var1;
                return Float.compare(this.topLeftCornerRadius, var2.topLeftCornerRadius) == 0
                        && Float.compare(this.topRightCornerRadius, var2.topRightCornerRadius) == 0
                        && Float.compare(this.bottomRightCornerRadius, var2.bottomRightCornerRadius) == 0
                        && Float.compare(this.bottomLeftCornerRadius, var2.bottomLeftCornerRadius) == 0;
            }

            return false;
        } else {
            return true;
        }
    }
}
