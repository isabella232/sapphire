/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Gregory Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar           
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.reflowOnResize;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.SectionDef;
import org.eclipse.sapphire.ui.forms.SectionPart;
import org.eclipse.sapphire.ui.forms.SectionPart.DescriptionEvent;
import org.eclipse.sapphire.ui.forms.SectionPart.TitleEvent;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.forms.swt.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SectionPresentation extends CompositePresentation
{
    private SapphireActionPresentationManager actionPresentationManager;
    private Section section;
    private SapphireFormText descriptionFormText;
    private Composite descriptionSpacer;
    private Composite sectionContentOuterComposite;
    private Composite sectionContentInnerComposite;

    public SectionPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public SectionPart part()
    {
        return (SectionPart) super.part();
    }
    
    @Override
    protected Composite renderOuterComposite( final GridData gd )
    {
        final SectionPart part = part();
        final SectionDef def = part.definition();
        
        final FormToolkit toolkit = new FormToolkit( composite().getDisplay() );
        
        final Composite outerComposite = new Composite( composite(), SWT.NONE );
        outerComposite.setLayoutData( gdhspan( ( part().getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        outerComposite.setLayout( glayout( 1, 10, 10, 10, 20 ) );
        
        register( outerComposite );
        
        final boolean collapsible = def.getCollapsible().content();
        final int style = Section.TITLE_BAR | ( collapsible ? Section.TWISTIE : SWT.NONE );
        
        this.section = toolkit.createSection( outerComposite, style );
        this.section.setLayoutData( gdfill() );
        
        if( collapsible )
        {
            this.section.setExpanded( ! part.folded() );
            
            this.section.addExpansionListener
            (
                new ExpansionAdapter()
                {
                    public void expansionStateChanged( final ExpansionEvent event )
                    {
                        if( event.getState() == true )
                        {
                            part.unfold();
                        }
                        else
                        {
                            part.fold();
                        }
                    }
                }
            );
        }
        
        this.sectionContentOuterComposite = new Composite( this.section, SWT.NONE );
        this.sectionContentOuterComposite.setLayout( glayout( 1, 0, 0 ) );
        
        final Color bgcolor = resources().color( part.getBackgroundColor(), org.eclipse.sapphire.Color.WHITE );
        this.sectionContentOuterComposite.setBackground( bgcolor );
        this.sectionContentOuterComposite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        attachPartListener
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof TitleEvent )
                    {
                        refreshTitle();
                    }
                    else if( event instanceof DescriptionEvent )
                    {
                        refreshDescription();
                    }
                }
            }
        );
        
        refreshTitle();
        refreshDescription();
        
        this.sectionContentInnerComposite = new Composite( this.sectionContentOuterComposite, SWT.NONE );
        this.sectionContentInnerComposite.setLayoutData( gdfill() );
        this.sectionContentInnerComposite.setLayout( glayout( 2, 0, 0 ) );
        
        final SapphireActionGroup actions = part.getActions();
        this.actionPresentationManager = new SapphireActionPresentationManager( this, actions );
        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( actionPresentationManager );
        
        final ToolBar toolbar = new ToolBar( this.section, SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT );
        toolBarActionsPresentation.setToolBar( toolbar );
        toolBarActionsPresentation.render();
        this.section.setTextClient( toolbar );
        
        final SapphireKeyboardActionPresentation keyboardActionsPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
        keyboardActionsPresentation.attach( toolbar );
        keyboardActionsPresentation.render();
        
        toolkit.paintBordersFor( this.section );
        this.section.setClient( this.sectionContentOuterComposite );
        
        return this.sectionContentInnerComposite;
    }
    
    private void refreshTitle()
    {
        String title = part().title();
        
        if( title == null )
        {
            title = "#null#";
        }
        else
        {
            title = title.trim();
            title = part().definition().adapt( LocalizationService.class ).transform( title, CapitalizationType.TITLE_STYLE, false );
        }
        
        this.section.setText( title.trim() );
    }

    private void refreshDescription()
    {
        String description = part().description();
        
        if( description != null )
        {
            description = description.trim();
            
            if( description.length() == 0 )
            {
                description = null;
            }
        }
        
        if( description == null )
        {
            if( this.descriptionFormText != null )
            {
                this.descriptionFormText.dispose();
                this.descriptionFormText = null;
                this.descriptionSpacer.dispose();
                this.descriptionSpacer = null;
            }
        }
        else
        {
            if( this.descriptionFormText == null )
            {
                this.descriptionFormText = new SapphireFormText( this.sectionContentOuterComposite, SWT.NONE );
                this.descriptionFormText.setLayoutData( gdhindent( gdwhint( gdhfill(), 100 ), 9 ) );
                reflowOnResize( this.descriptionFormText );
                
                this.descriptionSpacer = new Composite( this.sectionContentOuterComposite, SWT.NONE );
                this.descriptionSpacer.setLayoutData( gdhhint( gdhfill(), 5 ) );
                this.descriptionSpacer.setLayout( glayout( 1, 0, 0, 0, 0 ) );
                
                if( this.sectionContentInnerComposite != null )
                {
                    this.descriptionFormText.moveAbove( this.sectionContentInnerComposite );
                    this.descriptionSpacer.moveAbove( this.sectionContentInnerComposite );
                }
            }
            
            description = description.replace( "<", "&lt;" );
            
            final StringBuilder buf = new StringBuilder();
            buf.append( "<form><p vspace=\"false\">");
            buf.append( description );
            buf.append( "</p></form>" );
            
            this.descriptionFormText.setText( buf.toString(), true, false );
        }
        
        this.sectionContentOuterComposite.layout();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.actionPresentationManager != null )
        {
            this.actionPresentationManager.dispose();
            this.actionPresentationManager = null;
        }
        
        this.section = null;
        this.descriptionFormText = null;
        this.descriptionSpacer = null;
        this.sectionContentOuterComposite = null;
        this.sectionContentInnerComposite = null;
    }

}
