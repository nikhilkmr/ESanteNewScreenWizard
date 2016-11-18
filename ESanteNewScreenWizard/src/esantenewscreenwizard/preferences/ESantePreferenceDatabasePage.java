package esantenewscreenwizard.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import esantenewscreenwizard.Activator;

public class ESantePreferenceDatabasePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage {
		
	public ESantePreferenceDatabasePage() {
		super(GRID);
	}

	
	

	@Override
	protected void createFieldEditors() {
		
		
		StringFieldEditor userName = new StringFieldEditor(ESantePreferenceConstants.PREFERENCE_KEY_PREFIX+".database.username", "User Name", getFieldEditorParent());
		StringFieldEditor password = new StringFieldEditor(ESantePreferenceConstants.PREFERENCE_KEY_PREFIX+".database.password", "Password", getFieldEditorParent());
		addField(userName);
		addField(password);
		
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Evalon Sante datbase preferences");

	}

	

	

}
