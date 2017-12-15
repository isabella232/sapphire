/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.FormDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PageBookDef;
import org.eclipse.sapphire.ui.def.PageBookKeyMapping;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PageBookPart extends FormPart
{
    private Map<Object,FormDef> pageDefs;
    private FormDef defaultPageDef;
    private SapphirePartContainer currentPage;
    private boolean exposePageValidationState = false;
    private SapphirePartListener childPartListener = null;
    
    @Override
    protected void init()
    {
        super.init();
        
        final PageBookDef def = (PageBookDef) this.definition;
        
        this.pageDefs = new LinkedHashMap<Object,FormDef>();
        
        for( PageBookKeyMapping page : def.getPages() )
        {
            final Object key = parsePageKey( page.getKey().getText() );
            this.pageDefs.put( key, page );
        }
        
        this.defaultPageDef = def.getDefaultPage();
        
        if( this.defaultPageDef.getContent().isEmpty() )
        {
            this.defaultPageDef = initDefaultPageDef();
        }
    }
    
    protected FormDef initDefaultPageDef()
    {
        final ISapphireUiDef root = ISapphireUiDef.TYPE.instantiate();
        return (FormDef) root.getPartDefs().addNewElement( FormDef.TYPE );
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayoutData( gdhspan( ( getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
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
                
                if( PageBookPart.this.currentPage != null )
                {
                    PageBookPart.this.currentPage.render( new SapphireRenderingContext( PageBookPart.this, context, composite ) );
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
    
    public final SapphirePartContainer getCurrentPage()
    {
        return this.currentPage;
    }

    protected final void changePage( final IModelElement modelElementForPage,
                                     final Object pageKey )
    {
        FormDef pageDef = this.defaultPageDef;
        
        if( pageKey != null )
        {
            for( Map.Entry<Object,FormDef> entry : this.pageDefs.entrySet() )
            {
                if( entry.getKey().equals( pageKey ) )
                {
                    pageDef = entry.getValue();
                    break;
                }
            }
        }
        
        changePage( modelElementForPage, pageDef );
    }

    private void changePage( final IModelElement modelElementForPage,
                             final FormDef pageDef )
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
            this.currentPage = (SapphirePartContainer) create( this, modelElementForPage, pageDef, this.params );
            
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
    protected Status computeValidationState()
    {
        if( this.exposePageValidationState == true )
        {
            final SapphirePartContainer currentPage = getCurrentPage();
            
            if( currentPage != null )
            {
                return currentPage.getValidationState();
            }
        }
        
        return Status.createOkStatus();
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
                    public void handleValidateStateChange( final Status oldValidateState,
                                                           final Status newValidationState )
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
