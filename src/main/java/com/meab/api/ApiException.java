package com.meab.api;

import org.json.JSONObject;

/**
 * General class for API exceptions.
 */
class ApiException extends Exception {
  ApiException(String message) {
    super(message);
  }

  void toJson(JSONObject response) {
    response.put("ok", false);
    response.put("error", getMessage());
  }

  static String toJsonError(String message) {
    return new JSONObject().put("error", message).put("ok", false).toString();
  }
}
