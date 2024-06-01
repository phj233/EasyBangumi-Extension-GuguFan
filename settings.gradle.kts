pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven( url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        maven( url = uri("https://jitpack.io"))
        maven( url = uri("https://maven.aliyun.com/repository/public"))
        maven( url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public"))
    }
}

rootProject.name = "easybangumi-extension-gugufan"
include(":extension-app")
