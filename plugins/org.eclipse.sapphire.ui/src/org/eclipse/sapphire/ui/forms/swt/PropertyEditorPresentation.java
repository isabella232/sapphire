/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.reflowOnResize;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.runOnDisplayThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphirePart.FocusReceivedEvent;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.assist.AuxTextProvider;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorPresentation extends PropertyEditorPresentation2
{
    private static final String RELATED_CONTENT_WIDTH = "sapphire.related.content.width";
    
    @Text( "actions for {0}" )
    private static LocalizableText actionsContextLabel;
    
    @Text( "(deprecated)" )
    private static LocalizableText deprecatedLabelText;

    @Text( "deprecated" )
    private static LocalizableText deprecatedAccessibleText;

    static
    {
        LocalizableText.init( PropertyEditorPresentation.class );
    }

    protected PropertyEditorAssistDecorator decorator;
    private Label auxTextControl;
    private AuxTextProvider auxTextProvider;
    private final Set<Control> controls;
    private Composite mainPropertyEditorComposite;
    
    protected AbstractBinding binding;
    
    private final SapphireActionGroup actions;
    private final SapphireActionPresentationManager actionPresentationManager;
    private final SapphireKeyboardActionPresentation actionPresentationKeyboard;
    private final List<Runnable> onDisposeOperations = new ArrayList<Runnable>();
    
    private List<FormComponentPresentation> relatedContentPresentations;

    public PropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
        
        this.controls = new HashSet<Control>();
        this.actions = part.getActions();
        this.actionPresentationManager = new SapphireActionPresentationManager( this, this.actions );
        this.actionPresentationManager.setLabel( actionsContextLabel.format( property().definition().getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
        this.actionPresentationKeyboard = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
    }
    
    @Override
    public PropertyEditorPart part()
    {
        return (PropertyEditorPart) super.part();
    }
    
    public Property property()
    {
        return part().property();
    }
    
    public final Element element()
    {
        return property().element();
    }
    
    public final SapphireActionGroup getActions()
    {
        return this.actions;
    }
    
    public final SapphireActionPresentationManager getActionPresentationManager()
    {
        return this.actionPresentationManager;
    }
    
    @Override
    public final Rectangle bounds()
    {
        final Rectangle bounds = this.mainPropertyEditorComposite.getBounds();
        final Point position = this.mainPropertyEditorComposite.getParent().toDisplay( bounds.x, bounds.y );
        
        return new Rectangle( position.x, position.y, bounds.width, bounds.height );
    }
    
    protected boolean canScaleVertically()
    {
        return false;
    }
    
    public PropertyEditorPresentation2 createChildPropertyEditorPresentation( final PropertyEditorPart part )
    {
        throw new UnsupportedOperationException();
    }
    
    public final void render()
    {
        final PropertyEditorPart part = part();
        final Composite composite = composite();
        
        createContents( composite );
        
        final String auxText = part.getRenderingHint( PropertyEditorDef.HINT_AUX_TEXT, null );
        
        final Class<AuxTextProvider> auxTextProviderClass 
            = part.getRenderingHint( PropertyEditorDef.HINT_AUX_TEXT_PROVIDER, (Class<AuxTextProvider>) null );
        
        if( auxTextProviderClass != null )
        {
            try
            {
                this.auxTextProvider = auxTextProviderClass.newInstance();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        if( auxText != null || this.auxTextProvider != null )
        {
            final boolean spanBothColumns = part.getSpanBothColumns();
            
            if( ! spanBothColumns )
            {
                final Label placeholder = new Label( composite, SWT.NONE );
                placeholder.setLayoutData( gd() );
                placeholder.setText( MiscUtil.EMPTY_STRING );
            }
            
            final int hindent = part.getMarginLeft() + 9;
            
            this.auxTextControl = new Label( composite, SWT.WRAP );
            this.auxTextControl.setLayoutData( gdwhint( gdhindent( gdhspan( gdhfill(), spanBothColumns ? 2 : 1 ), hindent ), 10 ) );
            this.auxTextControl.setForeground( composite.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
            
            reflowOnResize( this.auxTextControl );
            
            addControl( this.auxTextControl );

            if( auxText != null )
            {
                this.auxTextControl.setText( auxText );
            }
        }
        
        final Listener propertyChangeListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                runOnDisplayThread
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            PropertyEditorPresentation.this.handlePropertyChangedEvent();
                        }
                    }
                );
            }
        };

        property().attach( propertyChangeListener );
        
        handlePropertyChangedEvent();

        attachPartListener
        (
            new FilteredListener<FocusReceivedEvent>()
            {
                @Override
                protected void handleTypedEvent( FocusReceivedEvent event )
                {
                    handleFocusReceivedEvent();
                }
            }
        );
        
        this.actionPresentationKeyboard.render();
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    property().detach( propertyChangeListener );
                }
            }
        );
    }
    
    protected abstract void createContents( final Composite parent );
    
    protected final Composite createMainComposite( final Composite parent )
    {
        return createMainComposite( parent, new CreateMainCompositeDelegate( part() ) );
    }
    
    protected class CreateMainCompositeDelegate
    {
        private final PropertyEditorPart part;
        
        public CreateMainCompositeDelegate( final PropertyEditorPart part )
        {
            this.part = part;
        }
        
        public boolean getShowLabel()
        {
            return ( this.part.label() != null );
        }
        
        public String getLabel( final CapitalizationType capitalizationType, final boolean includeMnemonic )
        {
            return this.part.label( capitalizationType, includeMnemonic );
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
            return PropertyEditorPresentation.this.canScaleVertically();
        }
    }
    
    protected final Composite createMainComposite( final Composite parent,
                                                   final CreateMainCompositeDelegate delegate )
    {
        final PropertyEditorPart part = part();
        
        final boolean showLabel = delegate.getShowLabel();
        final int leftMargin = delegate.getLeftMargin();
        final boolean spanBothColumns = delegate.getSpanBothColumns();
        final boolean singleLinePresentation = isSingleLine();
        final List<FormComponentPart> relatedContentParts = part.getRelatedContent();
        final int count = relatedContentParts.size();
        
        if( showLabel )
        {
            final Label label = new Label( parent, SWT.NONE );
            
            register( label );
            
            final Runnable updateLabelOp = new Runnable()
            {
                public void run()
                {
                    label.setText( delegate.getLabel( CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
                }
            };
            
            attachPartListener
            (
                new FilteredListener<LabelChangedEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final LabelChangedEvent event )
                    {
                        updateLabelOp.run();
                        layout();
                    }
                }
            );
            
            updateLabelOp.run();
            
            label.setLayoutData( gdhindent( gdhspan( gdvalign( gd(), singleLinePresentation ? SWT.CENTER : SWT.TOP ), spanBothColumns ? 2 : 1 ), leftMargin + 9 ) );
            
            addControl( label );
        }
        else if( ! spanBothColumns )
        {
            final Label spacer = new Label( parent, SWT.NONE );
            spacer.setLayoutData( gd() );
            spacer.setText( MiscUtil.EMPTY_STRING );
            
            register( spacer );
        }
        
        GridData gd;
        
        if( delegate.canScaleVertically() )
        {
            final boolean scaleVertically = part.getScaleVertically();
            gd = gdhhint( ( scaleVertically ? gdfill() : gdhfill() ), part.getHeight( 150 ) );
        }
        else
        {
            gd = gdhfill();
        }
        
        if( spanBothColumns )
        {
            gd = gdhindent( gdhspan( gd, 2 ), leftMargin );
        }
        
        gd = gdwhint( gd, part.getWidth( 200 ) );
        
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( gd );
        
        register( composite );
        
        if( count == 0 )
        {
            this.mainPropertyEditorComposite = composite;
        }
        else
        {
            composite.setLayout( glspacing( glayout( 3, 0, 0 ), 0 ) );
            
            final Composite mainPropertyEditorOuterComposite = new Composite( composite, SWT.NONE );
            mainPropertyEditorOuterComposite.setLayout( glayout( 1, 0, 4, 0, 0 ) );

            final Composite mainPropertyEditorComposite = new Composite( mainPropertyEditorOuterComposite, SWT.NONE );
            
            final Sash sash = new Sash( composite, SWT.VERTICAL );
            sash.setLayoutData( gdhhint( gdvfill(), 1 ) );
            
            final Composite relatedContentComposite = new Composite( composite, SWT.NONE );
            relatedContentComposite.setLayout( glayout( 2, 0, 0 ) );
            
            relatedContentComposite.setData( RELATED_CONTENT_WIDTH, ( (double) part.getRelatedContentWidth() ) / ( (double) 100 ) );
            
            composite.addListener
            ( 
                SWT.Resize,
                new org.eclipse.swt.widgets.Listener()
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
                new org.eclipse.swt.widgets.Listener()
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
            
            final ListFactory<FormComponentPresentation> relatedContentPresentations = ListFactory.start();

            for( final FormComponentPart relatedContentPart : relatedContentParts )
            {
                final FormComponentPresentation relatedContentPresentation = relatedContentPart.createPresentation( this, relatedContentComposite );
                relatedContentPresentations.add( relatedContentPresentation );
                relatedContentPresentation.render();
            }
            
            this.relatedContentPresentations = relatedContentPresentations.result();
            
            final boolean vcenter
                = ( singleLinePresentation && relatedContentParts.size() == 1 && relatedContentPresentations.get( 0 ).isSingleLine() );
            
            mainPropertyEditorOuterComposite.setLayoutData( vcenter ? gdhfill() : gdfill() );
            mainPropertyEditorComposite.setLayoutData( vcenter ? gdvalign( gdhfill(), GridData.CENTER ) : gdfill() );
            relatedContentComposite.setLayoutData( vcenter ? gdvalign( gdhfill(), GridData.CENTER ) : gdfill() );
        
            this.mainPropertyEditorComposite = mainPropertyEditorComposite;
        }
        
        return mainPropertyEditorComposite;
    }
    
    @Override
    public boolean isSingleLine()
    {
        final PropertyDef pdef = property().definition();
        
        if( pdef instanceof ValueProperty && ! pdef.hasAnnotation( LongString.class ) )
        {
            return true;
        }
        
        return false;
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
        this.decorator = new PropertyEditorAssistDecorator( part(), parent );
        return this.decorator;
    }
    
    protected final Control createDeprecationMarker( final Composite parent )
    {
        final Label label = new Label( parent, SWT.NONE );
        label.setText( deprecatedLabelText.text() );
        addControl( label );
        label.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
        
        return label;
    }
    
    protected final void addControl( final Control control )
    {
        final Property property = property();
        
        this.controls.add( control );
        
        control.setEnabled( property.enabled() );
        
        control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    PropertyEditorPresentation.this.controls.remove( control );
                    
                    boolean timeToDispose = true;
                    
                    for( Control control : PropertyEditorPresentation.this.controls )
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
        
        attachHelp( control, property );
    }
    
    protected void handlePropertyChangedEvent()
    {
        final boolean enabled = property().enabled();
        
        for( Control control : this.controls )
        {
            if( ! control.isDisposed() )
            {
                control.setEnabled( enabled );
            }
        }

        if( this.auxTextProvider != null )
        {
            final String auxText = this.auxTextProvider.getAuxText( property().element(), property().definition() );
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
    
    protected final void attachAccessibleName( final Control control )
    {
        final StringBuilder name = new StringBuilder();
        
        name.append( property().definition().getLabel( true, CapitalizationType.NO_CAPS, false ) );
        
        if( property().definition().hasAnnotation( Deprecated.class ) )
        {
            name.append( ' ' );
            name.append( deprecatedAccessibleText.text() );
        }
        
        attachAccessibleName( control, name.toString() );
    }
    
    protected final void attachAccessibleName( final Control control, final String name )
    {
        control.getAccessible().addAccessibleListener
        (
            new AccessibleAdapter()
            {
                @Override
                public void getName( final AccessibleEvent event )
                {
                    event.result = name;
                }
            }
        );
    }
    
    @Override
    public void dispose()
    {
        for( Runnable op : this.onDisposeOperations )
        {
            try
            {
                op.run();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        this.onDisposeOperations.clear();
        
        if( this.relatedContentPresentations != null )
        {
            for( final Presentation presentation : this.relatedContentPresentations )
            {
                presentation.dispose();
            }
            
            this.relatedContentPresentations = null;
        }

        super.dispose();
    }
    
}
