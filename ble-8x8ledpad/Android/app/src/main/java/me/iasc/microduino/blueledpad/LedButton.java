/**
 * Copyright (C) 2015 Iasc CHEN
 * Created on 15/3/16.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.iasc.microduino.blueledpad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class LedButton extends ImageButton {

    private int colorIndex = 0;

    public static final int[] LED_COLORS = {Color.GRAY, Color.RED, Color.YELLOW, Color.GREEN};

    public LedButton(Context context) {
        super(context);
    }

    public LedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw border
        Rect rec = canvas.getClipBounds();
        rec.bottom--;
        rec.right--;

        Paint paint = new Paint();
        paint.setColor(LED_COLORS[colorIndex]);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(rec, paint);
    }
}
