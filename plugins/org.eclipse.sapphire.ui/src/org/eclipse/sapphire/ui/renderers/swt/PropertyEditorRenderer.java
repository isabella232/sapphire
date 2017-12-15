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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_AUX_TEXT;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_AUX_TEXT_PROVIDER;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_EXPAND_VERTICALLY;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.AuxTextProvider;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorRenderer
{
    protected final SapphireRenderingContext context;
    private final SapphirePropertyEditor part;
    protected PropertyEditorAssistDecorator decorator;
    private Label auxTextControl;
    private AuxTextProvider auxTextProvider;
    private final Set<Control> controls;
    private boolean spanBothColumns;
    
    protected AbstractBinding binding;
    
    private final SapphireActionGroup actions;
    private final SapphireActionPresentationManager actionPresentationManager;
    private final SapphireKeyboardActionPresentation actionPresentationKeyboard;
    private final List<Runnable> onDisposeOperations = new ArrayList<Runnable>();

    public PropertyEditorRenderer( final SapphireRenderingContext context,
                                   final SapphirePropertyEditor part )
    {
        this.context = context;
        this.part = part;
        this.controls = new HashSet<Control>();
        this.spanBothColumns = false;
        this.actions = part.getActions( part.getActionContext() );
        this.actionPresentationManager = new SapphireActionPresentationManager( this.context, this.actions );
        this.actionPresentationManager.setLabel( NLS.bind( Resources.actionsContextLabel, this.part.getProperty().getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
        this.actionPresentationKeyboard = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
    }
    
    public SapphireRenderingContext getUiContext()
    {
        return this.context;
    }
    
    public SapphirePropertyEditor getPart()
    {
        return this.part;
    }
    
    public IModelElement getModelElement()
    {
        return this.part.getModelElement();
    }
    
    public ModelProperty getProperty()
    {
        return this.part.getProperty();
    }
    
    public SapphireImageCache getImageCache()
    {
        return this.part.getImageCache();
    }
    
    public final SapphireActionGroup getActions()
    {
        return this.actions;
    }
    
    public final SapphireActionPresentationManager getActionPresentationManager()
    {
        return this.actionPresentationManager;
    }
    
    protected boolean canExpandVertically()
    {
        return false;
    }
    
    protected final boolean getSpanBothColumns()
    {
        return this.spanBothColumns;
    }
    
    protected final void setSpanBothColumns( final boolean spanBothColumns )
    {
        this.spanBothColumns = spanBothColumns;
    }
    
    public final void create( final Composite parent )
    {
        createContents( parent );
        
        final String auxText = this.part.getRenderingHint( HINT_AUX_TEXT, null );
        
        final Class<AuxTextProvider> auxTextProviderClass 
            = this.part.getRenderingHint( HINT_AUX_TEXT_PROVIDER, (Class<AuxTextProvider>) null );
        
        if( auxTextProviderClass != null )
        {
            try
            {
                this.auxTextProvider = auxTextProviderClass.newInstance();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        if( auxText != null || this.auxTextProvider != null )
        {
            if( ! this.spanBothColumns )
            {
                final Label placeholder = new Label( parent, SWT.NONE );
                placeholder.setLayoutData( gd() );
                placeholder.setText( MiscUtil.EMPTY_STRING );
                this.context.adapt( placeholder );
            }
            
            final int hindent = this.part.getLeftMarginHint() + 9;
            
            this.auxTextControl = new Label( parent, SWT.NONE );
            this.auxTextControl.setLayoutData( gdhindent( gdhspan( gdhfill(), this.spanBothColumns ? 2 : 1 ), hindent ) );
            this.auxTextControl.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
            
            addControl( this.auxTextControl );

            if( auxText != null )
            {
                this.auxTextControl.setText( "(" + auxText + ")" );
            }
        }
        
        final SapphirePropertyEditor part = getPart();
        final IModelElement modelElement = getModelElement();
        final ModelProperty property = getProperty();
        
        final ModelPropertyListener propertyChangeListener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                PropertyEditorRenderer.this.handlePropertyChangedEvent();
            }
        };

        modelElement.addListener( propertyChangeListener, property.getName() );
        
        handlePropertyChangedEvent();

        final SapphirePartListener partListener = new SapphirePartListener()
        {
            @Override
            public void handleFocusReceivedEvent( final SapphirePartEvent event )
            {
                PropertyEditorRenderer.this.handleFocusReceivedEvent();
            }
        };
        
        part.addListener( partListener );
        
        this.actionPresentationKeyboard.render();
        
        final DisposeListener disposeListener = new DisposeListener()
        {
            boolean executed = false;
            
            public void widgetDisposed( final DisposeEvent event )
            {
                if( ! this.executed )
                {
                    part.removeListener( partListener );
                    modelElement.removeListener( propertyChangeListener, property.getName() );
                    handleDisposeEvent();
                    this.executed = true;
                }
            }
        };

        for( Control c : this.controls )
        {
            this.context.setHelp( c, modelElement, property );
            
            // This is not really a good way to do this...
            
            c.addDisposeListener( disposeListener );
        }
    }
    
    protected abstract void createContents( final Composite parent );
    
    protected final Composite createMainComposite( final Composite parent )
    {
        final List<SapphirePropertyEditor> auxPropertyEditors = this.part.getAuxPropertyEditors();
        final int count = auxPropertyEditors.size();
        
        GridData gd;
        
        if( canExpandVertically() )
        {
            final boolean expandVertically = this.part.getRenderingHint( HINT_EXPAND_VERTICALLY, false );
            final int heightHint = this.part.getRenderingHint( ISapphirePartDef.HINT_HEIGHT, 10 ) * 15;
            gd = ( expandVertically ? gdfill() : gdhhint( gdhfill(), heightHint ) );
        }
        else
        {
            gd = gdhfill();
        }
        
        if( this.spanBothColumns )
        {
            gd = gdhindent( gdhspan( gd, 2 ), this.part.getLeftMarginHint() );
        }
        
        gd = gdwhint( gd, 200 );
        
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( gd );
        this.context.adapt( composite );
        
        if( count == 0 )
        {
            return composite;
        }
        else
        {
            composite.setLayout( glspacing( glayout( count + 1, 0, 0 ), 5 ) );
            
            final Composite mainPropertyEditorComposite = new Composite( composite, SWT.NONE );
            mainPropertyEditorComposite.setLayoutData( gdvalign( gdfill(), SWT.CENTER ) );
            this.context.adapt( mainPropertyEditorComposite );
            
            for( SapphirePropertyEditor auxPropertyEditor : auxPropertyEditors )
            {
                final Composite auxPropertyEditorComposite = new Composite( composite, SWT.NONE );
                auxPropertyEditorComposite.setLayoutData( gdvalign( gd(), SWT.CENTER ) );
                auxPropertyEditorComposite.setLayout( glayout( 2, 0, 0 ) );
                this.context.adapt( auxPropertyEditorComposite );
                
                auxPropertyEditor.render( new SapphireRenderingContext( auxPropertyEditor, this.context, auxPropertyEditorComposite ) );
            }
            
            return mainPropertyEditorComposite;
        }
    }
    
    protected final PropertyEditorAssistDecorator createDecorator( final Composite parent )
    {
        this.decorator = new PropertyEditorAssistDecorator( this.part, this.context, parent );
        
        final SapphireAction assistAction = this.actions.getAction( SapphireActionSystem.ACTION_ASSIST );
        
        final SapphireActionHandler assistActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final SapphireRenderingContext context )
            {
                PropertyEditorRenderer.this.decorator.openAssistDialog();
                return null;
            }
        };
        
        assistActionHandler.init( assistAction, null );
        assistAction.addHandler( assistActionHandler );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    assistAction.removeHandler( assistActionHandler );
                }
            }
        );
        
        return this.decorator;
    }
    
    protected final void addControl( final Control control )
    {
        this.controls.add( control );
        
        control.setData( SapphirePropertyEditor.DATA_PROPERTY, getProperty() );
        
        if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                addControl( child );
            }
        }
        
        this.actionPresentationKeyboard.attach( control );
    }
    
    protected void handlePropertyChangedEvent()
    {
        final boolean enabled = getModelElement().isPropertyEnabled( getProperty() );
        
        for( Control control : this.controls )
        {
            if( ! control.isDisposed() )
            {
                control.setEnabled( enabled );
            }
        }

        if( this.decorator != null )
        {
            this.decorator.refresh();
        }
        
        if( this.auxTextProvider != null )
        {
            final String auxText = this.auxTextProvider.getAuxText( getModelElement(), getProperty() );
            this.auxTextControl.setText( "(" + auxText + ")" );
        }
    }
    
    protected void handleFocusReceivedEvent()
    {
    }
    
    protected void handleDisposeEvent()
    {
        for( Runnable op : this.onDisposeOperations )
        {
            try
            {
                op.run();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }
    
    protected final void addOnDisposeOperation( final Runnable op )
    {
        this.onDisposeOperations.add( op );
    }
    
    private static final class Resources extends NLS
    {
        public static String actionsContextLabel;
    
        static
        {
            initializeMessages( PropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
