package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

import de.fxdiagram.core.XRoot;
import net.atlassian.cmathtutor.presenter.PersistenceDiagramPresenter;

public class PersistenceDiagramView extends FXMLView {

    @Override
    public XRoot getView() {
	XRoot parent = (XRoot) super.getView();
	return parent;
    }

    @Override
    public PersistenceDiagramPresenter getPresenter() {
	return (PersistenceDiagramPresenter) super.getPresenter();
    }
}
