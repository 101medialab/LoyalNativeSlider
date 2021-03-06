package com.hkm.slider.SliderTypes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hkm.slider.R;

/**
 * Created by hesk on 18/4/16.
 */
public class AdjustableSlide extends DefaultSliderView {
    public AdjustableSlide(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.render_type_adjustable, null);
        ImageView target = (ImageView) v.findViewById(R.id.ns_slider_image);
        TextView imageCaption = (TextView) v.findViewById(R.id.ns_slider_image_caption);

        setImageCaption(imageCaption);
        applyImageWithSmartBothAndNotifyHeight(v, target, imageCaption);
        return v;
    }
}
