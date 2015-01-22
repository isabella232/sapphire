/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.modeling.annotations.EnumSerialization;
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public enum LineStyle 
{
    @Label( standard = "solid" )
    @EnumSerialization( primary = "solid" )
    
    SOLID,
    
    @Label( standard = "dash" )
    @EnumSerialization( primary = "dash" )
    
    DASH,
    
    @Label( standard = "dot" )
    @EnumSerialization( primary = "dot" )
    
    DOT,
    
    @Label( standard = "dash-dot" )
    @EnumSerialization( primary = "dash-dot" )
    
    DASH_DOT;
    
}
