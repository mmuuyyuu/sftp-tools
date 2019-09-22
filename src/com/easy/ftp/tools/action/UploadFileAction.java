package com.easy.ftp.tools.action;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.easy.ftp.tools.console.ConsoleFactory;
import com.easy.ftp.tools.enums.ConfigServer;
import com.easy.ftp.tools.util.FileProcessor;
import com.easy.ftp.tools.util.SFTPHelper;
import com.easy.ftp.tools.util.SFTPServer;

import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.core.resources.IFile;

@SuppressWarnings("restriction")
public class UploadFileAction implements IObjectActionDelegate {

	private Shell shell;

	private IStructuredSelection selection;

	@Override
	public void run(IAction action) {
		try {
			doRun(action);
		} catch (Exception e) {
			MessageDialog.openInformation(this.shell, "ftp file Manager >>", "upload failure:[" + e + "]");
		}
	}

	private int selectNum = 0;

	@SuppressWarnings({ "rawtypes" })
	public void doRun(IAction action) {

		if (this.selection == null || this.selection.isEmpty() || !(this.selection instanceof IStructuredSelection)) {
			return;
		}

		Iterator iter = this.selection.iterator();

		this.selectNum = this.selection.size();

		while (iter.hasNext()) {

			Object obj = iter.next();

			// org.eclipse.core.internal.resources.File
			// 非 java 类型的文件,直接上传
			if (obj instanceof IFile) {

				IFile files = (IFile) obj;
				// IProject pro = files.getProject() ;

				// System.out.println("项目名称 >>>" + pro.getName());
   
				// 初始化
				try {
					SFTPHelper.init(files);
				} catch (IOException e) {
					ConsoleFactory.printToConsole(e.getMessage());

					return;
				}
				uploadIFile(files);
 
			} else if (obj instanceof CompilationUnit) {
				// java 文件，先编译在上传编译的.class 文件
				CompilationUnit c = (CompilationUnit) obj;
				IFile ifile = (IFile) c.getResource();
				// 初始化
				try {
					SFTPHelper.init(ifile);
				} catch (Exception e) {
					ConsoleFactory.printToConsole(e.getMessage());

					return;
				}
				uploadCompilationUnit(ifile);
			} else {
				// 不处理
				MessageDialog.openInformation(this.shell, "ftp file Manager", "file of thie type can not be processed :[" + obj + "]");

				continue;

			}

		}
	 
		MessageDialog.openInformation(this.shell, "ftp file Manager", "upload completed!");

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
	}

	private void uploadIFile(IFile ifile) {
		File file = ifile.getLocation().toFile();
		String remo = "";
		try {
			remo = urlProcessor(ConfigServer.APP_NAME.getV(), file.getAbsolutePath());
		} catch (Exception e) {
			ConsoleFactory.printToConsole("[file:" + file.getName() + ",upload failure." + e);

		}
		try {
			upload(file.getAbsolutePath(), remo);
		} catch (Exception e) {
			ConsoleFactory.printToConsole("upload failure" + e);

			MessageDialog.openInformation(this.shell, "ftp file Manager", "upload failure" + e.getMessage());
			return;
		}

		ConsoleFactory.printToConsole("[file:" + file.getName() + "]upload to[" + remo + "] success!");

	}

	private void uploadCompilationUnit(IFile ifile) {

		File file = ifile.getLocation().toFile();
		/*
		 * 编译
		 */
		try {
			FileProcessor.compliler(file);
		} catch (Exception e) {
			ConsoleFactory.printToConsole("[file:" + file.getName() + "]compliler failure!" + e.getMessage());
			return;
		}
		// 获取编译后的文件
		String classPath = file.getAbsolutePath().replace(".java", ".class");
		/*
		 * 开始上传
		 */
		String remo = "";
		try {
			remo = urlProcessor(ConfigServer.APP_NAME.getV(), classPath);
		} catch (Exception e) {
 			ConsoleFactory.printToConsole("[file:" + file.getName() + "]upload failure!" + e.getMessage());

		}

		/*
		 * upload
		 */
		try {
			this.upload(classPath, remo);
		} catch (Exception e) {
			ConsoleFactory.printToConsole("upload failure >>" + e);
			MessageDialog.openInformation(this.shell, "ftp file Manager", "upload failure" + e.getMessage());

 			return;
		}

		ConsoleFactory.printToConsole("[file:" + file.getName() + "]upload to[" + remo + "] success!");

		File dele = new File(classPath);

		if (dele.exists()) {
			dele.delete();
		}

	}

	SFTPServer server = null;

	private void upload(String localFile, String remoteFile) throws Exception {

		if (server == null) {

			server = SFTPHelper.server();

			// open
			server.Open();

		}

		// upload
		server.Upload(localFile, remoteFile);
		this.selectNum--;

		if (this.selectNum <= 0) {
			server.Close();
			server = null;
		}
	}

	/**
	 * 
	 * @param 本地路径
	 * @return 服务器路径
	 * @throws Exception
	 */
	public String urlProcessor(String appName, String filePath) throws Exception {

		filePath = filePath.replace("\\", "/");

		StringBuilder newPath = new StringBuilder("app/").append(appName);

		int indexOf = 0;

		if ((indexOf = filePath.lastIndexOf("src/main/java")) != -1) {

			newPath.append("/classes");

			String subfile = filePath.substring(indexOf + 13, filePath.length());
			newPath.append(subfile);

			return newPath.toString();

		} else if ((indexOf = filePath.lastIndexOf("src/main/resources")) != -1) {

			// newPath.append("/etc");

			String subfile = filePath.substring(indexOf + 18, filePath.length());
			newPath.append(subfile);

			return newPath.toString();

		} else if (filePath.indexOf("src/main/webapp") != -1) {

			throw new Exception("webapp,this directory is not supported");
		} else {
			throw new Exception("this is not a maven project");
		}

	}

}
