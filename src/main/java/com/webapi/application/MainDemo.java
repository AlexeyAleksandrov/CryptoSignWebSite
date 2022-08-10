package com.webapi.application;

import com.webapi.application.services.libreoffice.DocumentConverter;
import com.webapi.application.services.signImage.SignImageCreator;

import java.io.IOException;

public class MainDemo
{
    public static void main(String[] args) throws IOException
    {
//        String outputImagePath = "C:\\Users\\ASUS\\Downloads\\image_java.jpg";   // путь сохранения файла картинки
//        String imageGerbPath = "C:\\Users\\ASUS\\Pictures\\mirea_gerb_52_65.png";    // путь к изображению герба
        String outputImagePath = "temp/sign_image.jpg";   // путь сохранения файла картинки
        String imageGerbPath = "src/main/resources/logo/mirea_gerb_52_65.png";    // путь к изображению герба

        SignImageCreator signImageCreator = new SignImageCreator(); // генератор картинок
        signImageCreator.setImageGerbPath(imageGerbPath);   // указываем путь к гербу
        signImageCreator.createSignImage(outputImagePath,
                "Петров Пётр Иванович",
                "120059595d5bb35e9f77ff73f600010059595d",
                "21.03.2022",
                "21.03.2023",
                true);  // создаем картинку
    }
}
