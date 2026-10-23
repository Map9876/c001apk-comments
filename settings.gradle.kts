pluginManagement {
    repositories {
        // 阿里云镜像优先：me.panpf:sketch-gif 等 JCenter 遗产包已从公共仓库下架，
        // 只在镜像上有；官方仓库保留在后面兜底
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        maven ("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/public")
        gradlePluginPortal()
    }
}

rootProject.name = "c001apk"
include(":app", ":mojito", ":SketchImageViewLoader", ":GlideImageLoader")
 