package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

import net.atlassian.cmathtutor.presenter.NewAssociationConnectionPresenter;

public class NewAssociationConnectionView extends FXMLView {

    @Override
    public NewAssociationConnectionPresenter getPresenter() {
	return (NewAssociationConnectionPresenter) super.getPresenter();
    }
}
