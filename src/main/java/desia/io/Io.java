package desia.io;

import java.util.Scanner;

public class Io {

    public int readInt(String prompt,int userChoices) {

        Scanner scan = new Scanner(System.in);
        int input;

        do{
            System.out.println(prompt);
            try {
                input=Integer.parseInt(scan.next());
            }catch(Exception e) {
                input= -1;
                System.out.println("잘못된 입력입니다.");
            }
        }while(input < 1 || input > userChoices);
        return input;
    }

}
