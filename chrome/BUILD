load("//chrome:extension.bzl", "chrome_extension")

chrome_extension(
    name = "chrome",
    srcs = glob([
        "js/*.js",
        "assets/*",
        "css/*",
    ]) + ["popup.html"],
)
