/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhy.http.okhttp.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * 根据不同的平台版本——SDK level不同，1.5是3; 1.6是4; 2.2是8。<p>
 * 使用不同的方法执行"耗时任务"——如：使用"handle"执行 或 使用"线程池"执行<br>
 */
public class Platform {
    private static final Platform PLATFORM = findPlatform();

    /**
     * 使用"日志"打印"当前平台版本"<p>
     * 返回一个平台类——这个类有"方法"，执行"耗时任务"<br>
     * 如：使用"handle"  或  使用"线程池"<br>
     * @return
     */
    public static Platform get() {
        L.e(PLATFORM.getClass().toString());
        return PLATFORM;
    }

    /**
     * 获取平台!
     * @return
     */
    private static Platform findPlatform() {
        try {
            // 程序需要兼容，我们知道不同的平台SDK level不同，1.5是3; 1.6是4; 2.2是8。
            // 对应的可以使用android.os.Build进行判断
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {               // 版本号——Build.VERSION.SDK_INT
                return new Android();                       // 使用 handle 执行任务
            }
        } catch (ClassNotFoundException ignored) {
        }
        return new Platform();                              // 如果 handle 执行任务出问题; 则使用"线程池"
    }


    /**
     * 获取线程池<p>
     * newCachedThreadPool创建一个可缓存线程池，       <br>
     * 如果线程池长度超过处理需要，可灵活回收空闲线程，<br>
     * 若无可回收，则新建线程。                        <br>
     * @return Executor——线程池(执行者)
     */
    public Executor defaultCallbackExecutor() {

        // Java通过Executors提供四种线程池，分别为：
        // newCachedThreadPool创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，
        // 若无可回收，则新建线程。
        return Executors.newCachedThreadPool();
    }

    /**
     * 使用"已获取到"的"线程池"执行——任务(Runnable)
     * @param runnable
     */
    public void execute(Runnable runnable) {
        defaultCallbackExecutor().execute(runnable);
    }


    /**
     * 如果——Build.VERSION.SDK_INT != 0<p>
     * 使用 handler 执行——任务
     */
    static class Android extends Platform {
        @Override
        public Executor defaultCallbackExecutor() {
            return new MainThreadExecutor();
        }

        /**
         * 使用 handler 执行——任务
         */
        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable r) {
                handler.post(r);
            }
        }
    }


}
