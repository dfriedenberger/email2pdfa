package de.frittenburger.email2pdfa.bo;

import de.frittenburger.core.I18n;
import de.frittenburger.form.Form;

public class GlobalConfig extends Form {

	public SandboxPath path;
		
	@Override
	public String getEntityName() {
		return I18n.tr("Data File");
	}
}
