/******************************************************************************
 * Copyright (c) 2015 Oracle, Accenture and Modelity Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Konstantin Komissarchik - initial implementation and ongoing maintenance
 *   Kamesh Sampath - [354199] Support content proposals in text field property editor
 *   Roded Bahat - [376198] Vertically align actions for @LongString property editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_BROWSE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.DATA_BINDING;
import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.SensitiveData;
import org.eclipse.sapphire.services.ContentProposal;
import org.eclipse.sapphire.services.ContentProposalService;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandler.PostExecuteEvent;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.internal.TextFieldBinding;
import org.eclipse.sapphire.ui.forms.swt.internal.TextOverlayPainter;
import org.eclipse.sapphire.ui.listeners.ValuePropertyEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:rodedb@gmail.com">Roded Bahat</a>
 */

public class TextFieldPropertyEditorPresentation extends ValuePropertyEditorPresentation
{
    private Text textField;
    
    private static final String CONTENT_ASSIST_KEY_STROKE_STRING = "Ctrl+Space";
    private static final KeyStroke CONTENT_ASSIST_KEY_STROKE;
    
    static
    {
        KeyStroke keyStroke = null;
        
        try
        {
            keyStroke = KeyStroke.getInstance( CONTENT_ASSIST_KEY_STROKE_STRING );
        }
        catch( ParseException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        CONTENT_ASSIST_KEY_STROKE = keyStroke;
    }

    public TextFieldPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        createContents( parent, false );
    }
    
