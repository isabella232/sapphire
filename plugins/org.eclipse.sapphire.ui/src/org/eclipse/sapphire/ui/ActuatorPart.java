/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [377989] ActuatorPart background color does match parent 
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.sizeOfImage;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.ActuatorDef;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.ImageReference;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class ActuatorPart extends SapphirePart
{
    private SapphireAction action;
    private String actionId;
    private Listener actionListener;
    private SapphireActionHandler actionHandler;
    private String actionHandlerId;
    private Listener actionHandlerListener;
    private boolean showLabel;
    private FunctionResult labelFunctionResult;
    private boolean showImage;
    private List<FunctionResult> imageFunctionResults;
    private String style;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ActuatorDef def = definition();
        
        this.actionId = def.getActionId().getContent();
        this.actionHandlerId = def.getActionHandlerId().getContent();
        
        final IModelElement element = getLocalModelElement();
        
        this.showLabel = def.getShowLabel().getContent();
        
        if( this.showLabel )
        {
            this.labelFunctionResult = initExpression
            (
                element,
                def.getLabel().getContent(), 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        broadcast( new LabelChangedEvent( ActuatorPart.this ) );
                    }
                }
            );
        }
        
        this.showImage = def.getShowImage().getContent();
        
        if( this.showImage )
        {
            final ReadOnlyListFactory<FunctionResult> imageFunctionResultsFactory = ReadOnlyListFactory.create();
            
            for( ImageReference imageReference : def.getImages() )
            {
                final FunctionResult imageFunctionResult = initExpression
                (
                    element,
                    imageReference.getImage().getContent(),
                    ImageData.class,
                    null,
                    new Runnable()
                    {
                        public void run()
                        {
                            broadcast( new ImageChangedEvent( ActuatorPart.this ) );
                        }
                    }
                );
                
                imageFunctionResultsFactory.add( imageFunctionResult );
            }
            
            this.imageFunctionResults = imageFunctionResultsFactory.export();
        }
        
        this.style = def.getStyle().getContent();
    }
    
    private void refreshActionHandler()
    {
        if( this.actionHandler != null )
        {
            this.actionHandler.detach( this.actionHandlerListener );
            this.actionHandler = null;
        }
        
        final SapphireAction action = action();
        
        if( this.actionHandlerId == null )
        {
            this.actionHandler = action.getFirstActiveHandler();
        }
        else
        {
            for( SapphireActionHandler h : action.getActiveHandlers() )
            {
                if( h.getId().equalsIgnoreCase( this.actionHandlerId ) )
                {
                    this.actionHandler = h;
                    break;
                }
            }
        }
        
        if( this.actionHandler != null )
        {
            if( this.actionHandlerListener == null )
            {
                this.actionHandlerListener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof SapphireActionHandler.EnablementChangedEvent )
                        {
                            broadcast( new EnablementChangedEvent( ActuatorPart.this ) );
                        }
                    }
                };
            }
            
            this.actionHandler.attach( this.actionHandlerListener );
        }
    }
    
    @Override
    public ActuatorDef definition()
    {
        return (ActuatorDef) super.definition();
    }

    public SapphireAction action()
    {
        if( this.action == null )
        {
            this.action = getAction( this.actionId );
            
            this.actionListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof SapphireAction.HandlersChangedEvent )
                    {
                        refreshActionHandler();
                    }
                    else if( event instanceof SapphireAction.LabelChangedEvent )
                    {
                        broadcast( new LabelChangedEvent( ActuatorPart.this ) );
                    }
                    else if( event instanceof SapphireAction.ImagesChangedEvent )
                    {
                        broadcast( new ImageChangedEvent( ActuatorPart.this ) );
                    }
                }
            };
            
            this.action.attach( this.actionListener );
        }
        
        return this.action;
    }
    
    public SapphireActionHandler handler()
    {
        if( this.actionHandler == null )
        {
            refreshActionHandler();
        }
        
        return this.actionHandler;
    }
    
    public boolean enabled()
    {
        final SapphireActionHandler handler = handler();
        return ( handler != null && handler.isEnabled() );
    }

    public String label()
    {
        return label( CapitalizationType.NO_CAPS );
    }
    
    public String label( final CapitalizationType capitalizationType )
    {
        return label( capitalizationType, false );
    }
    
    public String label( final CapitalizationType capitalizationType,
                         final boolean includeMnemonic )
    {
        if( this.showLabel )
        {
            String label = (String) this.labelFunctionResult.value();
            
            if( label == null )
            {
                final SapphireAction action = action();
                
                if( action != null )
                {
                    label = action.getLabel();
                }
            }
            
            final LocalizationService localizationService = this.definition.adapt( LocalizationService.class );
            
            return localizationService.text( label, capitalizationType, includeMnemonic );
        }

        return null;
    }
    
    public ImageData image( final int size )
    {
        if( this.showImage )
        {
            for( FunctionResult imageFunctionResult : this.imageFunctionResults )
            {
                final ImageData image = (ImageData) imageFunctionResult.value();
                
                if( image != null && sizeOfImage( image ) == size )
                {
                    return image;
                }
            }
            
            final SapphireAction action = action();
            
            if( action != null )
            {
                return action.getImage( size );
            }
        }
        
        return null;
    }
    
    public String style()
    {
        return this.style;
    }
    
    public void render( final SapphireRenderingContext context )
    {
        final SapphireActionGroup actions = getActions( getMainActionContext() );
        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( context, actions );
        final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
        
        final ActuatorDef def = definition();
        final Composite parent = context.getComposite();
        
        final HorizontalAlignment hAlign = def.getHorizontalAlignment().getContent();
        final int hAlignCode = ( hAlign == HorizontalAlignment.LEFT ? SWT.LEFT : ( hAlign == HorizontalAlignment.RIGHT ? SWT.RIGHT : SWT.CENTER ) );
        
        final int hSpan = ( def.getSpanBothColumns().getContent() ? 2 : 1 );
        
        if( hSpan == 1 )
        {
            final Label spacer = new Label( parent, SWT.NONE );
            spacer.setLayoutData( gd() );
            spacer.setText( EMPTY_STRING );
            context.adapt( spacer );
        }
        
        if( style().equals( "Sapphire.Actuator.Button" ) )
        {
            final Button button = new Button( parent, SWT.PUSH );
            button.setLayoutData( gdhspan( gdhindent( gdhalign( gd(), hAlignCode ), 8 ), hSpan ) );
            
            keyboardActionPresentation.attach( button );
            
            final String label = label( CapitalizationType.TITLE_STYLE, true );
            
            if( label != null )
            {
                button.setText( label );
            }
            
            final ImageData image = image( 16 );
            
            if( image != null )
            {
                button.setImage( getImageCache().getImage( image ) );
            }
            
            button.addSelectionListener
            (
                new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( SelectionEvent e )
                    {
                        final SapphireActionHandler handler = handler();
                        
                        if( handler != null )
                        {
                            handler.execute( context );
                        }
                    }
                }
            );
            
            button.setEnabled( enabled() );
            
            final Listener listener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof EnablementChangedEvent )
                    {
                        button.setEnabled( enabled() );
                    }
                    else if( event instanceof LabelChangedEvent )
                    {
                        final String label = label( CapitalizationType.TITLE_STYLE );
                        button.setText( label == null ? EMPTY_STRING : label );
                        button.getParent().layout( true, true );
                    }
                    else if( event instanceof ImageChangedEvent )
                    {
                        button.setImage( getImageCache().getImage( image( 16 ) ) );
                    }
                }
            };
            
            attach( listener );
                
            button.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        actionPresentationManager.dispose();
                        detach( listener );
                    }
                }
            );
        }
        else
        {
            final ImageData image = image( 16 );
            
            final Composite composite = new Composite( parent, SWT.NONE );
            composite.setLayoutData( gdhalign( gdhindent( gdhspan( gd(), hSpan ), 8 ), hAlignCode ) );
            composite.setLayout( glayout( ( image == null ? 1 : 2 ), 0, 0 ) );
            context.adapt(composite);

            final Label imageControl;
            
            if( image != null )
            {
                imageControl = new Label( composite, SWT.NONE );
                imageControl.setImage( getImageCache().getImage( image ) );
                imageControl.setLayoutData( gdvalign( gd(), SWT.CENTER ) );
                imageControl.setEnabled( enabled() );
                context.adapt(imageControl);
            }
            else
            {
                imageControl = null;
            }
            
            final SapphireFormText text = new SapphireFormText( composite, SWT.NONE );
            text.setLayoutData( gdvalign( gdhfill(), SWT.CENTER ) );
            
            context.adapt( text );
            keyboardActionPresentation.attach( text );
            
            String label = label( CapitalizationType.FIRST_WORD_ONLY );
            label = ( label == null ? Resources.labelNotSpecified : label );
            
            final StringBuilder buf = new StringBuilder();
            buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
            buf.append( label );
            buf.append( "</a></p></form>" );
            
            text.setText( buf.toString(), true, false );
            
            text.addHyperlinkListener
            (
                new HyperlinkAdapter()
                {
                    @Override
                    public void linkActivated( final HyperlinkEvent event )
                    {
                        final SapphireActionHandler handler = handler();
                        
                        if( handler != null )
                        {
                            handler.execute( context );
                        }
                    }
                }
            );
            
            text.setEnabled( enabled() );
            
            final Listener listener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof EnablementChangedEvent )
                    {
                        final boolean enabled = enabled();
                        
                        if( imageControl != null )
                        {
                            imageControl.setEnabled( enabled );
                        }
                        
                        text.setEnabled( enabled );
                    }
                    else if( event instanceof LabelChangedEvent )
                    {
                        final StringBuilder buf = new StringBuilder();
                        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
                        buf.append( label( CapitalizationType.FIRST_WORD_ONLY ) );
                        buf.append( "</a></p></form>" );
                        
                        text.setText( buf.toString(), true, false );
                        
                        composite.getParent().layout( true, true );
                    }
                    else if( event instanceof ImageChangedEvent )
                    {
                        if( imageControl != null )
                        {
                            imageControl.setImage( getImageCache().getImage( image( 16 ) ) );
                        }
                    }
                }
            };
            
            attach( listener );
                
            text.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        actionPresentationManager.dispose();
                        detach( listener );
                    }
                }
            );
        }
        
        keyboardActionPresentation.render();
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_ACTUATOR );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.action != null )
        {
            this.action.detach( this.actionListener );
        }
        
        if( this.actionHandler != null )
        {
            this.actionHandler.detach( this.actionHandlerListener );
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        if( this.imageFunctionResults != null )
        {
            for( FunctionResult imageFunctionResult : this.imageFunctionResults )
            {
                imageFunctionResult.dispose();
            }
        }
    }
    
    public static final class EnablementChangedEvent extends PartEvent
    {
        public EnablementChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }

    private static final class Resources extends NLS
    {
        public static String labelNotSpecified;
        
        static
        {
            initializeMessages( ActuatorPart.class.getName(), Resources.class );
        }
    }

}
