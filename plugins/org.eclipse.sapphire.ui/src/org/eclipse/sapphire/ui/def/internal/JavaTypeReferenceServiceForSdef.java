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

import org.eclipse.sapphire.java.ClassBasedJavaType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaTypeReferenceServiceForSdef

    extends JavaTypeReferenceService
    
{
    @Override
    public JavaType resolve( final String name )
    {
        final ISapphireUiDef sdef = element().nearest( ISapphireUiDef.class );
        final ClassLocator locator = sdef.adapt( ClassLocator.class );
        
        if( locator != null )
        {
            for( IPackageReference packageRef : sdef.getImportedPackages() )
            {
                final String packageName = packageRef.getName().getText();
                
                if( packageName != null )
                {
                    final String fullClassName = packageName + "." + name;
                    final Class<?> cl = locator.find( fullClassName );
                    
                    if( cl != null )
                    {
                        return new ClassBasedJavaType( cl );
                    }
                }
            }
        }
        
        return null;
    }
    
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            if( property instanceof ValueProperty && property.getTypeClass() == JavaTypeName.class && element.nearest( ISapphireUiDef.class ) != null )
            {
                final Reference referenceAnnotation = property.getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
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
            return new JavaTypeReferenceServiceForSdef();
        }
    }

}
