/******************************************************************************
 * Copyright (c) 2013 Oracle
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

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
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

public abstract class PageBookPart extends FormComponentPart
{
    private static FormDef systemDefaultPageDef;
    
    private Map<Object,FormDef> pageDefs;
    private FormDef defaultPageDef;
    private FormPart currentPage;
    private boolean exposePageValidationState = false;
    private Listener childPartListener = null;
    
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
        if( systemDefaultPageDef == null )
        {
            final ISapphireUiDef root = ISapphireUiDef.TYPE.instantiate();
            systemDefaultPageDef = (FormDef) root.getPartDefs().insert( FormDef.TYPE );
        }
        
        return systemDefaultPageDef;
    }
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        final Function function = new Function()
        {
            @Override
            public String name()
            {
                return "VisibleIfChildrenVisible";
            }

            @Override
            public FunctionResult evaluate( final FunctionContext context )
            {
                return new FunctionResult( this, context )
                {
                    @Override
                    protected void init()
                    {
                        final Listener pageVisibilityListener = new FilteredListener<PartVisibilityEvent>()
                        {
                            @Override
                            protected void handleTypedEvent( final PartVisibilityEvent event )
                            {
                                refresh();
                            }
                        };
                        
                        PageBookPart.this.attach
                        (
                            new FilteredListener<PageChangedEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final PageChangedEvent event )
                                {
                                    final FormPart page = getCurrentPage();
                                    
                                    if( page != null )
                                    {
                                        page.attach( pageVisibilityListener );
                                    }
                                    
                                    refresh();
                                }
                            }
                        );
                        
                        final FormPart page = getCurrentPage();
                        
                        if( page != null )
                        {
                            page.attach( pageVisibilityListener );
                        }
                    }

                    @Override
                    protected Object evaluate()
                    {
                        boolean visible = false;
                        
                        final FormPart page = getCurrentPage();
                        
                        if( page != null )
                        {
                            visible = page.visible();
                        }
                        
                        return visible;
                    }
                };
            }
        };
        
        function.init();
        
        final Function base = super.initVisibleWhenFunction();
        
        if( base == null )
        {
            return function;
        }
        else
        {
            return AndFunction.create( base, function );
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayoutData( gdhspan( ( getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        composite.setLayout( glayout( 2, 0, 0 ) );
        context.adapt( composite );
        
        final Listener pageChangeListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof PageChangedEvent )
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
            }
        };
        
        attach( pageChangeListener );
        
        composite.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    detach( pageChangeListener );
                }
            }
        );
        
        if( this.currentPage != null )
        {
            this.currentPage.render( new SapphireRenderingContext( this, context, composite ) );
        }
    }
    
    public final FormPart getCurrentPage()
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
        
        final FormPart pageToDispose = this.currentPage;

        this.currentPage = null;
        
        if( pageDef != null )
        {
            this.currentPage = createPagePart( modelElementForPage, pageDef );
            
            if( this.childPartListener != null )
            {
                this.currentPage.attach( this.childPartListener );
            }
        }
        
        if( pageToDispose != null )
        {
            pageToDispose.dispose();
        }
        
        refreshValidation();
        
        broadcast( new PageChangedEvent( this ) );
    }
    
    protected abstract Object parsePageKey( final String pageKeyString );
    
    protected FormPart createPagePart( final IModelElement modelElementForPage,
                                       final FormDef pageDef )
    {
        return (FormPart) create( this, modelElementForPage, pageDef, this.params );
    }
    
    @Override
    protected Status computeValidation()
    {
        if( this.exposePageValidationState == true )
        {
            final FormPart currentPage = getCurrentPage();
            
            if( currentPage != null )
            {
                return currentPage.validation();
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
                this.childPartListener = new FilteredListener<PartValidationEvent>()
                {
                    @Override
                    protected void handleTypedEvent( PartValidationEvent event )
                    {
                        refreshValidation();
                    }
                };
                
                if( this.currentPage != null )
                {
                    this.currentPage.attach( this.childPartListener );
                }
            }
            else
            {
                this.childPartListener = null;
            }
            
            refreshValidation();
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
    
    public static final class PageChangedEvent extends PartEvent
    {
        public PageChangedEvent( final SapphirePart part )
        {
            super( part );
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
