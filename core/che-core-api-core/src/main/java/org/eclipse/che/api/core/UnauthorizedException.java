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
package org.eclipse.che.api.core;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * A {@code UnauthorizedException} is thrown when caller isn't authorized to access some resource.
 *
 * <p>Typically in REST API such errors are converted in HTTP response with status 401.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends ApiException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(ServiceError serviceError) {
    super(serviceError);
  }

  public UnauthorizedException(String message, int errorCode, Map<String, String> attributes) {
    super(
        newDto(ExtendedError.class)
            .withMessage(message)
            .withErrorCode(errorCode)
            .withAttributes(attributes));
  }

  public UnauthorizedException(String message, int errorCode) {
    this(message, errorCode, Collections.emptyMap());
  }
}
