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

package org.eclipse.sapphire.java.jdt.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.java.jdt.JdtJavaType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JdtJavaTypeReferenceService extends JavaTypeReferenceService
{
    private final IJavaProject project;
    private IElementChangedListener listener;
    
    public JdtJavaTypeReferenceService( final IProject project )
    {
        this( JavaCore.create( project ) );
    }

    public JdtJavaTypeReferenceService( final IJavaProject project )
    {
        this.project = project;
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        this.listener = new IElementChangedListener()
        {
            public void elementChanged( final ElementChangedEvent event )
            {
                final IProject project = JdtJavaTypeReferenceService.this.project.getProject();
                
                if( ! project.exists() )
                {
                    // Project has been deleted, but the model has not yet been disposed. Might as well remove
                    // the listener at this point.
                    
                    JavaCore.removeElementChangedListener( this );
                }
                else if( project.isAccessible() )
                {
                    final Thread thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            element.refresh( property );
                        }
                    };
                    
                    thread.start();
                }
            }
        };
        
        JavaCore.addElementChangedListener( this.listener, ElementChangedEvent.POST_CHANGE );
    }

    @Override
    public JavaType resolve( final String name )
    {
        if( name.trim().length() == 0 || name.startsWith( "." ) || name.endsWith( "." ) )
        {
            return null;
        }
        
        try
        {
            final String n = name.replace( '$', '.' );
            final IType type = this.project.findType( n );
            
            if( type != null && type.exists() && ! type.isAnonymous() )
            {
                return new JdtJavaType( type );
            }
        }
        catch( JavaModelException e )
        {
            LoggingService.log( e );
        }
        
        return null;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        JavaCore.removeElementChangedListener( this.listener );
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final IModelElement element = context.find( IModelElement.class );
            final ValueProperty property = context.find( ValueProperty.class );
            final Reference referenceAnnotation = property.getAnnotation( Reference.class );

            if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
            {
                final IProject project = element.adapt( IProject.class );
                
                if( project != null )
                {
                    try
                    {
                        for( String nature : project.getDescription().getNatureIds() )
                        {
                            if( nature.equals( JavaCore.NATURE_ID ) )
                            {
                                return true;
                            }
                        }
                    }
                    catch( CoreException e )
                    {
                        LoggingService.log( e );
                    }
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            final IProject project = context.find( IModelElement.class ).adapt( IProject.class );
            return new JdtJavaTypeReferenceService( project );
        }
    }
    
}
