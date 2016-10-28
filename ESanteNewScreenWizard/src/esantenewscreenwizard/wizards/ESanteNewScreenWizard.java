package esantenewscreenwizard.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedConstructorsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class ESanteNewScreenWizard extends Wizard implements INewWizard {
	/**
	 * Public access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_PUBLIC = Flags.AccPublic;
	/**
	 * Private access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_PRIVATE = Flags.AccPrivate;
	/**
	 * Protected access flag. See The Java Virtual Machine Specification for
	 * more details.
	 */
	public int F_PROTECTED = Flags.AccProtected;
	/**
	 * Static access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_STATIC = Flags.AccStatic;
	/**
	 * Final access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_FINAL = Flags.AccFinal;
	/**
	 * Abstract property flag. See The Java Virtual Machine Specification for
	 * more details.
	 */
	public int F_ABSTRACT = Flags.AccAbstract;

	private IPackageFragment actionPackage;
	private StubTypeContext fSuperClassStubTypeContext;
	private IType fCurrType;
	private ESanteNewScreenWizardPage page;
	private ISelection selection;
	String directoryPath = "com.mutulles.rc";
	private String typeName;
	private String taskId;

	/**
	 * Constructor for ESanteNewScreenWizard.
	 */
	public ESanteNewScreenWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ESanteNewScreenWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String domain = page.getDomain().getText();
		final String transactionCode = page.getTransactionCode().getText();
		final String taskId = page.getTaskId().getText();
		final String screenName = page.getScreenName().getText();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					doFinish(containerName, domain, transactionCode, taskId,
							screenName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 * 
	 * @throws InterruptedException
	 */

	private void doFinish(String containerName, String domain,
			String transactionCode, String taskId, String screenName,
			String fileName, IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		// create a sample file
		this.taskId = taskId;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(containerName);
		IJavaProject javaProject = JavaCore.create(project);
		monitor.beginTask("Creating " + domain, 2);
		IResource resource = root.findMember(new Path(containerName));

		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;

		createPopAndMap(project, javaProject, container, monitor, domain,
				screenName);

		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				// try {
				// IDE.openEditor(page, file, true);
				// } catch (PartInitException e) {
				// }
			}
		});
		monitor.worked(1);
	}

	private void createPopAndMap(IProject project, IJavaProject javaProject,
			IContainer container, IProgressMonitor monitor, String domain,
			String screenName) throws CoreException, InterruptedException {
		create(project, javaProject, container, monitor, domain, "Pop"+screenName, "java.util.AbstractList", "actions");
		create(project, javaProject, container, monitor, domain, "Map"+screenName, "java.util.AbstractSet", "actions");
		create(project, javaProject, container, monitor, domain, "BPI"+screenName, "java.util.AbstractMap", "Bpi");
	}
	
	private void create(IProject project, IJavaProject javaProject,
			IContainer container, IProgressMonitor monitor, String domain,
			String screenName, String superclass, String packageName) throws CoreException, InterruptedException {
		IFolder sourceFolder = project.getFolder("src");
		IPackageFragmentRoot packageRoot = javaProject
				.getPackageFragmentRoot(sourceFolder);
		actionPackage = packageRoot.createPackageFragment(directoryPath + "."
				+ domain + "."+packageName, false, null);
		ICompilationUnit parentCU = actionPackage.createCompilationUnit(screenName + ".java", "", false, new SubProgressMonitor(
				monitor, 2));
		typeName =  screenName;// + ".java";
		parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
		String simpleTypeStub = "public class " + screenName + " {\n}";
		String lineDelimiter = StubUtility.getLineDelimiterUsed(actionPackage
				.getJavaProject());
		String cuContent = constructCUContent(parentCU, simpleTypeStub,
				lineDelimiter);

		fCurrType = parentCU.getType(typeName);
		IBuffer buffer = parentCU.getBuffer();
		buffer.setContents(cuContent);
		CompilationUnit astRoot = createASTForImports(parentCU);
		Set<String> existingImports = null;
		existingImports = getExistingImports(astRoot);

		ImportsManager imports = new ImportsManager(astRoot);
		// add an import that will be removed again. Having this import solves
		// 14661
		imports.addImport(JavaModelUtil.concatenateName(
				actionPackage.getElementName(), typeName));

		String typeContent = constructTypeStub(parentCU, imports, lineDelimiter);

		int index = cuContent.lastIndexOf(simpleTypeStub);
		if (index == -1) {
			AbstractTypeDeclaration typeNode = (AbstractTypeDeclaration) astRoot
					.types().get(0);
			int start = ((ASTNode) typeNode.modifiers().get(0))
					.getStartPosition();
			int end = typeNode.getStartPosition() + typeNode.getLength();
			buffer.replace(start, end - start, typeContent);
		} else {
			buffer.replace(index, simpleTypeStub.length(), typeContent);
		}

		IType createdType = parentCU.getType(typeName);
		
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}

		// add imports for superclass/interfaces, so types can be resolved
		// correctly

		ICompilationUnit cu = createdType.getCompilationUnit();

		imports.create(false, new SubProgressMonitor(monitor, 1));

		JavaModelUtil.reconcile(cu);

		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}

		// set up again
		CompilationUnit astRoot1 = createASTForImports(imports
				.getCompilationUnit());
		imports = new ImportsManager(astRoot1);

		createTypeMembers(createdType, imports, new SubProgressMonitor(monitor,
				1));
		createInheritedMethods(createdType, true, true, imports, new SubProgressMonitor(monitor, 1));
		// add imports
		imports.create(false, new SubProgressMonitor(monitor, 1));

		removeUnusedImports(cu, existingImports, false);

		JavaModelUtil.reconcile(cu);

		ISourceRange range = createdType.getSourceRange();

		IBuffer buf = cu.getBuffer();
		String originalContent = buf.getText(range.getOffset(),
				range.getLength());
		int indent = 0;
		String formattedContent = CodeFormatterUtil.format(
				CodeFormatter.K_CLASS_BODY_DECLARATIONS, originalContent,
				indent, lineDelimiter, actionPackage.getJavaProject());
		formattedContent = Strings.trimLeadingTabsAndSpaces(formattedContent);
		buf.replace(range.getOffset(), range.getLength(), formattedContent);

		String fileComment = getFileComment(cu);
		if (fileComment != null && fileComment.length() > 0) {
			buf.replace(0, 0, fileComment + lineDelimiter);
		}

		// if (needsSave) {
		cu.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
		// } else {
		// monitor.worked(1);
		// }

	}

	protected String getFileComment(ICompilationUnit parentCU) {
		return null;
	}

	private void removeUnusedImports(ICompilationUnit cu,
			Set<String> existingImports, boolean needsSave)
			throws CoreException {
		ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
		parser.setSource(cu);
		parser.setResolveBindings(true);

		CompilationUnit root = (CompilationUnit) parser.createAST(null);
		if (root.getProblems().length == 0) {
			return;
		}

		List<ImportDeclaration> importsDecls = root.imports();
		if (importsDecls.isEmpty()) {
			return;
		}
		ImportsManager imports = new ImportsManager(root);

		int importsEnd = ASTNodes.getExclusiveEnd(importsDecls.get(importsDecls
				.size() - 1));
		IProblem[] problems = root.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem curr = problems[i];
			if (curr.getSourceEnd() < importsEnd) {
				int id = curr.getID();
				if (id == IProblem.UnusedImport
						|| id == IProblem.NotVisibleType) { // not visible
															// problems hide
															// unused -> remove
															// both
					int pos = curr.getSourceStart();
					for (int k = 0; k < importsDecls.size(); k++) {
						ImportDeclaration decl = importsDecls.get(k);
						if (decl.getStartPosition() <= pos
								&& pos < decl.getStartPosition()
										+ decl.getLength()) {
							if (existingImports.isEmpty()
									|| !existingImports.contains(ASTNodes
											.asString(decl))) {
								String name = decl.getName()
										.getFullyQualifiedName();
								if (decl.isOnDemand()) {
									name += ".*"; //$NON-NLS-1$
								}
								if (decl.isStatic()) {
									imports.removeStaticImport(name);
								} else {
									imports.removeImport(name);
								}
							}
							break;
						}
					}
				}
			}
		}
		imports.create(needsSave, null);
	}

	protected void createTypeMembers(IType newType,
			final ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		// default implementation does nothing
		// example would be
		// String mainMathod= "public void foo(Vector vec) {}"
		// createdType.createMethod(main, null, false, null);
		// imports.addImport("java.lang.Vector");
	}

	/**
	 * We will initialize file contents with a sample text.
	 */

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "ESanteNewScreenWizard",
				IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	private String constructCUContent(ICompilationUnit cu, String typeContent,
			String lineDelimiter) throws CoreException {
		String fileComment = getFileComment(cu, lineDelimiter);
		fileComment.replace("#TASK_ID", taskId);
		String typeComment = getTypeComment(cu, lineDelimiter);
		IPackageFragment pack = (IPackageFragment) cu.getParent();
		String content = CodeGeneration.getCompilationUnitContent(cu,
				fileComment, typeComment, typeContent, lineDelimiter);
		if (content != null) {
			ASTParser parser = ASTParser
					.newParser(ASTProvider.SHARED_AST_LEVEL);
			parser.setProject(cu.getJavaProject());
			parser.setSource(content.toCharArray());
			CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			if ((pack.isDefaultPackage() || unit.getPackage() != null)
					&& !unit.types().isEmpty()) {
				return content;
			}
		}
		StringBuffer buf = new StringBuffer();
		if (!pack.isDefaultPackage()) {
			buf.append("package ").append(pack.getElementName()).append(';'); //$NON-NLS-1$
		}
		buf.append(lineDelimiter).append(lineDelimiter);
		if (typeComment != null) {
			buf.append(typeComment).append(lineDelimiter);
		}
		buf.append(typeContent);
		return buf.toString();
	}

	private String getFileComment(ICompilationUnit parentCU,
			String lineDelimiter) throws CoreException {

		return CodeGeneration.getFileComment(parentCU, lineDelimiter);

	}

	private CompilationUnit createASTForImports(ICompilationUnit cu) {
		ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setFocalPosition(0);
		return (CompilationUnit) parser.createAST(null);
	}

	private String getTypeComment(ICompilationUnit parentCU,
			String lineDelimiter) {

		try {
			StringBuffer typeName = new StringBuffer();
			typeName.append(getTypeNameWithoutParameters(typeName.toString()));
			String[] typeParamNames = new String[0];
			String comment = CodeGeneration.getTypeComment(parentCU,
					typeName.toString(), typeParamNames, lineDelimiter);
			if (comment != null && isValidComment(comment)) {
				return comment;
			}
		} catch (CoreException e) {
			JavaPlugin.log(e);
		}

		return null;
	}

	private static String getTypeNameWithoutParameters(
			String typeNameWithParameters) {
		int angleBracketOffset = typeNameWithParameters.indexOf('<');
		if (angleBracketOffset == -1) {
			return typeNameWithParameters;
		} else {
			return typeNameWithParameters.substring(0, angleBracketOffset);
		}
	}

	private boolean isValidComment(String template) {
		IScanner scanner = ToolFactory.createScanner(true, false, false, false);
		scanner.setSource(template.toCharArray());
		try {
			int next = scanner.getNextToken();
			while (TokenScanner.isComment(next)) {
				next = scanner.getNextToken();
			}
			return next == ITerminalSymbols.TokenNameEOF;
		} catch (InvalidInputException e) {
		}
		return false;
	}

	private Set<String> getExistingImports(CompilationUnit root) {
		List<ImportDeclaration> imports = root.imports();
		Set<String> res = new HashSet<String>(imports.size());
		for (int i = 0; i < imports.size(); i++) {
			res.add(ASTNodes.asString(imports.get(i)));
		}
		return res;
	}

	private String constructTypeStub(ICompilationUnit parentCU,
			ImportsManager imports, String lineDelimiter) throws CoreException {
		StringBuffer buf = new StringBuffer();

		int modifiers = getModifiers();
		buf.append(Flags.toString(modifiers));
		if (modifiers != 0) {
			buf.append(' ');
		}
		String type = ""; //$NON-NLS-1$
		String templateID = ""; //$NON-NLS-1$

		type = "class "; //$NON-NLS-1$
		templateID = CodeGeneration.CLASS_BODY_TEMPLATE_ID;

		buf.append(type);
		buf.append(getTypeName());
		writeSuperClass(buf, imports, "java.util.AbstractCollection");

		buf.append(" {").append(lineDelimiter); //$NON-NLS-1$
		String typeBody = CodeGeneration.getTypeBody(templateID, parentCU,
				getTypeName(), lineDelimiter);
		if (typeBody != null) {
			buf.append(typeBody);
		} else {
			buf.append(lineDelimiter);
		}
		buf.append('}').append(lineDelimiter);
		return buf.toString();
	}

	public int getModifiers() {
		int mdf = 0;
		// if (fAccMdfButtons.isSelected(PUBLIC_INDEX)) {
		mdf += F_PUBLIC;
		// } else if (fAccMdfButtons.isSelected(PRIVATE_INDEX)) {
		// mdf += F_PRIVATE;
		// } else if (fAccMdfButtons.isSelected(PROTECTED_INDEX)) {
		// mdf += F_PROTECTED;
		// }
		// if (fOtherMdfButtons.isSelected(ABSTRACT_INDEX)) {
		// mdf += F_ABSTRACT;
		// }
		// if (fOtherMdfButtons.isSelected(FINAL_INDEX)) {
		// mdf += F_FINAL;
		// }
		// if (fOtherMdfButtons.isSelected(STATIC_INDEX)) {
		// mdf += F_STATIC;
		// }
		return mdf;
	}

	@SuppressWarnings("restriction")
	private void writeSuperClass(StringBuffer buf, ImportsManager imports,
			String superclass) {
		// String superclass= getSuperClass();
		if (superclass.length() > 0 && !"java.lang.Object".equals(superclass)) { //$NON-NLS-1$
			buf.append(" extends "); //$NON-NLS-1$

			ITypeBinding binding = null;
			// if (fCurrType != null) {
			binding = TypeContextChecker.resolveSuperClass(superclass,
					fCurrType, getSuperClassStubTypeContext());
			// }
			if (binding != null) {
				buf.append(imports.addImport(binding));
			} else {
				buf.append(imports.addImport(superclass));
			}
		}
	}

	private StubTypeContext getSuperClassStubTypeContext() {
		if (fSuperClassStubTypeContext == null) {
			String typeName;
			if (fCurrType != null) {
				typeName = getTypeName();
			} else {
				typeName = JavaTypeCompletionProcessor.DUMMY_CLASS_NAME;
			}
			fSuperClassStubTypeContext = TypeContextChecker
					.createSuperClassStubTypeContext(typeName, null,
							actionPackage);
		}
		return fSuperClassStubTypeContext;
	}

	public static class ImportsManager {

		private final CompilationUnit fAstRoot;
		private final ImportRewrite fImportsRewrite;

		/* package */ImportsManager(CompilationUnit astRoot) {
			fAstRoot = astRoot;
			fImportsRewrite = StubUtility.createImportRewrite(astRoot, true);
		}

		/* package */ICompilationUnit getCompilationUnit() {
			return fImportsRewrite.getCompilationUnit();
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 *
		 * @param qualifiedTypeName
		 *            The fully qualified name of the type to import (dot
		 *            separated).
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 */
		public String addImport(String qualifiedTypeName) {
			return fImportsRewrite.addImport(qualifiedTypeName);
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 *
		 * @param qualifiedTypeName
		 *            The fully qualified name of the type to import (dot
		 *            separated).
		 * @param insertPosition
		 *            the offset where the import will be used
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 * 
		 * @since 3.8
		 */
		public String addImport(String qualifiedTypeName, int insertPosition) {
			ImportRewriteContext context = new ContextSensitiveImportRewriteContext(
					fAstRoot, insertPosition, fImportsRewrite);
			return fImportsRewrite.addImport(qualifiedTypeName, context);
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 *
		 * @param typeBinding
		 *            the binding of the type to import
		 *
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 */
		public String addImport(ITypeBinding typeBinding) {
			return fImportsRewrite.addImport(typeBinding);
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 *
		 * @param typeBinding
		 *            the binding of the type to import
		 * @param insertPosition
		 *            the offset where the import will be used
		 *
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 * 
		 * @since 3.8
		 */
		public String addImport(ITypeBinding typeBinding, int insertPosition) {
			ImportRewriteContext context = new ContextSensitiveImportRewriteContext(
					fAstRoot, insertPosition, fImportsRewrite);
			return fImportsRewrite.addImport(typeBinding, context);
		}

		/**
		 * Adds a new import declaration for a static type that is sorted in the
		 * existing imports. If an import already exists or the import would
		 * conflict with an import of an other static import with the same
		 * simple name, the import is not added.
		 *
		 * @param declaringTypeName
		 *            The qualified name of the static's member declaring type
		 * @param simpleName
		 *            the simple name of the member; either a field or a method
		 *            name.
		 * @param isField
		 *            <code>true</code> specifies that the member is a field,
		 *            <code>false</code> if it is a method.
		 * @return returns either the simple member name if the import was
		 *         successful or else the qualified name if an import conflict
		 *         prevented the import.
		 *
		 * @since 3.2
		 */
		public String addStaticImport(String declaringTypeName,
				String simpleName, boolean isField) {
			return fImportsRewrite.addStaticImport(declaringTypeName,
					simpleName, isField);
		}

		/* package */void create(boolean needsSave, IProgressMonitor monitor)
				throws CoreException {
			TextEdit edit = fImportsRewrite.rewriteImports(monitor);
			JavaModelUtil.applyEdit(fImportsRewrite.getCompilationUnit(), edit,
					needsSave, null);
		}

		/* package */void removeImport(String qualifiedName) {
			fImportsRewrite.removeImport(qualifiedName);
		}

		/* package */void removeStaticImport(String qualifiedName) {
			fImportsRewrite.removeStaticImport(qualifiedName);
		}
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	IMethod[] createInheritedMethods(IType type, boolean doConstructors, boolean doUnimplementedMethods, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
		final ICompilationUnit cu= type.getCompilationUnit();
		JavaModelUtil.reconcile(cu);
		IMethod[] typeMethods= type.getMethods();
		Set<String> handleIds= new HashSet<String>(typeMethods.length);
		for (int index= 0; index < typeMethods.length; index++)
			handleIds.add(typeMethods[index].getHandleIdentifier());
		ArrayList<IMethod> newMethods= new ArrayList<IMethod>();
		CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(type.getJavaProject());
		settings.createComments= true;
		ASTParser parser= ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
		parser.setResolveBindings(true);
		parser.setSource(cu);
		CompilationUnit unit= (CompilationUnit) parser.createAST(new SubProgressMonitor(monitor, 1));
		final ITypeBinding binding= ASTNodes.getTypeBinding(unit, type);
		if (binding != null) {
			if (doUnimplementedMethods) {
				AddUnimplementedMethodsOperation operation= new AddUnimplementedMethodsOperation(unit, binding, null, -1, false, true, false);
				operation.setCreateComments(settings.createComments);
				operation.run(monitor);
				createImports(imports, operation.getCreatedImports());
			}
			if (doConstructors) {
				AddUnimplementedConstructorsOperation operation= new AddUnimplementedConstructorsOperation(unit, binding, null, -1, false, true, false);
				operation.setOmitSuper(true);
				operation.setCreateComments(settings.createComments);
				operation.run(monitor);
				createImports(imports, operation.getCreatedImports());
			}
		}
		JavaModelUtil.reconcile(cu);
		typeMethods= type.getMethods();
//		CodeGeneration.getMethodBodyContent(cu, declaringTypeName, methodName, false, null, "\n")
		for (int index= 0; index < typeMethods.length; index++)
			if (!handleIds.contains(typeMethods[index].getHandleIdentifier()))
				newMethods.add(typeMethods[index]);
		IMethod[] methods= new IMethod[newMethods.size()];
		newMethods.toArray(methods);
		return methods;
	}
	private void createImports(ImportsManager imports, String[] createdImports) {
		for (int index= 0; index < createdImports.length; index++)
			imports.addImport(createdImports[index]);
	}
}