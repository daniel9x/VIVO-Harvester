/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Output Stream that breaks XML blobs into individual Records and writes to a RecordHandler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class XMLRecordOutputStream extends OutputStream implements Cloneable {
	/**
	 * Buffer to hold data until a complete record is formed
	 */
	private ByteArrayOutputStream buf;
	/**
	 * RecordHandler to write record to
	 */
	private RecordHandler rh;
	/**
	 * the byte array that represent a closing record tag
	 */
	private byte[][] closeTags;
	/**
	 * Regex to find the identifing data in the record data
	 */
	private Pattern idRegex;
	/**
	 * Prepend to each record
	 */
	private String header;
	/**
	 * Append to each record
	 */
	private String footer;
	/**
	 * The class writing records
	 */
	private Class<?> opClass;
	
	/**
	 * Constructor
	 * @param tagsToSplitOn defines the record tag types
	 * @param headerInfo prepended to each record
	 * @param footerInfo appended to each record
	 * @param idLocationRegex regex to find the data to be used as ID
	 * @param recordHandler RecordHandler to write records to
	 * @param operator the class writing records
	 */
	public XMLRecordOutputStream(String[] tagsToSplitOn, String headerInfo, String footerInfo, String idLocationRegex, RecordHandler recordHandler, Class<?> operator) {
		this.buf = new ByteArrayOutputStream();
		this.rh = recordHandler;
		this.idRegex = Pattern.compile(idLocationRegex);
		this.closeTags = new byte[tagsToSplitOn.length][];
		for(int x = 0; x < tagsToSplitOn.length; x++) {
			this.closeTags[x] = ("</" + tagsToSplitOn[x] + ">").getBytes();
		}
		this.header = headerInfo;
		this.footer = footerInfo;
		this.opClass = operator;
	}
	
	@Override
	public XMLRecordOutputStream clone() {
		XMLRecordOutputStream template = new XMLRecordOutputStream(new String[]{}, this.header, this.footer, this.idRegex.pattern(), this.rh, this.opClass);
		template.closeTags = this.closeTags;
		return template;
	}
	
	@Override
	public void write(int arg0) throws IOException {
		this.buf.write(arg0);
		byte[] a = this.buf.toByteArray();
		for(int x = 0; x < this.closeTags.length; x++) {
			if(compareByteArrays(a, this.closeTags[x])) {
				String record = new String(a);
				Matcher m = this.idRegex.matcher(record);
				m.find();
				String id = m.group(1);
				this.rh.addRecord(id.trim(), this.header + record.trim() + this.footer, this.opClass);
				this.buf.reset();
			}
		}
	}
	
	/**
	 * Compare two byte arrays
	 * @param arrayOne first to compare
	 * @param arrayTwo second to compare
	 * @return true if the last bytes in arrayOne is equivalent to arrayTwo, false otherwise
	 */
	private boolean compareByteArrays(byte[] arrayOne, byte[] arrayTwo) {
		if(arrayOne.length < arrayTwo.length) {
			return false;
		}
		int o = arrayOne.length - arrayTwo.length;
		for(int i = 0; i < arrayTwo.length; i++) {
			if(arrayOne[o + i] != arrayTwo[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Set the record handler
	 * @param rh new record handler
	 * @return self reference
	 */
	public XMLRecordOutputStream setRecordHandler(RecordHandler rh) {
		this.rh = rh;
		return this;
	}
}
