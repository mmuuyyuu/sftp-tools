package com.easy.ftp.tools.util;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class FileProcessor {

	public static void compliler(File srcjavaFile) throws Exception {

		if (srcjavaFile == null) {

			throw new Exception("compliler error,not found this file ");
		}

		/*
		 * 获取到java 编译器
		 */
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		int result = compiler.run(null, null, null, srcjavaFile.getAbsolutePath());

		if (result != 0) {
			throw new Exception(srcjavaFile.getAbsolutePath() + "compliler error.");
		}
	}

}
