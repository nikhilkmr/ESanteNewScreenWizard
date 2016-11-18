package esantenewscreenwizard.preferences;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import esantenewscreenwizard.Activator;

public class ESantePreferenceTemplatePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage {
	
	private Composite fieldEditorParent;
	private Combo selectTamplate;
	private Text previewArea;
	private int style;

	public ESantePreferenceTemplatePage() {
		super(GRID);
		style = GRID;
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Evalon Sante Templates");
		getFieldEditorParent();
	}

	protected Control createContents(Composite parent) {
		this.fieldEditorParent = new Composite(parent, SWT.NULL);
		GridLayout mainLayout = new GridLayout(4, false);
		//FillLayout layout = new FillLayout();
		//RowLayout layout = new RowLayout();
		
		
		
		
		this.fieldEditorParent.setLayout(mainLayout);
		mainLayout.verticalSpacing = 9;
		
		Label label1 = new Label(this.fieldEditorParent, SWT.NULL);
		label1.setText("&Select template");
		selectTamplate = new Combo(this.fieldEditorParent, SWT.DROP_DOWN
				| SWT.BORDER);
		selectTamplate.add(ESantePreferenceConstants.POP);
		selectTamplate.add(ESantePreferenceConstants.MAP);
		selectTamplate.add(ESantePreferenceConstants.ACTIONFORM);
		selectTamplate.add(ESantePreferenceConstants.BPI);
		selectTamplate.add(ESantePreferenceConstants.BUSINESS);
		selectTamplate.add(ESantePreferenceConstants.BEAN);
		selectTamplate.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				tamplateChanged();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		GridData selectTamplateGridData = new GridData();
		selectTamplateGridData.horizontalAlignment = GridData.FILL;
		selectTamplateGridData.horizontalSpan = 4;
		selectTamplate.setLayoutData(selectTamplateGridData);
		
		previewArea = new Text(this.fieldEditorParent, SWT.V_SCROLL);
		GridData previewAreaGridData = new GridData(400,200);
		previewAreaGridData.horizontalAlignment = GridData.FILL;
		previewAreaGridData.grabExcessHorizontalSpace = true;
		previewAreaGridData.verticalAlignment = GridData.FILL;
		previewAreaGridData.horizontalSpan = 4;
		previewArea.setLayoutData(previewAreaGridData);
		previewArea.setEditable(false);
		
		
		previewArea.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				createFieldEditors();

			}
		});

		// if (this.style == 1) {
		// adjustGridLayout();
		// }

		initialize();
		checkState();
		return this.fieldEditorParent;
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		InputDialog dlg = new InputDialog(
				Display.getCurrent().getActiveShell(),
				this.selectTamplate.getText() + " Format", "Edit Tamplate",
				getTamplateText(), null) {

			/**
			 * Override this method to make the text field multilined and give
			 * it a scroll bar. But...
			 */
			@Override
			protected int getInputTextStyle() {
				return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
			}

			/**
			 * ...it still is just one line high. This hack is not very nice,
			 * but at least it gets the job done... ;o)
			 */
			@Override
			protected Control createDialogArea(Composite parent) {
				Control res = super.createDialogArea(parent);
				((GridData) this.getText().getLayoutData()).heightHint = 100;
				return res;
			}

		};
		int textAreaState = dlg.open();
		if (textAreaState == InputDialog.OK) {
			String tamplateText = dlg.getValue();
			saveTamplate(tamplateText);
		}

	}

	private void saveTamplate(String tamplateText) {
		Activator.getDefault().getPreferenceStore()
				.setValue(this.selectTamplate.getText(), tamplateText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Evalon Sante tamplate preferences");
	}

	private void tamplateChanged() {
		String tamplateText = getTamplateText();
		previewArea.setText(tamplateText);
	}

	private String getTamplateText() {
		return getPreferenceStore()
				.getString(this.selectTamplate.getText());

	}

}
