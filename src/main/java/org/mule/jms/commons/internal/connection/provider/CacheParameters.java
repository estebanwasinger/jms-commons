/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.jms.commons.internal.connection.provider;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.jms.commons.api.connection.caching.CachingStrategy;
import org.mule.jms.commons.api.connection.caching.DefaultCachingStrategy;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import javax.jms.Connection;
import javax.jms.Session;

/**
 * Container group for connection factory cache parameters
 *
 * @since 1.0
 */
public class CacheParameters {

  /**
   * the strategy to be used for caching of {@link Session}s and {@link Connection}s
   */
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  @NullSafe(defaultImplementingType = DefaultCachingStrategy.class)
  private CachingStrategy cachingStrategy;

  public CachingStrategy getCachingStrategy() {
    return cachingStrategy;
  }

}
