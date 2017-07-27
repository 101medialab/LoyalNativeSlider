package com.hkm.slider;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.hkm.slider.SliderTypes.BaseSliderView;
import com.hkm.slider.Tricks.InfinitePagerAdapter;
import com.hkm.slider.Tricks.InfiniteViewPager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A slider adapter
 */
public class SliderAdapter<T extends BaseSliderView> extends PagerAdapter implements BaseSliderView.ImageLoadListener {
    private static final String TAG = SliderAdapter.class.getSimpleName();

    private SparseArray<Integer> measurement_height = new SparseArray<>();
    private Context mContext;
    private ArrayList<T> mImageContents;
    private int mLoadConfiguration = POSITION_NONE;
    private SliderLayout.OnViewConfigurationDetected mSetViewListener;

    protected OnLoadCompletedListener onLoadCompletedListener = null;
    protected int minHeightRequired = 0;


    public SliderAdapter(Context context) {
        mContext = context;
        mImageContents = new ArrayList<>();
    }

    public void addSlider(T slider) {
        int orderNumber = 0;
        slider.setOnImageLoadListener(this);
        slider.setSlideOrderNumber(orderNumber);
        mImageContents.add(slider);
        orderNumber++;
        notifyDataSetChanged();
    }

    public void loadSliders(List<T> slider) {
        mLoadConfiguration = POSITION_UNCHANGED;
        addSliders(slider);
    }

    public void addSliders(List<T> slider) {
        Iterator<T> it = slider.iterator();
        int orderNumber = 0;
        while (it.hasNext()) {
            T slide = it.next();
            slide.setOnImageLoadListener(this);
            slide.setSlideOrderNumber(orderNumber);
            if (mlayout != null) {
                slide.setSliderContainerInternal(mlayout);
            }
            mImageContents.add(slide);
            orderNumber++;
        }
        notifyDataSetChanged();
    }

    public BaseSliderView getSliderView(int position) {
        if (position < 0 || position >= mImageContents.size()) {
            return null;
        } else {
            return mImageContents.get(position);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return mLoadConfiguration;
    }

    public void removeSlider(BaseSliderView slider) {
        if (mImageContents.contains(slider)) {
            mImageContents.remove(slider);
            notifyDataSetChanged();
        }
    }

    public void removeSliderAt(int position) {
        if (mImageContents.size() < position) {
            mImageContents.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAllSliders() {
        mImageContents.clear();
        notifyDataSetChanged();
    }

    public void setOnLoadCompletedListener (OnLoadCompletedListener onLoadCompletedListener){
        this.onLoadCompletedListener = onLoadCompletedListener;
    }

    public interface OnLoadCompletedListener {
        public void onLoadCompleted(SliderAdapter adapter);
    }

    public int getMinHeightRequired() {
        return minHeightRequired;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
        if (container instanceof InfiniteViewPager) {
            for (BaseSliderView sliderView: mImageContents) {
                if (minHeightRequired < sliderView.getCaptionHeight()) {
                    minHeightRequired = sliderView.getCaptionHeight() + (int)(mContext.getResources().getDimension(R.dimen.caption_text_lower_margin));
                }
            }
        }

        if (onLoadCompletedListener != null) {
            onLoadCompletedListener.onLoadCompleted(this);
        }
    }

    @Override
    public int getCount() {
        return mImageContents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private boolean enable_layout_observer = false;

    public void setOnInitiateViewListener(SliderLayout.OnViewConfigurationDetected object) {
        mSetViewListener = object;
        enable_layout_observer = true;
    }

    private SliderLayout mlayout;

    public void setSliderContainerInternal(SliderLayout ob) {
        mlayout = ob;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        BaseSliderView b = mImageContents.get(position);
        View item = b.getView();
        // collectionConfiguration(item, position);
        container.addView(item);
        return item;
    }

    public void endLayoutObserver() {
        enable_layout_observer = false;
    }

    private void collectionConfiguration(final View layer, final int position) {
        if (mSetViewListener != null && enable_layout_observer) {
            layer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int debug = layer.getHeight();
                    Log.d("checkLayoutSlideHeight", String.format("%d px", debug));
                    measurement_height.append(position, layer.getHeight());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        layer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if (enable_layout_observer) {
                        mSetViewListener.onLayoutGenerated(measurement_height);
                    }
                }
            });
        }
    }

    @Override
    public void onStart(BaseSliderView target) {

    }

    /**
     * When image download error, then remove.
     *
     * @param result bool
     * @param target the based slider target
     */
    @Override
    public void onEnd(boolean result, BaseSliderView target) {
        if (target.isErrorDisappear() == false || result == true) {
            return;
        }
        if (!mRemoveItemOnFailureToLoad) return;
        for (BaseSliderView slider : mImageContents) {
            if (slider.equals(target)) {
                removeSlider(target);
                break;
            }
        }
    }

    private boolean mRemoveItemOnFailureToLoad = true;

    public final void setRemoveItemOnFailureToLoad(boolean b) {
        mRemoveItemOnFailureToLoad = b;
    }

}
