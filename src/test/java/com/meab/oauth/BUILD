java_test(
    name = "oauth",
    test_class = "com.meab.oauth.GitHubServletTest",
    srcs = ["GithubServletTest.java"],
    deps = [
        "//src/main/java/com/meab/oauth",
        "@com_google_truth//jar",
        "@javax_servlet_api//jar",
        "@org_junit//jar",
        "@org_mockito//jar",
    ]
)

java_test(
    name = "SecretDatastoreTest",
    test_class = "com.meab.oauth.SecretDatastoreTest",
    srcs = ["SecretDatastoreTest.java"],
    deps = [
        "//src/main/java/com/meab/oauth",
        "@appengine_testing//jar",
        "@com_google_appengine_java//:jars",
        "@com_google_truth//jar",
        "@javax_servlet_api//jar",
        "@org_junit//jar",
        "@org_mockito//jar",
    ],
)