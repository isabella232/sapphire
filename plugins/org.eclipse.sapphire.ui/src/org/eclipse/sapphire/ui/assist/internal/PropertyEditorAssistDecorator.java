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

package org.eclipse.sapphire.ui.assist.internal;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_ASSIST_CONTRIBUTORS;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SUPPRESS_ASSIST_CONTRIBUTORS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
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
    private static final List<PropertyEditorAssistContributor> SYSTEM_CONTRIBUTORS
        = new ArrayList<PropertyEditorAssistContributor>();
    
    static
    {
        SYSTEM_CONTRIBUTORS.add( new InfoSectionAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new FactsAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new ProblemsSectionAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new ActionsSectionAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new ResetActionsAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new RestoreInitialValueActionsAssistContributor() );
        SYSTEM_CONTRIBUTORS.add( new ShowInSourceActionAssistContributor() );
    }
    
    private static final ImageDescriptor IMG_ASSIST
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "Assist.png" );
    
    private static final ImageDescriptor IMG_ASSIST_FAINT
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "AssistFaint.png" );
    
    private static final ImageDescriptor IMG_ASSIST_CLEAR
        = SwtRendererUtil.createImageDescriptor( PropertyEditorAssistContext.class, "AssistClear.png" );
    
    private final SapphirePart part;
    private IModelElement element;
    private final ModelProperty property;
    private final SapphireRenderingContext context;
    private final Label control;
    private Control primary;
    private final Collection<String> contributorsToSuppress;
    private final Collection<Class<?>> additionalContributors;
    private PropertyEditorAssistContext assistContext;
    private Status problem;
    private boolean mouseOverEditorControl;
    private EditorControlMouseTrackListener mouseTrackListener;
    private ModelPropertyListener modelPropertyListener;
    
    public PropertyEditorAssistDecorator( final SapphirePropertyEditor part,
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
        this.property = property;
        this.context = context;
        this.mouseOverEditorControl = false;
        this.mouseTrackListener = new EditorControlMouseTrackListener();
        
        final ISapphirePartDef def = this.part.getDefinition();
        
        this.contributorsToSuppress = new ArrayList<String>();
        final String contributorsToSuppressStr = def.getHint( HINT_SUPPRESS_ASSIST_CONTRIBUTORS );
        
        if( contributorsToSuppressStr != null )
        {
            for( String segment : contributorsToSuppressStr.split( "," ) )
            {
                this.contributorsToSuppress.add( segment.trim() );
            }
        }
        
        this.additionalContributors = new ArrayList<Class<?>>();
        final String additionalContributorsStr = def.getHint( HINT_ASSIST_CONTRIBUTORS );
        
        if( additionalContributorsStr != null )
        {
            final ISapphireUiDef rootdef = def.nearest( ISapphireUiDef.class );
            
            for( String segment : additionalContributorsStr.split( "," ) )
            {
                final Class<?> cl = rootdef.resolveClass( segment.trim() );
                
                if( cl != null )
                {
                    this.additionalContributors.add( cl );
                }
            }
        }
        
        this.modelPropertyListener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
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
        
        element( element );
        
        final SapphireActionGroup actions = part.getActions();
        final SapphireAction assistAction = actions.getAction( SapphireActionSystem.ACTION_ASSIST );
        
        final SapphireActionHandler assistActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final SapphireRenderingContext context )
            {
                openAssistDialog();
                return null;
            }
        };
        
        assistActionHandler.init( assistAction, null );
        assistAction.addHandler( assistActionHandler );
        
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
                    element( null );
                    assistAction.removeHandler( assistActionHandler );
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
    
    public void element( final IModelElement element )
    {
        if( this.element != null )
        {
            this.element.removeListener( this.modelPropertyListener, this.property.getName() );
        }
        
        this.element = element;
        
        if( this.element != null )
        {
            this.element.addListener( this.modelPropertyListener, this.property.getName() );
        }
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
        final boolean enabled  = ( this.element == null ? false : this.element.isPropertyEnabled( this.property ) );
        
        if( enabled )
        {
            if( this.property instanceof ValueProperty )
            {
                final Value<?> value = this.element.read( (ValueProperty) this.property );
                this.problem = value.validate();
            }
            else if( this.property instanceof ListProperty )
            {
                final ModelElementList<?> list = this.element.read( (ListProperty) this.property );
                this.problem = list.validate();
            }
            else if( this.property instanceof ElementProperty )
            {
                final ModelElementHandle<?> handle = this.element.read( (ElementProperty) this.property );
                this.problem = handle.validate( false );
            }
            else
            {
                throw new IllegalStateException( this.property.getClass().getName() );
            }
            
            this.assistContext = new PropertyEditorAssistContext( this.part, this.element, this.property, this.context );
            
            final List<PropertyEditorAssistContributor> contributors 
                = new ArrayList<PropertyEditorAssistContributor>( SYSTEM_CONTRIBUTORS );
            
            contributors.add( new ProblemsAssistContributor( this.problem ) );
            
            for( String id : this.contributorsToSuppress )
            {
                for( Iterator<PropertyEditorAssistContributor> itr = contributors.iterator(); itr.hasNext(); )
                {
                    final PropertyEditorAssistContributor contributor = itr.next();
                    
                    if( contributor.getId().equals( id ) )
                    {
                        itr.remove();
                        break;
                    }
                }
            }
            
            for( Class<?> cl : this.additionalContributors )
            {
                try
                {
                    contributors.add( (PropertyEditorAssistContributor) cl.newInstance() );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }

            Collections.sort
            ( 
                contributors, 
                new Comparator<PropertyEditorAssistContributor>()
                {
                    public int compare( final PropertyEditorAssistContributor c1,
                                        final PropertyEditorAssistContributor c2 )
                    {
                        return ( c1.getPriority() - c2.getPriority() ); 
                    }
                }
            );
            
            for( PropertyEditorAssistContributor c : contributors )
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
    
    private class EditorControlMouseTrackListener
    
        extends MouseTrackAdapter
        
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
