/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - Bug 328777 Table cell editor overlaps neighboring field
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.swt.renderer.SapphireHotSpotsActionPresentation.ControlHotSpot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireTextCellEditor

    extends TextCellEditor

{
    private final SapphireRenderingContext context;
    private final StructuredViewer viewer;
    private final DefaultListPropertyEditorRenderer.SelectionProvider selectionProvider;
    private final IModelElement element;
    private final ValueProperty property;
    private final SapphireActionGroup actions;
    private Composite topComposite;
    private Composite innerComposite;
    private Text text;
    private boolean disableFocusLostHandler;
    private boolean isDefaultValue;
    private ISelection selectionPriorToActivation;
    
    public SapphireTextCellEditor( final SapphireRenderingContext context,
                                   final StructuredViewer parent,
                                   final DefaultListPropertyEditorRenderer.SelectionProvider selectionProvider,
                                   final IModelElement element,
                                   final ValueProperty property,
                                   final SapphireActionGroup actions,
                                   final int style )
    {
        super();
        
        this.context = context;
        this.viewer = parent;
        this.selectionProvider = selectionProvider;
        this.element = element;
        this.property = property;
        this.actions = actions;
        
        setStyle( style );
        create( (Composite) parent.getControl() );
    }
    
    public void setMaxWidth( final int maxWidth )
    {
        if( maxWidth == -1 )
        {
            this.innerComposite.setLayoutData( gdfill() );
        }
        else
        {
            this.innerComposite.setLayoutData( gdwhint( gd(), maxWidth ) );
        }
    }
    
    public void setHorizonalIndent( final int horizontalIndent )
    {
        ( (GridLayout) this.topComposite.getLayout() ).marginLeft = horizontalIndent;
    }
    
    @Override
    protected Object doGetValue()
    {
        if( this.isDefaultValue )
        {
            return null;
        }
        else
        {
            return super.doGetValue();
        }
    }

    @Override
    protected void doSetValue( final Object value )
    {
        final Value<?> val = (Value<?>) value;
        final String str = val.getText( true );
        
        super.doSetValue( str == null ? MiscUtil.EMPTY_STRING : str );
        
        if( val.getText( false ) == null && val.getDefaultContent() != null )
        {
            this.isDefaultValue = true;
        }
        else
        {
            this.isDefaultValue = false;
        }
    }

    @Override
    protected Control createControl( final Composite parent )
    {
        int style = getStyle();

        this.topComposite = new Composite( parent, SWT.NONE )
        {
            @Override
            public void addTraverseListener( final TraverseListener listener )
            {
                SapphireTextCellEditor.this.text.addTraverseListener( listener );  
            }

            @Override
            public void removeTraverseListener( final TraverseListener listener )
            {
                SapphireTextCellEditor.this.text.removeTraverseListener( listener );
            }
        };
        
        this.topComposite.setBackground( parent.getBackground() );
        this.topComposite.setLayout( glayout( 1, 0, 0 ) );
        
        this.innerComposite = new Composite( this.topComposite, style );
        this.innerComposite.setLayoutData( gdfill() );
        this.innerComposite.setLayout( glayout( 1 + this.actions.getActiveActions().size(), 0, 2, 0, 0 ) );
        this.innerComposite.setBackground( parent.getBackground() );
        
        setStyle( SWT.NONE );
        this.text = (Text) super.createControl( this.innerComposite );
        this.text.setLayoutData( gdhfill() );
        setStyle( style );
        
        this.text.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent event )
                {
                    SapphireTextCellEditor.this.isDefaultValue = false;
                }
            }
        );
        
        final SapphireActionHandler.Listener actionHandlerListener = new SapphireActionHandler.Listener()
        {
            @Override
            public void handleEvent( final SapphireActionHandler.Event event )
            {
                handleActionHandlerEvent( event );
            }
        };
        
        for( SapphireAction action : this.actions.getActions() )
        {
            for( SapphireActionHandler handler : action.getActiveHandlers() )
            {
                handler.addListener( actionHandlerListener );
            }
        }
        
        final SapphireActionPresentationManager actionPresentationManager 
            = new SapphireActionPresentationManager( this.context, this.actions );
        
        final CustomActionsPresentation buttonActionPresentation = new CustomActionsPresentation( actionPresentationManager );
        buttonActionPresentation.setParentComposite( this.innerComposite );
        buttonActionPresentation.render();
        
        final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
        keyboardActionPresentation.attach( this.text );
        keyboardActionPresentation.render();
        
        return this.topComposite;
    }

    private void handleActionHandlerEvent( final SapphireActionHandler.Event event )
    {
        final String type = event.getType();
        
        if( type.equals( SapphireActionHandler.EVENT_PRE_EXECUTE ) )
        {
            this.disableFocusLostHandler = true;
        }
        else if( type.equals( SapphireActionHandler.EVENT_POST_EXECUTE ) )
        {
            if( ! this.text.isDisposed() )
            {
                String newTextValue = this.element.read( this.property ).getText( false );
                
                if( newTextValue == null )
                {
                    newTextValue = "";
                }
                
                this.text.setText( newTextValue );
                this.text.setFocus();
                this.text.setSelection( 0, newTextValue.length() );
            }
            
            this.disableFocusLostHandler = false;
        }
    }
    
    @Override
    public void activate()
    {
        this.selectionPriorToActivation = this.viewer.getSelection();
        
        if( this.selectionProvider != null )
        {
            this.selectionProvider.setFakeSelection( this.selectionPriorToActivation );
        }
        
        this.viewer.setSelection( StructuredSelection.EMPTY );
        
        super.activate();
        
        this.topComposite.layout( true, true );
    }

    @Override
    public LayoutData getLayoutData() {
        return new LayoutData();
    }

    @Override
    protected void focusLost()
    {
        if( ! this.disableFocusLostHandler )
        {
            this.disableFocusLostHandler = true;
            super.focusLost();
            this.disableFocusLostHandler = false;
            
            this.viewer.setSelection( this.selectionPriorToActivation );

            if( this.selectionProvider != null )
            {
                this.selectionProvider.setFakeSelection( null );
            }
            
            this.selectionPriorToActivation = null;
        }
    }
    
    private static final class CustomActionsPresentation
    
        extends SapphireHotSpotsActionPresentation
        
    {
        private Composite parent;
        
        public CustomActionsPresentation( final SapphireActionPresentationManager manager )
        {
            super( manager );
        }
        
        public void setParentComposite( final Composite parent )
        {
            this.parent = parent;
        }

        @Override
        public void render()
        {
            final Cursor cursor = new Cursor( this.parent.getDisplay(), SWT.CURSOR_HAND );
            
            this.parent.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        cursor.dispose();
                    }
                }
            );
            
            final SapphireRenderingContext context = getManager().getContext();
            
            for( final SapphireAction action : getActions() )
            {
                final List<SapphireActionHandler> handlers = action.getActiveHandlers();
                
                final Label button = new Label( this.parent, SWT.NONE );
                button.setLayoutData( gd() );
                button.setBackground( this.parent.getBackground() );
                button.setImage( context.getImageCache().getImage( action.getImage( 11 ) ) );
                button.setCursor( cursor );
                button.setToolTipText( LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, false ) );

                registerHotSpot( action, new CustomHotSpot( button ) );
                
                button.addMouseListener
                (
                    new MouseAdapter()
                    {
                        @Override
                        public void mouseUp( final MouseEvent event )
                        {
                            if( handlers.size() == 1 )
                            {
                                handlers.get( 0 ).execute( context );
                            }
                            else
                            {
                                displayActionHandlerChoice( action );
                            }
                        }
                    }
                );
                
                final Runnable updateActionEnablementStateOp = new Runnable()
                {
                    public void run()
                    {
                        boolean enabled = false;
                        
                        for( SapphireActionHandler handler : handlers )
                        {
                            enabled = handler.isEnabled();
                            
                            if( enabled )
                            {
                                break;
                            }
                        }
                        
                        if( ! button.isDisposed() )
                        {
                            button.setEnabled( enabled );
                        }
                    }
                };
                
                action.addListener
                (
                    new SapphireAction.Listener()
                    {
                        @Override
                        public void handleEvent( final SapphireAction.Event event )
                        {
                            if( event.getType().equals( SapphireAction.EVENT_ENABLEMENT_STATE_CHANGED ) )
                            {
                                updateActionEnablementStateOp.run();
                            }
                        }
                    }
                );
                
                updateActionEnablementStateOp.run();
            }
        }
    }
    
    private static final class CustomHotSpot 
    
        extends ControlHotSpot
        
    {
        public CustomHotSpot( final Control control )
        {
            super( control );
        }

        @Override
        public Rectangle getBounds()
        {
            // Add a 3 pixel margin on top and the bottom.
            
            final Rectangle bounds = super.getBounds();
            return new Rectangle( bounds.x, bounds.y - 3, bounds.width, bounds.height + 6 );
        }
    }
    
}

