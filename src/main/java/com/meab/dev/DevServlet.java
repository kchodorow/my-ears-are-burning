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
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Preconditions.checkState(
      SystemProperty.environment.value() == SystemProperty.Environment.Value.Development);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Preconditions.checkState(
      SystemProperty.environment.value() == SystemProperty.Environment.Value.Development);
  }
}
