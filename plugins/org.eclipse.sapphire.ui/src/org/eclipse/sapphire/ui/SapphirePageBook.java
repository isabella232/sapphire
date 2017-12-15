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

import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphirePageBookDef;
import org.eclipse.sapphire.ui.def.ISapphirePageBookKeyMapping;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePageBook

    extends SapphirePart
    
{
    private Map<Object,ISapphireCompositeDef> pageDefs;
    private ISapphireCompositeDef defaultPageDef;
    private SapphireComposite currentPage;
    private boolean exposePageValidationState = false;
    private SapphirePartListener childPartListener = null;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphirePageBookDef def = (ISapphirePageBookDef) this.definition;
        
        this.pageDefs = new HashMap<Object,ISapphireCompositeDef>();
        
        for( ISapphirePageBookKeyMapping page : def.getPages() )
        {
            final Object key = parsePageKey( page.getKey().getText() );
            this.pageDefs.put( key, page );
        }
        
        this.defaultPageDef = def.getDefaultPage();
    }
    
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayoutData( hspan( gdhfill(), 2 ) );
        composite.setLayout( glayout( 2, 0, 0 ) );
        context.adapt( composite );
        
        final SapphirePageBookListener pageChangeListener = new SapphirePageBookListener()
        {
            @Override
            public void handlePageChange()
            {
                for( Control control : composite.getChildren() )
                {
                    control.dispose();
                }
                
                if( SapphirePageBook.this.currentPage != null )
                {
                    SapphirePageBook.this.currentPage.render( new SapphireRenderingContext( SapphirePageBook.this, context, composite ) );
                }
                
                context.layout();
            }
        };
        
        addListener( pageChangeListener );
        
        composite.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    removeListener( pageChangeListener );
                }
            }
        );
        
        if( this.currentPage != null )
        {
            this.currentPage.render( new SapphireRenderingContext( this, context, composite ) );
        }
    }
    
    public final SapphireComposite getCurrentPage()
    {
        return this.currentPage;
    }

    protected final void changePage( final IModelElement modelElementForPage,
                                     final Object pageKey )
    {
        ISapphireCompositeDef pageDef = this.defaultPageDef;
        
        for( Map.Entry<Object,ISapphireCompositeDef> entry : this.pageDefs.entrySet() )
        {
            if( entry.getKey().equals( pageKey ) )
            {
                pageDef = entry.getValue();
                break;
            }
        }
        
        changePage( modelElementForPage, pageDef );
    }

    protected final void changePage( final IModelElement modelElementForPage,
                                     final ISapphireCompositeDef pageDef )
    {
        if( modelElementForPage == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.currentPage != null )
        {
            this.currentPage.dispose();
        }
        
        if( pageDef != null )
        {
            this.currentPage = (SapphireComposite) create( this, modelElementForPage, pageDef, this.params );
            
            if( this.childPartListener != null )
            {
                this.currentPage.addListener( this.childPartListener );
            }
        }
        else
        {
            this.currentPage = null;
        }
        
        updateValidationState();
        
        for( SapphirePartListener listener : getListeners() )
        {
            if( listener instanceof SapphirePageBookListener )
            {
                ( (SapphirePageBookListener) listener ).handlePageChange();
            }
        }
    }
    
    protected abstract Object parsePageKey( final String pageKeyString );
    
    @Override
    protected IStatus computeValidationState()
    {
        if( this.exposePageValidationState == true )
        {
            final SapphireComposite currentPage = getCurrentPage();
            
            if( currentPage != null )
            {
                return currentPage.getValidationState();
            }
        }
        
        return Status.OK_STATUS;
    }
    
    protected final void setExposePageValidationState( final boolean exposePageValidationState )
    {
        if( this.exposePageValidationState != exposePageValidationState )
        {
            this.exposePageValidationState = exposePageValidationState;
            
            if( this.exposePageValidationState == true )
            {
                this.childPartListener = new SapphirePartListener()
                {
                    @Override
                    public void handleValidateStateChange( final IStatus oldValidateState,
                                                           final IStatus newValidationState )
                    {
                        updateValidationState();
                    }
                };
                
                if( this.currentPage != null )
                {
                    this.currentPage.addListener( this.childPartListener );
                }
            }
            else
            {
                this.childPartListener = null;
            }
            
            updateValidationState();
        }
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.currentPage != null )
        {
            this.currentPage.dispose();
        }
    }
    
    protected static final class ClassBasedKey
    {
        private final Class<?> cl;
        
        private ClassBasedKey( final Class<?> cl )
        {
            this.cl = cl;
        }
        
        public static ClassBasedKey create( final Class<?> cl )
        {
            return ( cl == null ? null : new ClassBasedKey( cl ) );
        }
        
        public static ClassBasedKey create( final Object obj )
        {
            return ( obj == null ? null : new ClassBasedKey( obj.getClass() ) );
        }
        
        public int hashCode()
        {
            return this.cl == null ? -1 : this.cl.hashCode();
        }
        
        public boolean equals( final Object obj )
        {
            if( ! ( obj instanceof ClassBasedKey ) )
            {
                return false;
            }
            else
            {
                final Class<?> cl2 = ( (ClassBasedKey) obj ).cl;
                return this.cl.isAssignableFrom( cl2 ) || cl2.isAssignableFrom( this.cl );
            }
        }
    }

}
