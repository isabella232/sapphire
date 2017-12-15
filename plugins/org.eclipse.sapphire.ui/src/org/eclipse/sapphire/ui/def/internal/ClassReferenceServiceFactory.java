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

package org.eclipse.sapphire.ui.def.internal;

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.PLUGIN_ID;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ReferenceService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.def.IImportDirective;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ClassReferenceServiceFactory

    extends ModelPropertyServiceFactory
    
{
    @Override
    public boolean applicable( final IModelElement element,
                               final ModelProperty property,
                               final Class<? extends ModelPropertyService> service )
    {
        if( property instanceof ValueProperty )
        {
            final Reference annotation = property.getAnnotation( Reference.class );
            
            if( annotation != null && annotation.target().equals( Class.class ) && annotation.service().equals( ReferenceService.class ) )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public ModelPropertyService create( final IModelElement element,
                                        final ModelProperty property,
                                        final Class<? extends ModelPropertyService> service )
    {
        final IModelElement root = (IModelElement) element.root();
        
        if( root instanceof ISapphireUiDef )
        {
            final ISapphireUiDef def = (ISapphireUiDef) root;
            
            return new ReferenceService()
            {
                @Override
                public Object resolve( final String reference )
                {
                    Class<?> cl 
                        = ImportDirectiveMethods.resolveClass( reference, PLUGIN_ID, PLUGIN_ID + ".actions",
                                                               PLUGIN_ID + ".listeners", PLUGIN_ID + ".xml" );
    
                    if( cl == null )
                    {
                        for( IImportDirective directive : def.getImportDirectives() )
                        {
                            cl = directive.resolveClass( reference );
                            
                            if( cl != null )
                            {
                                break;
                            }
                        }
                    }
                    
                    return cl;
                }
            };
        }
        
        return null;
    }
    
}
