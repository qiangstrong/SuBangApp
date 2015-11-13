package com.subang.applib.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.subang.applib.util.ComUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/11/12.
 */
public class WheelView extends ScrollView {

    private static final int DELAY_CHECK = 50;  //滚动检查的间隔，毫秒

    private Context context;
    private LinearLayout rootView;
    private int offset;                         // 偏移量（需要在最前面和最后面补全）
    private Paint paint;
    private OnItemSelectedListener onItemSelectedListener;

    //视图相关
    private List<String> items;
    private int itemCount;                      // 每页显示的数量
    private int itemHeight;
    private int viewWidth;

    //选择相关
    private int selectedIndex;
    private int checkOldy;

    //从xml文件中获取的属性
    private int attr_lineColor, attr_textSelectedColor, attr_textColor;
    private int attr_textSize, attr_textPadding;

    public WheelView(Context context) {
        super(context);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        TypedArray wheelViewAttrs = context.obtainStyledAttributes(attrs,
                R.styleable.WheelView);
        initAttrs(wheelViewAttrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        TypedArray wheelViewAttrs = context.obtainStyledAttributes(attrs,
                R.styleable.WheelView);
        initAttrs(wheelViewAttrs);
    }

    private void initAttrs(TypedArray attrs) {
        offset = attrs.getInteger(R.styleable.WheelView_offset, 3);
        //attr_lineColor=attrs.getColor(R.styleable.WheelView_lineColor, Color.parseColor("#83cde6"));
        attr_textSelectedColor = attrs.getColor(R.styleable.WheelView_textSelectedColor, 0xff0288ce);
        attr_textColor = attrs.getColor(R.styleable.WheelView_textColor, 0xffbbbbbb);
        attr_textSize = attrs.getDimensionPixelOffset(R.styleable.WheelView_textSize, 30);
        attr_textPadding = attrs.getDimensionPixelOffset(R.styleable.WheelView_textPadding, ComUtil.dp2px(context, 10));
    }

    private void init(Context context) {
        setVerticalScrollBarEnabled(false);
        this.context = context;
        rootView = new LinearLayout(context);
        rootView.setOrientation(LinearLayout.VERTICAL);
        addView(rootView);
        offset = 1;                                 //默认偏移量
        paint = new Paint();
        paint.setColor(Color.parseColor("#83cde6"));
        paint.setStrokeWidth(ComUtil.dp2px(context, 1f));
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public OnItemSelectedListener getOnItemSelectedListener() {
        return onItemSelectedListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        onItemSelected();
        this.post(new Runnable() {
            @Override
            public void run() {
                smoothScrollTo(0, WheelView.this.selectedIndex * itemHeight);
            }
        });
    }

    //加载数据，刷新View
    //items至少有一个元素
    public void setItems(List<String> items) {
        if (this.items == null) {
            this.items = new ArrayList<String>();
        }
        this.items.clear();
        this.items.addAll(items);

        // 前面和后面补全
        for (int i = 0; i < offset; i++) {
            this.items.add(0, "");
            this.items.add("");
        }
        refreshView();
    }

    private void refreshView() {
        TextView textView = null;
        rootView.removeAllViews();
        for (String item : items) {
            textView = createItemView(item);
            rootView.addView(textView);
        }

        itemCount = offset * 2 + 1;
        itemHeight = ComUtil.getViewMeasuredHeight(textView);
        rootView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * itemCount));
        setLayoutParams(new LinearLayout.LayoutParams(getLayoutParams().width, itemHeight * itemCount));

        setSelectedIndex(0);
        selectedItemView(0);
    }

    private TextView createItemView(String item) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setSingleLine(true);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, attr_textSize);
        textView.setText(item);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(attr_textPadding, attr_textPadding, attr_textPadding, attr_textPadding);
        return textView;
    }

    //根据坐标，改变选中item的颜色
    private void selectedItemView(int y) {
        int position = 0;
        int remainder = y % itemHeight;
        int quotient = y / itemHeight;

        if (remainder <= itemHeight / 2) {
            position = quotient + offset;
        } else {
            if (remainder > itemHeight / 2) {
                position = quotient + offset + 1;
            }
        }

        int childSize = rootView.getChildCount();               //与itemCount有区别吗？
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) rootView.getChildAt(i);
            if (itemView == null) {
                return;
            }
            if (position == i) {
                itemView.setTextColor(attr_textSelectedColor);
            } else {
                itemView.setTextColor(attr_textColor);
            }
        }
    }


    //边界相关
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        if (viewWidth == 0) {
            viewWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
        }

        Drawable background = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int y1 = itemHeight * offset;
                int y2 = itemHeight * (offset + 1);
                canvas.drawLine(viewWidth * 1 / 6, y1, viewWidth * 5 / 6, y1, paint);
                canvas.drawLine(viewWidth * 1 / 6, y2, viewWidth * 5 / 6, y2, paint);
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter cf) {
            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
        super.setBackgroundDrawable(background);
    }

    //选择相关
    private Runnable scrollRunnable = new Runnable() {

        public void run() {

            final int newy = getScrollY();
            if (checkOldy - newy == 0) {                            //滚动停止
                final int remainder = newy % itemHeight;
                final int quotient = newy / itemHeight;

                if (remainder == 0) {
                    selectedIndex = quotient;
                    onItemSelected();
                } else {
                    if (remainder > itemHeight / 2) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                smoothScrollTo(0, newy - remainder + itemHeight);
                                selectedIndex = quotient + 1;
                                onItemSelected();
                            }
                        });
                    } else {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                smoothScrollTo(0, newy - remainder);
                                selectedIndex = quotient;
                                onItemSelected();
                            }
                        });
                    }
                }
            } else {
                checkOldy = getScrollY();
                postDelayed(scrollRunnable, DELAY_CHECK);
            }
        }
    };

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        selectedItemView(t);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            checkOldy = getScrollY();
            postDelayed(scrollRunnable, DELAY_CHECK);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    private void onItemSelected() {
        if (onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(selectedIndex);
        }
    }

    //接口
    public interface OnItemSelectedListener {
        void onItemSelected(int selectedIndex);
    }
}