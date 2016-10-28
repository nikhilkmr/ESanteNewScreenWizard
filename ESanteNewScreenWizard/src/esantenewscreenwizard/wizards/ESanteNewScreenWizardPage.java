package esantenewscreenwizard.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class ESanteNewScreenWizardPage extends NewTypeWizardPage {
	private Combo  domain;
	private Text transactionCode;
	private Text taskId;
	private Text screenName;

	private Text containerText;
	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ESanteNewScreenWizardPage(ISelection selection) {
		super(true,"wizardPage");
		setTitle("Evolan sante new screen.");
		setDescription("This wizard creates necessary (Pop,Map,ActionForm and Jsps), which are required to depelop new screen in Evolan Sante.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(4, true);
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		Label label1 = new Label(container, SWT.NULL);
		label1.setText("&Domain:");
		
		domain = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
		RowLayout rowLayout = new RowLayout();
	    rowLayout.spacing = 15;
	    rowLayout.marginWidth = 15;
	    rowLayout.marginHeight = 15;
	    domain.add("ad");
	    domain.add("pr");
	    domain.add("rc");
	    domain.add("co");
		
	    Label label2 = new Label(container, SWT.NULL);
		label2.setText("&Transaction Code:");
	    GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
	    transactionCode  = new Text(container, SWT.BORDER | SWT.SINGLE);
	    transactionCode.setText("");
	    transactionCode.setLayoutData(gd1);
	    transactionCode.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

	    Label label3 = new Label(container, SWT.NULL);
		label3.setText("&Task Id:");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
	    taskId = new Text(container, SWT.NULL);
	    taskId.setLayoutData(gd2);
	    taskId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
	    
	    Label label5 = new Label(container, SWT.NULL);
		label5.setText("&Screen Name:");
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
	    screenName = new Text(container, SWT.NULL);
	    screenName.setLayoutData(gd5);
	    screenName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
	    Label label4 = new Label(container, SWT.NULL);
		label4.setText("&Container text:");
	    containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
	    GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
	    containerText.setLayoutData(gd3);
	    containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

	    
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText("new_file.mpe");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("mpe") == false) {
				updateStatus("File extension must be \"mpe\"");
				return;
			}
		}
		//updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	public Combo getDomain() {
		return domain;
	}

	public void setDomain(Combo domain) {
		this.domain = domain;
	}

	public Text getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(Text transactionCode) {
		this.transactionCode = transactionCode;
	}

	public Text getTaskId() {
		return taskId;
	}

	public void setTaskId(Text taskId) {
		this.taskId = taskId;
	}

	public Text getContainerText() {
		return containerText;
	}

	public void setContainerText(Text containerText) {
		this.containerText = containerText;
	}

	public Text getFileText() {
		return fileText;
	}

	public void setFileText(Text fileText) {
		this.fileText = fileText;
	}

	public ISelection getSelection() {
		return selection;
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	public Text getScreenName() {
		return screenName;
	}

	public void setScreenName(Text screenName) {
		this.screenName = screenName;
	}
}