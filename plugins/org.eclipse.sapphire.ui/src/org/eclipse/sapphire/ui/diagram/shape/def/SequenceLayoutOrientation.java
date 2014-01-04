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

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.modeling.annotations.EnumSerialization;
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public enum SequenceLayoutOrientation 
{
    @Label( standard = "vertical" )
    @EnumSerialization( primary = "vertical" )
    
    VERTICAL,
    
    @Label( standard = "horizontal" )
    @EnumSerialization( primary = "horizontal" )
    
    HORIZONTAL,
	    
    @Label( standard = "stacked" )
    @EnumSerialization( primary = "stacked" )
    
    STACKED
    
}
