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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_AUX_TEXT;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_AUX_TEXT_PROVIDER;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.reflowOnResize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.AuxTextProvider;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorRenderer
{
    private static final String RELATED_CONTENT_WIDTH = "sapphire.related.content.width";
    
    protected final SapphireRenderingContext context;
    private final SapphirePropertyEditor part;
    protected PropertyEditorAssistDecorator decorator;
    private Label auxTextControl;
    private AuxTextProvider auxTextProvider;
    private final Set<Control> controls;
    
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
        return this.part.getLocalModelElement();
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
    
    protected boolean canScaleVertically()
    {
        return false;
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
            final boolean spanBothColumns = this.part.getSpanBothColumns();
            
            if( ! spanBothColumns )
            {
                final Label placeholder = new Label( parent, SWT.NONE );
                placeholder.setLayoutData( gd() );
                placeholder.setText( MiscUtil.EMPTY_STRING );
                this.context.adapt( placeholder );
            }
            
            final int hindent = this.part.getMarginLeft() + 9;
            
            this.auxTextControl = new Label( parent, SWT.WRAP );
            this.auxTextControl.setLayoutData( gdwhint( gdhindent( gdhspan( gdhfill(), spanBothColumns ? 2 : 1 ), hindent ), 10 ) );
            this.auxTextControl.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
            
            reflowOnResize( this.auxTextControl );
            
            addControl( this.auxTextControl );

            if( auxText != null )
            {
                this.auxTextControl.setText( auxText );
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
                Display.getDefault().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            PropertyEditorRenderer.this.handlePropertyChangedEvent();
                        }
                    }
                );
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
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    part.removeListener( partListener );
                    modelElement.removeListener( propertyChangeListener, property.getName() );
                }
            }
        );
    }
    
    protected abstract void createContents( final Composite parent );
    
    protected final Composite createMainComposite( final Composite parent )
    {
        return createMainComposite( parent, new CreateMainCompositeDelegate( this.part ) );
    }
    
    protected class CreateMainCompositeDelegate
    {
        private final SapphirePropertyEditor part;
        
        public CreateMainCompositeDelegate( final SapphirePropertyEditor part )
        {
            this.part = part;
        }
        
        public boolean getShowLabel()
        {
            return this.part.getShowLabel();
        }
        
        public String getLabel( final CapitalizationType capitalizationType,
                                final boolean includeMnemonic )
        {
            return this.part.getLabel( capitalizationType, includeMnemonic );
        }
        
        public int getLeftMargin()
        {
            return this.part.getMarginLeft();
        }
        
        public boolean getSpanBothColumns()
        {
            return this.part.getSpanBothColumns();
        }
        
        public boolean canScaleVertically()
        {
            return PropertyEditorRenderer.this.canScaleVertically();
        }
    }
    
    protected final Composite createMainComposite( final Composite parent,
                                                   final CreateMainCompositeDelegate delegate )
    {
        final boolean showLabel = delegate.getShowLabel();
        final int leftMargin = delegate.getLeftMargin();
        final boolean spanBothColumns = delegate.getSpanBothColumns();
        final boolean singleLinePart = this.part.isSingleLinePart();
        final List<SapphirePart> relatedContentParts = this.part.getRelatedContent();
        final int count = relatedContentParts.size();
        
        if( showLabel )
        {
            final Label label = new Label( parent, SWT.NONE );
            label.setText( delegate.getLabel( CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
            label.setLayoutData( gdhindent( gdhspan( gdvalign( gd(), singleLinePart ? SWT.CENTER : SWT.TOP ), spanBothColumns ? 2 : 1 ), leftMargin + 9 ) );
            this.context.adapt( label );
            addControl( label );
        }
        else if( ! spanBothColumns )
        {
            final Label spacer = new Label( parent, SWT.NONE );
            spacer.setLayoutData( gd() );
            spacer.setText( MiscUtil.EMPTY_STRING );
            this.context.adapt( spacer );
        }
        
        GridData gd;
        
        if( delegate.canScaleVertically() )
        {
            final boolean scaleVertically = this.part.getScaleVertically();
            gd = gdhhint( ( scaleVertically ? gdfill() : gdhfill() ), this.part.getHeight( 150 ) );
        }
        else
        {
            gd = gdhfill();
        }
        
        if( spanBothColumns )
        {
            gd = gdhindent( gdhspan( gd, 2 ), leftMargin );
        }
        
        gd = gdwhint( gd, this.part.getWidth( 200 ) );
        
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( gd );
        this.context.adapt( composite );
        
        if( count == 0 )
        {
            return composite;
        }
        else
        {
            composite.setLayout( glspacing( glayout( 3, 0, 0 ), 0 ) );
            
            final boolean vcenter
                = ( this.part.isSingleLinePart() && relatedContentParts.size() == 1 && relatedContentParts.get( 0 ).isSingleLinePart() );
            
            final Composite mainPropertyEditorOuterComposite = new Composite( composite, SWT.NONE );
            mainPropertyEditorOuterComposite.setLayout( glayout( 1, 0, 4, 0, 0 ) );
            mainPropertyEditorOuterComposite.setLayoutData( vcenter ? gdhfill() : gdfill() );

            final Composite mainPropertyEditorComposite = new Composite( mainPropertyEditorOuterComposite, SWT.NONE );
            mainPropertyEditorComposite.setLayoutData( vcenter ? gdvalign( gdhfill(), GridData.CENTER ) : gdfill() );
            
            final Sash sash = new Sash( composite, SWT.VERTICAL );
            sash.setLayoutData( gdhhint( gdvfill(), 1 ) );
            
            final Composite relatedContentComposite = new Composite( composite, SWT.NONE );
            relatedContentComposite.setLayoutData( vcenter ? gdvalign( gdhfill(), GridData.CENTER ) : gdfill() );
            relatedContentComposite.setLayout( glayout( 2, 0, 0 ) );
            
            relatedContentComposite.setData( RELATED_CONTENT_WIDTH, ( (double) this.part.getRelatedContentWidth() ) / ( (double) 100 ) );
            
            composite.addListener
            ( 
                SWT.Resize,
                new Listener()
                {
                    public void handleEvent( final Event event )
                    {
                        refreshSashFormLayout( composite, mainPropertyEditorComposite, relatedContentComposite, sash );
                    }
                }
            );
            
            sash.addListener
            (
                SWT.Selection, 
                new Listener()
                {
                    public void handleEvent( final Event event )
                    {
                        final int width = composite.getClientArea().width - sash.getBounds().width;
                        double ratio = ( (double) ( width - event.x ) ) / ( (double) width );
                        
                        if( ratio < 0.2d )
                        {
                            ratio = 0.2d;
                        }
                        
                        if( ratio > 0.8d )
                        {
                            ratio = 0.8d;
                        }
                        
                        relatedContentComposite.setData( RELATED_CONTENT_WIDTH, ratio );
                        refreshSashFormLayout( composite, mainPropertyEditorComposite, relatedContentComposite, sash );
                    }
                }
            );

            this.context.adapt( composite );

            for( SapphirePart relatedContentPart : relatedContentParts )
            {
                relatedContentPart.render( new SapphireRenderingContext( relatedContentPart, this.context, relatedContentComposite ) );
            }
            
            return mainPropertyEditorComposite;
        }
    }
    
    private static final void refreshSashFormLayout( final Composite rootComposite,
                                                     final Composite mainPropertyEditorComposite,
                                                     final Composite relatedContentComposite,
                                                     final Sash sash )
    {
        final int rootCompositeWidth = rootComposite.getClientArea().width - sash.getBounds().width;
        final double relatedContentCompositeWidthRatio = (Double) relatedContentComposite.getData( RELATED_CONTENT_WIDTH );
        final int relatedContentCompositeWidth = (int) ( rootCompositeWidth * relatedContentCompositeWidthRatio );
        final int mainPropertyEditorCompositeWidth = rootCompositeWidth - relatedContentCompositeWidth;
        
        ( (GridData) mainPropertyEditorComposite.getLayoutData() ).widthHint = mainPropertyEditorCompositeWidth;
        ( (GridData) relatedContentComposite.getLayoutData() ).widthHint = relatedContentCompositeWidth;
        
        rootComposite.layout( true, true );
    }
    
    protected final PropertyEditorAssistDecorator createDecorator( final Composite parent )
    {
        this.decorator = new PropertyEditorAssistDecorator( this.part, this.context, parent );
        return this.decorator;
    }
    
    protected final Control createDeprecationMarker( final Composite parent )
    {
        final Label label = new Label( parent, SWT.NONE );
        label.setText( Resources.deprecatedLabelText );
        this.context.adapt( label );
        addControl( label );
        label.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
        
        return label;
    }
    
    protected final void addControl( final Control control )
    {
        final IModelElement element = getModelElement();
        final ModelProperty property = getProperty();
        
        this.controls.add( control );
        
        control.setEnabled( element.isPropertyEnabled( property ) );
        control.setData( SapphirePropertyEditor.DATA_PROPERTY, property );
        control.setData( SapphirePropertyEditor.DATA_ELEMENT, element );
        
        control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    PropertyEditorRenderer.this.controls.remove( control );
                    
                    boolean timeToDispose = true;
                    
                    for( Control control : PropertyEditorRenderer.this.controls )
                    {
                        if( ! control.isDisposed() )
                        {
                            timeToDispose = false;
                        }
                    }
                    
                    if( timeToDispose )
                    {
                        dispose();
                    }
                }
            }
        );
        
        if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                addControl( child );
            }
        }
        
        this.actionPresentationKeyboard.attach( control );
        
        this.context.setHelp( control, element, property );
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

        if( this.auxTextProvider != null )
        {
            final String auxText = this.auxTextProvider.getAuxText( getModelElement(), getProperty() );
            this.auxTextControl.setText( "(" + auxText + ")" );
        }
    }
    
    protected void handleFocusReceivedEvent()
    {
    }
    
    protected final void addOnDisposeOperation( final Runnable op )
    {
        this.onDisposeOperations.add( op );
    }
    
    private final void dispose()
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
        
        this.onDisposeOperations.clear();
    }
    
    private static final class Resources extends NLS
    {
        public static String actionsContextLabel;
        public static String deprecatedLabelText;
    
        static
        {
            initializeMessages( PropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
