package com.meab.oauth;

import com.meab.SecretDatastore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SecretDatastoreTest {

  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private File temp = new File(System.getenv("TEST_TMPDIR") + "/response");

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testAdd() throws IOException {
    SecretDatastore datastore = new SecretDatastore();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getRequestURI()).thenReturn("/secret?key=github&value=foo");
    when(response.getWriter()).thenReturn(new PrintWriter(temp));
    datastore.doGet(request, response);
    assertThat(datastore.get(SecretDatastore.GITHUB_ID)).isEqualTo("foo");
  }

  @Test
  public void testAddMultipleTimes() throws IOException {
    SecretDatastore datastore = new SecretDatastore();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getRequestURI()).thenReturn("/secret?key=github&value=foo");
    when(response.getWriter()).thenReturn(new PrintWriter(temp));
    datastore.doGet(request, response);

    when(request.getRequestURI()).thenReturn("/secret?key=github&value=bar");
    when(response.getWriter()).thenReturn(new PrintWriter(temp));
    datastore.doGet(request, response);

    assertThat(datastore.get(SecretDatastore.GITHUB_ID)).isEqualTo("foo");
  }

}
