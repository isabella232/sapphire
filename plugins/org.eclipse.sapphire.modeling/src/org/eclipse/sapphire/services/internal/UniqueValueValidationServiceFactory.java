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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.UniqueValueValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class UniqueValueValidationServiceFactory extends ServiceFactory
{
    @Override
    public boolean applicable( final ServiceContext context,
                               final Class<? extends Service> service )
    {
        final ValueProperty property = context.find( ValueProperty.class );
        final IModelElement element = context.find( IModelElement.class );
        return ( property != null && property.hasAnnotation( NoDuplicates.class ) && element.parent() instanceof ModelElementList );
    }

    @Override
    public Service create( final ServiceContext context,
                           final Class<? extends Service> service )
    {
        return new UniqueValueValidationService();
    }
    
}
