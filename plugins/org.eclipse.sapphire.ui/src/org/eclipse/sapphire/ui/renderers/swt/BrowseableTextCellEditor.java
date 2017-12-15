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

import static org.eclipse.sapphire.ui.util.SwtUtil.gd;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.glspacing;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireCommands;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.BrowseHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class BrowseableTextCellEditor

    extends TextCellEditor

{
    private final SapphirePart part;
    private final StructuredViewer viewer;
    private final DefaultListPropertyEditorRenderer.SelectionProvider selectionProvider;
    private Composite topComposite;
    private Composite innerComposite;
    private Text text;
    private Label button;
    private List<BrowseHandler> browseHandlers;
    private SapphireRenderingContext context;
    private boolean disableFocusLostHandler;
    private int maxWidth = -1;
    private boolean isDefaultValue;
    private ISelection selectionPriorToActivation;
    
    public BrowseableTextCellEditor( final SapphirePart part,
                                     final StructuredViewer parent,
                                     final DefaultListPropertyEditorRenderer.SelectionProvider selectionProvider,
                                     final int style )
    {
        super();
        
        this.part = part;
        this.viewer = parent;
        this.selectionProvider = selectionProvider;
        this.browseHandlers = Collections.emptyList();
        
        setStyle( style );
        create( (Composite) parent.getControl() );
    }
    
    public void setBrowseHandlers( final List<BrowseHandler> browseHandlers )
    {
        this.browseHandlers = ( browseHandlers != null ? browseHandlers : Collections.<BrowseHandler>emptyList() );
    }
    
    public void setContext( final SapphireRenderingContext context )
    {
        this.context = context;
    }
    
    public void setMaxWidth( final int maxWidth )
    {
        this.maxWidth = maxWidth;
        
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

        this.topComposite = new Composite( parent, SWT.NONE );
        this.topComposite.setBackground( parent.getBackground() );
        this.topComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 0 ) );
        
        this.innerComposite = new Composite( this.topComposite, style );
        this.innerComposite.setLayoutData( gdfill() );
        this.innerComposite.setLayout( new CustomLayout() );
        this.innerComposite.setBackground( parent.getBackground() );
        
        final Label spacer = new Label( this.topComposite, SWT.NONE );
        spacer.setText( MiscUtil.EMPTY_STRING );
        spacer.setLayoutData( gd() );
        
        setStyle( SWT.NONE );
        this.text = (Text) super.createControl( this.innerComposite );
        setStyle( style );
        
        SapphireCommands.configurePropertyEditorContext( this.text );
        
        this.text.addModifyListener
        (
            new ModifyListener()
            {
                public void modifyText( final ModifyEvent event )
                {
                    BrowseableTextCellEditor.this.isDefaultValue = false;
                }
            }
        );
        
        this.button = new Label( this.innerComposite, SWT.NONE );
        this.button.setBackground( parent.getBackground() );
        this.button.setImage( this.part.getImageCache().getImage( SapphireImageCache.ACTION_BROWSE_MINI ) );
        
        final Cursor browseButtonCursor = new Cursor( this.button.getDisplay(), SWT.CURSOR_HAND );
        this.button.setCursor( browseButtonCursor );
        
        this.button.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    browseButtonCursor.dispose();
                }
            }
        );
        
        this.button.addMouseListener
        (
            new MouseAdapter()
            {
                @Override
                public void mouseUp( final MouseEvent event )
                {
                    handleBrowseEvent();
                }
            }
        );
        
        final IHandler browseCommandHandler = new AbstractHandler() 
        {
            public Object execute( final ExecutionEvent event )
            {
                handleBrowseEvent();
                return null;
            }
        };
        
        SapphireCommands.attachBrowseHandler( this.text, browseCommandHandler );
        
        return this.topComposite;
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
        LayoutData data = new LayoutData();
        data.minimumWidth = 150;
        return data;
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
    
    private void handleBrowseEvent()
    {
        if( this.browseHandlers != null && ! this.browseHandlers.isEmpty() )
        {
            this.disableFocusLostHandler = true;
            
            final BrowseCommandHandler browseCommandHandler = new BrowseCommandHandler( this.context, this.browseHandlers )
            {
                @Override
                protected Rectangle getInvokerBounds()
                {
                    Rectangle bounds = BrowseableTextCellEditor.this.button.getBounds();
                    
                    final Point convertedCoordinates = BrowseableTextCellEditor.this.text.getParent().toDisplay( bounds.x, bounds.y );
                    bounds.x = convertedCoordinates.x;
                    bounds.y = convertedCoordinates.y;
                    
                    return bounds;
                }
    
                @Override
                protected void handleBrowseCompleted( final String text )
                {
                    BrowseableTextCellEditor.this.disableFocusLostHandler = false;
                    
                    if( text != null )
                    {
                        BrowseableTextCellEditor.this.text.setText( text );
                        focusLost();
                    }
                }

                @Override
                protected void handleBrowseCanceled()
                {
                    BrowseableTextCellEditor.this.disableFocusLostHandler = false;
                }
            };
            
            browseCommandHandler.execute( null );
        }
    }
    
    private final class CustomLayout
    
        extends Layout
    
    {
        private final Point ZERO_SIZE = new Point( 0, 0 );
        
        public void layout( final Composite editor,
                            final boolean force )
        {
            final Rectangle bounds = editor.getClientArea();
            final Point size = computeButtonSize( force );
            
            final int constraintedBoundsWidth 
                = ( BrowseableTextCellEditor.this.maxWidth != -1 ? Math.min( bounds.width, BrowseableTextCellEditor.this.maxWidth ) : bounds.width );
            
            if( BrowseableTextCellEditor.this.text != null )
            {
                BrowseableTextCellEditor.this.text.setBounds( 0, 0, constraintedBoundsWidth - size.x, bounds.height );
            }
            
            BrowseableTextCellEditor.this.button.setBounds( constraintedBoundsWidth - size.x, 0, size.x, bounds.height );
        }
        
        public Point computeSize( final Composite editor,
                                  final int wHint,
                                  final int hHint,
                                  final boolean force )
        {
            if( wHint != SWT.DEFAULT && hHint != SWT.DEFAULT )
            {
                return new Point( wHint, hHint );
            }
            
            final Point contentsSize 
                = BrowseableTextCellEditor.this.text.computeSize( SWT.DEFAULT, SWT.DEFAULT, force );
            
            final Point buttonSize = computeButtonSize( force );
            
            final Point result 
                = new Point( buttonSize.x, Math.max( contentsSize.y, buttonSize.y ) );
            
            return result;
        }
        
        private Point computeButtonSize( final boolean force )
        {
            if( BrowseableTextCellEditor.this.browseHandlers.isEmpty() )
            {
                return this.ZERO_SIZE;
            }
            else
            {
                return BrowseableTextCellEditor.this.button.computeSize( SWT.DEFAULT, SWT.DEFAULT, force );
            }
        }
    }
    
}

