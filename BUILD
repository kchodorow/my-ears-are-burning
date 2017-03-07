load("@io_bazel_rules_appengine//appengine:appengine.bzl", "appengine_war")

appengine_war(
    name = "backend",
    data = ["//webapp"],
    data_path = "/webapp",
    jars = ["//src/main/java:backend_deploy.jar"],
)
