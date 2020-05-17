package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

public class StartView extends FXMLView implements Titlable {

    private static final String TITLE = "Create or load project";

    @Override
    public String getTitle() {
	return TITLE;
    }

}
