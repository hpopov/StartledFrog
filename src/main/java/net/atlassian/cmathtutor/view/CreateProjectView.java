package net.atlassian.cmathtutor.view;

import com.airhacks.afterburner.views.FXMLView;

public class CreateProjectView extends FXMLView implements Titlable {

    private static final String TITLE = "Create new project";

    @Override
    public String getTitle() {
        return TITLE;
    }
}
