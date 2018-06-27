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
package org.eclipse.che.api.workspace.server.hc.probe.server;

import static java.util.Collections.singletonMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.hc.probe.HttpProbeConfig;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.token.MachineTokenException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Produces {@link HttpProbeConfig} for terminal agent liveness probes.
 *
 * @author Alexander Garagatyi
 */
public class TerminalServerLivenessProbeConfigFactory implements HttpProbeConfigFactory {

  private final int successThreshold;
  private final MachineTokenProvider machineTokenProvider;

  public TerminalServerLivenessProbeConfigFactory(
      MachineTokenProvider machineTokenProvider, int successThreshold) {
    this.machineTokenProvider = machineTokenProvider;
    this.successThreshold = successThreshold;
  }

  @Override
  public HttpProbeConfig get(String workspaceId, Server server)
      throws InternalInfrastructureException {
    return get(EnvironmentContext.getCurrent().getSubject().getUserId(), workspaceId, server);
  }

  @Override
  public HttpProbeConfig get(String userId, String workspaceId, Server server)
      throws InternalInfrastructureException {
    URI uri;
    Map<String, String> headers;
    try {
      uri = new URI(server.getUrl());
      headers =
          singletonMap(
              HttpHeaders.AUTHORIZATION,
              "Bearer " + machineTokenProvider.getToken(userId, workspaceId));
    } catch (URISyntaxException e) {
      throw new InternalInfrastructureException(
          "Terminal agent server liveness probe url is invalid. Error: " + e.getMessage());
    } catch (MachineTokenException e) {
      throw new InternalInfrastructureException(
          "Failed to retrieve workspace token for terminal server liveness probe. Error: "
              + e.getMessage());
    }
    String protocol;
    if ("wss".equals(uri.getScheme())) {
      protocol = "https";
    } else {
      protocol = "http";
    }
    int port;
    if (uri.getPort() == -1) {
      if ("http".equals(protocol)) {
        port = 80;
      } else {
        port = 443;
      }
    } else {
      port = uri.getPort();
    }

    String path = uri.getPath().replaceFirst("/pty$", "/liveness");
    return new HttpProbeConfig(
        port, uri.getHost(), protocol, path, headers, successThreshold, 3, 120, 10, 10);
  }
}
