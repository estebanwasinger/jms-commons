package org.mule.jms.commons.api.config;

import java.util.concurrent.TimeUnit;

public interface IJmsProducerConfig {

    public boolean isPersistentDelivery();

    public int getPriority();

    public long getTimeToLive();

    public TimeUnit getTimeToLiveUnit();

    public boolean isDisableMessageId();

    public boolean isDisableMessageTimestamp();

    public Long getDeliveryDelay();

    public TimeUnit getDeliveryDelayUnit();

    public String getJmsType();
}

