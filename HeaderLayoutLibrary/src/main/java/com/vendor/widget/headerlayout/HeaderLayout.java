package com.vendor.widget.headerlayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 实现头部布局
 * Created by ljfan on 2016-11-15.
 */
public class HeaderLayout extends RelativeLayout {

    private static final ImageView.ScaleType[] sScaleTypeArray = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };

    private enum MenuAlign {

        /** 文字在最右边 */
        ALIGN_TEXT(0),

        /** 图标在最右边 */
        ALIGN_ICON(1),

        /** 文字、按钮交替 */
        ALTERNATE(2),

        /** 文字、按钮交替 */
        ALTERNATE2(3);

        int type;

        MenuAlign(int type) {
            this.type = type;
        }

        public static MenuAlign getType(int type) {
            for (MenuAlign align : values()) {
                if (type == align.type) {
                    return align;
                }
            }

            return ALIGN_TEXT;
        }
    }

    private boolean isSupportTranslucentStatus;  //是否支持状态栏沉浸

    private int mSpitLineColor;
    private float mSpitLineHeight;

    private ImageView mIvStatusPadding;
    private TextView mTvTitle;

    private int mHedaderLayoutHeight = 0;

    private ColorStateList mTitleTextColor;
    private float mTitleTextSize;
    private boolean mTitleAlignLeft;

    private View mNavigationView;
    private float mNavigationWidth;
    private float mNavigationMinWidth;
    private float mNavigationMaxWidth;
    private float mNavigationHeight;
    private float mNavigationMinHeight;
    private float mNavigationMaxHeight;

    private LinearLayout mLlMenu;  //用于存储右边按钮
    private ColorStateList mItemTextColor;
    private float mItemTextSize;
    private float mItemTextPaddingLeftAndRight;

    public HeaderLayout(Context context) {
        this(context, null);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        String titleText = "";
        Drawable titleDrawableStart = null;
        Drawable titleDrawableTop = null;
        Drawable titleDrawableEnd = null;
        Drawable titleDrawableBottom = null;
        float titleDrawablePadding = 0;

        int navigationIcon = 0;
        String navigationText = "";
        int navigationScaleType = -1;

        int menuIcon = 0, menu2Icon = 0;  //右边的按钮
        int menuIconId = 0, menu2IconId = 0;  //右边的按钮id
        String menuText = "", menu2Text = "";  //右边的文字按钮
        int menuTextId = 0, menu2TextId = 0;  //右边的文字按钮
        int menuAlignType = -1;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeaderLayout);

            isSupportTranslucentStatus = a.getBoolean(R.styleable.HeaderLayout_hlSupportTranslucentStatus, false);

            //标题相关配置
            titleText = a.getString(R.styleable.HeaderLayout_hlTitleText);
            mTitleTextColor = a.getColorStateList(R.styleable.HeaderLayout_hlTitleTextColor);
            if(mTitleTextColor == null) {
                mTitleTextColor = getResources().getColorStateList(R.color.default_header_layout_title_textColor);
            }
            mTitleTextSize = a.getDimension(R.styleable.HeaderLayout_hlTitleTextSize, getResources().getDimension(R.dimen.default_header_layout_title_textSize));
            mTitleAlignLeft = a.getBoolean(R.styleable.HeaderLayout_hlTitleAlignLeft, false);

            int titleDrawableStartRes = a.getResourceId(R.styleable.HeaderLayout_hlTitleTextDrawableStart, 0);
            int titleDrawableTopRes = a.getResourceId(R.styleable.HeaderLayout_hlTitleTextDrawableTop, 0);
            int titleDrawableEndRes = a.getResourceId(R.styleable.HeaderLayout_hlTitleTextDrawableEnd, 0);
            int titleDrawableBottomRes = a.getResourceId(R.styleable.HeaderLayout_hlTitleTextDrawableBottom, 0);
            titleDrawablePadding = a.getDimension(R.styleable.HeaderLayout_hlTitleTextDrawablePadding, 0);
            if(titleDrawableStartRes != 0) {
                titleDrawableStart = getResources().getDrawable(titleDrawableStartRes);
            }
            if(titleDrawableTopRes != 0) {
                titleDrawableTop = getResources().getDrawable(titleDrawableTopRes);
            }
            if(titleDrawableEndRes != 0) {
                titleDrawableEnd = getResources().getDrawable(titleDrawableEndRes);
            }
            if(titleDrawableBottomRes != 0) {
                titleDrawableBottom = getResources().getDrawable(titleDrawableBottomRes);
            }

            menu2TextId = a.getResourceId(R.styleable.HeaderLayout_hlMenu2TextId, +0xa25);

            //文字按钮相关配置
            mItemTextColor = a.getColorStateList(R.styleable.HeaderLayout_hlItemTextColor);
            if(mItemTextColor == null) {
                mItemTextColor = getResources().getColorStateList(R.color.default_header_layout_title_textColor);
            }
            mItemTextSize = a.getDimension(R.styleable.HeaderLayout_hlItemTextSize, getResources().getDimension(R.dimen.default_header_layout_menu_textSize));
            mItemTextPaddingLeftAndRight = a.getDimension(R.styleable.HeaderLayout_hlItemTextPaddingStartAndEnd, getResources().getDimension(R.dimen.default_header_layout_menu_textSize) / 2);  //大概半个字的间距

            navigationIcon = a.getResourceId(R.styleable.HeaderLayout_hlNavigationIcon, 0);
            navigationText = a.getString(R.styleable.HeaderLayout_hlNavigationText);
            mNavigationWidth = a.getDimension(R.styleable.HeaderLayout_hlNavigationWidth, 0);
            mNavigationMinWidth = a.getDimension(R.styleable.HeaderLayout_hlNavigationMinWidth, 0);
            mNavigationMaxWidth = a.getDimension(R.styleable.HeaderLayout_hlNavigationMaxWidth, 0);
            mNavigationHeight = a.getDimension(R.styleable.HeaderLayout_hlNavigationHeight, 0);
            mNavigationMinHeight = a.getDimension(R.styleable.HeaderLayout_hlNavigationMinHeight, 0);
            mNavigationMaxHeight = a.getDimension(R.styleable.HeaderLayout_hlNavigationMaxHeight, 0);
            navigationScaleType = a.getInt(R.styleable.HeaderLayout_hlNavigationScaleType, -1);

            menuIcon = a.getResourceId(R.styleable.HeaderLayout_hlMenuIcon, 0);
            menuIconId = a.getResourceId(R.styleable.HeaderLayout_hlMenuIconId, +0xa22);
            menu2Icon = a.getResourceId(R.styleable.HeaderLayout_hlMenu2Icon, 0);
            menu2IconId = a.getResourceId(R.styleable.HeaderLayout_hlMenu2IconId, +0xa23);
            menuText = a.getString(R.styleable.HeaderLayout_hlMenuText);
            menuTextId = a.getResourceId(R.styleable.HeaderLayout_hlMenuTextId, +0xa24);
            menu2Text = a.getString(R.styleable.HeaderLayout_hlMenu2Text);
            menuAlignType = a.getInt(R.styleable.HeaderLayout_hlMenuAlign, -1);

            mSpitLineColor = a.getResourceId(R.styleable.HeaderLayout_hlSpitLineColor, 0);
            mSpitLineHeight = a.getDimension(R.styleable.HeaderLayout_hlSpitLineHeight, 2);

            a.recycle();
        }

        LayoutParams params;

        //头部间距  用于状态栏沉浸时候使用
        mIvStatusPadding = new ImageView(context);
        mIvStatusPadding.setId(R.id.hl_iv_status_padding);
        addView(mIvStatusPadding);

        //左边按钮
        if(navigationIcon != 0) {
            mNavigationView = new ImageView(getContext());
            addView(mNavigationView);
            mNavigationView.setId(R.id.hl_view_navigation);
            addButtonConfig((ImageView) mNavigationView, navigationIcon, navigationScaleType);
        } else if(!TextUtils.isEmpty(navigationText)){
            mNavigationView = new TextView(getContext());
            addView(mNavigationView);
            mNavigationView.setId(R.id.hl_view_navigation);
            addButtonConfig((TextView) mNavigationView, navigationText, mItemTextSize, mItemTextColor, (int) mItemTextPaddingLeftAndRight);
        }
        if(mNavigationView != null) {
            mNavigationView.setOnClickListener(new OnClickListener() {  //处理返回键点击事件
                @Override
                public void onClick(View view) {
                    ((Activity) getContext()).finish();
                }
            });
        }

        if (menuIcon != 0 || !TextUtils.isEmpty(menuText)) {  //说明有右边按钮
            mLlMenu = new LinearLayout(getContext());
            mLlMenu.setOrientation(LinearLayout.HORIZONTAL);
            addView(mLlMenu);

            params = (LayoutParams) mLlMenu.getLayoutParams();
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.MATCH_PARENT;
            params.addRule(RelativeLayout.BELOW, mIvStatusPadding.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mLlMenu.setLayoutParams(params);

            switch (MenuAlign.getType(menuAlignType)) {
                case ALIGN_TEXT:
                    createMenuIconButton(menu2Icon, menu2IconId, navigationScaleType);
                    createMenuIconButton(menuIcon, menuIconId, navigationScaleType);
                    createMenuTextButton(menu2Text, menu2TextId);
                    createMenuTextButton(menuText, menuTextId);
                    break;
                case ALIGN_ICON:
                    createMenuTextButton(menu2Text, menu2TextId);
                    createMenuTextButton(menuText, menuTextId);
                    createMenuIconButton(menu2Icon, menu2IconId, navigationScaleType);
                    createMenuIconButton(menuIcon, menuIconId, navigationScaleType);
                    break;
                case ALTERNATE:
                    createMenuIconButton(menu2Icon, menu2IconId, navigationScaleType);
                    createMenuTextButton(menu2Text, menu2TextId);
                    createMenuIconButton(menuIcon, menuIconId, navigationScaleType);
                    createMenuTextButton(menuText, menuTextId);
                    break;
                case ALTERNATE2:
                    createMenuTextButton(menu2Text, menu2TextId);
                    createMenuIconButton(menu2Icon, menu2IconId, navigationScaleType);
                    createMenuTextButton(menuText, menuTextId);
                    createMenuIconButton(menuIcon, menuIconId, navigationScaleType);
                    break;
            }
        }

        //标题栏
        mTvTitle = new TextView(getContext());
        mTvTitle.setMaxLines(1);

        // 设置drawable相关
        if (titleDrawableStart != null || titleDrawableTop != null || titleDrawableEnd != null || titleDrawableBottom != null) {
            mTvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    titleDrawableStart,
                    titleDrawableTop,
                    titleDrawableEnd,
                    titleDrawableBottom
            );
        }
        mTvTitle.setCompoundDrawablePadding((int) titleDrawablePadding);

        addView(mTvTitle);
        mTvTitle.setGravity(Gravity.CENTER);  //实现文字居中效果  在setSupportTranslucentStatus中设置高度和HeaderLayout一样就好了
        params = (LayoutParams) mTvTitle.getLayoutParams();
