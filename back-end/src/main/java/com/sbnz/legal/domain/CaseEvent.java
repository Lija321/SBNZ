package com.sbnz.legal.domain;

/**
 * Marker for time-stamped events over a case (CEP timeline).
 */
public interface CaseEvent {
    String getCaseId();

    long getTs();
}
