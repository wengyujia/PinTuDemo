package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jia.pintu.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import util.ImagePiece;
import util.ImageSplitterUtil;

/**
 * Created by W on 2016/9/5.
 */
public class GamePinTuLayout extends RelativeLayout implements View.OnClickListener {

    /**
     * 4..声明所需的变量
     */
    private int mColum = 3;//块块是几乘几的

    private int mPadding;//容器的内边距

    private int mWidth;//容器的宽度

    private int mItemWidth;//小图片的宽度（包括高度）

    private ImageView[] mGamePinTuItems;//存储所有ImageView

    private Bitmap mBitmap;//拼图的图片

    private int mMargin = 3;//每个小图片之间的间距（横纵一样） 是dp的还需要转换为px

    private List<ImagePiece> mItemBitmaps = new ArrayList<ImagePiece>();//存储切片的图片

    private boolean once;


    /**
     * 10.创建回调接口实现游戏过关
     */
    private boolean isGameSucess;
    private boolean isGameOver;

    //a.声明接口
    public interface GamePituListener {
        void nextLevel(int nextLevel);

        void timeChanged(int currentTime);

        void gameOver();
    }

    //d.接口成员变量
    public GamePituListener mListener;

    //设置接口回调
    public void setOnGamePintuListener(GamePituListener mListener) {
        this.mListener = mListener;
    }

    private int level = 1;
    //c.声明handleMessage接收的类型
    private static final int NEXT_LEVEL = 0X111;
    private static final int TIME_CHANGED = 0X222;

