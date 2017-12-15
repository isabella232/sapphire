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

package org.eclipse.sapphire.samples.ezbug;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IExecutableModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBindingModelImpl;
import org.eclipse.sapphire.modeling.xml.annotations.RootXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.ezbug.internal.FileBugReportOpMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBindingModelImpl
@RootXmlBinding( elementName = "report" )

public interface IFileBugReportOp

    extends IModelForXml, IExecutableModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IFileBugReportOp.class );
    
    // *** BugReport ***
    
    @Type( base = IBugReport.class )
    @Label( standard = "bug report" )
    @XmlBinding( path = "bug" )
    
    ElementProperty PROP_BUG_REPORT = new ElementProperty( TYPE, "BugReport" );
    
    IBugReport getBugReport();
    IBugReport getBugReport( boolean createIfNecessary );
    
    // *** Method: execute ***
    
    @DelegateImplementation( FileBugReportOpMethods.class )
    
    IStatus execute( IProgressMonitor monitor );
    
}
