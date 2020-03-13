package busstop.customtrip.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class CustomView extends android.support.v7.widget.AppCompatImageView {

    private Paint currentPaint;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setColor(0xFFFFFFFF);  // alpha.r.g.b
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(3.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int level = getBackground().getLevel(); // 10000 - mOpenGraphicPercentage * 100
        int y     = getHeight() - getHeight() * level / 10000;

        Log.d("SDR", "Heigth=" + getHeight() + " Level=" + level);

        canvas.drawLine(0, y, getWidth(), y, currentPaint);
    }
}
