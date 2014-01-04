/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class StandardImplicitConnectionPart extends StandardDiagramConnectionPart 
{
    private Element srcNodeModel;
    private Element targetNodeModel;
    private DiagramImplicitConnectionTemplate connectionTemplate;
    
    public StandardImplicitConnectionPart(Element srcNodeModel, Element targetNodeModel)
    {
        this.srcNodeModel = srcNodeModel;
        this.targetNodeModel = targetNodeModel;
    }
    
    @Override
    protected void init()
    {   
        this.connectionTemplate = (DiagramImplicitConnectionTemplate)parent();
        this.modelElement = getModelElement();
    }
    
    @Override
    public Element getEndpoint1()
    {
        return this.srcNodeModel;
    }
    
    @Override
    public Element getEndpoint2()
    {
        return this.targetNodeModel;
    }
    
    @Override
    public boolean removable()
    {
    	return false;
    }
        
    @Override
    public void dispose()
    {
    }
    
    @Override
    public DiagramConnectionTemplate getDiagramConnectionTemplate()
    {
        return this.connectionTemplate;
    }
    
    @Override
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        return null;
    }
    
    @Override
    public void addModelListener()
    {    	
    }
    
    @Override
    public void removeModelListener()
    {        
    }
    
    @Override
    public void addReferenceServiceListeners()
    {    	
    }
    
}
