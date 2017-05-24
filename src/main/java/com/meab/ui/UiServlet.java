package com.meab.ui;

import com.google.common.collect.ImmutableMap;
import com.meab.StartupRegistrar;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

public class UiServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String uri = request.getRequestURI();
    // Starts with '/':
    uri = uri.substring(1);
    if (uri.isEmpty()) {
      uri = "index";
    }
    String page = uri + ".ftlh";

    Configuration freemarkerConfig = StartupRegistrar.getFreemarkerConfig();
    Template template = freemarkerConfig.getTemplate("common.ftlh");
    try {
      try {
        freemarkerConfig.getTemplate(page);
      } catch (TemplateNotFoundException e) {
        response.sendRedirect("/404");
        return;
      }
      template.process(ImmutableMap.of("page", page), response.getWriter());
    } catch (TemplateException e) {
      e.printStackTrace();
    }
  }
}
