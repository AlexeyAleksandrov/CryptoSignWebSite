package com.webapi.application.services.libreoffice;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/**
 * The Contents of this file are made available subject to the terms of
 * the BSD license.
 * <p>
 * Copyright 2000, 2010 Oracle and/or its affiliates.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Sun Microsystems, Inc. nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *************************************************************************/

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ooo.connector.BootstrapSocketConnector;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;

/**
 * Класс <CODE>DocumentConverter</CODE> позволяет конвертировать файлы, которые поддерживает LibreOffice:Writer в форматы *.docx и *.pdf
 */
@Service
public class DocumentConverter
{
    /**
     * Путь к папке LibreOffice, для запуска
     */
    private String oooExeFolder = null;

    public static final String oooExeFolderWindows = "C:/Program Files/LibreOffice/program/";
    public static final String oooExeFolderLinux = "/usr/lib/libreoffice/program/";

    /**
     * Конструктор конвертера документов
     * @param oooExeFolder путь к папке LibreOffice, для запуска
     * @throws BootstrapException исключение библиотеки Bootstrap
     * @throws Exception исключение невозможности создания экземпляра LibreOffice
     */
    public DocumentConverter(String oooExeFolder) throws BootstrapException, Exception
    {
        this.oooExeFolder = oooExeFolder;
        configure();
    }

    /**
     * Конструктор конвертера документов
     * @throws BootstrapException исключение библиотеки Bootstrap
     * @throws Exception исключение невозможности создания экземпляра LibreOffice
     */
    public DocumentConverter() throws BootstrapException, Exception
    {
        if(SystemUtils.IS_OS_WINDOWS)
        {
            this.oooExeFolder = oooExeFolderWindows;
        }
        else if(SystemUtils.IS_OS_LINUX)
        {
            this.oooExeFolder = oooExeFolderLinux;
        }
        configure();
    }


    /**
     * Функция стратовой конфигурации обработчика LibreOffice
     * @throws Exception исключение библиотеки Bootstrap
     * @throws BootstrapException исключение невозможности создания экземпляра LibreOffice
     */
    private void configure() throws Exception, BootstrapException
    {
        // get the remote office component context
        System.out.println("Search LibreOffice on " + oooExeFolder);
        XComponentContext xContext = BootstrapSocketConnector.bootstrap(oooExeFolder);
        System.out.println("Connected to a running office ...");

        // get the remote office service manager
        com.sun.star.lang.XMultiComponentFactory xMCF = xContext.getServiceManager();

        Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);

