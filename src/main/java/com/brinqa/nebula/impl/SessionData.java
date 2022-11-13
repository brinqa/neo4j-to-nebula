package com.brinqa.nebula.impl;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SessionData {
  int referenceCount;
  long sessionId;
  int timezoneOffset;

  public SessionData incrementRef() {
    return ref(1);
  }

  public SessionData decrementRef() {
    return ref(-1);
  }

  private SessionData ref(int diff) {
    return this.toBuilder().referenceCount(referenceCount + diff).build();
  }
}
