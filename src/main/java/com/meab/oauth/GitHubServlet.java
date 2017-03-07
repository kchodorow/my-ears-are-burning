package com.meab.oauth;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authenticate with GitHub.
 */
public class GitHubServlet extends HttpServlet {
    static final String CLIENT_ID_KEY = "client_id";
    static final String CLIENT_ID_VALUE = "63784a223920d4d5609c";
    static final String SCOPE_KEY = "scope";
    static final String SCOPE_VALUE = "notifications";
    static final String STATE_KEY = "scope";

    static final String REDIRECT_URL = "https://github.com/login/oauth/authorize";

    /**
     * Step 1: Redirect to GitHub.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String query = "?" + CLIENT_ID_KEY + "=" + CLIENT_ID_VALUE + "&"
            + SCOPE_KEY + "=" + SCOPE_VALUE + "&"
            + STATE_KEY + "=" + "QUERTY";
        response.sendRedirect(REDIRECT_URL + query);
    }
}
