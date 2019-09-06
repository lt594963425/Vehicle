package com.tao.vehiclemvdemo.android.utils.file.IO;

import android.content.Context;

import com.tao.vehiclemvdemo.android.utils.file.FileStream;

import java.io.PrintStream;

public class ExceptionWriter {
	private Throwable exception;

	public ExceptionWriter(Throwable ex) {
		this.exception = ex;
	}

	public void saveStackTraceToSD(Context context) {
		try {
			PrintStream out = new PrintStream(FileStream.getExceptionFileStream(context));
			exception.printStackTrace(out);
			out.close();
		} catch (Exception excep) {
			excep.printStackTrace();
		}
	}
}
