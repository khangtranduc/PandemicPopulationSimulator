package model;

import controller.LinkedControllers;

import java.io.*;
import java.util.Scanner;

public class Disease {
    private String name;
    private double infectiousness;
    //maybe implement an ID system
    private static String ID;

    public Disease(String name, double infectiousness, boolean record) throws InvalidNameException{
        if (name.equals("")){
            throw new InvalidNameException("Invalid Name");
        }
        this.name = name;
        this.infectiousness = infectiousness;

        if (record){
            try{
                PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/diseases.csv", true), true);
                writer.println(toString());
                writer.close();
            }
            catch (IOException e){
                LinkedControllers.throwInvalidAlert("Missing File", "'src/resources/diseases.csv' - not found");
            }
        }
    }

    public void editName(String name){
        if (name.equals("")){
            LinkedControllers.throwInvalidAlert("Invalid Disease Name", "Please enter a String");
        }
        String original = toString();
        this.name = name;
        updateEntry(original, false);
    }

    public void editInfectiousness(double infectiousness){
        String original = toString();
        this.infectiousness = infectiousness;
        updateEntry(original, false);
    }

    //updates the entry in the file diseases.csv
    public void updateEntry(String original, boolean isDelete){
        boolean flag = false;
        String upper = "", lower = "";
        try{
            Scanner reader = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/src/resources/diseases.csv"));
            while (reader.hasNext()){
                String line = reader.nextLine();
                if (line.equals(original)){
                    flag = true;
                    break;
                }
                upper += line + "\n";
            }
            while (reader.hasNext()){
                String line = reader.nextLine();
                lower = line + "\n";
            }
            reader.close();

            if (flag){
                PrintWriter writer = new PrintWriter(new FileOutputStream(System.getProperty("user.dir") + "/src/resources/diseases.csv", false));
                writer.print(upper);
                if (!isDelete){
                    writer.println(toString());
                }
                writer.print(lower);
                writer.close();
            }
        }
        catch (IOException e){
            LinkedControllers.throwInvalidAlert("Missing File", "'src/resources/diseases.csv' - not found");
        }
    }

    //getters
    public String getName() {
        return name;
    }
    public double getInfectiousness() {
        return infectiousness;
    }

    @Override
    public String toString(){
        return name + "," + infectiousness;
    }
}
