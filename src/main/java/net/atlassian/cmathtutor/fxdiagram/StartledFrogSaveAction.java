package net.atlassian.cmathtutor.fxdiagram;

import de.fxdiagram.core.XRoot;
import de.fxdiagram.core.tools.actions.SaveAction;
import lombok.AllArgsConstructor;
import net.atlassian.cmathtutor.domain.persistence.model.PersistenceModel;
import net.atlassian.cmathtutor.service.PersistenceDomainService;

@AllArgsConstructor
public class StartledFrogSaveAction extends SaveAction {

    private final PersistenceDomainService persistenceDomainService;
    private final PersistenceModel persistenceModel;

    @Override
    public void perform(XRoot root) {
	persistenceDomainService.persistModel(persistenceModel);
	super.perform(root);
    }
}
