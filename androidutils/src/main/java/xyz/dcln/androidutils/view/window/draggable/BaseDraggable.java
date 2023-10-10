package xyz.dcln.androidutils.view.window.draggable;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import xyz.dcln.androidutils.view.window.Floaty;

public abstract class BaseDraggable implements View.OnTouchListener {

    private Floaty mWindow;
    private View mDecorView;

    private final Rect mTempRect = new Rect();

    private int mCurrentWindowWidth;
    private int mCurrentWindowHeight;
    private int mCurrentViewOnScreenX;
    private int mCurrentViewOnScreenY;
    private int mCurrentWindowInvisibleWidth;
    private int mCurrentWindowInvisibleHeight;

    /**
     * Toast 显示后回调这个类
     */
    @SuppressLint("ClickableViewAccessibility")
    public void start(Floaty window) {
        mWindow = window;
        mDecorView = window.getDecorView();
        mDecorView.setOnTouchListener((v, event) -> {
            refreshLocationCoordinate();
            return BaseDraggable.this.onTouch(v, event);
        });
        mDecorView.post(this::refreshLocationCoordinate);
    }

    protected Floaty getWindow() {
        return mWindow;
    }

    protected View getDecorView() {
        return mDecorView;
    }

    /**
     * 获取当前 Window 的宽度
     */
    protected int getWindowWidth() {
        return mCurrentWindowWidth;
    }

    /**
     * 获取当前 Window 的高度
     */
    protected int getWindowHeight() {
        return mCurrentWindowHeight;
    }

    /**
     * 获取窗口不可见的宽度，一般情况下为横屏状态下刘海的高度
     */
    protected int getWindowInvisibleWidth() {
        return mCurrentWindowInvisibleWidth;
    }

    /**
     * 获取窗口不可见的高度，一般情况下为状态栏的高度
     */
    protected int getWindowInvisibleHeight() {
        return mCurrentWindowInvisibleHeight;
    }

    /**
     * 刷新当前 View 在屏幕的坐标信息
     */
    public void refreshLocationCoordinate() {
        View decorView = getDecorView();
        if (decorView == null) {
            return;
        }
        // 这里为什么要这么写，因为发现了鸿蒙手机在进行屏幕旋转的时候
        // 回调 onConfigurationChanged 方法的时候获取到这些参数已经变化了
        // 所以需要提前记录下来，避免后续进行坐标计算的时候出现问题
        decorView.getWindowVisibleDisplayFrame(mTempRect);
        mCurrentWindowWidth = mTempRect.right - mTempRect.left;
        mCurrentWindowHeight = mTempRect.bottom - mTempRect.top;

        int[] location = new int[2];
        decorView.getLocationOnScreen(location);
        mCurrentViewOnScreenX = location[0];
        mCurrentViewOnScreenY = location[1];

        mCurrentWindowInvisibleWidth = mTempRect.left;
        mCurrentWindowInvisibleHeight = mTempRect.top;
    }

    /**
     * 屏幕方向发生了变化
     */
    public void onScreenOrientationChange() {
        int viewWidth = getDecorView().getWidth();
        int viewHeight = getDecorView().getHeight();

        int startX = mCurrentViewOnScreenX - mCurrentWindowInvisibleWidth;
        int startY = mCurrentViewOnScreenY - mCurrentWindowInvisibleHeight;

        float percentX;
        if (startX < 1f) {
            percentX = 0;
        } else if (Math.abs(mCurrentWindowWidth - (startX + viewWidth)) < 1f) {
            percentX = 1;
        } else {
            float centerX = startX + viewWidth / 2f;
            percentX = centerX / (float) mCurrentWindowWidth;
        }

        float percentY;
        if (startY < 1f) {
            percentY = 0;
        } else if (Math.abs(mCurrentWindowHeight - (startY + viewHeight)) < 1f) {
            percentY = 1;
        } else {
            float centerY = startY + viewHeight / 2f;
            percentY = centerY / (float) mCurrentWindowHeight;
        }

        getWindow().postDelayed(() -> {
            getDecorView().getWindowVisibleDisplayFrame(mTempRect);
            int windowWidth = mTempRect.right - mTempRect.left;
            int windowHeight = mTempRect.bottom - mTempRect.top;
            int x = (int) (windowWidth * percentX - viewWidth / 2f);
            int y = (int) (windowHeight * percentY - viewWidth / 2f);
            updateLocation(x, y);
            // 需要注意，这里需要延迟执行，否则会有问题
            getWindow().post(this::refreshLocationCoordinate);
        }, 100);
    }

    /**
     * 更新悬浮窗的位置
     *
     * @param x             x 坐标
     * @param y             y 坐标
     */
    protected void updateLocation(float x, float y) {
        updateLocation((int) x, (int) y);
    }

    /**
     * 更新 WindowManager 所在的位置
     */
    protected void updateLocation(int x, int y) {
        // 屏幕默认的重心（一定要先设置重心位置为左上角）
        int screenGravity = Gravity.TOP | Gravity.START;
        WindowManager.LayoutParams params = mWindow.getWindowParams();
        if (params == null) {
            return;
        }
        // 判断本次移动的位置是否跟当前的窗口位置是否一致
        if (params.gravity == screenGravity && params.x == x && params.y == y) {
            return;
        }

        params.x = x;
        params.y = y;
        params.gravity = screenGravity;

        mWindow.update();
    }

    /**
     * 判断用户手指是否移动了，判断标准以下：
     * 根据手指按下和抬起时的坐标进行判断，不能根据有没有 move 事件来判断
     * 因为在有些机型上面，就算用户没有手指没有移动也会产生 move 事件
     *
     * @param downX         手指按下时的 x 坐标
     * @param upX           手指抬起时的 x 坐标
     * @param downY         手指按下时的 y 坐标
     * @param upY           手指抬起时的 y 坐标
     */
    protected boolean isFingerMove(float downX, float upX, float downY, float upY) {
        float minTouchSlop = getMinTouchDistance();
        return Math.abs(downX - upX) >= minTouchSlop || Math.abs(downY - upY) >= minTouchSlop;
    }

    /**
     * 获取最小触摸距离
     */
    protected float getMinTouchDistance() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                Resources.getSystem().getDisplayMetrics());
    }
}