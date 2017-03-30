package com.meab.api;

import com.google.common.base.Preconditions;
import com.meab.servlet.MeabServlet;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Parent class for API servlets.
 */
public class ApiServlet extends MeabServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    getOrPost(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    getOrPost(request, response);
  }

  void apiGet(User user, HttpServletRequest request, JSONObject response) throws ApiException {
  }

  void apiPost(User user, HttpServletRequest request, JSONObject response) throws ApiException {
  }

  private void getOrPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    Preconditions.checkArgument(
      request.getMethod().equals("GET") || request.getMethod().equals("POST"),
      "Method must be GET or POST (got " + request.getMethod() + ")");
    User user;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      response.getWriter().write(ApiException.toJsonError(e.getMessage()));
      return;
    }
    JSONObject jsonResponse = new JSONObject();
    try {
      if (request.getMethod().equals("GET")) {
        apiGet(user, request, jsonResponse);
      } else {
        apiPost(user, request, jsonResponse);
      }
    } catch (ApiException e) {
      e.toJson(jsonResponse);
    }
    response.setContentType("application/json");
    response.getWriter().write(jsonResponse.toString());
  }
}
