/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.api.exception;

import static org.mule.jms.commons.api.exception.JmsError.SECURITY;

/**
 * {@link JmsExtensionException} to be thrown in the case that an Security related error occurs.
 *
 * @since 1.0
 */
public class JmsSecurityException extends JmsExtensionException {

  /**
   * Creates a new JMS Security Exception
   *
   * @param message   Message of the new exception
   * @param exception Cause of this exception
   */
  public JmsSecurityException(String message, Exception exception) {
    super(message, SECURITY, exception);
  }
}
