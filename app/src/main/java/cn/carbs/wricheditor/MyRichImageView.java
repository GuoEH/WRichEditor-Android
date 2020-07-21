package cn.carbs.wricheditor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import cn.carbs.wricheditor.library.models.cell.ImageCellData;
import cn.carbs.wricheditor.library.views.RichImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

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

    @Override
    public void setSelectMode(boolean selectMode) {
        super.setSelectMode(selectMode);

        if (mImageViewCover == null) {
            return;
        }
        if (selectMode) {
            // 模糊逐渐显现
            if (mImageViewCover.getVisibility() == View.VISIBLE) {
                return;
            }
            mImageViewCover.setAlpha(0f);
            mImageViewCover.setVisibility(View.VISIBLE);

            if (getCellData() != null && !TextUtils.isEmpty(getCellData().imageNetUrl)) {
                Glide
                        .with(this)
                        .load(getCellData().imageNetUrl)
                        .transform(new BlurTransformation(25, 5))
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                if (mImageViewCover != null) {
//                                    mImageViewCover.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    mImageViewCover.setImageDrawable(resource);
                                    toShowDimEffect();
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
        } else {
            // 模糊逐渐消失
            if (mImageViewCover.getVisibility() != View.VISIBLE) {
                return;
            }
            mImageViewCover.setAlpha(1f);
            mImageViewCover.setVisibility(View.VISIBLE);
            toHideDimEffect();
        }
    }

    @Override
    public void setCellData(ImageCellData cellData) {
        super.setCellData(cellData);
        if (!mDataLoaded && getCellData() != null && getImageView() != null) {
            loadImageUrl(getCellData().imageNetUrl);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mDataLoaded && getCellData() != null && getImageView() != null) {
            loadImageUrl(getCellData().imageNetUrl);
        }
    }

    private void toShowDimEffect() {
        if (mImageViewCover == null) {
            return;
        }
        ObjectAnimator translationAnimator =
                ObjectAnimator.ofFloat(mImageViewCover, "alpha", 0f, 1f);
        translationAnimator.setDuration(200);
        translationAnimator.start();
    }

    private void toHideDimEffect() {
        if (mImageViewCover == null) {
            return;
        }
        ObjectAnimator translationAnimator =
                ObjectAnimator.ofFloat(mImageViewCover, "alpha", 1f, 0f);
        translationAnimator.setDuration(200);
        translationAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mImageViewCover != null) {
                    mImageViewCover.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        translationAnimator.start();
    }

    protected void loadImageUrl(String imageUrl) {
        mDataLoaded = true;
        Glide
                .with(getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mDataLoaded = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mDataLoaded = true;
                        setImageWidthAndHeight(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                        resource.getIntrinsicHeight();
                        return false;
                    }
                })
                .into(getImageView());
    }


}
