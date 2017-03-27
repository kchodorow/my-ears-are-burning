package com.meab.api;

import com.google.common.base.Preconditions;
import com.meab.user.User;
import com.meab.user.UserDatastore;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Parent class for API servlets.
 */
public class ApiServlet extends HttpServlet {
  UserDatastore userDatastore = new UserDatastore();

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

  private User getUser(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    String id = User.getIdFromCookie(request);
    if (id == null) {
      response.getWriter().write(ApiException.toJsonError("Not logged in."));
      return null;
    }
    User user = userDatastore.getUser(id);
    if (user == null) {
      User.unsetCookie(id, response);
      response.getWriter().write(ApiException.toJsonError("Couldn't find user for " + id));
      return null;
    }
    return user;
  }

  private void getOrPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    Preconditions.checkArgument(
      request.getMethod().equals("GET") || request.getMethod().equals("POST"),
      "Method must be GET or POST (got " + request.getMethod() + ")");
    User user = getUser(request, response);
    if (user == null) {
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