//        params.addRule(RelativeLayout.BELOW, mIvStatusPadding.getId());  //本来需要这样处理，可是实际效果发现应该注释
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.MATCH_PARENT;

        if (!mTitleAlignLeft) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else {
            if(mNavigationView != null) {
                params.addRule(RelativeLayout.RIGHT_OF, mNavigationView.getId());  //基于左边按钮的显示
            } else {
                params.leftMargin = (int)(mTitleTextSize / 2);  //基于左边的显示
                params.addRule(RelativeLayout.ALIGN_RIGHT);
            }
        }
        mTvTitle.setLayoutParams(params);

        mTvTitle.setText(titleText);
        mTvTitle.setTextColor(mTitleTextColor);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);

        //底部的分割线
        if (mSpitLineColor != 0) {
            ImageView ivSplitLine = new ImageView(getContext());
            addView(ivSplitLine);
            params = (LayoutParams) ivSplitLine.getLayoutParams();
            params.width = LayoutParams.MATCH_PARENT;
            params.height = (int) mSpitLineHeight;
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            ivSplitLine.setLayoutParams(params);

            ivSplitLine.setImageResource(mSpitLineColor);
        }
    }

    private void createMenuTextButton(String menuText, int menuTextId) {
        if (!TextUtils.isEmpty(menuText)) {  //添加第1个文字按钮
            TextView tv = new TextView(getContext());
            if (menuTextId != 0) {
                tv.setId(menuTextId);
            }
            mLlMenu.addView(tv);
            addButtonConfig(tv, menuText, mItemTextSize, mItemTextColor, (int) mItemTextPaddingLeftAndRight);
        }
    }

    private void createMenuIconButton(int menuIcon, int menuIconId, int navigationScaleType) {
        if (menuIcon != 0) {  //添加第1个图标按钮
            ImageView iv = new ImageView(getContext());
            if (menuIconId != 0) {
                iv.setId(menuIconId);
            }
            mLlMenu.addView(iv);
            addButtonConfig(iv, menuIcon, navigationScaleType);
        }
    }

    private void addButtonConfig(ImageView view, int navigationIcon, int navigationScaleType) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }

        if (params instanceof LayoutParams) {  //createMenuIconButton传入的是LinearLayout.LayoutParams
            ((LayoutParams) params).addRule(RelativeLayout.BELOW, mIvStatusPadding.getId());
        }
        if (mNavigationWidth > 0) {  //设置宽高属性
            params.width = (int) mNavigationWidth;
        } else {
            params.width = LayoutParams.WRAP_CONTENT;

            //设置Min Max宽属性
            if (mNavigationMinWidth > 0) {
                view.setMinimumWidth((int) mNavigationMinWidth);
            }
            if (mNavigationMaxWidth > 0) {
                view.setMaxWidth((int) mNavigationMaxWidth);
            }
        }
        if (mNavigationHeight > 0) {
            params.height = (int) mNavigationHeight;
        } else {
            params.height = LayoutParams.MATCH_PARENT;

            //设置Min Max高属性
            if (mNavigationMinHeight > 0) {
                view.setMinimumHeight((int) mNavigationMinHeight);
            }
            if (mNavigationMaxHeight > 0) {
                view.setMaxHeight((int) mNavigationMaxHeight);
            }
        }
        view.setLayoutParams(params);

        if (navigationIcon != 0) {  //设置icon
            view.setImageResource(navigationIcon);
        }
        if (navigationScaleType >= 0) {  //设置样式
            view.setScaleType(sScaleTypeArray[navigationScaleType]);
        }

    }

    private void addButtonConfig(TextView view, String text, float textSize, ColorStateList textColor, int padding) {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (params instanceof LayoutParams) {
            ((LayoutParams) params).addRule(RelativeLayout.BELOW, mIvStatusPadding.getId());
        }
        if (mNavigationWidth > 0) {  //设置宽高属性
            params.width = (int) mNavigationWidth;
        } else {
            params.width = LayoutParams.WRAP_CONTENT;

            //设置Min Max宽属性
            if (mNavigationMinWidth > 0) {
                view.setMinimumWidth((int) mNavigationMinWidth);
            }
            if (mNavigationMaxWidth > 0) {
                view.setMaxWidth((int) mNavigationMaxWidth);
            }
        }
        if (mNavigationHeight > 0) {
            params.height = (int) mNavigationHeight;
        } else {
            params.height = LayoutParams.MATCH_PARENT;

            //设置Min Max高属性
            if (mNavigationMinHeight > 0) {
                view.setMinimumHeight((int) mNavigationMinHeight);
            }
            if (mNavigationMaxHeight > 0) {
                view.setMaxHeight((int) mNavigationMaxHeight);
            }
        }
        view.setLayoutParams(params);

        view.setText(text);
        view.setTextColor(textColor);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        view.setPadding(padding, 0, padding, 0);
        view.setGravity(Gravity.CENTER);

    }

    /**
     * 获取标题View控件
     * @return title 标题
     */
    public TextView getTitleView() {
        return mTvTitle;
    }

    /**
     * 获取标题
     * @param title 标题
     */
    public void setTitleText(String title) {
        if(mTvTitle != null){
            mTvTitle.setText(title);
        }
    }

    /**
     * 获取导航栏左边按钮
     * @return title 标题
     */
    public View getNavigationView() {
        return mNavigationView;
    }

    /***
     * 左边按钮点击事件
     * @param l 点击监听
     */
    public void setOnNavigationClickListener(OnClickListener l) {  //处理返回键点击事件
        if(mNavigationView != null) {
            mNavigationView.setOnClickListener(l);
        }
    }

    /***
     * 右边按钮点击事件
     * @param l 点击监听
     */
    public void setMenuClickListener(OnClickListener l) {  //处理返回键点击事件
        if(mNavigationView != null) {
            mNavigationView.setOnClickListener(l);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHedaderLayoutHeight == 0) {
            mHedaderLayoutHeight = getLayoutParams().height;
        }

        seTranslucentStatus(isSupportTranslucentStatus);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void seTranslucentStatus(boolean isSupport) {
        int top = 0;
        if (getContext() instanceof Activity) {
            top = isSupport ? getStatusHeight((Activity) getContext()) : 0;
        }

        getLayoutParams().height = mHedaderLayoutHeight + top;
        setLayoutParams(getLayoutParams());

        mTvTitle.setPadding(0, top + 1, 0, 0);

        LayoutParams params = (LayoutParams) mIvStatusPadding.getLayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = top;
        mIvStatusPadding.setLayoutParams(params);
    }

    /**
     * 状态栏高度算法
     * @param activity act
     * @return status bar height
     */
    @SuppressLint("PrivateApi")
    public static int getStatusHeight(Activity activity) {
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        int statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }
}