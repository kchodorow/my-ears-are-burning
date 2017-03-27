java_library(
    name = "oauth",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java:__pkg__",
        "//src/test/java/com/meab/oauth:__pkg__"
    ],
    deps = [
        "//src/main/java/com/meab",
        "//src/main/java/com/meab/notifications",
        "//src/main/java/com/meab/user",
        "@com_google_appengine_java//:api",
        "@apache_httpclient//jar",
        "@apache_httpcore//jar",
        "@commons_codec//jar",
        "@commons_logging//jar",
        "@guava//jar",
        "@javax_servlet_api//jar",
        "@json//jar",
    ],
)

