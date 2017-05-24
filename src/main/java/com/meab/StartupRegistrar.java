package com.meab;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class StartupRegistrar implements ServletContextListener {
  private static Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_26);

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    try {
      freemarkerConfig.setTemplateLoader(new SymlinkedTemplateLoader(new File("templates")));
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    freemarkerConfig.setLocalizedLookup(false);
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

  /**
   * Freemarker doesn't like symlinks in paths.
   */
  private static class SymlinkedTemplateLoader extends FileTemplateLoader {
    SymlinkedTemplateLoader(File templateDir) throws IOException {
      super(templateDir);
    }

    @Override
    public Object findTemplateSource(String s) throws IOException {
      return new File(getBaseDirectory() + "/" + s);
    }
  }
}
