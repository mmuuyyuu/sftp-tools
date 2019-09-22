package com.easy.ftp.tools.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleFactory implements IConsoleFactory {

	private static MessageConsole console = new MessageConsole("", null);

	static boolean exists = false;

	static IConsoleManager manager;

	public void openConsole() {
		showConsole();
	}

	private static void showConsole() {

		if (console != null) {

			manager = ConsolePlugin.getDefault().getConsoleManager();

			IConsole[] existing = manager.getConsoles();
			exists = false;

			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i])
					exists = true;
			}
			if (!exists) {
				manager.addConsoles(new IConsole[] { console });
			}
		}
	}

	public static void closeConsole() {
		manager = ConsolePlugin.getDefault().getConsoleManager();
		if (console != null) {
			manager.removeConsoles(new IConsole[] { console });
		}
	}

	public static MessageConsole getConsole() {
		showConsole();

		return console;
	}

	public static void printToConsole(String message) {
		printToConsole(message, true);
	}

	public static void printToConsole(String message, boolean activate) {
		MessageConsoleStream printer = getConsole().newMessageStream();
		printer.setActivateOnWrite(activate);
		printer.println(message);
	}
}
