package com.scc.service.converter;


public class RowParsingException extends ConvertException {

    private final long rowNumber;

    public RowParsingException(String message, long rowNumber) {
        super(String.format("[row %d]: %s", rowNumber, message));
        this.rowNumber = rowNumber;
    }

    public RowParsingException(String message, Throwable cause, long rowNumber) {
        super(String.format("[row %d]: %s", rowNumber, message), cause);
        this.rowNumber = rowNumber;
    }

    public long getRowNumber() {
        return rowNumber;
    }
}
