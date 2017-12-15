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

package org.eclipse.sapphire.ui;

import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWithDirectiveHelper
{
    public static ResolvePathResult resolvePath( final IModelElement context,
                                                 final ISapphireWithDirectiveDef def,
                                                 final Map<String,String> params )
    {
        final ResolvePathResult result = new ResolvePathResult();
        
        final String pathString = def.getPath().getText();
        result.path = new ModelPath( pathString );
        
        result.element = context;
        
        for( int i = 0, n = result.path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = result.path.segment( i );
            
            if( segment instanceof ModelPath.ModelRootSegment )
            {
                result.element = (IModelElement) result.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                IModelParticle parent = result.element.parent();
                
                if( ! ( parent instanceof IModelElement ) )
                {
                    parent = parent.parent();
                }
                
                result.element = (IModelElement) parent;
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                final ModelProperty prop = SapphirePart.resolve( result.element, ( (ModelPath.PropertySegment) segment ).getPropertyName(), params );
                
                if( prop instanceof ImpliedElementProperty )
                {
                    result.element = result.element.read( (ImpliedElementProperty) prop );
                }
                else if( prop instanceof ElementProperty )
                {
                    result.property = (ElementProperty) prop;
                    
                    if( i + 1 != n )
                    {
                        throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
                    }
                }
                else
                {
                    throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
                }
            }
            else
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
            }
        }
        
        return result;
    }

    public static final class ResolvePathResult
    {
        public ModelPath path;
        public IModelElement element;
        public ElementProperty property;
    }
    
    private static final class Resources extends NLS
    {
        public static String invalidPath;
        
        static
        {
            initializeMessages( SapphireWithDirectiveHelper.class.getName(), Resources.class );
        }
    }

}
