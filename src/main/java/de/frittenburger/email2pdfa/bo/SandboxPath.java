package de.frittenburger.email2pdfa.bo;

import java.io.File;

import de.frittenburger.bo.AdminPanelException;
import de.frittenburger.bo.TextInput;
import de.frittenburger.core.I18n;

public class SandboxPath extends TextInput {
	
	@Override
	public String getDefaultDescription() {
		return I18n.tr("Sandbox Path");
	}
	
	@Override
	public void verify(String value) throws AdminPanelException {
		super.verify(value);
		if(!new File(value).isDirectory()) 
			throw new AdminPanelException(AdminPanelException.TValidation,value + " is not a directory");
	}

}
