/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.internal.connection.session;

import javax.jms.Session;

import java.util.Optional;

/**
 * Wrapper element for a JMS {@link Session} that relates the
 * session with its AckID
 *
 * @since 1.0
 */
public interface JmsSession extends AutoCloseable {

  /**
   * @return the JMS {@link Session}
   */
  Session get();

  /**
   * @return the AckId of this {@link Session} or {@link Optional#empty} if no AckId is required
   */
  Optional<String> getAckId();

}
