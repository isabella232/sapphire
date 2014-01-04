/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [377989] ActuatorPart background color does match parent 
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.sizeOfImage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.ActuatorActionHandlerEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.ImageReference;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.ActuatorButtonPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.ActuatorLinkPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class ActuatorPart extends FormComponentPart
{
    @Text( "Actuator label not specified" )
    private static LocalizableText labelNotSpecified;
    
    static
    {
        LocalizableText.init( ActuatorPart.class );
    }

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
        
        this.actionId = def.getActionId().content();
        this.actionHandlerId = def.getActionHandlerId().content();
        
        this.showLabel = def.getShowLabel().content();
        
        if( this.showLabel )
        {
            this.labelFunctionResult = initExpression
            (
                def.getLabel().content(), 
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
        
        this.showImage = def.getShowImage().content();
        
        if( this.showImage )
        {
            final ListFactory<FunctionResult> imageFunctionResultsFactory = ListFactory.start();
            
            for( ImageReference imageReference : def.getImages() )
            {
                final FunctionResult imageFunctionResult = initExpression
                (
                    imageReference.getImage().content(),
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
            
            this.imageFunctionResults = imageFunctionResultsFactory.result();
        }
        
        this.style = def.getStyle().content();
    }
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        return AndFunction.create
        (
            super.initVisibleWhenFunction(),
            new Function()
            {
                @Override
                public String name()
                {
                    return "VisibleIfActionHandlerExists";
                }
    
                @Override
                public FunctionResult evaluate( final FunctionContext context )
                {
                    return new FunctionResult( this, context )
                    {
                        @Override
                        protected void init()
                        {
                            ActuatorPart.this.attach
                            (
                                new FilteredListener<ActuatorActionHandlerEvent>()
                                {
                                    @Override
                                    protected void handleTypedEvent( final ActuatorActionHandlerEvent event )
                                    {
                                        refresh();
                                    }
                                }
                            );
                        }
    
                        @Override
                        protected Object evaluate()
                        {
                            return ( handler() != null );
                        }
                    };
                }
            }
        );
    }
    
    private void refreshActionHandler()
    {
        final SapphireActionHandler oldActionHandler = this.actionHandler;
        
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
        
        if( this.actionHandler != oldActionHandler )
        {
            broadcast( new ActuatorActionHandlerEvent( this ) );
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
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        if( style().equals( "Sapphire.Actuator.Button" ) )
        {
            return new ActuatorButtonPresentation( this, parent, composite );
        }
        else
        {
            return new ActuatorLinkPresentation( this, parent, composite );
        }
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

}