    protected Control createContents( final Composite parent,
                                      final boolean suppressBrowseAction )
    {
        final PropertyEditorPart part = part();
        final Value<?> property = (Value<?>) part.property();
        
        final boolean isLongString = property.definition().hasAnnotation( LongString.class );
        final boolean isDeprecated = property.definition().hasAnnotation( Deprecated.class );
        final boolean isReadOnly = ( property.definition().isReadOnly() || part.getRenderingHint( PropertyEditorDef.HINT_READ_ONLY, false ) );
        final boolean isSensitiveData = property.definition().hasAnnotation( SensitiveData.class );
        
        final SapphireActionGroup actions = getActions();
        final SapphireActionHandler jumpActionHandler = actions.getAction( ACTION_JUMP ).getFirstActiveHandler();

        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( getActionPresentationManager() );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_ASSIST ) );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_JUMP ) );
        
        actions.addFilter
        (
            new SapphireActionHandlerFilter()
            {
                @Override
                public boolean check( final SapphireActionHandler handler )
                {
                    final String actionId = handler.getAction().getId();
                    
                    if( actionId.equals( ACTION_BROWSE ) && ( isReadOnly || suppressBrowseAction ) )
                    {
                        return false;
                    }
                    
                    return true;
                }
            }
        );
        
        final boolean isActionsToolBarNeeded = toolBarActionsPresentation.hasActions();
        final boolean isBrowseOnly = part.getRenderingHint( PropertyEditorDef.HINT_BROWSE_ONLY, false );
        
        final Composite textFieldParent = createMainComposite
        (
            parent,
            new CreateMainCompositeDelegate( part )
            {
                @Override
                public boolean canScaleVertically()
                {
                    return isLongString;
                }
            }
        );
        
        addControl( textFieldParent );

        int textFieldParentColumns = 1;
        if( isActionsToolBarNeeded ) textFieldParentColumns++;
        if( isDeprecated ) textFieldParentColumns++;
        
        textFieldParent.setLayout( glayout( textFieldParentColumns, 0, 0, 0, 0 ) );
        
        final Composite nestedComposite = new Composite( textFieldParent, SWT.NONE );
        nestedComposite.setLayoutData( isLongString ? gdfill() : gdvalign( gdhfill(), SWT.CENTER ) );
        nestedComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
        
        addControl( nestedComposite );
        
        final PropertyEditorAssistDecorator decorator = createDecorator( nestedComposite ); 
        
        decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );
        decorator.addEditorControl( nestedComposite );
        
        final int style 
            = SWT.BORDER | 
              ( isLongString ? SWT.MULTI | SWT.WRAP | SWT.V_SCROLL : SWT.NONE ) |
              ( ( isReadOnly || isBrowseOnly ) ? SWT.READ_ONLY : SWT.NONE ) |
              ( isSensitiveData ? SWT.PASSWORD : SWT.NONE );
        
        this.textField = new Text( nestedComposite, style );
        this.textField.setLayoutData( gdfill() );
        decorator.addEditorControl( this.textField, true );
        
        final Serialization serialization = property.definition().getAnnotation( Serialization.class );

        final TextOverlayPainter.Controller textOverlayPainterController = new TextOverlayPainter.Controller()
        {
            @Override
            public boolean isHyperlinkEnabled()
            {
                return ( jumpActionHandler == null ? false : jumpActionHandler.isEnabled() );
            }

            @Override
            public void handleHyperlinkEvent()
            {
                if( jumpActionHandler != null )
                {
                    jumpActionHandler.execute( TextFieldPropertyEditorPresentation.this );
                }
            }

            @Override
            public String overlay()
            {
                String def = property.disposed() ? null : property.getDefaultText();
                
                if( def != null && isSensitiveData )
                {
                    final StringBuilder buf = new StringBuilder();
                    
                    for( int i = 0, n = def.length(); i < n; i++ )
                    {
                        buf.append( "\u25CF" );
                    }
                    
                    def = buf.toString();
                }
                
                if( def == null && serialization != null )
                {
                    def = serialization.primary();
                }
                
                return def;
            }
        };
            
        TextOverlayPainter.install( this.textField, textOverlayPainterController );
        
        if( isBrowseOnly || isReadOnly )
        {
            final Color bgcolor = new Color( this.textField.getDisplay(), 245, 245, 245 );
            this.textField.setBackground( bgcolor );
            
            this.textField.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        bgcolor.dispose();
                    }
                }
            );
        }
        
        attachAccessibleName( this.textField );
        
        final List<Control> relatedControls = new ArrayList<Control>();
        this.textField.setData( RELATED_CONTROLS, relatedControls );
        
        final Listener actionHandlerListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof PostExecuteEvent )
                {
                    if( ! TextFieldPropertyEditorPresentation.this.textField.isDisposed() )
                    {
                        TextFieldPropertyEditorPresentation.this.textField.setFocus();
                        TextFieldPropertyEditorPresentation.this.textField.setSelection( 0, TextFieldPropertyEditorPresentation.this.textField.getText().length() );
                    }
                }
            }
        };
        
        for( SapphireAction action : actions.getActions() )
        {
            if( ! action.getId().equals( ACTION_ASSIST ) )
            {
                for( SapphireActionHandler handler : action.getActiveHandlers() )
                {
                    handler.attach( actionHandlerListener );
                }
            }
        }
        
        if( isActionsToolBarNeeded )
        {
            final int alignment = ( isLongString ? SWT.VERTICAL : SWT.HORIZONTAL );
            final ToolBar toolbar = new ToolBar( textFieldParent, SWT.FLAT | alignment );
            toolbar.setLayoutData( gdvfill() );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
            addControl( toolbar );
            decorator.addEditorControl( toolbar );
            relatedControls.add( toolbar );
        }
        
        final ContentProposalService contentProposalService = property.service( ContentProposalService.class );
        
        if( contentProposalService != null )
        {
            final ContentProposalProvider contentProposalProvider = new ContentProposalProvider( contentProposalService, part );
            
            final ContentProposalAdapter contentProposalAdapter 
                = new ContentProposalAdapter( this.textField, new TextContentAdapter(), contentProposalProvider, CONTENT_ASSIST_KEY_STROKE, null );
            
            contentProposalAdapter.setPropagateKeys( true );
            contentProposalAdapter.setLabelProvider( new ContentProposalLabelProvider() );
            contentProposalAdapter.setProposalAcceptanceStyle( ContentProposalAdapter.PROPOSAL_REPLACE );
        }
        
        if( isDeprecated )
        {
            final Control deprecationMarker = createDeprecationMarker( textFieldParent );
            deprecationMarker.setLayoutData( gd() );
        }
        
        this.binding = new TextFieldBinding( this, this.textField );

        this.textField.setData( DATA_BINDING, this.binding );
        
        addControl( this.textField );
        
        // Hookup property editor listeners.
        
        final List<Class<?>> listenerClasses 
            = part.getRenderingHint( PropertyEditorDef.HINT_LISTENERS, Collections.<Class<?>>emptyList() );
        
        if( ! listenerClasses.isEmpty() )
        {
            final List<ValuePropertyEditorListener> listeners = new ArrayList<ValuePropertyEditorListener>();
            
            for( Class<?> cl : listenerClasses )
            {
                try
                {
                    final ValuePropertyEditorListener listener = (ValuePropertyEditorListener) cl.newInstance();
                    listener.initialize( this );
                    listeners.add( listener );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
            
            if( ! listeners.isEmpty() )
            {
                this.textField.addModifyListener
                (
                    new ModifyListener()
                    {
                        public void modifyText( final ModifyEvent event )
                        {
                            for( ValuePropertyEditorListener listener : listeners )
                            {
                                try
                                {
                                    listener.handleValueChanged();
                                }
                                catch( Exception e )
                                {
                                    Sapphire.service( LoggingService.class ).log( e );
                                }
                            }
                        }
                    }
                );
            }
        }

        return this.textField;
    }

    @Override
    protected boolean canScaleVertically()
    {
        return property().definition().hasAnnotation( LongString.class );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.textField.setFocus();
    }

    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            if( part.property().definition() instanceof ValueProperty )
            {
                return new TextFieldPropertyEditorPresentation( part, parent, composite );
            }
            
            return null;
        }
    }
    
    private static final class ContentProposalProvider implements
            IContentProposalProvider {
        private ContentProposalService contentProposalService;

        private ContentProposalService.Session session = null;
        private SapphirePart sapphirePart;

        public ContentProposalProvider(
                ContentProposalService contentProposalService,
                SapphirePart sapphirePart) {
            this.contentProposalService = contentProposalService;
            this.sapphirePart = sapphirePart;
        }

        public IContentProposal[] getProposals(String contents, int position) {
            if (this.session == null) {
                this.session = this.contentProposalService.session();
            }

            final String oldFilter = this.session.filter();
            final int oldFilterLength = oldFilter.length();
            final String newFilter = contents.substring( 0, position );

            if( position < oldFilterLength || ! oldFilter.equals( newFilter ) ) 
            {
                this.session = this.contentProposalService.session();
                
                if( position > 0 )
                {
                    this.session.advance( newFilter );
                }
            } 
            else if( position > oldFilterLength )
            {
                this.session.advance( newFilter.substring( oldFilterLength ) );
            }

            List<ContentProposal> filterProposals = this.session.proposals();
            IContentProposal[] arrContentProposals = makeProposalArray(filterProposals);
            return arrContentProposals;
        }

        private IContentProposal[] makeProposalArray(
                List<ContentProposal> proposals) {
            if (proposals != null) {
                IContentProposal[] arrContentProposals = new IContentProposal[proposals
                        .size()];
                for (int i = 0; i < proposals.size(); i++) {
                    ContentProposal contentProposalInfo = proposals.get(i);
                    ImageContentProposal contentProposal = new ImageContentProposal(
                            contentProposalInfo.content(),
                            contentProposalInfo.label(),
                            contentProposalInfo.description(),
                            contentProposalInfo.content().length(),
                            this.sapphirePart.getSwtResourceCache().image(
                                    contentProposalInfo.image()));
                    arrContentProposals[i] = contentProposal;
                }
                return arrContentProposals;
            } else {
                return new IContentProposal[0];
            }
        }
    }

    private static final class ContentProposalLabelProvider extends LabelProvider
    {
        @Override
        public Image getImage(Object element) {
    
            return ((ImageContentProposal) element).getImage();
        }
    
        @Override
        public String getText(Object element) {
            return ((ImageContentProposal) element).getLabel();
        }
    }

    private static final class ImageContentProposal extends org.eclipse.jface.fieldassist.ContentProposal {
    
        private Image image;
    
        public ImageContentProposal(String content, String label,
                String description, int cursorPosition, Image image) {
            super(content, label, description, cursorPosition);
            this.image = image;
        }
    
        public Image getImage() {
            return this.image;
        }
    }
    
}
