package com.meab.ui;

import com.google.common.collect.ImmutableMap;
import com.meab.StartupRegistrar;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IndexServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Configuration freemarkerConfig = StartupRegistrar.getFreemarkerConfig();
    Template template = freemarkerConfig.getTemplate("index.ftlh");
    try {
      template.process(ImmutableMap.of(), response.getWriter());
    } catch (TemplateException e) {
      e.printStackTrace();
    }
  }

}
