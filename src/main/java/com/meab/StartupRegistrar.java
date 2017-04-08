package com.meab;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

public class StartupRegistrar implements ServletContextListener {
  private static Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_26);

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    try {
      freemarkerConfig.setDirectoryForTemplateLoading(new File("templates"));
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    freemarkerConfig.setDefaultEncoding("UTF-8");
    freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    freemarkerConfig.setLogTemplateExceptions(false);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {

  }

  public static Configuration getFreemarkerConfig() {
    return freemarkerConfig;
  }
}
