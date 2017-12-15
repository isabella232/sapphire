/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329115] support more details link for long descriptions
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Gregory Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar           
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.reflowOnResize;

import java.util.Collections;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.def.SectionDef;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class SectionPart extends CompositePart
{
    private FunctionResult titleFunctionResult;
    private FunctionResult descriptionFunctionResult;
    
    private SapphireRenderingContext context;
    private Section section;
    private SapphireFormText descriptionFormText;
    private Composite descriptionSpacer;
    private Composite sectionContentOuterComposite;
    private Composite sectionContentInnerComposite;
    private boolean expanded;
    
    @Override
    protected void init()
    {
        super.init();
        
        final SectionDef def = definition();
        
        this.expanded = ! def.getCollapsedInitially().getContent();
    }
    
    @Override
    public SectionDef definition()
    {
        return (SectionDef) super.definition();
    }

    @Override
    protected Composite createOuterComposite( final SapphireRenderingContext context )
    {
        this.context = context;
        this.section = null;
        this.descriptionFormText = null;
        this.descriptionSpacer = null;
        this.sectionContentOuterComposite = null;
        this.sectionContentInnerComposite = null;
        
        final FormToolkit toolkit = new FormToolkit( context.getDisplay() );
        
        final SectionDef def = definition();
        
        final Composite outerComposite = new Composite( context.getComposite(), SWT.NONE );
        outerComposite.setLayoutData( createSectionLayoutData() );
        outerComposite.setLayout( glayout( 1, 10, 10, 10, 20 ) );
        context.adapt( outerComposite );
        
        final boolean collapsible = def.getCollapsible().getContent();
        final int style = Section.TITLE_BAR | ( collapsible ? Section.TWISTIE : SWT.NONE );
        
        this.section = toolkit.createSection( outerComposite, style );
        this.section.setLayoutData( gdfill() );
        
        if( collapsible )
        {
            this.section.setExpanded( this.expanded );
            
            this.section.addExpansionListener
            (
                new ExpansionAdapter()
                {
                    public void expansionStateChanged( final ExpansionEvent event )
                    {
                        SectionPart.this.expanded = event.getState();
                    }
                }
            );
        }
        
        this.titleFunctionResult = initExpression
        (
            getModelElement(),
            def.getLabel().getContent(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    refreshTitle();
                }
            }
        );
        
        refreshTitle();
        
        this.sectionContentOuterComposite = new Composite( this.section, SWT.NONE );
        this.sectionContentOuterComposite.setLayout( glayout( 1, 0, 0 ) );
        context.adapt( this.sectionContentOuterComposite );

        this.descriptionFunctionResult = initExpression
        (
            getModelElement(),
            def.getDescription().getContent(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    refreshDescription();
                }
            }
        );

        refreshDescription();
        
        this.sectionContentInnerComposite = new Composite( this.sectionContentOuterComposite, SWT.NONE );
        this.sectionContentInnerComposite.setLayoutData( gdfill() );
        this.sectionContentInnerComposite.setLayout( glayout( 2, 0, 0 ) );
        context.adapt( this.sectionContentInnerComposite );
        
        final SapphireActionGroup actions = getActions();
        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( context, actions );
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
    
    protected Object createSectionLayoutData()
    {
        return gdhspan( ( getScaleVertically() ? gdfill() : gdhfill() ), 2 );
    }
    
    private void refreshTitle()
    {
        String title = (String) this.titleFunctionResult.value();
        
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
        String description = (String) this.descriptionFunctionResult.value();
        
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
                this.context.adapt( this.descriptionSpacer );
                
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

}
