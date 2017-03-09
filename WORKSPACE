git_repository(
    name = "io_bazel_rules_appengine",
    remote = "https://github.com/bazelbuild/rules_appengine.git",
    commit = "9f07d4f20f780d57556843d1e9ccf615caa2553e",
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
    artifact = "com.google.guava:guava:jar:21.0",
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
