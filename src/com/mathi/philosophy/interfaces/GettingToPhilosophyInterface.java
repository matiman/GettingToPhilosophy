package com.mathi.philosophy.interfaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/*
 * I can have another implementation of getting path so I decided to have an interface for future use
 */
public interface GettingToPhilosophyInterface {

	//This is the only method I want to expose without worrying about how it is implemented 
	public abstract List<String> getPathFor(String title) throws IOException,
			ClassNotFoundException, SQLException;

}