/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.def.IDefinitionReference;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireUiDefMethods
{
    @Text( "Part \"{0}\" is not of type \"{1}\" as expected." )
    private static LocalizableText doesNotImplement;
    
    static
    {
        LocalizableText.init( SapphireUiDefMethods.class );
    }

    public static PartDef getPartDef( final ISapphireUiDef rootdef,
                                      final String id,
                                      final boolean searchImportedDefinitions,
                                      final Class<?> expectedType )
    {
        if( id != null )
        {
            for( PartDef def : rootdef.getPartDefs() )
            {
                if( id.equals( def.getId().text() ) )
                {
                    if( expectedType != null && ! expectedType.isAssignableFrom( def.getClass() ) )
                    {
                        final String msg = doesNotImplement.format( id, expectedType.getName() );
                        Sapphire.service( LoggingService.class ).logError( msg );
                        
                        return null;
                    }
                    
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( IDefinitionReference ref : rootdef.getImportedDefinitions() )
                {
                    final ISapphireUiDef sdef = ref.getPath().target();
                    
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
                if (id.equals(def.getId().text())) {
                    return def;
                }
            }

            if( searchImportedDefinitions )
            {
                for( IDefinitionReference ref : rootdef.getImportedDefinitions() )
                {
                    final ISapphireUiDef sdef = ref.getPath().target();
                    
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
            final String packageName = packageRef.getName().text();
            
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
    
    public static PropertyDef resolveProperty( final ISapphireUiDef def,
                                                 final String qualifiedPropertyName )
    {
        PropertyDef property = null;
        
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
                property = (PropertyDef) field.get( null ); 
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
    
}
