java_library(
    name = "user",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/com/meab",
        "@apache_httpclient//jar",
        "@apache_httpcore//jar",
        "@com_google_appengine_java//:api",
        "@guava//jar",
        "@javax_servlet_api//jar",
        "@json//jar",
        "@jsr305//jar",
    ],
    visibility = [
        "//src/main/java:__subpackages__",
        "//src/test/java:__subpackages__",
    ],
)
