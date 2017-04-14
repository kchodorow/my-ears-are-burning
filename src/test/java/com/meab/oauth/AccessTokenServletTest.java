package com.meab.oauth;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.meab.DatastoreConstants;
import com.meab.SecretDatastore;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import com.meab.user.UserDatastore;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.UUID;

/**
 * Tests for {@link AccessTokenServlet}.
 */
@RunWith(JUnit4.class)
public class AccessTokenServletTest {
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AccessTokenServlet servlet;
  private UserDatastore userDatastore;
  private int id;
  private JSONObject gitHubObject;
  private Entity userEntity;

  @Before
  public void setupServlet() throws Exception {
    helper.setUp();
    userDatastore = Mockito.mock(UserDatastore.class);
    NotificationDatastore notificationDatastore = Mockito.mock(NotificationDatastore.class);
    SecretDatastore secretDatastore = Mockito.mock(SecretDatastore.class);
    servlet = new AccessTokenServlet(userDatastore, notificationDatastore, secretDatastore);
    gitHubObject = new JSONObject();
    id = 123456;
    gitHubObject.put("id", id);
    User user = User.create("abc123", gitHubObject);
    userEntity = user.getEntity();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testExistingUserWithCookie() throws Exception {
    String uuid = UUID.randomUUID().toString();
    userEntity.setProperty(DatastoreConstants.User.COOKIE, uuid);
    when(userDatastore.getGitHubUserByAccessToken(anyString())).thenReturn(gitHubObject);
    when(userDatastore.getEntityById(id)).thenReturn(userEntity);
    servlet.getUser("abc123");
  }

  @Test
  public void testNoGitHubUser() throws Exception {
    when(userDatastore.getGitHubUserByAccessToken(anyString()))
      .thenThrow(new IOException("Error downloading from GitHub"));
    expectedException.expect(LoginException.class);
    servlet.getUser("abc123");
  }

  @Test
  public void testNonExistantUser() throws Exception {
    when(userDatastore.getGitHubUserByAccessToken(anyString())).thenReturn(gitHubObject);
    when(userDatastore.getEntityById(anyInt())).thenThrow(
      new EntityNotFoundException(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, id)));
    servlet.getUser("abc123");
  }

  @Test
  public void testNoCookie() throws Exception {
    when(userDatastore.getGitHubUserByAccessToken(anyString())).thenReturn(gitHubObject);
    when(userDatastore.getEntityById(id)).thenReturn(userEntity);
    servlet.getUser("abc123");
  }
}
