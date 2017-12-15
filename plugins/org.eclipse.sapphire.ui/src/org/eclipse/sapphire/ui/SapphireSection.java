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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twd;
import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twdindent;
import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twlayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.actions.ActionsRenderer;
import org.eclipse.sapphire.ui.actions.RestoreDefaultsAction;
import org.eclipse.sapphire.ui.actions.ShowHelpAction;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.internal.ActionsHostUtil;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireSection

    extends SapphireComposite
    
{
    private ISapphireSectionDef definition;
    private List<ActionGroup> actions = null;
    private SapphireCondition visibleWhenCondition;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.definition = (ISapphireSectionDef) super.definition;
        
        this.visibleWhenCondition = null;
        
        final Class<?> visibleWhenConditionClass = this.definition.getVisibleWhenConditionClass().resolve();
        
        if( visibleWhenConditionClass != null )
        {
            try
            {
                this.visibleWhenCondition = (SapphireCondition) visibleWhenConditionClass.newInstance();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            if( this.visibleWhenCondition != null )
            {
                final String parameter = this.definition.getVisibleWhenConditionParameter().getText();
                this.visibleWhenCondition.init( new SapphirePartContext( this ), parameter );                
            }
        }
    }
    
    @Override
    protected Composite createOuterComposite( final SapphireRenderingContext context )
    {
        final String title = this.definition.getLabel().getLocalizedText();
        final String description = this.definition.getDescription().getLocalizedText();
        
        final FormToolkit toolkit = new FormToolkit( context.getDisplay() );
        
        final Section section = toolkit.createSection( context.getComposite(), Section.TITLE_BAR );
        section.setLayoutData( twd() );
        section.setText( title.trim() );
        
        final Composite outerComposite = new Composite( section, SWT.NONE );
        outerComposite.setLayout( twlayout( 1, 0, 0, 0, 0 ) );
        context.adapt( outerComposite );
        
        if( description != null )
        {
            final Label descriptionControl = new Label( outerComposite, SWT.WRAP );
            descriptionControl.setLayoutData( twdindent( twd(), 9 ) );
            descriptionControl.setText( description.trim() );
        }
        
        final Composite innerComposite = new Composite( outerComposite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0, ( description != null ? 8 : 0 ), 0 ) );
        innerComposite.setLayoutData( twd() );
        context.adapt( innerComposite );
        
        final ToolBar toolbar = new ToolBar( section, SWT.FLAT | SWT.HORIZONTAL );
        ActionsRenderer.fillToolBar( toolbar, getActions() );
        section.setTextClient( toolbar );
        
        toolkit.paintBordersFor( section );
        section.setClient( outerComposite );
        
        return innerComposite;
    }

    public List<ActionGroup> getActions()
    {
        if( this.actions == null )
        {
            this.actions = new ArrayList<ActionGroup>();
            
            final ActionGroup systemActionsGroup = new ActionGroup();
            systemActionsGroup.addAction( new RestoreDefaultsAction() );
            systemActionsGroup.addAction( new ShowHelpAction() );
            
            this.actions.add( systemActionsGroup );
            
            ActionsHostUtil.initActions( this.actions, this.definition.getActionSetDef() );
            
            for( ActionGroup group : this.actions )
            {
                for( Action action : group.getActions() )
                {
                    action.setPart( this );
                }
            }
        }
        
        return this.actions;
    }
    
    @Override
    public Action getAction( final String id )
    {
        return super.getAction( id );
    }
    
    @Override
    public String getHelpContextId()
    {
        return this.definition.getHelpContextId().getText();
    }

    public SapphireCondition getVisibleWhenCondition()
    {
        return this.visibleWhenCondition;
    }
    
    public boolean checkVisibleWhenCondition()
    {
        if( this.visibleWhenCondition != null )
        {
            return this.visibleWhenCondition.evaluate();
        }
        
        return true;
    }
    
}
