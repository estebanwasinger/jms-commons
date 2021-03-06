/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.internal.connection;

import static org.mule.jms.commons.internal.common.JmsCommons.closeQuietly;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.Preconditions.checkState;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.jms.commons.internal.connection.exception.CompositeJmsExceptionListener;
import org.mule.jms.commons.internal.connection.session.JmsSession;
import org.mule.jms.commons.internal.connection.session.JmsSessionManager;
import org.mule.jms.commons.internal.support.JmsSupport;
import org.mule.jms.commons.internal.connection.session.TransactionStatus;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.extension.api.connectivity.TransactionalConnection;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import java.util.Optional;

import org.slf4j.Logger;

/**
 * Implementation of the {@link JmsConnection} which implements {@link TransactionalConnection} for Transaction Support
 * in the {@link JmsConnector}
 *
 * @since 1.0
 */
public class JmsTransactionalConnection extends JmsConnection implements TransactionalConnection {

  private static final Logger LOGGER = getLogger(JmsTransactionalConnection.class);
  private static final String COMMIT = "Commit";
  private static final String ROLLBACK = "Rollback";

  public JmsTransactionalConnection(JmsSupport jmsSupport, Connection connection, JmsSessionManager jmsSessionManager,
                                    CompositeJmsExceptionListener exceptionListener) {
    super(jmsSupport, connection, jmsSessionManager, exceptionListener);
  }

  /**
   * Begins a new Transaction for a JMS Session indicating in the {@link JmsSessionManager} that the current
   * {@link Thread} is being part of a transaction.
   */
  @Override
  public void begin() throws TransactionException {
    jmsSessionManager.changeTransactionStatus(TransactionStatus.STARTED);
  }

  /**
   * Executes a commit action over the bound {@link JmsSession} to the current {@link Thread}
   */
  @Override
  public void commit() throws TransactionException {
    try {
      executeTransactionAction(COMMIT, Session::commit);
    } catch (Exception e) {
      throw new TransactionException(createStaticMessage("Could not commit transaction: " + e.getMessage()), e);
    }
  }

  /**
   * Executes a rollback action over the bound {@link JmsSession} to the current {@link Thread}
   */
  @Override
  public void rollback() throws TransactionException {
    try {
      executeTransactionAction(ROLLBACK, Session::rollback);
    } catch (Exception e) {
      throw new TransactionException(createStaticMessage("Could not rollback transaction: " + e.getMessage()), e);
    }
  }

  private void executeTransactionAction(String action, SessionAction transactionalAction) throws JMSException {
    Optional<JmsSession> transactedSession = jmsSessionManager.getTransactedSession();
    checkState(transactedSession.isPresent(), "Unable to " + action + " transaction, the TX Session doesn't exist.");

    JmsSession internalJmsSession = transactedSession.get();
    Session jmsSession = internalJmsSession.get();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("JMS Transaction " + action + " over Session [" + jmsSession + "]");
    }

    try {
      transactionalAction.execute(jmsSession);
    } finally {
      closeQuietly(internalJmsSession);
      jmsSessionManager.changeTransactionStatus(TransactionStatus.NONE);
      jmsSessionManager.unbindSession();
    }
  }

  @FunctionalInterface
  private interface SessionAction {

    void execute(Session session) throws JMSException;
  }
}
