/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.integration;

import com.gtwm.pb.model.interfaces.WikiRecordDataRowInfo;

public class WikiRecordDataRow implements WikiRecordDataRowInfo {
	
	public WikiRecordDataRow(String pageName, String pageContentSnippet) {
		this.pageName = pageName;
		this.pageContentSnippet = pageContentSnippet;
	}
	
	public String getPageName() {
		return this.pageName;
	}
	
	public String getPageContentSnippet() {
		return this.pageContentSnippet;
	}
	
	public String toString() {
		return this.pageName + ": " + this.pageContentSnippet;
	}
	
	private String pageName = "";
	
	private String pageContentSnippet = "";

}
