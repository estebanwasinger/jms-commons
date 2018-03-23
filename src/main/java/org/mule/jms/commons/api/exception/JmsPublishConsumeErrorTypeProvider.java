/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.api.exception;

import static org.mule.jms.commons.api.exception.JmsError.ACK;
import static org.mule.jms.commons.api.exception.JmsError.CONSUMING;
import static org.mule.jms.commons.api.exception.JmsError.DESTINATION_NOT_FOUND;
import static org.mule.jms.commons.api.exception.JmsError.ILLEGAL_BODY;
import static org.mule.jms.commons.api.exception.JmsError.PUBLISHING;
import static org.mule.jms.commons.api.exception.JmsError.SECURITY;
import static org.mule.jms.commons.api.exception.JmsError.TIMEOUT;
import org.mule.jms.commons.api.message.JmsMessageBuilder;
import org.mule.jms.commons.internal.config.JmsConfig;
import org.mule.jms.commons.internal.connection.JmsConnection;
import org.mule.jms.commons.internal.consume.JmsConsumeParameters;
import org.mule.jms.commons.internal.operation.JmsPublishConsume;
import org.mule.jms.commons.internal.publish.JmsPublishParameters;
import org.mule.jms.commons.internal.config.JmsConfig;
import org.mule.jms.commons.internal.connection.JmsConnection;
import org.mule.jms.commons.internal.consume.JmsConsumeParameters;
import org.mule.jms.commons.internal.operation.JmsPublishConsume;
import org.mule.jms.commons.internal.publish.JmsPublishParameters;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Errors that can be thrown in the
 * {@link JmsPublishConsume#publishConsume(JmsConfig, JmsConnection, String, JmsMessageBuilder, JmsPublishParameters, JmsConsumeParameters)}
 * operation operation.
 *
 * @since 1.0
 */
public class JmsPublishConsumeErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return ImmutableSet.<ErrorTypeDefinition>builder()
        .add(JmsError.PUBLISHING)
        .add(JmsError.ILLEGAL_BODY)
        .add(JmsError.CONSUMING)
        .add(JmsError.TIMEOUT)
        .add(JmsError.DESTINATION_NOT_FOUND)
        .add(JmsError.SECURITY)
        .add(JmsError.ACK)
        .build();
  }
}

