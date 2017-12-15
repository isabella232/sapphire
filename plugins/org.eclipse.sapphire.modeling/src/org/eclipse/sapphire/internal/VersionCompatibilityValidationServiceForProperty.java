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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * An implementation of ValidationService that produces a validation error when a property 
 * is not compatible with the version compatibility target yet contains data. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityValidationServiceForProperty extends VersionCompatibilityValidationService
{
    @Override
    protected IModelElement element()
    {
        return context( IModelElement.class );
    }

    @Override
    protected ModelProperty property()
    {
        return context( ModelProperty.class );
    }
    
    @Override
    protected boolean problem()
    {
        return ( super.problem() && ! element().empty( property() ) );
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return true;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new VersionCompatibilityValidationServiceForProperty();
        }
    }

}
