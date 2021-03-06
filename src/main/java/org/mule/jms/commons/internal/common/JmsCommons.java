/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.internal.common;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mule.jms.commons.api.message.JmsMessageBuilder.BODY_CONTENT_TYPE_JMS_PROPERTY;
import static org.mule.jms.commons.api.message.JmsMessageBuilder.BODY_ENCODING_JMS_PROPERTY;
import static org.mule.jms.commons.internal.config.InternalAckMode.IMMEDIATE;
import static org.mule.jms.commons.internal.config.InternalAckMode.MANUAL;
import static org.mule.jms.commons.internal.config.InternalAckMode.TRANSACTED;
import static org.mule.runtime.extension.api.tx.OperationTransactionalAction.NOT_SUPPORTED;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.jms.commons.api.config.AckMode;
import org.mule.jms.commons.api.destination.ConsumerType;
import org.mule.jms.commons.api.destination.DestinationType;
import org.mule.jms.commons.api.exception.JmsAckException;
import org.mule.jms.commons.internal.config.InternalAckMode;
import org.mule.jms.commons.internal.config.JmsAckMode;
import org.mule.jms.commons.internal.connection.JmsConnection;
import org.mule.jms.commons.internal.connection.session.JmsSession;
import org.mule.jms.commons.internal.connection.session.JmsSessionManager;
import org.mule.jms.commons.internal.source.JmsListenerLock;
import org.mule.jms.commons.api.destination.ConsumerType;
import org.mule.jms.commons.api.destination.DestinationType;
import org.mule.jms.commons.internal.config.InternalAckMode;
import org.mule.jms.commons.internal.config.JmsAckMode;
import org.mule.jms.commons.internal.connection.JmsConnection;
import org.mule.jms.commons.internal.connection.session.JmsSession;
import org.mule.jms.commons.internal.connection.session.JmsSessionManager;
import org.mule.jms.commons.internal.source.JmsListenerLock;
import org.mule.runtime.extension.api.tx.OperationTransactionalAction;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;

import java.util.Optional;

import org.slf4j.Logger;

/**
 * Utility class to reuse logic for JMS Extension
 *
 * @since 1.0
 */
public final class JmsCommons {

  private static final Logger LOGGER = getLogger(JmsCommons.class);

  public static final String TOPIC = "TOPIC";
  public static final String QUEUE = "QUEUE";
  public static final String EXAMPLE_ENCODING = "UTF-8";
  public static final String EXAMPLE_CONTENT_TYPE = "application/json";

  public static String resolveMessageContentType(Message message, String defaultType) {
    try {
      String contentType = message.getStringProperty(BODY_CONTENT_TYPE_JMS_PROPERTY);
      return isBlank(contentType) ? defaultType : contentType;
    } catch (JMSException e) {
      LOGGER.warn(format("Failed to read the Message ContentType from its properties. A default value of [%s] will be used.",
                         defaultType));
      return defaultType;
    }
  }

  public static String resolveMessageEncoding(Message message, String defaultType) {
    try {
      String contentType = message.getStringProperty(BODY_ENCODING_JMS_PROPERTY);
      return isBlank(contentType) ? defaultType : contentType;
    } catch (JMSException e) {
      LOGGER.warn(format("Failed to read the Message ContentType from its properties. A default value of [%s] will be used.",
                         defaultType));
      return defaultType;
    }
  }

  public static <T> T resolveOverride(T configValue, T operationValue) {
    return operationValue == null ? configValue : operationValue;
  }

