package com.meab.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.meab.DatastoreConstants;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class NotificationDatastoreTest {
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  User fakeUser;
  JSONObject fakeNotification;

  @Before
  public void setupServlet() throws Exception {
    helper.setUp();
    Entity userEntity = new Entity(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, 12345));
    userEntity.setProperty(DatastoreConstants.User.USER_INFO, new Text("{\"login\":\"foo\"}"));
    fakeUser = User.fromEntity(userEntity);

    fakeNotification = new JSONObject();
    JSONObject subject = new JSONObject();
    fakeNotification.put("subject", subject.put("url", "http://whatever"));
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private static class FakeNotificationDatastore extends NotificationDatastore {
    private final GitHubApi api;
    FakeNotificationDatastore() {
      api = mock(GitHubApi.class);
    }

    @Override
    protected GitHubApi getApi(User user) {
      return api;
    }
  }

  @Test
  public void testInitialMention() throws Exception {
    FakeNotificationDatastore datastore = new FakeNotificationDatastore();
    when(datastore.getApi(fakeUser).getArray(anyString())).thenReturn(new JSONArray());
    JSONObject jsonObject = new JSONObject()
      .put("body", "For @foo bar.")
      .put("comments", 2);
    when(datastore.getApi(fakeUser).getObject(anyString())).thenReturn(jsonObject);
    Entity entity = new Entity(KeyFactory.createKey("x", "y"));
    JSONObject actualObject = datastore.getMention(fakeUser, fakeNotification, entity);
    assertSame(jsonObject, actualObject);
  }
}
