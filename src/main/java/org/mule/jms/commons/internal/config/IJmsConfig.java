package org.mule.jms.commons.internal.config;

import org.mule.jms.commons.api.config.IJmsConsumerConfig;
import org.mule.jms.commons.api.config.IJmsProducerConfig;

public interface IJmsConfig<Consumer extends IJmsConsumerConfig, Produce extends IJmsProducerConfig> {

    String getContentType();

    String getEncoding();

    Consumer getConsumerConfig();

    Produce getProducerConfig();
}
