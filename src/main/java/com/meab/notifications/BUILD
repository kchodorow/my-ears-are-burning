java_library(
    name = "notifications",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/meab:__subpackages__",
        "//src/test:__subpackages__",
    ],
    deps = [
        "//src/main/java/com/meab",
        "//src/main/java/com/meab/user",
        "@apache_httpclient//jar",
        "@apache_httpcore//jar",
        "@com_google_appengine_java//:api",
        "@guava//jar",
        "@json//jar",
    ],
)
