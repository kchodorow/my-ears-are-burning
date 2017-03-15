java_library(
    name = "notifications",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/meab/oauth:__pkg__",
        "//src/test/java/com/meab/oauth:__pkg__",
    ],
    deps = [
        "//src/main/java/com/meab",
        "//src/main/java/com/meab/user",
        "@apache_httpclient//jar",
        "@apache_httpcore//jar",
        "@com_google_appengine_java//:api",
        "@guava//jar",
        "@javax_servlet_api//jar",
        "@json//jar",
    ],
)
