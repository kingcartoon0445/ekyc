package com.gtel.ekyc.roundcornerlayout;

import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public final class RoundOutlineProvider extends ViewOutlineProvider {

    @NonNull private final CornersHolder cornersHolder;

    public void getOutline(@NotNull View view, @NotNull Outline outline) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(outline, "outline");
        RectF rectF =
                new RectF(0.0F, 0.0F, (float) view.getMeasuredWidth(), (float) view.getMeasuredHeight());
        Path var5 = new Path();
        CornerExtensions.addRoundRectWithRoundCorners(var5, rectF, this.cornersHolder);
        var5.close();
        outline.setConvexPath(var5);
    }

    public RoundOutlineProvider(@NotNull CornersHolder cornersHolder) {
        super();
        Intrinsics.checkParameterIsNotNull(cornersHolder, "cornersHolder");
        this.cornersHolder = cornersHolder;
    }

    public RoundOutlineProvider(float cornerRadius) {
        this(new CornersHolder(cornerRadius, cornerRadius, cornerRadius, cornerRadius));
    }
}