    //b.由于是与主界面UI的操作，使用handler
    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                /**
                 * 12.接收到handler发送的信息，通知程序游戏过关，在MainActivity中调用接口进行画面更新
                 */
                case NEXT_LEVEL:
                    level = level + 1;
                    if (mListener != null) {
                        mListener.nextLevel(level);
                    } else {
                        nextLevel();
                    }
                    break;
                case TIME_CHANGED:
                    //是否是这三个状态，是就什么都不做
                    if (isGameSucess || isGameOver || isPause)
                        return;
                    if (mListener != null) {
                        mListener.timeChanged(mTime);
                    }
                    if (mTime == 0) {
                        isGameOver = true;
                        mListener.gameOver();
                        return;
                    }
                    mTime--;
                    handler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
                    break;
            }
        }
    };

    //继续游戏
    public void restart() {
        isGameOver = false;
        mColum--;
        nextLevel();
    }

    /**
     * 15.游戏暂停与恢复
     */
    private boolean isPause;

    //游戏暂停
    public void pause() {
        isPause = true;
        handler.removeMessages(TIME_CHANGED);
    }

    //游戏恢复
    public void resume() {
        if (isPause) {
            isPause = false;
            handler.sendEmptyMessage(TIME_CHANGED);
        }
    }

    public void nextLevel() {
        this.removeAllViews();
        mAnimationLayout = null;
        mColum++;
        isGameSucess = false;
        checkTimeEnable();
        initBitmap();
        initItem();
    }


    //设置是否启动时间
    private boolean isTiemEnabled = false;
    private int mTime;

    public void setIsTiemEnabled(boolean isTiemEnabled) {
        this.isTiemEnabled = isTiemEnabled;
    }

    /**
     * 3.重写三个构造方法
     */
    public GamePinTuLayout(Context context) {
        this(context, null);
    }

    public GamePinTuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GamePinTuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /**
         * 5.初始化代码
         */
        init();
    }


    private void init() {
        //把margin从dp转换为px的值
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        //边距取最小值
        mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());
    }

    //获取多个边距参数的最小值(迭代)
    private int min(int... params) {

        int min = params[0];
        for (int param : params) {
            if (param < min) {
                min = param;
            }
        }
        return min;
    }


    /**
     * 6.确定当前布局的大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());//取容器宽高的最小值为宽度
        if (!once) {
            //进行切图，以及排序
            initBitmap();
            //设置ImageView(Item)的宽高等属性
            initItem();

            /**
             * 14.设置游戏时间和关卡
             */
            checkTimeEnable();

            once = true;//防止多次执行（切图）
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    //设置游戏时间和关卡
    private void checkTimeEnable() {
        if (isTiemEnabled) {
            //根据当前等级设置时间
            cuontTimeBaseLevel();
            handler.sendEmptyMessage(TIME_CHANGED);
        }
    }

    //为mTime赋值
    private void cuontTimeBaseLevel() {
        mTime = (int) Math.pow(2, level) * 60;//时间是成指数增长，所以用Math.pow（）
    }

    //进行切图，以及排序
    private void initBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
            //通过切片类把图片切成块存储在List<>中 每个小图片都有自己的index和bitmap
        }
        mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColum);
        //把切片好的小图片进行乱序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece a, ImagePiece b) {
                return Math.random() > 0.5 ? 1 : -1;//Math.random()的数值在0~~0.999...之间
            }
        });
    }

    //设置ImageView(Item)的宽高等属性
    private void initItem() {
        //获得小图片的宽度
        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColum - 1)) / mColum;
        mGamePinTuItems = new ImageView[mColum * mColum];
        //生成Item，设置Rule(item之间的关系：高低...)
        for (int i = 0; i < mGamePinTuItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);//设置item的点击事件
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());//设置图片

            mGamePinTuItems[i] = item;
            item.setId(i + 1);
            //在item的tag中存储index 每个item都有一个index，在成功后就可按照顺序判断是否过关
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);

            //拼图中第一行没有上边距，二行和三行有上边；第一列没有左边距，二列和三列有左边距

            //设置Item间的横向间隙，通过RigthtMargin 不是最后一列
            if ((i + 1) % mColum != 0) {
                lp.rightMargin = mMargin;
            }
            //不是第一列
            if (i % mColum != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF, mGamePinTuItems[i - 1].getId());
            }

            //设置Item间的纵向间隙，如果不是第一行
            if ((i + 1) > mColum) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW, mGamePinTuItems[i - mColum].getId());
            }
            addView(item, lp);
        }
    }

    /**
     * 7.设置图片的点击事件
     */

    private ImageView mFrist;
    private ImageView mSecond;

    @Override
    public void onClick(View v) {

        //正在动画效果时，点击无效
        if (isAnimation)
            return;

        //l两次点击同一个小图片
        if (mFrist == v) {
            mFrist.setColorFilter(null);
            mFrist = null;
            return;
        }

        if (mFrist == null) {
            mFrist = (ImageView) v;
            mFrist.setColorFilter(Color.parseColor("#55CEDF15"));
        } else {
            mSecond = (ImageView) v;
            exchangeView();
        }
    }

    //动画层
    private RelativeLayout mAnimationLayout;
    private boolean isAnimation;

    //交换图片
    private void exchangeView() {

        /**
         * *8.使用动画层效果
         * TranslateAnimation 平移动画（TranslateAnimation有个方法（ setFillAfter() )不会改变位置）
         */
        mFrist.setColorFilter(null);
        //a.构造动画层
        setUpAnimation();

        //b.复制一个Image到动画层上
        ImageView frist = new ImageView(getContext());
        final Bitmap mFristBitmap = mItemBitmaps.get(getImameIdByTag((String) mFrist.getTag())).getBitmap();//得到bitmap
        frist.setImageBitmap(mFristBitmap);
        LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = mFrist.getLeft() - mPadding;
        lp.topMargin = mFrist.getTop() - mPadding;
        frist.setLayoutParams(lp);
        mAnimationLayout.addView(frist);

        ImageView second = new ImageView(getContext());
        final Bitmap mSecondBitmap = mItemBitmaps.get(getImameIdByTag((String) mSecond.getTag())).getBitmap();//得到bitmap
        second.setImageBitmap(mSecondBitmap);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
        lp2.leftMargin = mSecond.getLeft() - mPadding;
        lp2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimationLayout.addView(second);


        //c.设置动画 上面已经得到了Imageview
        TranslateAnimation animation = new TranslateAnimation(0, mSecond.getLeft() - mFrist.getLeft(), 0, mSecond.getTop() - mFrist.getTop());
        animation.setDuration(300);//动画时间
        animation.setFillAfter(true);
        frist.setAnimation(animation);

        TranslateAnimation animationSecond = new TranslateAnimation(0, -mSecond.getLeft() + mFrist.getLeft(), 0, -mSecond.getTop() + mFrist.getTop());
        animationSecond.setDuration(300);
        animationSecond.setFillAfter(true);
        second.setAnimation(animationSecond);


        //d.监听动画
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFrist.setVisibility(View.INVISIBLE);
                mSecond.setVisibility(View.INVISIBLE);
                isAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                String mFristTag = (String) mFrist.getTag();
                String mSecondTag = (String) mSecond.getTag();
                mFrist.setImageBitmap(mSecondBitmap);
                mSecond.setImageBitmap(mFristBitmap);
                //改变tag
                mFrist.setTag(mSecondTag);
                mSecond.setTag(mFristTag);


                mFrist.setVisibility(View.VISIBLE);
                mSecond.setVisibility(View.VISIBLE);

                mFrist = mSecond = null;//回到最初始的状态
                mAnimationLayout.removeAllViews();

                /**
                 * 9.判断游戏是否成功
                 */
                checkSuccess();
                isAnimation = false;


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    //判断游戏是否成功
    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < mGamePinTuItems.length; i++) {
            ImageView imageView = mGamePinTuItems[i];
            if (getImageIndex((String) imageView.getTag()) != i) {
                isSuccess = false;
            }
        }
        if (isSuccess) {
            Toast.makeText(getContext(), "拼图成功", Toast.LENGTH_LONG).show();
            /**
             * 11.成功之后发送信息给handler
             */

            isGameSucess = true;
            handler.removeMessages(TIME_CHANGED);

            handler.sendEmptyMessage(NEXT_LEVEL);
        }
    }

    //根据tag获取Id
    public int getImameIdByTag(String tag) {
        String[] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    public int getImageIndex(String tag) {
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }


    //构造动画层
    private void setUpAnimation() {
        if (mAnimationLayout == null) {
            mAnimationLayout = new RelativeLayout(getContext());
            addView(mAnimationLayout);
        }
    }

}
