git_repository(
    name = "io_bazel_rules_appengine",
    remote = "https://github.com/bazelbuild/rules_appengine.git",
    commit = "de7ce682031aa5ea9aa74ed7144d118f297f2afa",
)
load("@io_bazel_rules_appengine//appengine:appengine.bzl", "appengine_repositories")
appengine_repositories()

maven_jar(
    name = "apache_httpclient",
    artifact = "org.apache.httpcomponents:httpclient:4.5.3",
)
maven_jar(
    name = "apache_httpcore",
    artifact = "org.apache.httpcomponents:httpcore:4.4.6",
)
maven_jar(
    name = "guava",
    artifact = "com.google.guava:guava:20.0",
)
maven_jar(
    name = "commons_logging",
    artifact = "commons-logging:commons-logging:1.2",
)
maven_jar(
    name = "commons_codec",
    artifact = "commons-codec:commons-codec:jar:1.10",
)
maven_jar(
    name = "appengine_testing",
    artifact = "com.google.appengine:appengine-testing:1.9.48",
)
maven_jar(
    name = "json",
    artifact = "org.json:json:20160810",
)
maven_jar(
    name = "jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.1",
)
maven_jar(
    name = "stripe",
    artifact = "com.stripe:stripe-java:4.0.0",
)
maven_jar(
    name = "gson",
    artifact = "com.google.code.gson:gson:2.8.0",
)

new_http_archive(
    name = "autovalue",
    url = "http://repo1.maven.org/maven2/com/google/auto/value/auto-value/1.3/auto-value-1.3.jar",
    build_file_content = """
java_import(
    name = "jar",
    jars = ["auto-value-1.3.jar"],
    visibility = ["//visibility:public"],
)
java_plugin(
    name = "autovalue-plugin",
    generates_api = 1,
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    deps = [":jar"],
    visibility = ["//visibility:public"],
)
java_library(
    name = "processor",
    exported_plugins = [":autovalue-plugin"],
    exports = [":jar"],
    visibility = ["//visibility:public"],
)
""",
)

# Testing.

maven_jar(
    name = "org_junit",
    artifact = "junit:junit:4.4",
)

maven_jar(
    name = "org_mockito",
    artifact = "org.mockito:mockito-all:1.9.5",
)

maven_jar(
    name = "com_google_truth",
    artifact = "com.google.truth:truth:0.30",
)
