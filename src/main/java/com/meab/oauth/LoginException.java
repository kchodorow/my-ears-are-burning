package com.meab.oauth;

/**
 * Exception for login errors.
 */
public class LoginException extends Exception {
  public LoginException(String message) {
    super(message);
  }
}
