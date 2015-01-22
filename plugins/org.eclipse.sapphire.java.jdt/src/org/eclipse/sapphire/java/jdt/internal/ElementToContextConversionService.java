/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.Element;

/**
 * ConversionService implementation for Element to Context conversions when the resource is inside a Java project.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementToContextConversionService extends ConversionService<Element,Context>
{
    public ElementToContextConversionService()
    {
        super( Element.class, Context.class );
    }

    @Override
    public Context convert( final Element element )
    {
        final IProject project = element.adapt( IProject.class );
        
        if( isJavaProject( project ) )
        {
            return new JavaProjectContext( JavaCore.create( project ) );
        }
        
        return null;
    }
    
    private static boolean isJavaProject( final IProject project )
    {
        try
        {
            return ( project != null && project.hasNature( JavaCore.NATURE_ID ) );
        }
        catch( final CoreException e )
        {
            // Ignore exception and treat the project as not a Java project.
        }
        
        return false;
    }

}
