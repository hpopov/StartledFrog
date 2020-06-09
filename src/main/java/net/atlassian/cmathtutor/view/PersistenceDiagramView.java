package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

import de.fxdiagram.core.XRoot;

public class PersistenceDiagramView extends FXMLView {

    @Override
    public XRoot getView() {
	XRoot parent = (XRoot) super.getView();
	return parent;
    }
}
