/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import org.eclipse.sapphire.modeling.Status;

/**
 * Aggregates the data from all applicable validation services in order to produce a single validation result.
 * 
 * <p>Implementations of this service are provided with Sapphire. This service is not intended to
 * be implemented by adopters.</p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ValidationAggregationService extends DataService<Status>
{
    public final Status validation()
    {
        return data();
    }

}
