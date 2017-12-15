/******************************************************************************
 * Copyright (c) 2011 Oracle
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

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.PLUGIN_ID;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.def.IDefinitionReference;
import org.eclipse.sapphire.ui.def.IImportDirective;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireDialogDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.ISapphireWizardDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireUiDefMethods
{
    public static List<ISapphireUiDef> getImportedDefinitions( final ISapphireUiDef def )
    {
        final List<ISapphireUiDef> result = new ArrayList<ISapphireUiDef>();
        
        for( IImportDirective importDirective : def.getImportDirectives() )
        {
            final String bundleId = importDirective.getBundle().getText();
            
            if( bundleId != null )
            {
                for( IDefinitionReference ref : importDirective.getDefinitions() )
                {
                    final String path = ref.getPath().getText();
                    
                    if( path != null )
                    {
                        final ISapphireUiDef referencedDefinition = SapphireUiDefFactory.load( bundleId, path );
                        
                        if( referencedDefinition != null )
                        {
                            result.add( referencedDefinition );
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public static ISapphireCompositeDef getCompositeDef( final ISapphireUiDef rootdef,
                                                         final String id,
                                                         final boolean searchImportedDefinitions )
    {
        if( id != null )
        {
            for( ISapphireCompositeDef def : rootdef.getCompositeDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions() )
                {
                    final ISapphireCompositeDef def = importedDefinition.getCompositeDef( id, true );
                    
                    if( def != null )
                    {
                        return def;
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

            if (searchImportedDefinitions) {
                for (ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions()) {
                    final ISapphireDocumentationDef def = importedDefinition.getDocumentationDef(id, true);

                    if (def != null) {
                        return def;
                    }
                }
            }
        }

        return null;
    }

    public static ISapphireDialogDef getDialogDef( final ISapphireUiDef rootdef,
                                                   final String id,
                                                   final boolean searchImportedDefinitions )
    {
        if( id != null )
        {
            for( ISapphireDialogDef def : rootdef.getDialogDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions() )
                {
                    final ISapphireDialogDef def = importedDefinition.getDialogDef( id, true );
                    
                    if( def != null )
                    {
                        return def;
                    }
                }
            }
        }
        
        return null;
    }
    
    public static ISapphireWizardDef getWizardDef( final ISapphireUiDef rootdef,
                                                   final String id,
                                                   final boolean searchImportedDefinitions )
    {
        if( id != null )
        {
            for( ISapphireWizardDef def : rootdef.getWizardDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions() )
                {
                    final ISapphireWizardDef def = importedDefinition.getWizardDef( id, true );
                    
                    if( def != null )
                    {
                        return def;
                    }
                }
            }
        }
        
        return null;
    }
    
    public static IMasterDetailsTreeNodeDef getMasterDetailsTreeNodeDef( final ISapphireUiDef rootdef,
                                                                         final String id,
                                                                         final boolean searchImportedDefinitions )
    {
        if( id != null )
        {
            for( IMasterDetailsTreeNodeDef def : rootdef.getMasterDetailsTreeNodeDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions() )
                {
                    final IMasterDetailsTreeNodeDef def = importedDefinition.getMasterDetailsTreeNodeDef( id, true );
                    
                    if( def != null )
                    {
                        return def;
                    }
                }
            }
        }
        
        return null;
    }
    
    public static IMasterDetailsTreeNodeFactoryDef getMasterDetailsTreeNodeFactoryDef( final ISapphireUiDef rootdef,
                                                                                       final String id,
                                                                                       final boolean searchImportedDefinitions )
    {
        if( id != null )
        {
            for( IMasterDetailsTreeNodeFactoryDef def : rootdef.getMasterDetailsTreeNodeFactoryDefs() )
            {
                if( id.equals( def.getId().getText() ) )
                {
                    return def;
                }
            }
            
            if( searchImportedDefinitions )
            {
                for( ISapphireUiDef importedDefinition : rootdef.getImportedDefinitions() )
                {
                    final IMasterDetailsTreeNodeFactoryDef def = importedDefinition.getMasterDetailsTreeNodeFactoryDef( id, true );
                    
                    if( def != null )
                    {
                        return def;
                    }
                }
            }
        }
        
        return null;
    }
    
    public static Class<?> resolveClass( final ISapphireUiDef def,
                                         final String className )
    {
        Class<?> cl 
            = ImportDirectiveMethods.resolveClass( className, PLUGIN_ID, PLUGIN_ID + ".actions",
                                                   PLUGIN_ID + ".listeners", PLUGIN_ID + ".xml" );

        if( cl == null )
        {
            for( IImportDirective directive : def.getImportDirectives() )
            {
                cl = directive.resolveClass( className );
                
                if( cl != null )
                {
                    break;
                }
            }
        }
        
        return cl;
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
            final Class<?> cl = def.resolveClass( className );
            
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
    
    public static ImageDescriptor resolveImage( final ISapphireUiDef def,
                                                final String imagePath )
    {
        ImageDescriptor img = null;
        
        for( IImportDirective directive : def.getImportDirectives() )
        {
            img = directive.resolveImage( imagePath );
            
            if( img != null )
            {
                break;
            }
        }
        
        return img;
    }
    
}
