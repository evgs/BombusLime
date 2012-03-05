package org.bombusim.lime.widgets;

import org.bombusim.lime.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class OkCancelBar extends LinearLayout{

    Button mButtonOk;
    Button mButtonCancel;
    
    private OnButtonActionListener mOnButtonAction;
    
    public interface OnButtonActionListener {
        public void onPositive();
        public void onNegative();
    }
    
    OnClickListener mOnClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (mOnButtonAction == null) return;
            
            if (v == mButtonOk)     { mOnButtonAction.onPositive();  } 
            
            if (v == mButtonCancel) { mOnButtonAction.onNegative(); }
            
        }
    };
    public OkCancelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.ok_cancel_bar, this);
        
        mButtonOk = (Button) findViewById(R.id.buttonOk);
        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        
        mButtonOk.setOnClickListener(mOnClickListener);
        mButtonCancel.setOnClickListener(mOnClickListener);
        
        TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.OkCancelBar);

        mButtonOk.setText(a.getString(R.styleable.OkCancelBar_positive));
        mButtonCancel.setText(a.getString(R.styleable.OkCancelBar_negative));
    }

    public void setOnButtonActionListener(OnButtonActionListener listener) { this.mOnButtonAction = listener; }

    public void setPositiveButtonText(int resId) { mButtonOk.setText(resId); }
    public void setPositiveButtonText(CharSequence text) { mButtonOk.setText(text); }
    
}
