/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller.transform;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ProcessType;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public interface TransformationContext {

    /**
     * Get the transformation target.
     *
     * @return the transformation target
     */
    TransformationTarget getTarget();

    /**
     * Get the type of process in which this operation is executing.
     *
     * @return the process type. Will not be {@code null}
     */
    ProcessType getProcessType();

    /**
     * Gets the running mode of the process.
     *
     * @return the running mode. Will not be {@code null}
     */
    RunningMode getRunningMode();

    /**
     * Get the management resource registration.
     *
     * @param address the path address
     * @return the registration
     */
    ImmutableManagementResourceRegistration getResourceRegistration(PathAddress address);

    /**
     * Get the management resource registration.
     *
     * @param address the path address
     * @return the registration
     */
    ImmutableManagementResourceRegistration getResourceRegistrationFromRoot(PathAddress address);

    /**
     * Read a model resource.
     *
     * @param address the path address
     * @return a read-only resource
     */
    Resource readResource(PathAddress address);

    /**
     * Read a model resource from the root.
     *
     * @param address the path address
     * @return the read-only resource
     */
    Resource readResourceFromRoot(PathAddress address);

    /**
     * Returns Transformers logger that must be used for reporting any problems with transformation
     *
     * @return TransformersLogger associated with target host
     */
    TransformersLogger getLogger();


    /**
     * Return {@code true} if we should skip the runtime check for slave HCs set up to ignore
     * domain resources which do not affect their servers
     *
     * @return whether to skip or not
     */
    boolean isSkipRuntimeIgnoreCheck();
}
