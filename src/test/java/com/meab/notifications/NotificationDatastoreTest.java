package com.meab.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.meab.DatastoreConstants;
import com.meab.user.User;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertSame;

@RunWith(JUnit4.class)
public class NotificationDatastoreTest {
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  @Before
  public void setupServlet() throws Exception {
    helper.setUp();
  }


  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testInitialMention() throws Exception {
    NotificationDatastore newDatastore = new NotificationDatastore();
    Entity userEntity = new Entity(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, 12345));
    userEntity.setProperty(DatastoreConstants.User.USER_INFO, new Text("{\"login\":\"foo\"}"));
    User user = User.fromEntity(userEntity);
    JSONObject jsonObject = new JSONObject().put("body", "For @foo bar.");
    Entity entity = new Entity(KeyFactory.createKey("x", "y"));
    JSONObject actualObject = newDatastore.getMention(user, jsonObject, entity);
    assertSame(jsonObject, actualObject);
  }
}
