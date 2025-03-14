package com.gtel.ekyc.roundcornerlayout;

import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public final class CornerExtensions {

    public static final void updateOutlineProvider(
            @NotNull View $receiver, @NotNull CornersHolder cornersHolder) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
        Intrinsics.checkParameterIsNotNull(cornersHolder, "cornersHolder");
        $receiver.setOutlineProvider((ViewOutlineProvider) (new RoundOutlineProvider(cornersHolder)));
    }

    public static final void updateOutlineProvider(@NotNull View $receiver, float cornerRadius) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
        $receiver.setOutlineProvider((ViewOutlineProvider) (new RoundOutlineProvider(cornerRadius)));
    }

    public static final void addRoundRectWithRoundCorners(
            @NotNull Path $receiver, @NotNull RectF rectF, @NotNull CornersHolder cornersHolder) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
        Intrinsics.checkParameterIsNotNull(rectF, "rectF");
        Intrinsics.checkParameterIsNotNull(cornersHolder, "cornersHolder");
        addRoundRectWithRoundCorners(
                $receiver,
                rectF,
                cornersHolder.getTopLeftCornerRadius(),
                cornersHolder.getTopRightCornerRadius(),
                cornersHolder.getBottomRightCornerRadius(),
                cornersHolder.getBottomLeftCornerRadius());
    }

    public static void addRoundRectWithRoundCorners(
            @NotNull Path $receiver,
            @NotNull RectF rectF,
            float topLeftCornerRadius,
            float topRightCornerRadius,
            float bottomRightCornerRadius,
            float bottomLeftCornerRadius) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
        Intrinsics.checkParameterIsNotNull(rectF, "rectF");
        $receiver.addRoundRect(
                rectF,
                new float[] {
                    topLeftCornerRadius,
                    topLeftCornerRadius,
                    topRightCornerRadius,
                    topRightCornerRadius,
                    bottomRightCornerRadius,
                    bottomRightCornerRadius,
                    bottomLeftCornerRadius,
                    bottomLeftCornerRadius
                },
                Path.Direction.CW);
    }

    @NotNull
    public static CornersHolder getCornerRadius(
            @NotNull TypedArray $receiver,
            int attrCornerRadius,
            int attrTopLeftCornerRadius,
            int attrTopRightCornerRadius,
            int attrBottomRightCornerRadius,
            int attrBottomLeftCornerRadius) {
        Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
        float cornerRadius = $receiver.getDimension(attrCornerRadius, 0.0F);
        float topLeftCornerRadius = $receiver.getDimension(attrTopLeftCornerRadius, cornerRadius);
        float topRightCornerRadius = $receiver.getDimension(attrTopRightCornerRadius, cornerRadius);
        float bottomRightCornerRadius =
                $receiver.getDimension(attrBottomRightCornerRadius, cornerRadius);
        float bottomLeftCornerRadius = $receiver.getDimension(attrBottomLeftCornerRadius, cornerRadius);
        return new CornersHolder(
                topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius);
    }
}
