package com.meab.servlet;

/**
 * General exception class for servlets.
 */
public class MeabServletException extends Exception {
  MeabServletException(String message) {
    super(message);
  }
}
