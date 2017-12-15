/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import java.lang.reflect.Field;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.IDefinitionReference;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireUiDefMethods
{
    public static PartDef getPartDef( final ISapphireUiDef rootdef,
                                      final String id,
                                      final boolean searchImportedDefinitions,
                                      final Class<?> expectedType )
    {
        if( id != null )
        {
            for( PartDef def : rootdef.getPartDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    if( expectedType != null && ! expectedType.isAssignableFrom( def.getClass() ) )
                    {
                        final String msg = Resources.bind( Resources.doesNotImplement, id, expectedType.getName() );
                        SapphireUiFrameworkPlugin.logError( msg );
                        
                        return null;
                    }
                    
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( IDefinitionReference ref : rootdef.getImportedDefinitions() )
                {
                    final ISapphireUiDef sdef = ref.getPath().resolve();
                    
                    if( sdef != null )
                    {
                        final PartDef def = sdef.getPartDef( id, true, expectedType );
                        
                        if( def != null )
                        {
                            return def;
                        }
                    }
                }
            }
        }
        
        return null;
    }

    public static ISapphireDocumentationDef getDocumentationDef(final ISapphireUiDef rootdef, final String id,
                                                            final boolean searchImportedDefinitions) {
        if (id != null) {
            for (ISapphireDocumentationDef def : rootdef.getDocumentationDefs()) {
                if (id.equals(def.getId().getText())) {
                    return def;
                }
            }

            if( searchImportedDefinitions )
            {
                for( IDefinitionReference ref : rootdef.getImportedDefinitions() )
                {
                    final ISapphireUiDef sdef = ref.getPath().resolve();
                    
                    if( sdef != null )
                    {
                        final ISapphireDocumentationDef def = sdef.getDocumentationDef( id, true );
                        
                        if( def != null )
                        {
                            return def;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Class<?> resolveClass( final ISapphireUiDef def,
                                         final String className )
    {
        final Context context = def.adapt( Context.class );
        
        for( IPackageReference packageRef : def.getImportedPackages() )
        {
            final String packageName = packageRef.getName().getText();
            
            if( packageName != null )
            {
                final String fullClassName = packageName + "." + className;
                final Class<?> cl = context.findClass( fullClassName );
                
                if( cl != null )
                {
                    return cl;
                }
            }
        }
        
        return null;
    }
    
    public static ModelProperty resolveProperty( final ISapphireUiDef def,
                                                 final String qualifiedPropertyName )
    {
        ModelProperty property = null;
        
        if( qualifiedPropertyName != null )
        {
            final int dot = qualifiedPropertyName.indexOf( '.' );
            final String className = qualifiedPropertyName.substring( 0, dot );
            final String propertyName = qualifiedPropertyName.substring( dot + 1 );
            final Class<?> cl = resolveClass( def, className );
            
            if( cl == null )
            {
                return null;
            }
            
            try
            {
                final Field field = cl.getField( propertyName );
                property = (ModelProperty) field.get( null ); 
            }
            catch( Throwable e )
            {
                System.err.println( "Failed to resolve property: " + qualifiedPropertyName );
                e.printStackTrace();
                return null;
            }
        }
        
        return property;
    }
    
    private static final class Resources extends NLS
    {
        public static String doesNotImplement;
    
        static
        {
            initializeMessages( SapphireUiDefMethods.class.getName(), Resources.class );
        }
    }
    
}
