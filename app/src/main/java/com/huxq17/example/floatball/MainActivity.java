package com.huxq17.example.floatball;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.huxq17.example.floatball.permission.FloatPermissionManager;
import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.floatball.FloatBallCfg;
import com.huxq17.floatball.libarary.menu.FloatMenuCfg;
import com.huxq17.floatball.libarary.menu.MenuItem;
import com.huxq17.floatball.libarary.utils.BackGroudSeletor;
import com.huxq17.floatball.libarary.utils.DensityUtil;

public class MainActivity extends Activity {
    private FloatBallManager mFloatballManager;
    private FloatPermissionManager mFloatPermissionManager;
    private ActivityLifeCycleListener mActivityLifeCycleListener = new ActivityLifeCycleListener();
    private int resumed;

    public void showFloatBall(View v) {
        mFloatballManager.show();
//        setFullScreen(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean showMenu = true;//换成false试试
        init(showMenu);
        //5 如果没有添加菜单，可以设置悬浮球点击事件
        if (mFloatballManager.getMenuItemSize() == 0) {
            mFloatballManager.setOnFloatBallClickListener(new FloatBallManager.OnFloatBallClickListener() {
                @Override
                public void onFloatBallClick() {
                    toast("点击了悬浮球");
                }
            });
        }
        //6 如果想做成应用内悬浮球，可以添加以下代码。
        // getApplication().registerActivityLifecycleCallbacks(mActivityLifeCycleListener);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // mFloatballManager.show();
        // mFloatballManager.onFloatBallClick();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFloatballManager.hide();
    }

    private boolean isfull = false;

    //全屏设置和退出全屏
    private void setFullScreen() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isfull = true;
    }

    private void quitFullScreen() {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        isfull = false;
    }

    public void setFullScreen(View view) {
        if (isfull == true) {
            quitFullScreen();
        } else {
            setFullScreen();
        }
    }

    private void init(boolean showMenu) {
        //1 初始化悬浮球配置，定义好悬浮球大小和icon的drawable
        int ballSize = DensityUtil.dip2px(this, 45);
        Drawable ballIcon = BackGroudSeletor.getdrawble("ic_floatball", this);
        //可以尝试使用以下几种不同的config。
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_CENTER,false);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_BOTTOM, -100);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_TOP, 100);
        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER);
        //设置悬浮球不半隐藏
        ballCfg.setHideHalfLater(false);
        if (showMenu) {
            //2 需要显示悬浮菜单
            //2.1 初始化悬浮菜单配置，有菜单item的大小和菜单item的个数
            int menuSize = DensityUtil.dip2px(this, 180);
            int menuItemSize = DensityUtil.dip2px(this, 40);
            FloatMenuCfg menuCfg = new FloatMenuCfg(menuSize, menuItemSize);
            //3 生成floatballManager
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg, menuCfg);
            addFloatMenuItem();

        } else {
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg);
        }
        setFloatPermission();
    }

    private void setFloatPermission() {
        // 设置悬浮球权限，用于申请悬浮球权限的，这里用的是别人写好的库，可以自己选择
        //如果不设置permission，则不会弹出悬浮球
        mFloatPermissionManager = new FloatPermissionManager();
        mFloatballManager.setPermission(new FloatBallManager.IFloatBallPermission() {
            @Override
            public boolean onRequestFloatBallPermission() {
                requestFloatBallPermission(MainActivity.this);
                return true;
            }

            @Override
            public boolean hasFloatBallPermission(Context context) {
                return mFloatPermissionManager.checkPermission(context);
            }

            @Override
            public void requestFloatBallPermission(Activity activity) {
                mFloatPermissionManager.applyPermission(activity);
            }

        });
    }

    public class ActivityLifeCycleListener implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++resumed;
            setFloatballVisible(true);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --resumed;
            if (!isApplicationInForeground()) {
                setFloatballVisible(false);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void addFloatMenuItem() {
        MenuItem personItem = new MenuItem(BackGroudSeletor.getdrawble("ic_weixin", this)) {
            @Override
            public void action() {
                toast("打开微信");
                mFloatballManager.closeMenu();
            }
        };
        MenuItem walletItem = new MenuItem(BackGroudSeletor.getdrawble("ic_weibo", this)) {
            @Override
            public void action() {
                toast("打开微博");
            }
        };
        MenuItem settingItem = new MenuItem(BackGroudSeletor.getdrawble("ic_email", this)) {
            @Override
            public void action() {
                toast("打开邮箱");
                mFloatballManager.closeMenu();
            }
        };
        mFloatballManager.addMenuItem(personItem)
                .addMenuItem(walletItem)
                .addMenuItem(personItem)
                .addMenuItem(walletItem)
                .addMenuItem(settingItem)
                .buildMenu();
    }

    private void setFloatballVisible(boolean visible) {
        if (visible) {
            mFloatballManager.show();
        } else {
            mFloatballManager.hide();
        }
    }

    public boolean isApplicationInForeground() {
        return resumed > 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注册ActivityLifeCyclelistener以后要记得注销，以防内存泄漏。
        // getApplication().unregisterActivityLifecycleCallbacks(mActivityLifeCycleListener);
    }
}
