/**
 * 
 */
package com.sam.jcc.cloud.i.project;

/**
 * @author Alec Kotovich
 *
 */
public interface IProjectMetadata {

	Long getId();

	String getName();

	boolean hasSources();

	boolean hasVCS();

	boolean hasCI();

	boolean hasDb();
}
