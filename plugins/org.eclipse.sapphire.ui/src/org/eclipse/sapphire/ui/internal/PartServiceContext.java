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

package org.eclipse.sapphire.ui.internal;

import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactoryProxy;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.def.ServiceDef;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.sapphire.util.ReadOnlySetFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PartServiceContext extends ServiceContext
{
    private final ISapphirePart part;
    
    public PartServiceContext( final ISapphirePart part )
    {
        super( "Sapphire.Part", null );
        
        this.part = part;
    }
    
    @Override
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type.isInstance( this.part ) )
            {
                obj = type.cast( this.part );
            }
            else if( IModelElement.class.isAssignableFrom( type ) )
            {
                final IModelElement element = this.part.getLocalModelElement();
                
                if( element != null )
                {
                    if( type == IModelElement.class )
                    {
                        obj = type.cast( element );
                    }
                    else
                    {
                        obj = element.nearest( type );
                    }
                }
            }
        }
        
        return obj;
    }

    @Override
    protected List<ServiceFactoryProxy> local()
    {
        final ReadOnlyListFactory<ServiceFactoryProxy> local = ReadOnlyListFactory.create();
        final PartDef partDef = this.part.definition();
        
        for( ServiceDef serviceDef : partDef.getServices() )
        {
            final Class<? extends Service> serviceImplClass = resolve( serviceDef.getImplementation() );
            
            final ReadOnlySetFactory<String> overridesSetFactory = ReadOnlySetFactory.create();
            
            for( ServiceDef.Override override : serviceDef.getOverrides() )
            {
                String id = override.getId().getText();
                
                if( id != null )
                {
                    id = id.trim();
                    
                    if( id.length() > 0 )
                    {
                        overridesSetFactory.add( id );
                    }
                }
            }
            
            final Set<String> overrides = overridesSetFactory.export();
            
            if( serviceImplClass != null )
            {
                final ServiceFactoryProxy proxy = new ServiceFactoryProxy()
                {
                    @Override
                    public String id()
                    {
                        return serviceImplClass.getName();
                    }
                    
                    @Override
                    public Class<? extends Service> type()
                    {
                        return serviceImplClass;
                    }

                    @Override
                    public Set<String> overrides()
                    {
                        return overrides;
                    }

                    @Override
                    protected Service createHandOff( ServiceContext context,
                                                     Class<? extends Service> service )
                    {
                        Service instance = null;
                        
                        try
                        {
                            instance = serviceImplClass.newInstance();
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                        }
                        
                        return instance;
                    }
                    
                };
                
                local.add( proxy );
            }
        }
        
        return local.export();
    }
    
    @SuppressWarnings( "unchecked" )
    private static <T> Class<T> resolve( final ReferenceValue<JavaTypeName,JavaType> ref )
    {
        final JavaType type = ref.resolve();
        return ( type != null ? (Class<T>) type.artifact() : null );
    }
    
}
