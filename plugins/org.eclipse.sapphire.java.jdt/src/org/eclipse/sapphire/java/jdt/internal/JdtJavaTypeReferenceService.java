/******************************************************************************
 * Copyright (c) 2011 Oracle
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
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.annotations.Reference;

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
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
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

    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
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
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            final IProject project = element.adapt( IProject.class );
            return new JdtJavaTypeReferenceService( project );
        }
    }
    
}
