package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

import net.atlassian.cmathtutor.presenter.ConfigurationPresenter;

public class ConfigurationView extends FXMLView {

    @Override
    public ConfigurationPresenter getPresenter() {
	return (ConfigurationPresenter) super.getPresenter();
    }
}
