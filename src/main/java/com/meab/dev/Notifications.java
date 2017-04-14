package com.meab.dev;

import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Testing servlet mimicking GitHub notification API.
 */
public class Notifications extends DevServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.doGet(request, response);
    JSONArray array = new JSONArray();
    response.getWriter().write(array.toString());
  }
}
