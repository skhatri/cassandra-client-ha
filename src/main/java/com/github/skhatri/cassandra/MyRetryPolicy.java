package com.github.skhatri.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.connection.ClosedConnectionException;
import com.datastax.oss.driver.api.core.connection.HeartbeatException;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.retry.RetryDecision;
import com.datastax.oss.driver.api.core.retry.RetryPolicy;
import com.datastax.oss.driver.api.core.servererrors.CoordinatorException;
import com.datastax.oss.driver.api.core.servererrors.DefaultWriteType;
import com.datastax.oss.driver.api.core.servererrors.ReadFailureException;
import com.datastax.oss.driver.api.core.servererrors.WriteFailureException;
import com.datastax.oss.driver.api.core.servererrors.WriteType;
import com.datastax.oss.driver.api.core.session.Request;

public class MyRetryPolicy implements RetryPolicy {
    private final String profileName;
    private final DriverExecutionProfile profile;

    public MyRetryPolicy(DriverContext context, String profileName) {
        this.profileName = profileName;
        this.profile = context.getConfig().getDefaultProfile();
        System.out.println("starting retry policy with " + profileName);
    }

    @Override
    public void close() {

    }

    @Override
    public RetryDecision onReadTimeout(Request request, ConsistencyLevel cl, int blockFor, int received, boolean dataPresent, int retryCount) {
        RetryDecision decision =
            (retryCount == 0 && received >= blockFor && !dataPresent)
                ? RetryDecision.RETRY_SAME
                : RetryDecision.RETHROW;
        System.out.printf("profile=%s, event=onReadTimeout, action=%s \n", profileName, decision.name());
        return decision;
    }

    @Override
    public RetryDecision onWriteTimeout(Request request, ConsistencyLevel cl, WriteType writeType, int blockFor, int received, int retryCount) {
        RetryDecision decision =
            (retryCount == 0 && writeType == DefaultWriteType.BATCH_LOG)
                ? RetryDecision.RETRY_SAME
                : RetryDecision.RETHROW;
        System.out.printf("profile=%s, event=onWriteTimeout, action=%s \n", profileName, decision.name());
        return decision;
    }

    @Override
    public RetryDecision onUnavailable(Request request, ConsistencyLevel cl, int required, int alive, int retryCount) {
        RetryDecision decision = (retryCount == 0) ? RetryDecision.RETRY_NEXT : RetryDecision.RETHROW;
        System.out.printf("profile=%s, event=onUnavailable, action=%s \n", profileName, decision.name());

        //advanced.load-balancing-policy.dc-failover.allow-for-local-consistency-levels=true
        //advanced.load-balancing-policy.dc-failover.max-nodes-per-remote-dc=1
        int nodesPerRemoteDc = profile.getInt(DefaultDriverOption.LOAD_BALANCING_DC_FAILOVER_MAX_NODES_PER_REMOTE_DC);
        boolean allowLocalConsistency = profile.getBoolean(DefaultDriverOption.LOAD_BALANCING_DC_FAILOVER_ALLOW_FOR_LOCAL_CONSISTENCY_LEVELS);
        System.out.printf("remote-connection=%d, allow_local_consistency=%v\n", nodesPerRemoteDc, allowLocalConsistency);

        return decision;
    }

    @Override
    public RetryDecision onRequestAborted(Request request, Throwable error, int retryCount) {
        RetryDecision decision =
            (error instanceof ClosedConnectionException || error instanceof HeartbeatException)
                ? RetryDecision.RETRY_NEXT
                : RetryDecision.RETHROW;
        System.out.printf("profile=%s, event=onRequestAborted, action=%s\n", profileName, decision.name());
        return decision;
    }

    @Override
    public RetryDecision onErrorResponse(Request request, CoordinatorException error, int retryCount) {
        RetryDecision decision =
            (error instanceof ReadFailureException || error instanceof WriteFailureException)
                ? RetryDecision.RETHROW
                : RetryDecision.RETRY_NEXT;
        System.out.printf("profile=%s, event=onErrorResponse, action=%s\n", profileName, decision.name());
        return decision;
    }
}
