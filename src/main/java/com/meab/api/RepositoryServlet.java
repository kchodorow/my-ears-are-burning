package com.meab.api;

import com.google.common.collect.Maps;
import com.google.appengine.api.datastore.Entity;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
    Map<String, Integer> seenRepositories = Maps.newHashMap();
    for (Entity entity : notificationDatastore.getNotifications(user.id())) {
      Notification notification = Notification.fromEntity(entity);
      if (notification == null) {
        continue;
      }
      String repository = notification.getRepository();
      if (!seenRepositories.containsKey(repository)) {
        seenRepositories.put(repository, 0);
      }
      Integer num = seenRepositories.get(repository);
      seenRepositories.put(repository, ++num);
    }
    PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(
      10, new NumberSeenComparator());
    for (Map.Entry<String, Integer> repo : seenRepositories.entrySet()) {
      queue.add(repo);
    }

    JSONArray repositories = new JSONArray();
    while (queue.size() > 0) {
      Map.Entry<String, Integer> entry = queue.remove();
      repositories.put(
        new JSONObject().put("name", entry.getKey()).put("count", entry.getValue()));
    }
    System.out.println("Tracked: " + user.trackedRepositories());
    JSONArray tracked = new JSONArray();
    for (String repo : user.trackedRepositories()) {
      tracked.put(repo);
    }

    response.put("ok", true);
    response.put("subscribed", user.maxRepositories() > 1);
    response.put("repositories", repositories);
    response.put("tracked", tracked);
    response.put("name", user.getUsername());
  }

  private static class NumberSeenComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
      if (o1.getValue() > o2.getValue()) {
        return -1;
      }
      if (o1.getValue() < o2.getValue()) {
        return 1;
      }
      return 0;
    }
  }

  /**
   * Adds or removes a repository from the user's "tracked" list.
   */
  @Override
  public void apiPost(User user, HttpServletRequest request, JSONObject response) {
    String action = request.getParameter("action");
    String repository = request.getParameter("repo");
    Entity userEntity = user.getEntity();
    List<String> repos = (List<String>) userEntity.getProperty(
      DatastoreConstants.User.TRACKED_REPOSITORIES);
    if (action.equals("track")) {
      Integer max = (Integer) user.getEntity().getProperty(DatastoreConstants.User.MAX_REPOS);
      if (repos.size() >= max) {
        repos.clear();
      }
      if (!repos.contains(repository)) {
        repos.add(repository);
      }
    } else if (action.equals("untrack")) {
      repos.remove(repository);
    }
    userDatastore.update(userEntity);

    JSONArray tracking = new JSONArray();
    for (String repo : repos) {
      tracking.put(repo);
    }
    response.put("tracked", tracking);
    response.put("ok", true);
  }
}
