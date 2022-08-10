package com.webapi.application;

import com.webapi.application.controllers.FileUploadController;
import com.webapi.application.services.handlers.Excel.ExcelHandler;
import com.webapi.application.services.handlers.PDF.PDFHandler;
import com.webapi.application.services.handlers.UploadedFileHandler;
import com.webapi.application.services.handlers.Word.WordHandler;
import com.webapi.application.models.FileConvertParamsModel;
import com.webapi.application.services.signImage.SignImageCreator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

public class CMDApplication
{
    public static void main(String[] args) throws URISyntaxException
    {
        System.out.println("Кол-во: " + args.length);
        for (int i = 0; i< args.length; i++)
        {
            System.out.println(args[i]);
        }
//        args = new String[9];
//        args[0] = "C:\\Users\\ASUS\\Downloads\\Otchyot_po_praktike.docx";
//        args[1] = "C:\\Users\\ASUS\\Downloads\\Otchyot_po_praktike_cmd.pdf";
//        args[2] = "Vladelets";
//        args[3] = "46r7346t3486tr834t83";
//        args[4] = "08.04.2022";
//        args[5] = "08.04.2023";
//        args[6] = "true";
//        args[7] = "false";
//        args[8] = "В конец документа";

        // java -jar application.jar C:\Users\ASUS\Downloads\Otchyot_po_praktike.docx C:\Users\ASUS\Downloads\Otchyot_po_praktike_cmd_1.pdf Alexxey 46376r736r7346r734 08.04.2022 08.04.2023 true false "В конец документа"

        if(args.length < 9)
        {
            System.out.println("Недостаточно параметров!");
            System.exit(1);
        }

        File fileInput = new File(args[0]);
        File fileOutput = new File(args[1]);
        String signOwner = args[2];
        String signCertificate = args[3];
        String signDateFrom = args[4];
        String signDateTo = args[5];
        boolean drawLogo = Boolean.parseBoolean(args[6]);
        boolean checkTransitionToNewPage = Boolean.parseBoolean(args[7]);
        String insertType = args[8];

        String fileInputOriginalName = fileInput.getName();
//        String currentDir = System.getProperty("user.dir");
//        String currentDir = Paths.get("").toAbsolutePath().toString();
//        String currentDir = new File(CMDApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
//        String fileName = currentDir + "/uploadedfiles/" + fileInputOriginalName;   // получаем оригинальное название файла, который был загружен
//        String currentDir = new File("").getAbsolutePath();
        String currentDir = getCurrentPath();
        System.out.println("current = " + currentDir);

        File outputDir = new File(currentDir + "output/");   // папка вывода
        File uploadDir = new File(currentDir + "uploadedfiles/");    // папка сохранения
        File tempDir = new File(currentDir + "temp/");   // папка для временных данных

        String fileName = uploadDir.getAbsolutePath() + File.separator + fileInputOriginalName;   // получаем оригинальное название файла, который был загружен
        FileConvertParamsModel convertParams = new FileConvertParamsModel();    // модель получаемых данных, для удобства

        // заносим полученные параметры в модель данных
        convertParams.setFileName(fileInputOriginalName);
        convertParams.setSignOwner(signOwner);
        convertParams.setSignCertificate(signCertificate);
        convertParams.setSignDateStart(signDateFrom);
        convertParams.setSignDateEnd(signDateTo);
        convertParams.setDrawLogo(drawLogo);
        convertParams.setCheckTransitionToNewPage(checkTransitionToNewPage);

        switch (insertType)
        {
            case "В конец документа":
            {
                convertParams.setInsertType(0);
                break;
            }
            case "По координатам":
            {
                convertParams.setInsertType(1);
                break;
            }
            case "По тэгу":
            {
                convertParams.setInsertType(2);
                break;
            }
            default:
            {
                convertParams.setInsertType(-1);
                break;
            }
        }

        // проверка корректности входных данных
        if(convertParams.getInsertType() == -1
                || (convertParams.getInsertType() == 2 && !(fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf"))))
        {
            System.out.println("Error! Выбранный тип подписи не подходит для данного типа файлов! Тип: " + convertParams.getInsertType());
            System.exit(1);
        }

        // начинаем обработку файла
        if (fileInput.exists())
        {
            try
            {
                // проверяем наличие папок для сохранения и вывода
                boolean uploadDirCreated = true;
                boolean outputDirCreated = true;
                boolean tempDirCreated = true;

                if(!uploadDir.exists())
                {
                    uploadDirCreated = uploadDir.mkdir();
                }
                if(!outputDir.exists())
                {
                    outputDirCreated = outputDir.mkdir();
                }
                if(!tempDir.exists())
                {
                    tempDirCreated = tempDir.mkdir();
                }

                // проверка наличия
                if(!uploadDirCreated || !outputDirCreated || !tempDirCreated)
                {
                    System.out.println("Не удалось загрузить файл, т.к. файловая система не позволяет выполнить сохранение!");
                    System.out.println("uploaded = " + uploadDir.getAbsolutePath());
                    System.out.println("output = " + outputDir.getAbsolutePath());
                    System.out.println("temp = " + tempDir.getAbsolutePath());

                    System.exit(1);
                }

                // сохраняем файл на устройстве
                FileInputStream fileInputStream = new FileInputStream(fileInput);
                int fileInputSize = (int) fileInput.length();   // размер файла
                byte[] bytes = new byte[fileInputSize];     // создаем массив байт
                fileInputStream.read(bytes);    // считываем файл
                fileInputStream.close();

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
                stream.write(bytes);
                stream.close();

                // создаём изображение подписи
                SignImageCreator signImageCreator = new SignImageCreator(); // создаём генератор изображения подписи
                signImageCreator.setImageGerbPath(currentDir + FileUploadController.signImageLogoPath);       // указываем путь к гербу
                signImageCreator.createSignImage(currentDir + FileUploadController.singImagePath, convertParams); // создаём изображение подписи

                UploadedFileHandler documentHandler = null; // обработчик документов
                String outputFileName = null;
                // определяем тип полученного файла
                if (fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".rtf"))
                {
                    documentHandler = new WordHandler();
                }
                else if (fileName.endsWith(".pdf"))
                {
                    documentHandler = new PDFHandler();   // создаём обработчик
                }
                else if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))
                {
                    documentHandler = new ExcelHandler();
                }
                else
                {
                    System.out.println("Данный тип файлов не поддерживается!");
                    System.exit(1);
                }

                // обрабатываем полученный файл
                documentHandler.setCurrentDir(currentDir);  // задаём корневую папку
                documentHandler.setSingImagePath(currentDir + FileUploadController.singImagePath); // указываем путь к картинке, которую нужно будет вставить
                documentHandler.setParams(convertParams);    // указываем параметры обработки
                outputFileName = documentHandler.processDocument(fileName);   // запускаем обработку

                // считываем конечный файл
                File outputFile = new File(currentDir + "output/" + outputFileName);
                int outputFileSize = (int) outputFile.length();
                byte[] outputFileData = new byte[outputFileSize];
                FileInputStream outFileInputStream = new FileInputStream(outputFile);
                outFileInputStream.read(outputFileData);
                outFileInputStream.close();

//                byte[] outputFileData = new FileInputStream(outputFile).readAllBytes(); // считываем весь файл
                // записываем файл туда, куда указано
                FileOutputStream fileOutputStream = new FileOutputStream(fileOutput);
                fileOutputStream.write(outputFileData); // записываем все данные обратно
                fileOutputStream.close();

                // очищаем папки
                for(File file : uploadDir.listFiles())  // очистка папки входных файлов
                {
                    if(file.isFile())
                    {
                        file.delete();
                    }
                }
                for(File file : outputDir.listFiles())  // очистка папки выходных файлов
                {
                    if(file.isFile())
                    {
                        file.delete();
                    }
                }

                System.out.println("OK! " + outputFileName);
                System.exit(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("Error! Не удалось загрузить " + fileName + " => " + e.getMessage());
                System.out.println(Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.out.println("Error! Не удалось загрузить файл, потому что он пустой.");
            System.exit(1);
        }
    }

    public static String getCurrentPath() throws URISyntaxException
    {
//        //        File jarDir = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).toURI());
//        String jarPath = CMDApplication.class
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation()
//                .toURI()
//                .getPath();
//        //        System.out.println("jarDir = " + jarPath.replace("application.jar", ""));
//        jarPath = jarPath.replace("\\", "/");
//        int lastIndex = jarPath.lastIndexOf("/");
//        StringBuilder sb = new StringBuilder(jarPath);
//        int startIndex = 1;
//        if(SystemUtils.IS_OS_LINUX)
//        {
//            startIndex = 0;
//        }
//        jarPath = sb.substring(startIndex, lastIndex+1);
////        System.out.println("jarDir = " + jarPath);
//
//        return jarPath;
        return "";
    }
}
