package com.meab.dev;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Preconditions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Parent class for development servlets.
 */
public class DevServlet extends HttpServlet {
  @Override
  public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Preconditions.checkState(
      SystemProperty.environment.value() == SystemProperty.Environment.Value.Development);
    get(request, response);
  }

  @Override
  public final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Preconditions.checkState(
      SystemProperty.environment.value() == SystemProperty.Environment.Value.Development);
    post(request, response);
  }

  public void get(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

  }

  public void post(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

  }
}
