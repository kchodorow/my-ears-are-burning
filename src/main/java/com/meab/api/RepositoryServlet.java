package com.meab.api;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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
import java.util.Comparator;
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
    JSONArray tracked = new JSONArray();
    for (String repo : user.trackedRepositories()) {
      tracked.put(repo);
    }

    response.put("ok", true);
    response.put("repositories", repositories);
    response.put("tracked", tracked);
    response.put("name", user.userInfo().getString("login"));
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
  public void apiPost(User user, HttpServletRequest request, JSONObject response)
    throws ApiException {
    System.out.println("params: " + request.getParameterMap());
    String repository = request.getParameter("track");
    Set<String> trackedRepositories = user.trackedRepositories();
    if (trackedRepositories.size() != 0) {
      throw new ApiException(
        "Already tracking " + trackedRepositories.size() + " repositories");
    }

    userDatastore.addTrackedRepository(user, repository);
    response.put("ok", true);
  }
}