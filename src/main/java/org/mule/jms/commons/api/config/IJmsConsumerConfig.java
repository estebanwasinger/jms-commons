package org.mule.jms.commons.api.config;

import org.mule.jms.commons.api.destination.ConsumerType;

public interface IJmsConsumerConfig<C extends ConsumerType> {

    public int getMaxRedelivery();

    public String getSelector();

    public C getConsumerType();

    public AckMode getAckMode();
}
