package fr.diginamic;

import java.io.*;
public class TestLireFichierAvecJavaIo {
public static void main(String[] args) {
try {
FileReader fileReader = new FileReader(new File("C:/Temp/saisieClavier.txt"));
BufferedReader reader = new BufferedReader(fileReader);
String line = null;
while ((line= reader.readLine())!=null){
                System.out.println(line);
            }
            reader.close();
            fileReader.close();
        }
catch (IOException e){
throw new RuntimeException(e);
        }
    }
}
