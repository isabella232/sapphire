/******************************************************************************
 * Copyright (c) 2012 Oracle
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
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.services.EnablementServiceData;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ParentBasedEnablementService extends EnablementService
{
    private IModelElement parentElement;
    private ModelProperty parentProperty;
    private ModelPropertyListener listener;
    
    @Override
    protected void initEnablementService()
    {
        final IModelElement element = context( IModelElement.class );
        
        IModelParticle parent = element.parent();
        
        if( ! ( parent instanceof IModelElement ) )
        {
            parent = parent.parent();
        }
        
        this.parentElement = (IModelElement) parent;
        this.parentProperty = element.getParentProperty();
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refresh();
            }
        };
        
        this.parentElement.addListener( this.listener, this.parentProperty.getName() );
    }

    @Override
    public EnablementServiceData compute()
    {
        return new EnablementServiceData( this.parentElement.isPropertyEnabled( this.parentProperty ) );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.parentElement.removeListener( this.listener, this.parentProperty.getName() );
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return ( context.find( IModelElement.class ).getParentProperty() != null );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ParentBasedEnablementService();
        }
    }
    
}
