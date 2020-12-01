package com.yj.sticker;

import android.app.Application;

import dev.utils.app.logger.DevLogger;
import dev.utils.app.logger.LogConfig;
import dev.utils.app.logger.LogLevel;

public class APP extends Application {
    private static APP app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initLog();
    }

    public static APP getApp() {
        return app;
    }

    private void initLog() {
        // 初始化日志配置
        LogConfig logConfig = new LogConfig();
        if (BuildConfig.DEBUG) {
            // 堆栈方法总数(显示经过的方法)
            logConfig.methodCount = 3;
            // 堆栈方法索引偏移(0 = 最新经过调用的方法信息, 偏移则往上推, 如 1 = 倒数第二条经过调用的方法信息)
            logConfig.methodOffset = 0;
            // 是否输出全部方法(在特殊情况下, 如想要打印全部经过的方法, 但是不知道经过的总数)
            logConfig.outputMethodAll = false;
            // 显示日志线程信息(特殊情况, 显示经过的线程信息, 具体情况如上)
            logConfig.displayThreadInfo = false;
            // 是否排序日志(格式化后)
            logConfig.sortLog = true; // 是否美化日志, 边框包围
            // 日志级别
            logConfig.logLevel = LogLevel.DEBUG;
            // 设置 TAG (特殊情况使用, 不使用全部的 TAG 时, 如单独输出在某个 TAG 下)
            logConfig.tag = "BaseLog";
            // 进行初始化配置, 这样设置后, 默认全部日志都使用改配置, 特殊使用 DevLogger.other(config).d(xxx);
        } else {
            logConfig.logLevel = LogLevel.NONE;
        }
        DevLogger.init(logConfig);
    }
}
