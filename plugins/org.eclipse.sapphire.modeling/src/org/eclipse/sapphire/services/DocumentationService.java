/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class DocumentationService extends Service
{
    public abstract String content();
    
    public List<Topic> topics() 
    {
        return Collections.emptyList();
    }
    
    public static final class Topic
    {
        private final String label;
        private final String url;
        
        public Topic( final String label,
                      final String url )
        {
            this.label = label;
            this.url = url;
        }
        
        public String label()
        {
            return this.label;
        }
        
        public String url()
        {
            return this.url;
        }
    }

}
