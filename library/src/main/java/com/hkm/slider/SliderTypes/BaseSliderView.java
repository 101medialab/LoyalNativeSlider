package com.hkm.slider.SliderTypes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hkm.slider.BuildConfig;
import com.hkm.slider.CapturePhotoUtils;
import com.hkm.slider.LoyalUtil;
import com.hkm.slider.R;
import com.hkm.slider.SliderLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.hkm.slider.SliderTypes.DefaultSliderView} and
 * {@link com.hkm.slider.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {
    private static final String TAG = BaseSliderView.class.getSimpleName();
    private static final int REQUEST_EXTERNAL_STORAGE = 0x30;

    protected Object current_image_holder, current_caption_holder, current_slider_holder;
    protected Context mContext;
    protected boolean imageLoaded = false;
    private RequestCreator rq = null;
    private final Bundle mBundle;
    protected int mTargetWidth, mTargetHeight;
    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mCaption;
    protected String mUrl;
    protected File mFile;
    protected int mRes;
    private int mSlideNumber;
    private int mSlideHeight;
    protected OnSliderClickListener mOnSliderClickListener;
    private TextView mTextView;
    private ImageView mImageView;

    protected boolean mErrorDisappear, mLongClickSaveImage;
    protected boolean mImageLocalStorageEnable;

    protected OnImageSavedListener onImageSavedListener = null;

    private ImageLoadListener mLoadListener;

    private String mDescription;

    private Uri touch_information;
    /**
     * Scale type of the image.
     */
    protected ScaleType mScaleType = ScaleType.Fit;

    protected Typeface mTypeface;

    /**
     * reference of the parent
     */
    protected WeakReference<SliderLayout> sliderContainer;

    public void setSliderContainerInternal(SliderLayout b) {
        this.sliderContainer = new WeakReference<SliderLayout>(b);
    }

    public enum ScaleType {
        CenterCrop, CenterInside, Fit, FitCenterCrop
    }

    protected BaseSliderView(Context context) {
        mContext = context;
        this.mBundle = new Bundle();
        mLongClickSaveImage = false;
        mImageLocalStorageEnable = false;
        mTextView = new TextView(context);
        mImageView = new ImageView(context);
    }

    public final void setSlideOrderNumber(final int order) {
        mSlideNumber = order;
    }

    public final int getSliderOrderNumber() {
        return mSlideNumber;
    }

    /**
     * the placeholder image when loading image from url or file.
     *
     * @param resId Image resource id
     * @return BaseSliderView
     */
    public BaseSliderView empty(int resId) {
        mEmptyPlaceHolderRes = resId;
        return this;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     *
     * @param disappear boolean
     * @return BaseSliderView
     */
    public BaseSliderView errorDisappear(boolean disappear) {
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * if you set errorDisappear false, this will set a error placeholder image.
     *
     * @param resId image resource id
     * @return BaseSliderView
     */
    public BaseSliderView error(int resId) {
        mErrorPlaceHolderRes = resId;
        return this;
    }

    /**
     * the description of a slider image.
     *
     * @param description String
     * @return BaseSliderView
     */
    public BaseSliderView description(String description) {
        mDescription = description;
        return this;
    }

    /**
     * the url of the link or something related when the touch happens.
     *
     * @param info Uri
     * @return BaseSliderView
     */
    public BaseSliderView setUri(Uri info) {
        touch_information = info;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     *
     * @param url String
     * @return BaseSliderView
     */
    public BaseSliderView image(String url) {
        if (mFile != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     *
     * @param file java.io.File
     * @return BaseSliderView
     */
    public BaseSliderView image(File file) {
        if (mUrl != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(@DrawableRes int res) {
        if (mUrl != null || mFile != null) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    public BaseSliderView setCaption(String caption) {
        mCaption = caption;
        return this;
    }

    public BaseSliderView descriptionTypeface(Typeface typeface) {
        mTypeface = typeface;
        return this;
    }

    public String getCaption() {
        return mCaption;
    }

    /**
     * get the url of the image path
     *
     * @return the path in string
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * get the url from the touch event
     *
     * @return the touch event URI
     */
    public Uri getTouchURI() {
        return touch_information;
    }

    public boolean isErrorDisappear() {
        return mErrorDisappear;
    }

    public int getEmpty() {
        return mEmptyPlaceHolderRes;
    }

    public int getError() {
        return mErrorPlaceHolderRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Context getContext() {
        return mContext;
    }

    public OnImageSavedListener getOnImageSavedListener() {
        return onImageSavedListener;
    }

    public void setOnImageSavedListener(OnImageSavedListener onImageSavedListener) {
        this.onImageSavedListener = onImageSavedListener;
    }

    /**
     * set a slider image click listener
     *
     * @param l the listener
     * @return the base slider
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    protected View.OnLongClickListener mDefaultLongClickListener = null;
    protected WeakReference<FragmentManager> fmg;

    /**
     * to enable the slider for saving images
     *
     * @param mfmg FragmentManager
     * @return this thing
     */
    public BaseSliderView enableSaveImageByLongClick(FragmentManager mfmg) {
        mLongClickSaveImage = true;
//        mDefaultLongClickListener = null;
        this.fmg = new WeakReference<FragmentManager>(mfmg);
        return this;
    }

    public View.OnLongClickListener getLongClickListener() {
        return mDefaultLongClickListener;
    }

    /**
     * to set custom listener for long click event
     *
     * @param listen the listener
     * @return thos thomg
     */
    public BaseSliderView setSliderLongClickListener(View.OnLongClickListener listen) {
        mDefaultLongClickListener = listen;
        mLongClickSaveImage = false;
        return this;
    }

    public BaseSliderView setSliderLongClickListener(View.OnLongClickListener listen, FragmentManager mfmg) {
        mDefaultLongClickListener = listen;
        mLongClickSaveImage = false;
        this.fmg = new WeakReference<FragmentManager>(mfmg);
        return this;
    }

    public BaseSliderView enableImageLocalStorage() {
        mImageLocalStorageEnable = true;
        return this;
    }

    protected void hideLoadingProgress(View mView) {
        if (mView.findViewById(R.id.ns_loading_progress) != null) {
            hideoutView(mView.findViewById(R.id.ns_loading_progress));
        }
    }

    /**
     * when {@link #mLongClickSaveImage} is true and this function will be triggered to watch the long action run
     *
     * @param mView the slider view object
     */
    protected void triggerOnLongClick(View mView) {
        if (mLongClickSaveImage && fmg != null) {
            if (mDefaultLongClickListener != null) {
                mView.setOnLongClickListener(mDefaultLongClickListener);
            }
        }
    }

    private final View.OnClickListener click_triggered = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnSliderClickListener != null) {
                mOnSliderClickListener.onSliderClick(BaseSliderView.this);
            }
        }
    };

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     *
     * @param v               the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShowPicasso(final View v, final ImageView targetImageView) {
        current_image_holder = targetImageView;
        v.setOnClickListener(click_triggered);
        mLoadListener.onStart(this);
        final Picasso p = Picasso.with(mContext);
        rq = null;
        if (mUrl != null) {
            rq = p.load(mUrl);
        } else if (mFile != null) {
            rq = p.load(mFile);
        } else if (mRes != 0) {
            rq = p.load(mRes);
        } else {
            return;
        }
        if (rq == null) {
            return;
        }
        if (getEmpty() != 0) {
            rq.placeholder(getEmpty());
        }
        if (getError() != 0) {
            rq.error(getError());
        }
        if (mTargetWidth > 0 || mTargetHeight > 0) {
            rq.resize(mTargetWidth, mTargetHeight);
        }
        if (mImageLocalStorageEnable) {
            rq.memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE);
        }

        switch (mScaleType) {
            case Fit:
                rq.fit();
                break;
            case CenterCrop:
                rq.fit().centerCrop();
                break;
            case CenterInside:
                rq.fit().centerInside();
                break;
        }

        rq.into(targetImageView, new Callback() {
            @Override
            public void onSuccess() {
                imageLoaded = true;
                hideLoadingProgress(v);
                triggerOnLongClick(v);
                reportStatusEnd(true);
            }

            @Override
            public void onError() {
                reportStatusEnd(false);
            }
        });
    }

    protected void setupDescription(TextView descTextView) {
        descTextView.setText(mDescription);
        if (mTypeface != null) {
            descTextView.setTypeface(mTypeface);
        }
    }

    protected void setImageCaption(final TextView captionTextView) {
        mTextView = captionTextView;
        mTextView.setText(getCaption());
        mTextView.setVisibility(View.INVISIBLE);
    }

    protected void applyImageWithSmartBothAndNotifyHeight(final View v, final ImageView target, final TextView captionTextView) {
        current_image_holder = target;
        current_caption_holder = captionTextView;
        current_slider_holder = v;
        mImageView = target;
        LoyalUtil.hybridImplementation(getUrl(), target, getContext(), new Runnable() {
            @Override
            public void run() {
                imageLoaded = true;
                if (sliderContainer == null) return;
                if (sliderContainer.get().getCurrentPosition() == getSliderOrderNumber()) {
                    sliderContainer.get().setFitToCurrentImageHeight();
                }

            }
        });
        hideLoadingProgress(v);
        triggerOnLongClick(v);
        reportStatusEnd(true);
    }


    public TextView getCaptionTextView() {
        return mTextView;
    }

    public ImageView getLoadedImageView() {
        return mImageView;
    }

    protected void reportStatusEnd(boolean b) {
        if (mLoadListener != null) {
            mLoadListener.onEnd(b, this);
        }
    }

    final android.os.Handler nh = new android.os.Handler();

    protected void workAroundGetImagePicasso() {

        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }

    protected void workGetImage(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
        imageView.buildDrawingCache(true);
        output_bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
        imageView.setDrawingCacheEnabled(false);
    }

    private Bitmap output_bitmap = null;

    private class getImageTask extends AsyncTask<Void, Void, Integer> {
        private ImageView imageView;

        public getImageTask(ImageView taskTarget) {
            imageView = taskTarget;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int tried = 0;
            while (tried < 5) {
                try {
                    workGetImage(imageView);
                    return 1;
                } catch (Exception e) {
                    Log.e(TAG, "failed to retrieve image", e);
                    tried++;
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                CapturePhotoUtils.insertImage(mContext, output_bitmap, mDescription, new CapturePhotoUtils.Callback() {
                            @Override
                            public void complete() {
                                nh.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (onImageSavedListener != null) {
                                            onImageSavedListener.onImageSaved(mDescription);
                                        }
                                   }
                                });
                            }
                        }
                );
            } else {
                if (onImageSavedListener != null) {
                    onImageSavedListener.onImageSaveFailed();
                }
            }
        }
    }

    @SuppressLint("ValidFragment")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SaveImageDialog extends DialogFragment {
        protected Context mContext = null;
        protected int mTheme;

        protected AlertDialog.OnClickListener onPositiveButtonListener = null;
        protected AlertDialog.OnClickListener onNegativeButtonListener = null;

        public SaveImageDialog(Context context, int theme) {
            super();
            mContext = context;
            mTheme = theme;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (mContext == null) return null;
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, mTheme);
            builder.setMessage(R.string.save_image)
                    .setPositiveButton(R.string.yes_save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (onPositiveButtonListener != null) {
                                onPositiveButtonListener.onClick(dialog, id);
                            }
                        }
                    })
                    .setNegativeButton(R.string.no_keep, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (onNegativeButtonListener != null) {
                                onNegativeButtonListener.onClick(dialog, id);
                            }
                        }
                    });
            Dialog dialog = builder.create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            // Create the AlertDialog object and return it
            return dialog;
        }

        public void setOnPositiveButtonListener(AlertDialog.OnClickListener onPositiveButtonListener) {
            this.onPositiveButtonListener = onPositiveButtonListener;
        }

        public void setOnNegativeButtonListener(AlertDialog.OnClickListener onNegativeButtonListener) {
            this.onNegativeButtonListener = onNegativeButtonListener;
        }
    }

    protected void saveImageActionTrigger() {
        if (current_image_holder == null) return;
        if (current_image_holder instanceof ImageView) {
            ImageView fast = (ImageView) current_image_holder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "require permission to write external storage; requesting...");
                    }
                    ActivityCompat.requestPermissions(
                            (Activity)mContext,
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_EXTERNAL_STORAGE
                    );
                } else {
                    getImageTask t = new getImageTask(fast);
                    t.execute();
                }
            } else {
                getImageTask t = new getImageTask(fast);
                t.execute();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    final protected void hideoutView(@Nullable final View view) {
        if (view == null) return;
        view.animate().alpha(0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    public BaseSliderView setScaleType(ScaleType type) {
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     *
     * @return View
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     *
     * @param l ImageLoadListener
     */
    public void setOnImageLoadListener(ImageLoadListener l) {
        mLoadListener = l;
    }

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView coreSlider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     *
     * @return Bundle
     */
    public Bundle getBundle() {
        return mBundle;
    }

    public interface ImageLoadListener {
        void onStart(BaseSliderView target);

        void onEnd(boolean result, BaseSliderView target);
    }

    public int getCaptionHeight() {
        int textWidth = mTextView.getWidth();
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(textWidth,
                View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        if (textWidth < 1) {
            return 0;
        }

        mTextView.measure(widthMeasureSpec, heightMeasureSpec);
        return mTextView.getMeasuredHeight();

    }

    public Object getImageView() {
        return current_image_holder;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public Object getSliderView() {
        return current_slider_holder;
    }

    public interface OnImageSavedListener {
        void onImageSaved(String description);
        void onImageSaveFailed();
    }
}
