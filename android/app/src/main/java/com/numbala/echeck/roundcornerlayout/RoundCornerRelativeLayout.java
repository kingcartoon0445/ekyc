/* (C) 2020 */
package com.gtel.ekyc.roundcornerlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


import com.gtel.ekyc.R;

import org.jetbrains.annotations.NotNull;


import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public final class RoundCornerRelativeLayout extends RelativeLayout {

    private final CanvasRounder canvasRounder;

    protected void onSizeChanged(int currentWidth, int currentHeight, int oldWidth, int oldheight) {
        super.onSizeChanged(currentWidth, currentHeight, oldWidth, oldheight);
        this.canvasRounder.updateSize(currentWidth, currentHeight);
    }

    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        this.canvasRounder.round(
                canvas,
                (Function1)
                        (new Function1() {
                            // $FF: synthetic method
                            // $FF: bridge method
                            public Object invoke(Object var1) {
                                this.invoke((Canvas) var1);
                                return Unit.INSTANCE;
                            }

                            public final void invoke(@NotNull Canvas it) {
                                Intrinsics.checkParameterIsNotNull(it, "it");
                                RoundCornerRelativeLayout.super.draw(it);
                            }
                        }));
    }

    protected void dispatchDraw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        this.canvasRounder.round(
                canvas,
                (Function1)
                        (new Function1() {
                            // $FF: synthetic method
                            // $FF: bridge method
                            public Object invoke(Object var1) {
                                this.invoke((Canvas) var1);
                                return Unit.INSTANCE;
                            }

                            public final void invoke(@NotNull Canvas it) {
                                Intrinsics.checkParameterIsNotNull(it, "it");
                                RoundCornerRelativeLayout.super.dispatchDraw(it);
                            }
                        }));
    }

    public final void setCornerRadius(float cornerRadius, @NotNull CornerType cornerType) {
        this.canvasRounder.setCornerRadius(cornerRadius);
        // $FF: Couldn't be decompiled
    }

    // $FF: synthetic method
    // $FF: bridge method
    public static void setCornerRadius$default(
            RoundCornerRelativeLayout var0, float var1, CornerType var2, int var3, Object var4) {
        if ((var3 & 2) != 0) {
            var2 = CornerType.ALL;
        }

        var0.setCornerRadius(var1, var2);
    }

    public RoundCornerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        Intrinsics.checkParameterIsNotNull(context, "context");
        TypedArray array =
                context.obtainStyledAttributes(attrs, R.styleable.RoundCornerRelativeLayout, 0, 0);
        Intrinsics.checkExpressionValueIsNotNull(array, "array");
        CornersHolder cornersHolder =
                CornerExtensions.getCornerRadius(
                        array,
                        R.styleable.RoundCornerRelativeLayout_corner_radius,
                        R.styleable.RoundCornerRelativeLayout_top_left_corner_radius,
                        R.styleable.RoundCornerRelativeLayout_top_right_corner_radius,
                        R.styleable.RoundCornerRelativeLayout_bottom_right_corner_radius,
                        R.styleable.RoundCornerRelativeLayout_bottom_left_corner_radius);
        array.recycle();
        this.canvasRounder = new CanvasRounder(cornersHolder);
        CornerExtensions.updateOutlineProvider(this, cornersHolder);
        if (Build.VERSION.SDK_INT < 18) {
            this.setLayerType(LAYER_TYPE_SOFTWARE, (Paint) null);
        }
    }
}
