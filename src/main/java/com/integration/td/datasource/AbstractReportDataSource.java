package com.integration.td.datasource;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractReportDataSource extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6815200696803250209L;
	protected String[] columnNames;
	protected List rows;
	
	public AbstractReportDataSource() {
		super();
	}
	
	public AbstractReportDataSource(String[] columnNames, List rows) {
		super();
		this.columnNames = columnNames;
		this.rows = rows;
	}

	public AbstractReportDataSource(List rows) {
		super();
		this.rows = rows;
	}

	public AbstractReportDataSource(String[] columnNames) {
		super();
		this.columnNames = columnNames;
	}

	public int getRowCount() {
		if(rows == null) {
			return 0;
		}
		return rows.size();
	}

	public int getColumnCount() {
		if(columnNames == null) {
			return 0;
		}
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public List getRows() {
		return rows;
	}

	public void setRows(List rows) {
		this.rows = rows;
	}

}
