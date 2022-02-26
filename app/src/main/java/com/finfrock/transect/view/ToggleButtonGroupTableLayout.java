package com.finfrock.transect.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.IdRes;

/**
 *
 */
public class ToggleButtonGroupTableLayout extends TableLayout  implements OnClickListener {

    private static final String TAG = "ToggleButtonGroupTableLayout";
    private RadioButton activeRadioButton;
    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener;

    /**
     * @param context
     */
    public ToggleButtonGroupTableLayout(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public ToggleButtonGroupTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if ( activeRadioButton != null ) {
            activeRadioButton.setChecked(false);
        }
        rb.setChecked(true);
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(null, rb.getId());
        }
        activeRadioButton = rb;
    }

    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setChildrenOnClickListener((TableRow)child);
    }

    public void clearCheck() {
        if ( activeRadioButton != null ) {
            activeRadioButton.setChecked(false);
            activeRadioButton = null;
        }
    }

    public void setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public void check(@IdRes int id) {
        if (activeRadioButton != null && activeRadioButton.getId() == id) {
            activeRadioButton.setChecked(true);
            return;
        }
        for (int i = 0; i < getChildCount(); i++){
            TableRow row = (TableRow) this.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++){
                RadioButton rb = (RadioButton) row.getChildAt(j);
                if (rb.getId() == id) {
                    if ( activeRadioButton != null ) {
                        activeRadioButton.setChecked(false);
                    }
                    rb.setChecked(true);
                    activeRadioButton = rb;
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setChildrenOnClickListener((TableRow)child);
    }


    private void setChildrenOnClickListener(TableRow tr) {
        final int c = tr.getChildCount();
        for (int i=0; i < c; i++) {
            final View v = tr.getChildAt(i);
            if ( v instanceof RadioButton ) {
                v.setOnClickListener(this);
            }
        }
    }
}
