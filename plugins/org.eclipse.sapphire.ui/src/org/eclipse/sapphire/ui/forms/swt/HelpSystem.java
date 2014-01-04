/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.help.IContext;
import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.internal.DocumentationUtil;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationTopicDef;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class HelpSystem {
    
    public static IContext getContext(ISapphireDocumentationDef documentationDef) {
        final DocumentationContext context = new DocumentationContext(documentationDef);
        if (context.getText() != null || (context.getRelatedTopics() != null && context.getRelatedTopics().length > 0)) {
            return context;
        }
        return null;
    }

    public static Point computePopUpLocation(Display display) {
        Point point = display.getCursorLocation();
        return new Point(point.x + 15, point.y);
    }

    public static void setHelp(Control control, ISapphireDocumentationDef helpContentDef) {
        final DocumentationContext context = new DocumentationContext(helpContentDef);
        if ( context.getText() != null )
        {
            control.addHelpListener(new HelpListener() 
            {
                public void helpRequested(HelpEvent event) 
                {
                    // determine a location in the upper right corner of the widget
                    final Point point = computePopUpLocation(event.widget.getDisplay());
                    // display the help
                    PlatformUI.getWorkbench().getHelpSystem().displayContext(context, point.x, point.y);
                }
            });
        }
    }
    
    private static class DocumentationContext implements IContext, IContext2 {
        
        private String title = null;
        private String content = null;
        private IHelpResource[] topics;
        
        public DocumentationContext(final ISapphireDocumentationDef def) {
            if (def != null) {
                this.title = def.getTitle().localized();
                if (this.title != null) {
                    this.title = LabelTransformer.transform( this.title, CapitalizationType.TITLE_STYLE, false );
                }
                this.content = DocumentationUtil.decodeDocumentationTags(def.getContent().localized());
                
                ElementList<ISapphireDocumentationTopicDef> topics = def.getTopics();
                final int length = topics.size();
                this.topics = new IHelpResource[length];
                for( int i = 0, n = length; i < n; i++ ) {
                    final ISapphireDocumentationTopicDef topic = topics.get(i);
                    this.topics[i] = new IHelpResource() {
                        public String getHref() {
                            return topic.getHref().text();
                        }
        
                        public String getLabel() {
                            String label = topic.getLabel().localized();
                            if (label != null) {
                                return LabelTransformer.transform( label, CapitalizationType.TITLE_STYLE, false );
                            }
                            return label;
                        }
                    };
                }
            }
        }

        public String getTitle() {
            return this.title;
        }

        public String getStyledText() {
            return null;
        }

        public String getCategory(IHelpResource topic) {
            return null;
        }

        public IHelpResource[] getRelatedTopics() {
            return this.topics;
        }

        public String getText() {
            return this.content;
        }

    }
}
