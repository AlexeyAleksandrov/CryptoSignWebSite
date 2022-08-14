package com.webapi.application.services.handlers;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.webapi.application.services.handlers.Excel.ExcelHandlerException;
import com.webapi.application.services.handlers.PDF.PDFHandlerException;
import com.webapi.application.services.handlers.Word.WordHandlerException;
import com.webapi.application.models.sign.CreateSignFormModel;

import java.io.IOException;

public abstract class UploadedFileHandler
{
    protected String singImagePath = null;
    protected CreateSignFormModel params = null;
    protected String currentDir = "";

    public String processDocument(String fileName) throws PDFHandlerException, IOException, WordHandlerException, BootstrapException, Exception, ExcelHandlerException { return null; };

    public void processDocument(String fileName, String outputFileName) throws PDFHandlerException, IOException, WordHandlerException, BootstrapException, Exception, ExcelHandlerException { };

    public String getSingImagePath()
    {
        return singImagePath;
    }

    public void setSingImagePath(String singImagePath)
    {
        this.singImagePath = singImagePath;
    }

    public CreateSignFormModel getParams()
    {
        return params;
    }

    public void setParams(CreateSignFormModel params)
    {
        this.params = params;
    }

    public void setCurrentDir(String currentDir)
    {
        this.currentDir = currentDir;
    }
}
