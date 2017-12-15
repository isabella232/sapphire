/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329115] support more details link for long descriptions
 *                 [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twd;
import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twdindent;
import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twlayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.swt.SapphireTextPopup;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireSection

    extends SapphireComposite
    
{
    private final static String BREAK_TOKEN = "###brk###";
    
    private ISapphireSectionDef definition;
    private SapphireCondition visibleWhenCondition;
    private Section section;
    private FunctionResult titleFunctionResult;
    private SapphireFormText descriptionFormText;
    private FunctionResult descriptionFunctionResult;
    private String descriptionExtendedContent;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.definition = (ISapphireSectionDef) super.definition;
        
        this.visibleWhenCondition = null;
        
        final Class<?> visibleWhenConditionClass = this.definition.getVisibleWhenConditionClass().resolve();
        
        if( visibleWhenConditionClass != null )
        {
            final String parameter = this.definition.getVisibleWhenConditionParameter().getText();
            this.visibleWhenCondition = SapphireCondition.create( this, visibleWhenConditionClass, parameter );
        }
    }
    
    @Override
    protected Composite createOuterComposite( final SapphireRenderingContext context )
    {
        final String description = this.definition.getDescription().getLocalizedText();
        
        final FormToolkit toolkit = new FormToolkit( context.getDisplay() );
        
        this.section = toolkit.createSection( context.getComposite(), Section.TITLE_BAR );
        this.section.setLayoutData( twd() );
        
        this.titleFunctionResult = initExpression
        ( 
            this.definition.getLabel(), 
            new Runnable()
            {
                public void run()
                {
                    refreshTitle();
                }
            }
        );
        
        refreshTitle();
        
        final Composite outerComposite = new Composite( this.section, SWT.NONE );
        outerComposite.setLayout( twlayout( 1, 0, 0, 0, 0 ) );
        context.adapt( outerComposite );
        
        this.descriptionFunctionResult = initExpression
        ( 
            this.definition.getDescription(), 
            new Runnable()
            {
                public void run()
                {
                    refreshDescription();
                }
            }
        );
        
        if( this.descriptionFunctionResult != null )
        {
            this.descriptionFormText = new SapphireFormText( outerComposite, SWT.NONE );
            this.descriptionFormText.setLayoutData( twdindent( twd(), 9 ) );
            context.adapt( this.descriptionFormText );
            
            this.descriptionFormText.addHyperlinkListener
            (
                new HyperlinkAdapter()
                {
                    @Override
                    public void linkActivated( final HyperlinkEvent event )
                    {
                        activateExtendedDescriptionContentPopup();
                    }
                }
            );

            refreshDescription();
        }
        
        final Composite innerComposite = new Composite( outerComposite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0, ( description != null ? 8 : 0 ), 0 ) );
        innerComposite.setLayoutData( twd() );
        context.adapt( innerComposite );
        
        final SapphireActionGroup actions = getActions();
        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( context, actions );
        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( actionPresentationManager );
        
        final ToolBar toolbar = new ToolBar( this.section, SWT.FLAT | SWT.HORIZONTAL );
        toolBarActionsPresentation.setToolBar( toolbar );
        toolBarActionsPresentation.render();
        this.section.setTextClient( toolbar );
        
        toolkit.paintBordersFor( this.section );
        this.section.setClient( outerComposite );
        
        return innerComposite;
    }
    
    private void refreshTitle()
    {
        String title = null;
        
        if( this.titleFunctionResult != null )
        {
            title = (String) this.titleFunctionResult.value();
        }
        
        if( title == null )
        {
            title = "#null#";
        }
        else
        {
            title = title.trim();
            title = this.definition.adapt( LocalizationService.class ).transform( title, CapitalizationType.TITLE_STYLE, false );
        }
        
        this.section.setText( title.trim() );
    }

    private void refreshDescription()
    {
        String description = null;
        
        if( this.descriptionFunctionResult != null )
        {
            description = (String) this.descriptionFunctionResult.value();
        }
        
        if( description == null )
        {
            description = "#null#";
        }
        else
        {
            description = description.trim();
        }
    
        final int index = description.indexOf( BREAK_TOKEN );
        
        final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\">");
    
        if( index > 0 ) 
        {
            final String displayDescription = description.substring( 0, index );
    
            buf.append( displayDescription );
            buf.append( "<a href=\"action\" nowrap=\"true\">");
            buf.append( Resources.moreDetails );
            buf.append( "</a>" );
            
            this.descriptionExtendedContent 
                = displayDescription + description.substring( index + BREAK_TOKEN.length(), description.length() );
        }  
        else 
        {
            buf.append( description );
            this.descriptionExtendedContent = null;
        }
        
        buf.append( "</p></form>" );
        this.descriptionFormText.setText( buf.toString(), true, false );
    }

    private void activateExtendedDescriptionContentPopup()
    {
        if( this.descriptionExtendedContent != null )
        {
            final Point cursor = this.descriptionFormText.getDisplay().getCursorLocation();
            final Rectangle bounds = this.descriptionFormText.getBounds();
            final Point location = this.descriptionFormText.toDisplay( new Point( bounds.x, bounds.y ) );
            final Rectangle displayBounds = new Rectangle( location.x, location.y, bounds.width, bounds.height );
            
            Point position;
            
            if( displayBounds.contains( cursor ) ) 
            {
                position = cursor;
            } 
            else 
            {
                position = new Point( location.x, location.y + bounds.height + 2 );
            }
            
            final SapphireTextPopup popup = new SapphireTextPopup( this.descriptionFormText.getShell(), position );
            popup.setText( this.descriptionExtendedContent );
            popup.open();
        }
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_SECTION );
    }

    @Override
    public IContext getDocumentationContext()
    {
        final ISapphireDocumentation doc = this.definition.getDocumentation().element();
        
        if( doc != null )
        {
            ISapphireDocumentationDef docdef = null;
            
            if( doc instanceof ISapphireDocumentationDef )
            {
                docdef = (ISapphireDocumentationDef) doc;
            }
            else
            {
                docdef = ( (ISapphireDocumentationRef) doc ).resolve();
            }
            
            if( docdef != null )
            {
                return SapphireHelpSystem.getContext( docdef );
            }
        }
        
        return null;
    }

    public SapphireCondition getVisibleWhenCondition()
    {
        return this.visibleWhenCondition;
    }
    
    public boolean checkVisibleWhenCondition()
    {
        if( this.visibleWhenCondition != null )
        {
            return this.visibleWhenCondition.getConditionState();
        }
        
        return true;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.visibleWhenCondition != null )
        {
            this.visibleWhenCondition.dispose();
        }
    }

    private static final class Resources
    
        extends NLS
    
    {
        public static String moreDetails;
        
        static
        {
            initializeMessages( SapphireSection.class.getName(), Resources.class );
        }
    }

}
