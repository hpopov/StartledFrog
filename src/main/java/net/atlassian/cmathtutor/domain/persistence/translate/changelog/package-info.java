
@XmlSchema(xmlns = {
	@XmlNs(prefix = "", namespaceURI = "http://www.liquibase.org/xml/ns/dbchangelog") },
	namespace = "http://www.liquibase.org/xml/ns/dbchangelog",
	location = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd",
	elementFormDefault = XmlNsForm.QUALIFIED)
package net.atlassian.cmathtutor.domain.persistence.translate.changelog;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
