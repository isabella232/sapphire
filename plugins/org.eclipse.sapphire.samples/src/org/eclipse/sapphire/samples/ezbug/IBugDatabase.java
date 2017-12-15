/******************************************************************************
 * Copyright (c) 2013 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.samples.ezbug;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@GenerateImpl
@XmlBinding( path = "bug-database" )

public interface IBugDatabase extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( IBugDatabase.class );

    // *** BugReports ***
    
    @Type( base = IBugReport.class )
    @Label( standard = "bug report" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "bug", type = IBugReport.class ) )
    
    ListProperty PROP_BUG_REPORTS = new ListProperty( TYPE, "BugReports" );
    
    ModelElementList<IBugReport> getBugReports();
    
}
