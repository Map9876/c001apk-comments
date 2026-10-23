package pl.droidsonoids.relinker;

import android.content.Context;

/**
 * sketch-gif 的 POM 声明依赖 pl.droidsonoids:relinker:1.3.1，该产物已从所有公共仓库
 * （Maven Central / Google / JCenter 各镜像）下架，无法解析。LibraryLoader 仅在
 * System.loadLibrary 失败时回退调用 ReLinker.loadLibrary，因此提供等价 stub，
 * 避免 NoClassDefFoundError 与 R8 缺失类警告。
 */
public final class ReLinker {

    private ReLinker() {
    }

    public static void loadLibrary(Context context, String library) {
        System.loadLibrary(library);
    }

    public static void loadLibrary(Context context, String library, String version) {
        System.loadLibrary(library);
    }

    public static void loadLibrary(Context context, String library, ReLinkerInstanceListener listener) {
        System.loadLibrary(library);
    }

    public interface ReLinkerInstanceListener {
        void onSuccess();
    }
}
