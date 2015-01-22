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

package org.eclipse.sapphire.ui.def;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.FormComponentDef;
import org.eclipse.sapphire.ui.forms.WizardDef;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefinitionLoader
{
    private static final Map<CacheKey,SoftReference<DefinitionLoader>> cache = new HashMap<CacheKey,SoftReference<DefinitionLoader>>();
    
    private final Context context;
    private ISapphireUiDef sdef;
    
    private DefinitionLoader( final Context context )
    {
        this.context = context;
    }
    
    public static DefinitionLoader context( final Context context )
    {
        if( context == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new DefinitionLoader( context );
    }
    
    public static DefinitionLoader context( final ClassLoader loader )
    {
        if( loader == null )
        {
            throw new IllegalArgumentException();
        }
        
        return context( Context.adapt( loader ) );
    }
    
    public static DefinitionLoader context( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        return context( Context.adapt( cl ) );
    }

    public static DefinitionLoader sdef( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        return context( cl ).sdef( cl.getSimpleName() );
    }
    
    public DefinitionLoader sdef( final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( name.endsWith( ".sdef" ) || name.contains( "/" ) )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.sdef != null )
        {
            throw new IllegalStateException();
        }
        
        final CacheKey defLoaderCacheKey = new CacheKey( this.context, name );
        
        if( ! Sapphire.isDevMode() )
        {
            synchronized( cache )
            {
                final SoftReference<DefinitionLoader> defLoaderRef = cache.get( defLoaderCacheKey );
                
                if( defLoaderRef != null )
                {
                    final DefinitionLoader loader = defLoaderRef.get();
                    
                    if( loader != null )
                    {
                        return loader;
                    }
                }
            }
        }
        
        final InputStream stream = this.context.findResource( name.replace( '.', '/' ) + ".sdef" );
        
        if( stream == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Resource resource;
        
        try
        {
            try
            {
                resource = new RootXmlResource( new XmlResourceStore( new DefinitionLoaderResourceStore( stream, this.context ) ) );
            }
            catch( ResourceStoreException e )
            {
                throw new IllegalArgumentException( e );
            }
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch( IOException e ) {}
        }
        
        this.sdef = ISapphireUiDef.TYPE.instantiate( resource );
        
        if( ! Sapphire.isDevMode() )
        {
            synchronized( cache )
            {
                cache.put( defLoaderCacheKey, new SoftReference<DefinitionLoader>( this ) );
            }
        }
        
        return this;
    }
    
    public Reference<ISapphireUiDef> root()
    {
        return new Reference<ISapphireUiDef>( this, this.sdef );
    }
    
    public Reference<EditorPageDef> page()
    {
        return page( null );
    }
    
    public Reference<EditorPageDef> page( final String id )
    {
        EditorPageDef def = null;
        
        if( id == null )
        {
            for( PartDef d : this.sdef.getPartDefs() )
            {
                if( d instanceof EditorPageDef )
                {
                    def = (EditorPageDef) d;
                    break;
                }
            }
        }
        else
        {
            def = (EditorPageDef) this.sdef.getPartDef( id, true, EditorPageDef.class );
        }
        
        if( def == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new Reference<EditorPageDef>( this, def );
    }

    public Reference<WizardDef> wizard()
    {
        return wizard( null );
    }
    
    public Reference<WizardDef> wizard( final String id )
    {
        WizardDef def = null;
        
        if( id == null )
        {
            for( PartDef d : this.sdef.getPartDefs() )
            {
                if( d instanceof WizardDef )
                {
                    def = (WizardDef) d;
                    break;
                }
            }
        }
        else
        {
            def = (WizardDef) this.sdef.getPartDef( id, true, WizardDef.class );
        }
        
        if( def == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new Reference<WizardDef>( this, def );
    }

    public Reference<DialogDef> dialog()
    {
        return dialog( null );
    }
    
    public Reference<DialogDef> dialog( final String id )
    {
        DialogDef def = null;
        
        if( id == null )
        {
            for( PartDef d : this.sdef.getPartDefs() )
            {
                if( d instanceof DialogDef )
                {
                    def = (DialogDef) d;
                    break;
                }
            }
        }
        else
        {
            def = (DialogDef) this.sdef.getPartDef( id, true, DialogDef.class );
        }
        
        if( def == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new Reference<DialogDef>( this, def );
    }
    
    public Reference<FormComponentDef> form()
    {
        return form( null );
    }
    
    public Reference<FormComponentDef> form( final String id )
    {
        FormComponentDef def = null;
        
        if( id == null )
        {
            for( PartDef d : this.sdef.getPartDefs() )
            {
                if( d instanceof FormComponentDef )
                {
                    def = (FormComponentDef) d;
                    break;
                }
            }
        }
        else
        {
            def = (FormComponentDef) this.sdef.getPartDef( id, true, FormComponentDef.class );
        }
        
        if( def == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new Reference<FormComponentDef>( this, def );
    }
    
    @Override
    protected void finalize()
    {
        if( this.sdef != null )
        {
            this.sdef.dispose();
        }
    }
    
    public static final class Reference<T extends Element> implements Disposable
    {
        // Must reference loader to make sure it doesn't go away while this reference is still in use.
        // When the loader goes away, the sdef is disposed.
        
        @SuppressWarnings( "unused" )
        private DefinitionLoader loader;
        
        private T def;
        
        private Reference( final DefinitionLoader loader,
                           final T def )
        {
            if( loader == null )
            {
                throw new IllegalArgumentException();
            }
            
            if( def == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.loader = loader;
            this.def = def;
        }
        
        public T resolve()
        {
            if( this.def == null )
            {
                throw new IllegalStateException();
            }
            
            final Status validation = this.def.validation();
            
            if( validation.severity() == Status.Severity.ERROR )
            {
                throw new InvalidDefinitionException( validation );
            }
            
           return this.def;
        }
        
        public T resolveIgnoringValidation()
        {
            if( this.def == null )
            {
                throw new IllegalStateException();
            }
            
            return this.def;
        }
        
        @Override
        public void dispose()
        {
            this.loader = null;
            this.def = null;
        }
    }

    private static final class DefinitionLoaderResourceStore extends ByteArrayResourceStore
    {
        private final Context context;
        
        public DefinitionLoaderResourceStore( final InputStream in, final Context context ) throws ResourceStoreException
        {
            super( in );
            
            this.context = context;
        }

        @Override
        public <A> A adapt( final Class<A> adapterType )
        {
            if( adapterType == Context.class )
            {
                return adapterType.cast( this.context );
            }
            
            return super.adapt( adapterType );
        }
    }

    private static final class CacheKey
    {
        private final Context context;
        private final String name;
        
        public CacheKey( final Context context, final String name )
        {
            this.context = context;
            this.name = name;
        }

        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof CacheKey )
            {
                final CacheKey key = (CacheKey) obj;
                return EqualsFactory.start().add( this.context, key.context ).add( this.name, key.name ).result();
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return HashCodeFactory.start().add( this.context ).add( this.name ).result();
        }
    }
    
}