  public static void evaluateMessageAck(InternalAckMode ackMode, JmsSession session, Message receivedMessage,
                                        JmsSessionManager messageSessionManager, JmsListenerLock jmsLock)
      throws JMSException {
    try {
      if (ackMode.equals(InternalAckMode.IMMEDIATE)) {
        LOGGER.debug("Automatically performing an ACK over the message, since AckMode was IMMEDIATE");
        receivedMessage.acknowledge();

      } else if (ackMode.equals(InternalAckMode.MANUAL)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Registering pending ACK on session: " + session.getAckId());
        }
        String id = session.getAckId()
            .orElseThrow(() -> new IllegalArgumentException("An AckId is required when MANUAL AckMode is set"));

        messageSessionManager.registerMessageForAck(id, receivedMessage, session.get(), jmsLock);
      }
    } catch (JMSException e) {
      throw new JmsAckException("An error occurred while acknowledging the message", e);
    }
  }

  /**
   * Utility method to create new {@link JmsSession} from a given {@link JmsConnection}
   *
   * @param jmsConnection the connection from where create a new {@link JmsSession}
   * @param ackMode the {@link InternalAckMode} to use
   * @param isTopic Indicates if the destination is whether a topic or a queue
   * @param jmsSessionManager {@link JmsSessionManager} to retrieve information about the current transaction status
   * @return a new {@link JmsSession} from the given {@link JmsConnection}
   * @throws JMSException If an error happens creating a new {@link JmsSession}
   */
  public static JmsSession createJmsSession(JmsConnection jmsConnection, InternalAckMode ackMode, boolean isTopic,
                                            JmsSessionManager jmsSessionManager, OperationTransactionalAction transactionalAction)
      throws JMSException {

    return !transactionalAction.equals(NOT_SUPPORTED)
        ? getOrCreateTransactedSession(jmsConnection, ackMode, isTopic, jmsSessionManager,
                                       jmsSessionManager.getTransactedSession())
        : jmsConnection.createSession(ackMode, isTopic);
  }

  public static InternalAckMode toInternalAckMode(JmsAckMode jmsAckMode) {
    return jmsAckMode == null ? null : jmsAckMode.getInternalAckMode();
  }

  /**
   * Releases all the resources that are required to close.
   * The session only will be closed if this one doesn't belong to the current transaction or it doesn't have
   * an a Client ACK.
   *
   * @param session        Session to close
   * @param sessionManager Session manager to check the transactional status
   * @param closeables     All the things that can be closed
   */
  public static void releaseResources(JmsSession session, JmsSessionManager sessionManager, AutoCloseable... closeables) {
    stream(closeables).forEach(JmsCommons::closeQuietly);

    if (!isManualAck(session) && isPartOfCurrentTx(session, sessionManager)) {
      closeQuietly(session);
    }
  }

  /**
   * Verifies if the given session ACK Mode is {@link AckMode#MANUAL}
   */
  private static boolean isManualAck(JmsSession session) {
    return session.getAckId().isPresent();
  }

  /**
   * Verifies if the given session is part of the current TX.
   */
  private static boolean isPartOfCurrentTx(JmsSession session, JmsSessionManager sessionManager) {
    return !sessionManager.getTransactedSession().isPresent() || sessionManager.getTransactedSession().get() != session;
  }

  /**
   * Closes {@code this} {@link Connection} resource without throwing an exception (an error message is logged instead)
   *
   * @param closable the resource to close
   */
  public static void closeQuietly(AutoCloseable closable) {
    if (closable != null) {
      try {
        closable.close();
      } catch (Exception e) {
        LOGGER.warn("Failed to close jms connection resource: ", e);
      }
    }
  }

  public static String getDestinationType(ConsumerType consumerType) {
    return consumerType.topic() ? TOPIC : QUEUE;
  }

  public static String getDestinationType(DestinationType consumerType) {
    return consumerType.isTopic() ? TOPIC : QUEUE;
  }

  private static JmsSession getOrCreateTransactedSession(JmsConnection jmsConnection, InternalAckMode ackMode, boolean isTopic,
                                                         JmsSessionManager jmsSessionManager,
                                                         Optional<JmsSession> transactedSession)
      throws JMSException {
    JmsSession session;
    if (transactedSession.isPresent()) {
      session = transactedSession.get();
    } else {
      switch (jmsSessionManager.getTransactionStatus()) {
        case STARTED:
          ackMode = InternalAckMode.TRANSACTED;
          session = jmsConnection.createSession(ackMode, isTopic);
          jmsSessionManager.bindToTransaction(session);
          break;
        default:
          session = jmsConnection.createSession(ackMode, isTopic);
          break;
      }
    }
    return session;
  }
}
