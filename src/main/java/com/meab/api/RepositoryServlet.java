package com.meab.api;

import com.google.common.collect.Sets;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableSet;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Update tracked repositories.
 */
public class RepositoryServlet extends ApiServlet {
  private NotificationDatastore notificationDatastore = new NotificationDatastore();

  /**
   * Returns the list of repositories we know about from the user's notifications.
   */
  @Override
  public void apiGet(User user, HttpServletRequest request, JSONObject response)
    throws ApiException {
    JSONArray repositories = new JSONArray();
    Set<String> seenRepositories = Sets.newHashSet();
    for (Entity entity : notificationDatastore.getNotifications(user.id())) {
      Notification notification = Notification.fromEntity(entity);
      if (notification == null) {
        continue;
      }
      seenRepositories.add(notification.getRepository());
    }
    for (String repo : seenRepositories) {
      repositories.put(repo);
    }
    response.put("ok", true);
    response.put("repositories", repositories);
    response.put("tracked", user.trackedRepositories().toString());
    response.put("name", user.userInfo().getString("login"));
  }

  /**
   * Adds or removes a repository from the user's "tracked" list.
   */
  @Override
  public void apiPost(User user, HttpServletRequest request, JSONObject response)
    throws ApiException {
    String repository = request.getParameter("user") + "/" + request.getParameter("repo");
    String action = request.getParameter("action");
    Set<String> trackedRepositories = user.trackedRepositories();
    boolean updateDb = true;
    if (action.equals("add")) {
      if (trackedRepositories.size() == 0) {
        trackedRepositories = ImmutableSet.of(repository);
      } else {
        throw new ApiException(
          "Already tracking " + trackedRepositories.size() + " repositories");
      }
    } else if (action.equals("remove")) {
      updateDb = trackedRepositories.remove(repository);
    }

    if (updateDb) {
      Entity entity = user.getEntity();
      entity.setProperty(DatastoreConstants.User.TRACKED_REPOSITORIES, trackedRepositories);
      userDatastore.update(user);
    }
    response.put("ok", true);
  }
}
