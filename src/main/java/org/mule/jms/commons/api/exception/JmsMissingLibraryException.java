/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.api.exception;

import static org.mule.jms.commons.api.exception.JmsError.MISSING_LIBRARIES;

/**
 * Exception which is thrown when a required library was not found.
 *
 * @since 1.0
 */
public class JmsMissingLibraryException extends JmsExtensionException {

  /**
   * Creates a new exception
   *
   * @param exception Exception cause
   * @param message   The error message
   */
  public JmsMissingLibraryException(Exception exception, String message) {
    super(message, JmsError.MISSING_LIBRARIES, exception);
  }
}
