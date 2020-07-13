package cn.carbs.wricheditor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import cn.carbs.wricheditor.library.views.RichImageView;

public class MyRichImageView extends RichImageView {

    public MyRichImageView(Context context) {
        super(context);
    }

    public MyRichImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRichImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageUrl(String imageUrl) {

        ImageView imageView = getImageView();
        imageView.setImageResource(R.drawable.image_picture);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(null)
                .error(null)
                .centerInside();
        Glide
                .with(getContext())
                .load(R.drawable.image_picture)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        setImageWidthAndHeight(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                        resource.getIntrinsicHeight();
                        return false;
                    }
                })
                .into(imageView);
    }

}
