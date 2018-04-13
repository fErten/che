/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

/**
 * Default implementation of {@link WorkspaceStatusCache} based on Map. Note that is not safe for
 * multiple thread usage without external synchronization.
 *
 * @author Anton Korneta
 */
public class DefaultWorkspaceStatusCache implements WorkspaceStatusCache {

  private final Map<String, WorkspaceStatus> delegate = new ConcurrentHashMap<>();

  @Override
  public WorkspaceStatus get(String workspaceId) {
    return delegate.get(workspaceId);
  }

  @Override
  public WorkspaceStatus replace(String workspaceId, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, newStatus);
  }

  @Override
  public boolean replace(
      String workspaceId, WorkspaceStatus prevStatus, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, prevStatus, newStatus);
  }

  @Override
  public WorkspaceStatus remove(String workspaceId) {
    return delegate.remove(workspaceId);
  }

  @Override
  public WorkspaceStatus putIfAbsent(String workspaceId, WorkspaceStatus status) {
    return delegate.putIfAbsent(workspaceId, status);
  }

  @Override
  public Map<String, WorkspaceStatus> mapSlice() {
    return new HashMap<>(delegate);
  }

  @Override
  public boolean containsValue(WorkspaceStatus status) {
    return delegate.containsValue(status);
  }
}
