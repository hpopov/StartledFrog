package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

import net.atlassian.cmathtutor.presenter.NewPersistenceUnitPresenter;

public class NewPersistenceUnitView extends FXMLView {

    @Override
    public NewPersistenceUnitPresenter getPresenter() {
        return (NewPersistenceUnitPresenter) super.getPresenter();
    }
}
