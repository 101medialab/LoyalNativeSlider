package com.hkm.slider.TouchView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import java.io.File;
import java.io.FileInputStream;

public class FileTouchImageView extends UrlTouchImageView 
{
	
    public FileTouchImageView(Context ctx)
    {
        super(ctx);

    }
    public FileTouchImageView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public void setUrl(String imagePath) {
    }
}
