package org.rems.rsdkv5;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class TouchControlsView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 4;
    private static final int RIGHT = 8;
    private static final int BUTTON_A = 16;
    private static final int BUTTON_B = 32;

    private int touchMask = 0;
    private boolean tracking = false;

    public TouchControlsView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private float dpadX() { return getWidth() * 0.16f; }
    private float dpadY() { return getHeight() * 0.78f; }
    private float dpadR() {
        return Math.min(getWidth() * 0.115f, getHeight() * 0.19f);
    }
    private float buttonR() {
        return Math.min(getWidth() * 0.055f, getHeight() * 0.095f);
    }

    private void circle(Canvas c, float x, float y, float r, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(x, y, r, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = dpadX();
        float y = dpadY();
        float r = dpadR();

        int outer = Color.argb(105, 225, 235, 245);
        int inner = Color.argb(145, 45, 55, 75);
        int symbol = Color.argb(210, 255, 255, 255);

        circle(canvas, x, y, r, outer);
        circle(canvas, x, y, r * 0.36f, inner);

        paint.setColor(symbol);
        paint.setStyle(Paint.Style.FILL);

        float a = r * 0.28f;
        float b = r * 0.62f;

        canvas.drawRect(x - a, y - b, x + a, y - a, paint);
        canvas.drawRect(x - a, y + a, x + a, y + b, paint);
        canvas.drawRect(x - b, y - a, x - a, y + a, paint);
        canvas.drawRect(x + a, y - a, x + b, y + a, paint);

        float br = buttonR();
        float by = getHeight() * 0.79f;
        float bxA = getWidth() * 0.78f;
        float bxB = getWidth() * 0.90f;

        circle(canvas, bxA, by, br, outer);
        circle(canvas, bxB, by - br * 0.55f, br, outer);

        paint.setColor(symbol);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(br * 0.85f);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawText("B", bxA, by + br * 0.30f, paint);
        canvas.drawText("A", bxB, by - br * 0.25f, paint);
    }

    private int buttonAt(float px, float py) {
        float x = dpadX();
        float y = dpadY();
        float r = dpadR();
        float dx = px - x;
        float dy = py - y;

        if (dx * dx + dy * dy <= r * r * 1.25f) {
            if (Math.abs(dx) > Math.abs(dy))
                return dx < 0 ? LEFT : RIGHT;
            return dy < 0 ? UP : DOWN;
        }

        float br = buttonR();
        float by = getHeight() * 0.79f;
        float bxA = getWidth() * 0.78f;
        float bxB = getWidth() * 0.90f;
        float ayB = by - br * 0.55f;

        if (Math.hypot(px - bxA, py - by) <= br * 1.35f)
            return BUTTON_B;
        if (Math.hypot(px - bxB, py - ayB) <= br * 1.35f)
            return BUTTON_A;

        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN) {
            tracking = buttonAt(event.getX(index), event.getY(index)) != 0;
            if (!tracking)
                return false;
        }

        if (!tracking)
            return false;

        int mask = 0;

        for (int i = 0; i < event.getPointerCount(); i++) {
            if ((action == MotionEvent.ACTION_UP ||
                 action == MotionEvent.ACTION_POINTER_UP ||
                 action == MotionEvent.ACTION_CANCEL) && i == index)
                continue;

            mask |= buttonAt(event.getX(i), event.getY(i));
        }

        touchMask = mask;
        RSDK.nativeSetTouchButtons(touchMask);
        invalidate();

        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL) {
            tracking = false;
            touchMask = 0;
            RSDK.nativeSetTouchButtons(0);
        }

        return true;
    }
}
