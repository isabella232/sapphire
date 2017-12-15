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

package org.eclipse.sapphire.ui.assist.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorAssistDecorator
{
    private static final List<Class<? extends PropertyEditorAssistContributor>> SYSTEM_CONTRIBUTORS
        = new ArrayList<Class<? extends PropertyEditorAssistContributor>>();
    
    static
    {
        SYSTEM_CONTRIBUTORS.add( InfoSectionAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( FactsAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( ProblemsSectionAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( ActionsSectionAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( ResetActionsAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( RestoreInitialValueActionsAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( ShowInSourceActionAssistContributor.class );
        SYSTEM_CONTRIBUTORS.add( ProblemsAssistContributor.class );
    }
    
    private static final ImageDescriptor IMG_ASSIST
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "Assist.png" );
    
    private static final ImageDescriptor IMG_ASSIST_FAINT
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "AssistFaint.png" );
    
    private static final ImageDescriptor IMG_ASSIST_CLEAR
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "AssistClear.png" );
    
    private final SapphirePart part;
    private final IModelElement element;
    private final ModelProperty property;
    private final SapphireRenderingContext context;
    private final Label control;
    private Control primary;
    private PropertyEditorAssistContext assistContext;
    private Status problem;
    private boolean mouseOverEditorControl;
    private EditorControlMouseTrackListener mouseTrackListener;
    private final Listener modelPropertyListener;
    private final SapphireAction assistAction;
    private final SapphireActionHandler assistActionHandler;
    private final List<PropertyEditorAssistContributor> contributors;
    
    public PropertyEditorAssistDecorator( final PropertyEditorPart part,
                                          final SapphireRenderingContext context,
                                          final Composite parent )
    {
        this( part, part.getLocalModelElement(), part.getProperty(), context, parent );
    }
    
    public PropertyEditorAssistDecorator( final SapphirePart part,
                                          final IModelElement element,
                                          final ModelProperty property,
                                          final SapphireRenderingContext context,
                                          final Composite parent )
    {
        this.part = part;
        this.element = element;
        this.property = property;
        this.context = context;
        this.mouseOverEditorControl = false;
        this.mouseTrackListener = new EditorControlMouseTrackListener();
        
        final PartDef def = this.part.definition();
        
        final List<Class<?>> contributorClasses = new ArrayList<Class<?>>();
        
        contributorClasses.addAll( SYSTEM_CONTRIBUTORS );
        
        final String additionalContributorsStr = def.getHint( PropertyEditorDef.HINT_ASSIST_CONTRIBUTORS );
        
        if( additionalContributorsStr != null )
        {
            final ISapphireUiDef rootdef = def.nearest( ISapphireUiDef.class );
            
            for( String segment : additionalContributorsStr.split( "," ) )
            {
                final Class<?> cl = rootdef.resolveClass( segment.trim() );
                
                if( cl != null )
                {
                    contributorClasses.add( cl );
                }
            }
        }
        
        this.contributors = new ArrayList<PropertyEditorAssistContributor>();
        
        for( Class<?> cl : contributorClasses )
        {
            try
            {
                this.contributors.add( (PropertyEditorAssistContributor) cl.newInstance() );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        final String contributorsToSuppressStr = def.getHint( PropertyEditorDef.HINT_SUPPRESS_ASSIST_CONTRIBUTORS );
        
        if( contributorsToSuppressStr != null )
        {
            for( String segment : contributorsToSuppressStr.split( "," ) )
            {
                final String id = segment.trim();

                for( Iterator<PropertyEditorAssistContributor> itr = this.contributors.iterator(); itr.hasNext(); )
                {
                    final PropertyEditorAssistContributor contributor = itr.next();
                    
                    if( contributor.getId().equals( id ) )
                    {
                        itr.remove();
                        break;
                    }
                }
            }
        }
        
        final Listener contributorListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                Display.getDefault().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            refresh();
                        }
                    }
                );
            }
        };
        
        for( PropertyEditorAssistContributor contributor : this.contributors )
        {
            try
            {
                contributor.init( this.element, this.property );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            contributor.attach( contributorListener );
        }
        
        Collections.sort
        ( 
            this.contributors, 
            new Comparator<PropertyEditorAssistContributor>()
            {
                public int compare( final PropertyEditorAssistContributor c1,
                                    final PropertyEditorAssistContributor c2 )
                {
                    return ( c1.getPriority() - c2.getPriority() ); 
                }
            }
        );
        
        this.modelPropertyListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                Display.getDefault().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            refresh();
                        }
                    }
                );
            }
        };
        
        this.element.attach( this.modelPropertyListener, this.property.getName() );
        
        this.assistAction = part.getActions().getAction( SapphireActionSystem.ACTION_ASSIST );
        
        this.assistActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final SapphireRenderingContext context )
            {
                openAssistDialog();
                return null;
            }
        };
        
        this.assistActionHandler.init( this.assistAction, null );
        this.assistAction.addHandler( this.assistActionHandler );
        
        this.control = new Label( parent, SWT.NONE );
        this.context.adapt( this.control );
        
        this.control.addMouseListener
        (
            new MouseAdapter()
            {
                @Override
                public void mouseUp( final MouseEvent event )
                {
                    openAssistDialog();
                }
            }
        );
        
        this.control.addMouseTrackListener
        (
            new EditorControlMouseTrackListener()
            {
                @Override
                public void mouseEnter( MouseEvent event )
                {
                    super.mouseEnter( event );
                    refreshImageAndCursor();
                }

                @Override
                public void mouseHover( final MouseEvent event )
                {
                    // Suppress default behavior.
                }
            }
        );
        
        this.control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    dispose();
                }
            }
        );
        
        refresh();
    }
    
    public Label control()
    {
        return this.control;
    }
    
    public SapphireRenderingContext context()
    {
        return this.context;
    }
    
    public Shell shell()
    {
        return this.context.getShell();
    }
    
    public SapphirePart part()
    {
        return this.part;
    }
    
    public IModelElement element()
    {
        return this.element;
    }
    
    public ModelProperty property()
    {
        return this.property;
    }
    
    public void addEditorControl( final Control control )
    {
        addEditorControl( control, false );
    }
    
    public void addEditorControl( final Control control,
                                  final boolean primary )
    {
        if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                addEditorControl( child );
            }
        }
        
        control.addMouseTrackListener( this.mouseTrackListener );
        
        if( primary )
        {
            this.primary = control;
        }
    }
    
    public void removeEditorControl( final Control control )
    {
        if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                removeEditorControl( child );
            }
        }
        
        control.removeMouseTrackListener( this.mouseTrackListener );
        
        if( this.primary == control )
        {
            this.primary = null;
        }
    }

    private void openAssistDialog()
    {
        if( this.assistContext != null && ! this.assistContext.isEmpty() )
        {
            final Rectangle decoratorControlBounds = this.control.getBounds();
            final Rectangle primaryControlBounds = ( this.primary == null ? null : this.primary.getBounds() );
            
            Point position;
            
            if( primaryControlBounds != null && primaryControlBounds.height < 50 )
            {
                position = new Point( primaryControlBounds.x, primaryControlBounds.y + primaryControlBounds.height + 1 );
                position = this.primary.getParent().toDisplay( position );
            }
            else
            {
                position = new Point( decoratorControlBounds.x + decoratorControlBounds.width + 2, decoratorControlBounds.y + 2 );
                position = this.control.getParent().toDisplay( position );
            }
            
            final PropertyEditorAssistDialog dialog = new PropertyEditorAssistDialog( shell(), position, this.assistContext );
            
            dialog.open();
        }
    }
    
    private void refresh()
    {
        if( this.control.isDisposed() ) 
        {
            return;
        }
        
        final boolean enabled  = this.element.enabled( this.property );
        
        if( enabled )
        {
            this.assistContext = new PropertyEditorAssistContext( this.part, this.element, this.property, this.context );
            this.problem = this.element.validation( this.property );
            
            for( PropertyEditorAssistContributor c : this.contributors )
            {
                c.contribute( this.assistContext );
            }
            
            if( this.assistContext.isEmpty() )
            {
                this.assistContext = null;
            }
            else
            {
                final Status.Severity valResultSeverity = this.problem.severity();
                
                if( valResultSeverity != Status.Severity.ERROR && valResultSeverity != Status.Severity.WARNING && valResultSeverity != Status.Severity.INFO )
                {
                    this.problem = null;
                }
            }
        }
        else
        {
            this.assistContext = null;
            this.problem = null;
        }

        refreshImageAndCursor();
    }
    
    private void refreshImageAndCursor()
    {
        if( this.control.isDisposed() ) 
        {
            return;
        }
        
        final SapphireImageCache imageCache = this.part.getImageCache();
        
        if( this.assistContext != null )
        {
            if( this.problem != null )
            {
                final String fieldDecorationId;
                
                switch( this.problem.severity() )
                {
                    case ERROR:
                    {
                        fieldDecorationId = FieldDecorationRegistry.DEC_ERROR;
                        break;
                    }
                    case WARNING:
                    {
                        fieldDecorationId = FieldDecorationRegistry.DEC_WARNING;
                        break;
                    }
                    default:
                    {
                        fieldDecorationId = FieldDecorationRegistry.DEC_INFORMATION;
                        break;
                    }
                }
                
                final FieldDecoration fieldDecoration
                    = FieldDecorationRegistry.getDefault().getFieldDecoration( fieldDecorationId );
            
                this.control.setImage( fieldDecoration.getImage() );
            }
            else
            {
                if( this.mouseOverEditorControl )
                {
                    this.control.setImage( imageCache.getImage( IMG_ASSIST ) );
                }
                else
                {
                    this.control.setImage( imageCache.getImage( IMG_ASSIST_FAINT ) );
                }
            }
            
            this.control.setVisible( true );
            this.control.setCursor( Display.getCurrent().getSystemCursor( SWT.CURSOR_HAND ) );
        }
        else
        {
            this.control.setVisible( false );
            this.control.setImage( imageCache.getImage( IMG_ASSIST_CLEAR ) );
            this.control.setCursor( null );
        }
    }
    
    private void dispose()
    {
        this.element.detach( this.modelPropertyListener, this.property.getName() );
        this.assistAction.removeHandler( this.assistActionHandler );
        
        for( PropertyEditorAssistContributor contributor : this.contributors )
        {
            try
            {
                contributor.dispose();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }
    
    private class EditorControlMouseTrackListener extends MouseTrackAdapter
    {
        @Override
        public void mouseEnter( final MouseEvent event )
        {
            PropertyEditorAssistDecorator.this.mouseOverEditorControl = true;
        }
        
        @Override
        public void mouseHover( final MouseEvent event )
        {
            refreshImageAndCursor();
        }

        @Override
        public void mouseExit( final MouseEvent event )
        {
            PropertyEditorAssistDecorator.this.mouseOverEditorControl = false;
            performedDelayedImageRefresh();
        }
        
        private void performedDelayedImageRefresh()
        {
            final Runnable op = new Runnable()
            {
                public void run()
                {
                    refreshImageAndCursor();
                }
            };
            
            final Thread thread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep( 250 );
                    }
                    catch( InterruptedException e ) {}
                    
                    Display.getDefault().asyncExec( op );
                }
            };
            
            thread.start();
        }
    };

}
