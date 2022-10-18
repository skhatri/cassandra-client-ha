package com.github.skhatri.cassandra;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.connection.ReconnectionPolicy;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy;
import com.datastax.oss.driver.shaded.guava.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MyExponentialReconnectionPolicy implements ReconnectionPolicy {


    private static final Logger LOG = LoggerFactory.getLogger(ExponentialReconnectionPolicy.class);

    private final String logPrefix;
    private final long baseDelayMs;
    private final long maxDelayMs;
    private final long maxAttempts;

    public MyExponentialReconnectionPolicy(DriverContext context) {
        this.logPrefix = context.getSessionName();

        DriverExecutionProfile config = context.getConfig().getDefaultProfile();

        this.baseDelayMs = config.getDuration(DefaultDriverOption.RECONNECTION_BASE_DELAY).toMillis();
        this.maxDelayMs = config.getDuration(DefaultDriverOption.RECONNECTION_MAX_DELAY).toMillis();

        Preconditions.checkArgument(
            baseDelayMs > 0,
            "%s must be strictly positive (got %s)",
            DefaultDriverOption.RECONNECTION_BASE_DELAY.getPath(),
            baseDelayMs);
        Preconditions.checkArgument(
            maxDelayMs >= 0,
            "%s must be positive (got %s)",
            DefaultDriverOption.RECONNECTION_MAX_DELAY.getPath(),
            maxDelayMs);
        Preconditions.checkArgument(
            maxDelayMs >= baseDelayMs,
            "%s must be bigger than %s (got %s, %s)",
            DefaultDriverOption.RECONNECTION_MAX_DELAY.getPath(),
            DefaultDriverOption.RECONNECTION_BASE_DELAY.getPath(),
            maxDelayMs,
            baseDelayMs);

        // Maximum number of attempts after which we overflow
        int ceil = (baseDelayMs & (baseDelayMs - 1)) == 0 ? 0 : 1;
        this.maxAttempts = 64L - Long.numberOfLeadingZeros(Long.MAX_VALUE / baseDelayMs) - ceil;
    }

    /**
     * The base delay in milliseconds for this policy (e.g. the delay before the first reconnection
     * attempt).
     *
     * @return the base delay in milliseconds for this policy.
     */
    public long getBaseDelayMs() {
        return baseDelayMs;
    }

    /**
     * The maximum delay in milliseconds between reconnection attempts for this policy.
     *
     * @return the maximum delay in milliseconds between reconnection attempts for this policy.
     */
    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    @Override
    public ReconnectionSchedule newNodeSchedule(Node node) {
        System.out.printf("[%s] Creating new schedule for %s dc=%s\n", logPrefix, node, node.getDatacenter());
        System.out.flush();
        return new MyExponentialReconnectionPolicy.ExponentialSchedule(Map.of("node", node.getBroadcastAddress().toString(),
            "dc", node.getDatacenter()));
    }

    @Override
    public ReconnectionSchedule newControlConnectionSchedule(
        @SuppressWarnings("ignored") boolean isInitialConnection) {
        System.out.printf("[%s] Creating new schedule for the control connection\n", logPrefix);
        System.out.flush();
        return new MyExponentialReconnectionPolicy.ExponentialSchedule(Map.of());
    }

    @Override
    public void close() {
        // nothing to do
    }

    private class ExponentialSchedule implements ReconnectionSchedule {

        private int attempts;
        private final Map<String,String> attrib;


        ExponentialSchedule(Map<String,String> attrib) {
            this.attrib = attrib;
        }

        @Override
        public Duration nextDelay() {
            long delay = (attempts > maxAttempts) ? maxDelayMs : calculateDelayWithJitter();
            System.out.printf("attempts=%d, max-attempts=%d, delay=%d, attrib=%v\n",
                attempts, maxAttempts, delay, attrib);
            return Duration.ofMillis(delay);
        }

        private long calculateDelayWithJitter() {
            // assert we haven't hit the max attempts
            assert attempts <= maxAttempts;
            // get the pure exponential delay based on the attempt count
            long delay = Math.min(baseDelayMs * (1L << attempts++), maxDelayMs);
            // calculate up to 15% jitter, plus or minus (i.e. 85 - 115% of the pure value)
            int jitter = ThreadLocalRandom.current().nextInt(85, 116);
            // apply jitter
            delay = (jitter * delay) / 100;
            // ensure the final delay is between the base and max
            delay = Math.min(maxDelayMs, Math.max(baseDelayMs, delay));
            return delay;
        }
    }

    public long getMaxAttempts() {
        return maxAttempts;
    }
}
