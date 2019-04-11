package io;

import java.io.File;

public interface TeXWriter {

	String saveToFile(File f);

	boolean isWholeDoc();

	void setWholedoc(boolean wholedoc);

}