        xCompLoader = UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class, oDesktop);
    }

    /**
     * Контейнер загруженного документа
     */
    private com.sun.star.frame.XComponentLoader xCompLoader = null;


    /**
     * Перечисление возможных вариантов конвертации документов:
     * <CODE>CONVERT_TO_DOCX</CODE> экспортирует документ в docx, если это поддерживает Writter.
     * <CODE>EXPORT_TO_PDF</CODE> экспортирует документ в PDF, если это поддерживает Writter.
     */
    public enum ConvertType
    {
        CONVERT_TO_DOCX, EXPORT_TO_PDF
    }


    /**
     * Функция конвертации файлов средствами LibreOffice
     * @param inputFile исходный файл, который будет конвертирован
     * @param outputFile выходной файл, в который будет сохранён результат конвертации
     * @param convertType тип конвертации, выбирается из доступных {@link ConvertType}
     */
    public void convertTo(String inputFile, String outputFile, ConvertType convertType)
    {
        if (inputFile == null || inputFile.isEmpty())
        {
            throw new IllegalArgumentException("Не указан входной файл для конвертации");
        }

        if (outputFile == null || outputFile.isEmpty())
        {
            throw new IllegalArgumentException("Не указан выходной файл для конвертации");
        }

        // Converting the document to the favoured type
        try
        {
            // Composing the URL by replacing all backslashes
            String sUrl = "file:///" + inputFile.replace('\\', '/');    // приводим путь к файлу в формат для Linux
            String sOutUrl = "file:///" + outputFile.replace('\\', '/');

            // Loading the wanted document
            com.sun.star.beans.PropertyValue propertyValues[] = new com.sun.star.beans.PropertyValue[1];
            propertyValues[0] = new com.sun.star.beans.PropertyValue();
            propertyValues[0].Name = "Hidden";
            propertyValues[0].Value = Boolean.TRUE;

            Object oDocToStore = this.xCompLoader.loadComponentFromURL(sUrl, "_blank", 0, propertyValues);

            // Getting an object that will offer a simple way to store
            // a document to a URL.
            com.sun.star.frame.XStorable xStorable = UnoRuntime.queryInterface(com.sun.star.frame.XStorable.class, oDocToStore);

            // Preparing properties for converting the document
            propertyValues = new com.sun.star.beans.PropertyValue[2];
            // Setting the flag for overwriting
            propertyValues[0] = new com.sun.star.beans.PropertyValue();
            propertyValues[0].Name = "Overwrite";
            propertyValues[0].Value = Boolean.TRUE;
            // Setting the filter name
            propertyValues[1] = new com.sun.star.beans.PropertyValue();
            propertyValues[1].Name = "FilterName";
//            propertyValues[1].Value = DocumentConverter.sConvertType;

            // Appending the favoured extension to the origin document name
//            int index1 = sUrl.lastIndexOf('/');
//            int index2 = sUrl.lastIndexOf('.');
//            String sStoreUrl = sOutUrl + sUrl.substring(index1, index2 + 1) + DocumentConverter.sExtension;

            // Storing and converting the document
            if(convertType == ConvertType.CONVERT_TO_DOCX)
            {
                propertyValues[1].Value = "Office Open XML Text";
                xStorable.storeAsURL(sOutUrl, propertyValues);
            }
            else if (convertType == ConvertType.EXPORT_TO_PDF)
            {
                // если экспорт Word
                if(inputFile.endsWith(".docx") || inputFile.endsWith(".doc") || inputFile.endsWith(".rtf"))
                {
                    propertyValues[1].Value = "writer_pdf_Export";
                }
                // если экспорт Excel
                else if(inputFile.endsWith(".xlsx") || inputFile.endsWith(".xls"))
                {
                    propertyValues[1].Value = "calc_pdf_Export";
                }
                // иначе - ошибка
                else
                {
                    propertyValues[1].Value = null;
                }
                xStorable.storeToURL(sOutUrl, propertyValues);
            }

            // Closing the converted document. Use XCloseable.close if the
            // interface is supported, otherwise use XComponent.dispose
            com.sun.star.util.XCloseable xCloseable = UnoRuntime.queryInterface(com.sun.star.util.XCloseable.class, xStorable);

            if (xCloseable != null)
            {
                xCloseable.close(false);
            }
            else
            {
                com.sun.star.lang.XComponent xComp = UnoRuntime.queryInterface(com.sun.star.lang.XComponent.class, xStorable);

                xComp.dispose();
            }

            System.out.println("Successfully converted!");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String args[]) throws BootstrapException
    {
        // делаю экспорт файлов
        try
        {
            DocumentConverter documentConverter = new DocumentConverter();  // создаём конвертер документов
;
            documentConverter.convertTo(
                    "C:\\Users\\ASUS\\Downloads\\docs_conv\\титульники.rtf",
                    "C:\\Users\\ASUS\\Downloads\\docs_conv\\titilnik.docx",
                    DocumentConverter.ConvertType.CONVERT_TO_DOCX);     // конвертируем rtf в docx

            documentConverter.convertTo(
                    "C:\\Users\\ASUS\\Downloads\\docs_conv\\титульники.rtf",
                    "C:\\Users\\ASUS\\Downloads\\docs_conv\\титульник.pdf",
                    DocumentConverter.ConvertType.EXPORT_TO_PDF);       // конвертируем rtf в pdf

            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

//    /**
//     * Bootstrap UNO, getting the remote component context, getting a new instance
//     * of the desktop (used interface XComponentLoader) and calling the
//     * static method traverse
//     *
//     * @param args The array of the type String contains the directory, in which
//     *             all files should be converted, the favoured converting type
//     *             and the wanted extension
//     */
//    public static void main(String args[])
//    {
//        String params[] = new String[4];
//        params[0] = "C:\\Users\\ASUS\\Downloads\\docs_conv";    // папка с входными файлами
//        //        params[1] = "swriter: MS Word 97";
//        //        params[1] = "Office Open XML Text";
//        params[1] = "writer_pdf_Export";    // тип сохранения
//        //        params[2] = "docx";
//        params[2] = "pdf";  // расширение выходного файла
//        params[3] = "C:\\Users\\ASUS\\Downloads\\docs_conv\\output";    // папка, куда всё будет сохранено
//
//        args = params;
//
//        if (args.length < 3)
//        {
//            System.out.println("usage: java -jar DocumentConverter.jar " + "\"<directory to convert>\" \"<type to convert to>\" " + "\"<extension>\" \"<output_directory>\"");
//            System.out.println("\ne.g.:");
//            System.out.println("usage: java -jar DocumentConverter.jar " + "\"c:/myoffice\" \"swriter: MS Word 97\" \"doc\"");
//            System.exit(1);
//        }

//        try
//        {
//
//
//
//
//            System.exit(0);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace(System.err);
//            System.exit(1);
//        }
//    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
